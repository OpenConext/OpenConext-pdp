package pdp.serviceregistry;

import pdp.domain.EntityMetaData;

import java.util.List;
import java.util.Set;

public interface ServiceRegistry {

  List<EntityMetaData> serviceProviders();

  List<EntityMetaData> identityProviders();

  Set<EntityMetaData> identityProvidersByAuthenticatingAuthority(String authenticatingAuthority);

  Set<EntityMetaData> serviceProvidersByInstitutionId(String institutionId);
}
