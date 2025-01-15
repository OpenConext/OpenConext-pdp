package pdp.conflicts;

import org.springframework.util.CollectionUtils;
import pdp.domain.PdpPolicyDefinition;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.IntStream.range;

public class PolicyConflictService {

    public Map<String, List<PdpPolicyDefinition>> conflicts(List<PdpPolicyDefinition> policies) {
        Set<PdpPolicyDefinition> conflicts = new HashSet<>();
        //we check each policy with every other policy, so we need an outer and inner loop
        range(0, policies.size()).forEach(i -> range(i + 1, policies.size()).forEach(j -> {
            PdpPolicyDefinition one = policies.get(i);
            PdpPolicyDefinition two = policies.get(j);
            if (conflict(one, two)) {
                conflicts.addAll(asList(one, two));
            }
        }));
        Map<String, List<PdpPolicyDefinition>> result = new HashMap<>();
        conflicts.forEach(conflict -> {
            conflict.getServiceProviderNames().forEach(name -> {
                if (result.containsKey(name)) {
                    List<PdpPolicyDefinition> value = result.get(name);
                    value.add(conflict);

                } else {
                    List<PdpPolicyDefinition> value = new ArrayList<>();
                    value.add(conflict);
                    result.put(name, value);
                }

            });
        });
        return result;

    }

    //if the two SP's are equal and there are overlapping IdP's or one policy has no IdP then there is a conflict
    private boolean conflict(PdpPolicyDefinition one, PdpPolicyDefinition two) {
        return one.getType().equals(two.getType()) && overlapping(one.getServiceProviderIds(), two.getServiceProviderIds()) &&
            (CollectionUtils.isEmpty(one.getIdentityProviderIds()) ||
                CollectionUtils.isEmpty(two.getIdentityProviderIds()) ||
                overlapping(one.getIdentityProviderIds(), two.getIdentityProviderIds()));
    }

    private boolean overlapping(List<String> idps, List<String> otherIdps) {
        return otherIdps.stream().map(idp -> idps.stream().anyMatch(s -> s.equals(idp))).anyMatch(b -> b);
    }


}
