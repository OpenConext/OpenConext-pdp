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
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static pdp.PdpApplication.singletonCollector;

public class OpenConextPDPEngine extends OpenAZPDPEngine {

  private final boolean policyIncludeAggregatedAttributes;

  private static final List<String> includeAggregatedAttributesIdentifiers = Arrays.asList(TeamsPIP.GROUP_URN, SabPIP.SAB_URN);

  public OpenConextPDPEngine(boolean policyIncludeAggregatedAttributes, EvaluationContextFactory evaluationContextFactoryIn, Decision defaultDecisionIn, ScopeResolver scopeResolverIn) {
    super(evaluationContextFactoryIn, defaultDecisionIn, scopeResolverIn);
    this.policyIncludeAggregatedAttributes = policyIncludeAggregatedAttributes;
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

  @Override
  public Response decide(Request pepRequest) throws PDPException {
    Response pdpResponse = super.decide(pepRequest);
    pdpResponse = includeAggregatedAttributes(pdpResponse);
    return pdpResponse;
  }

  //Add the PIP attributes that were aggregated
  private Response includeAggregatedAttributes(Response pdpResponse) {
    Result result = pdpResponse.getResults().stream().collect(singletonCollector());

    StdMutableResult newResult = new StdMutableResult(result.getDecision(), result.getStatus());
    newResult.addObligations(result.getObligations());
    newResult.addAdvice(result.getAssociatedAdvice());
    newResult.addPolicyIdentifiers(result.getPolicyIdentifiers());
    newResult.addPolicySetIdentifiers(result.getPolicySetIdentifiers());

    //feature toggle
    if (policyIncludeAggregatedAttributes && result.getDecision().equals(Decision.PERMIT)) {
      /*
       * We could filter out all the attributes that were already in the request, but for now we hard-code
       * which attributes we will send back. When new PIPEngine implementations are added then the
       * attribute identifier(s) need to be added
       */
      List<AttributeCategory> attributeCategories = result.getAttributes().stream().filter(attrCat -> isAddedAttributeCategory(attrCat)).distinct().collect(toList());
      newResult.addAttributeCategories(attributeCategories);
    }

    return new StdMutableResponse(newResult);
  }

  private boolean isAddedAttributeCategory(AttributeCategory attributeCategory) {
    return includeAggregatedAttributesIdentifiers.contains(attributeCategory.getCategory().getUri().toString());
  }


}
