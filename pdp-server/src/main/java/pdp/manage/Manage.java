package pdp.manage;

import pdp.domain.EntityMetaData;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface Manage {

    List<EntityMetaData> serviceProviders();

    List<EntityMetaData> identityProviders();

    Set<EntityMetaData> identityProvidersByAuthenticatingAuthority(String authenticatingAuthority);

    Set<EntityMetaData> serviceProvidersByInstitutionId(String institutionId);

    Optional<EntityMetaData> serviceProviderOptionalByEntityId(String entityId);

    Optional<EntityMetaData> identityProviderOptionalByEntityId(String entityId);

    EntityMetaData serviceProviderByEntityId(String entityId);

    EntityMetaData identityProviderByEntityId(String entityId);

    List<String> identityProviderNames(List<String> entityIds);
}
