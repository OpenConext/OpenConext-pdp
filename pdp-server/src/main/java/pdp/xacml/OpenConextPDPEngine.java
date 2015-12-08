package pdp.xacml;

import org.apache.openaz.xacml.api.*;
import org.apache.openaz.xacml.api.pdp.PDPException;
import org.apache.openaz.xacml.api.pdp.ScopeResolver;
import org.apache.openaz.xacml.pdp.OpenAZPDPEngine;
import org.apache.openaz.xacml.pdp.eval.EvaluationContext;
import org.apache.openaz.xacml.pdp.eval.EvaluationContextFactory;
import org.apache.openaz.xacml.std.StdMutableResponse;
import org.apache.openaz.xacml.std.StdMutableResult;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static pdp.PdpApplication.singletonCollector;

public class OpenConextPDPEngine extends OpenAZPDPEngine {

  private final boolean policyIncludeAggregatedAttributes;

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
    pdpResponse = includeAggregatedAttributes(pepRequest, pdpResponse);
    return pdpResponse;
  }

  //Add the PIP attributes that were aggregated
  private Response includeAggregatedAttributes(Request request, Response pdpResponse) {
    Result result = pdpResponse.getResults().stream().collect(singletonCollector());

    StdMutableResult newResult = new StdMutableResult(result.getDecision(), result.getStatus());
    newResult.addObligations(result.getObligations());
    newResult.addAdvice(result.getAssociatedAdvice());
    newResult.addPolicyIdentifiers(result.getPolicyIdentifiers());
    newResult.addPolicySetIdentifiers(result.getPolicySetIdentifiers());

    //feature toggle
    if (policyIncludeAggregatedAttributes && result.getDecision().equals(Decision.PERMIT)) {
      Collection<RequestAttributes> requestAttributes = request.getRequestAttributes();
      List<Attribute> attributes = requestAttributes.stream().map(RequestAttributes::getAttributes).flatMap(Collection::stream).collect(toList());
      List<AttributeCategory> attributeCategories = result.getAttributes().stream().filter(attrCat -> isAddedAttributeCategory(attributes, attrCat)).collect(toList());
      newResult.addAttributeCategories(attributeCategories);
    }

    return new StdMutableResponse(newResult);
  }

  private boolean isAddedAttributeCategory(Collection<Attribute> attributes, AttributeCategory attributeCategory) {
    return attributes.stream().filter(attr -> attr.getAttributeId().equals(attributeCategory.getCategory())).count() == 0;
  }


}
