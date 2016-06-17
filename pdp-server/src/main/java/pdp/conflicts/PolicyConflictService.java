package pdp.conflicts;

import org.springframework.util.CollectionUtils;
import pdp.domain.PdpPolicyDefinition;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.IntStream.range;

public class PolicyConflictService {

  public Map<String, List<PdpPolicyDefinition>> conflicts(List<PdpPolicyDefinition> policies) {
    Set<PdpPolicyDefinition> conflicts = new HashSet<>();
    //we check each policy with every other policy, so we need an outer and inner loop
    range(0, policies.size()).forEachOrdered(i -> range(i + 1, policies.size()).forEachOrdered(j -> {
      PdpPolicyDefinition one = policies.get(i);
      PdpPolicyDefinition two = policies.get(j);
      if (conflict(one, two)) {
        conflicts.addAll(asList(one, two));
      }
    }));
    return conflicts.stream().collect(groupingBy(PdpPolicyDefinition::getServiceProviderName));
  }

  //if the two SP's are equal and there are overlapping IdP's or one policy has no IdP then there is a conflict
  private boolean conflict(PdpPolicyDefinition one, PdpPolicyDefinition two) {
    return one.getServiceProviderId().equals(two.getServiceProviderId()) &&
        (CollectionUtils.isEmpty(one.getIdentityProviderIds()) ||
            CollectionUtils.isEmpty(two.getIdentityProviderIds()) ||
            overlapping(one.getIdentityProviderIds(), two.getIdentityProviderIds()));
  }

  private boolean overlapping(List<String> idps, List<String> otherIdps) {
    return otherIdps.stream().map(idp -> idps.stream().anyMatch(s -> s.equals(idp))).anyMatch(b -> b);
  }


}
