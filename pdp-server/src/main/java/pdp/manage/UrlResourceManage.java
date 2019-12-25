package pdp.manage;

import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import pdp.domain.EntityMetaData;

import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class UrlResourceManage implements Manage {

    private static final Logger LOG = LoggerFactory.getLogger(UrlResourceManage.class);
    private final String manageBaseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpHeaders httpHeaders;

    private String requestedAttributes = "REQUESTED_ATTRIBUTES\":[\"metaDataFields.coin:institution_id\"," +
            " \"metaDataFields" +
            ".coin:policy_enforcement_decision_required\", \"allowedall\", \"allowedEntities\"]";

    private String body = "{\"" + requestedAttributes + "}";
    private String bodyForEntity = "{\"entityid\":\"@@entityid@@\", \"" + requestedAttributes + "}";
    private String bodyForEntityIdIn = "{\"entityid\":[@@entityids@@], \"" + requestedAttributes + "}";
    private String bodyForInstitutionId = "{\"metaDataFields.coin:institution_id\":\"@@institution_id@@\", \"" +
            requestedAttributes + "}";

    public UrlResourceManage(
            String username,
            String password,
            String manageBaseUrl) {

        String basicAuth = "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));

        this.manageBaseUrl = manageBaseUrl;

        this.httpHeaders = new HttpHeaders();
        this.httpHeaders.add(HttpHeaders.CONTENT_TYPE, "application/json");
        this.httpHeaders.add(HttpHeaders.AUTHORIZATION, basicAuth);

        SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate
                .getRequestFactory();
        requestFactory.setConnectTimeout(10 * 1000);
    }

    private Resource getIdpResource(String body) {
        return doGetResource(body, "saml20_idp");
    }

    private Resource getSpResource(String body) {
        return doGetResource(body, "saml20_sp");
    }

    private Resource getRpResource(String body) {
        return doGetResource(body, "oidc10_rp");
    }

    private Resource doGetResource(String body, final String type) {
        LOG.debug("Fetching " + type + " metadata entries from {} with body {}", manageBaseUrl, body);
        ResponseEntity<String> responseEntity = restTemplate.exchange
                (manageBaseUrl + "/manage/api/internal/search/" + type, HttpMethod.POST,
                        new HttpEntity<>(body, this.httpHeaders), String.class);
        return new ByteArrayResource(responseEntity.getBody().getBytes());
    }

    @Override
    public List<EntityMetaData> serviceProviders() {
        List<EntityMetaData> serviceProviders = parseEntities(getSpResource(this.body));
        serviceProviders.addAll(parseEntities(getRpResource(this.body)));
        return serviceProviders;
    }

    @Override
    public List<EntityMetaData> identityProviders() {
        return parseEntities(getIdpResource(this.body));
    }

    @Override
    public Set<EntityMetaData> identityProvidersByAuthenticatingAuthority(String authenticatingAuthority) {
        EntityMetaData idp = identityProviderByEntityId(authenticatingAuthority);
        String institutionId = idp.getInstitutionId();
        if (StringUtils.hasText(institutionId)) {
            String body = this.bodyForInstitutionId.replace("@@institution_id@@", institutionId);
            List<EntityMetaData> entityMetaData = parseEntities(getIdpResource(body));
            return new HashSet<>(entityMetaData);
        } else {
            return Collections.singleton(idp);
        }

    }

    @Override
    public Set<EntityMetaData> serviceProvidersByInstitutionId(String institutionId) {
        if (StringUtils.isEmpty(institutionId)) {
            return Collections.emptySet();
        }
        HashSet<EntityMetaData> serviceProviders = new HashSet<>(parseEntities(getSpResource(this.bodyForInstitutionId.replace("@@institution_id@@",
                institutionId))));
        serviceProviders.addAll(new HashSet<>(parseEntities(getRpResource(this.bodyForInstitutionId.replace("@@institution_id@@",
                institutionId)))));
        return serviceProviders;
    }

    @Override
    public Optional<EntityMetaData> serviceProviderOptionalByEntityId(String entityId) {
        List<EntityMetaData> entityMetaData = parseEntities(getSpResource(this.bodyForEntity.replace("@@entityid@@",
                entityId)));
        if (entityMetaData.isEmpty()) {
            entityMetaData = parseEntities(getRpResource(this.bodyForEntity.replace("@@entityid@@",
                    entityId)));
        }
        return entityMetaData.isEmpty() ? Optional.empty() : Optional.of(entityMetaData.get(0));
    }

    @Override
    public Optional<EntityMetaData> identityProviderOptionalByEntityId(String entityId) {
        String replaced = this.bodyForEntity.replace("@@entityid@@", entityId);
        List<EntityMetaData> entityMetaData = parseEntities(getIdpResource(replaced));
        return entityMetaData.isEmpty() ? Optional.empty() : Optional.of(entityMetaData.get(0));
    }

    @Override
    public Map<String, EntityMetaData> identityProvidersByEntityIds(Collection<String> entityIds) {
        return getEntityMetaDataMap(entityIds, this::getIdpResource);
    }

    @Override
    public Map<String, EntityMetaData> serviceProvidersByEntityIds(Collection<String> entityIds) {
        Map<String, EntityMetaData> serviceProviders = getEntityMetaDataMap(entityIds, this::getSpResource);
        //for all missing ones we fetch them from oidc10_rp collection
        List<String> missing = serviceProviders.keySet().stream().filter(s -> !entityIds.contains(s)).collect(toList());
        if (!missing.isEmpty()) {
            Map<String, EntityMetaData> relyiingParties = getEntityMetaDataMap(missing, this::getRpResource);
            serviceProviders.putAll(relyiingParties);
        }
        return serviceProviders;
    }

    private Map<String, EntityMetaData> getEntityMetaDataMap(Collection<String> entityIds, Function<String, Resource> resource) {
        String queryValue = entityIds.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));

        String replaced = this.bodyForEntityIdIn.replace
                ("@@entityids@@", queryValue);
        Resource idpResource = resource.apply(replaced);
        return parseEntities(idpResource).stream().collect(toMap(e -> e.getEntityId(), e -> e));
    }

    @Override
    public EntityMetaData serviceProviderByEntityId(String entityId) {
        return nonEmptyOptionalToEntityMetaData(entityId, serviceProviderOptionalByEntityId(entityId));
    }

    @Override
    public EntityMetaData identityProviderByEntityId(String entityId) {
        return nonEmptyOptionalToEntityMetaData(entityId, identityProviderOptionalByEntityId(entityId));
    }

}
