package pdp.manage;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import pdp.JsonMapper;
import pdp.domain.EntityMetaData;
import pdp.domain.PdpPolicyDefinition;
import pdp.policies.PolicyMissingServiceProviderValidator;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class UrlResourceManage implements Manage {

    private static final Logger LOG = LoggerFactory.getLogger(UrlResourceManage.class);
    private final String manageBaseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpHeaders httpHeaders;

    private String requestedAttributes = "REQUESTED_ATTRIBUTES\":[\"metaDataFields.coin:institution_id\"," +
        " \"metaDataFields" +
        ".coin:policy_enforcement_decision_required\", \"allowedall\", \"allowedEntities\"]";

    private String body = "{\"" + requestedAttributes + "}";
    private String bodyForEntity = "{\"entityid\":\"@@entityid@@\", \""+requestedAttributes+"}";
    private String bodyForEntityIdIn = "{\"entityid\":[@@entityids@@], \""+requestedAttributes+"}";
    private String bodyForInstitutionId = "{\"metaDataFields.coin:institution_id\":\"@@institution_id@@\", \""+requestedAttributes+"}";

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
        LOG.debug("Fetching IDP metadata entries from {} with body {}", manageBaseUrl, body);
        ResponseEntity<String> responseEntity = restTemplate.exchange
            (manageBaseUrl + "/manage/api/internal/search/saml20_idp", HttpMethod.POST,
                new HttpEntity<>(body, this.httpHeaders), String.class);
        return new ByteArrayResource(responseEntity.getBody().getBytes());
    }

    private Resource getSpResource(String body) {
        LOG.debug("Fetching SP metadata entries from {} with body {}", manageBaseUrl, body);
        ResponseEntity<String> responseEntity = restTemplate.exchange
            (manageBaseUrl + "/manage/api/internal/search/saml20_sp", HttpMethod.POST,
                new HttpEntity<>(body, this.httpHeaders), String.class);
        return new ByteArrayResource(responseEntity.getBody().getBytes());
    }

    @Override
    public List<EntityMetaData> serviceProviders() {
        return parseEntities(getSpResource(this.body));
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
        return new HashSet<>(parseEntities(getSpResource(this.bodyForInstitutionId.replace("@@institution_id@@", institutionId))));
    }

    @Override
    public Optional<EntityMetaData> serviceProviderOptionalByEntityId(String entityId) {
        List<EntityMetaData> entityMetaData = parseEntities(getSpResource(this.bodyForEntity.replace("@@entityid@@",
            entityId)));
        return entityMetaData.isEmpty() ? Optional.empty() : Optional.of(entityMetaData.get(0));
    }

    @Override
    public Optional<EntityMetaData> identityProviderOptionalByEntityId(String entityId) {
        List<EntityMetaData> entityMetaData = parseEntities(getIdpResource(this.bodyForEntity.replace("@@entityid@@",
            entityId)));
        return entityMetaData.isEmpty() ? Optional.empty() : Optional.of(entityMetaData.get(0));
    }

    @Override
    public EntityMetaData serviceProviderByEntityId(String entityId) {
        return nonEmptyOptionalToEntityMetaData(entityId, serviceProviderOptionalByEntityId(entityId));
    }

    @Override
    public EntityMetaData identityProviderByEntityId(String entityId) {
        return nonEmptyOptionalToEntityMetaData(entityId, identityProviderOptionalByEntityId(entityId));
    }

    @Override
    public void enrichPdPPolicyDefinition(PdpPolicyDefinition pd) {
        List<String> entityIds = pd.getIdentityProviderIds();
        if (CollectionUtils.isEmpty(entityIds) || entityIds.stream().filter(s -> StringUtils.isEmpty(s)).count() == 0L) {
            pd.setIdentityProviderNames(new ArrayList<>());
            pd.setIdentityProviderNamesNl(new ArrayList<>());
        } else {
            String queryValue = String.join(",", entityIds.stream().map(s -> "\"\"").collect(toList()));

            List<EntityMetaData> identityProviders = parseEntities(getSpResource(this.bodyForEntityIdIn.replace
                ("@@entityids@@", queryValue)));

            pd.setIdentityProviderNames(identityProviders.stream().map(EntityMetaData::getNameEn).collect(toList()));
            pd.setIdentityProviderNamesNl(identityProviders.stream().map(EntityMetaData::getNameNl).collect(toList()));
        }
    }
}
