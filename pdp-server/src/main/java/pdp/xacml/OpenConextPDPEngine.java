package pdp.xacml;

import org.apache.openaz.xacml.api.*;
import org.apache.openaz.xacml.api.pdp.PDPException;
import org.apache.openaz.xacml.api.pdp.ScopeResolver;
import org.apache.openaz.xacml.pdp.OpenAZPDPEngine;
import org.apache.openaz.xacml.pdp.eval.EvaluationContext;
import org.apache.openaz.xacml.pdp.eval.EvaluationContextFactory;
import org.apache.openaz.xacml.std.StdMutableResponse;
import org.apache.openaz.xacml.std.StdMutableResult;
import pdp.sab.SabPIP;
import pdp.teams.TeamsPIP;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static pdp.util.StreamUtils.singletonCollector;

public class OpenConextPDPEngine extends OpenAZPDPEngine {

  public OpenConextPDPEngine(EvaluationContextFactory evaluationContextFactoryIn, Decision defaultDecisionIn, ScopeResolver scopeResolverIn) {
    super(evaluationContextFactoryIn, defaultDecisionIn, scopeResolverIn);
  }

  @Override
  protected Result processRequest(EvaluationContext evaluationContext) {
    Result result = super.processRequest(evaluationContext);
    String statusMessage = result.getStatus().getStatusMessage();
    if (result.getDecision().equals(Decision.INDETERMINATE) && "No matching root policy found".equals(statusMessage)) {
      /*
       * The spec is very clear in this: http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047211
       * We will override the INDETERMINATE
       */
      StdMutableResult mutableResult = (StdMutableResult) result;
      mutableResult.setDecision(Decision.NOTAPPLICABLE);
    }
    return result;
  }

}
