package pdp.teams;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static pdp.teams.VootClientConfig.URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN;

public class MockTeamsPIP extends TeamsPIP {

  private VootClient mockVootClient = new VootClient(null, null) {
    @Override
    public List<String> groups(String userUrn) {
      return URN_COLLAB_PERSON_EXAMPLE_COM_ADMIN.equals(userUrn) ?
          Arrays.asList("urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:managementvo") : Collections.EMPTY_LIST;
    }
  };

  @Override
  public VootClient getVootClient() {
    return mockVootClient;
  }

}
