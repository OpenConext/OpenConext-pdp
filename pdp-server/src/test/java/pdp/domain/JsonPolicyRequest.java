package pdp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class JsonPolicyRequest {

  @JsonProperty("Request")
  public Request request;

  public JsonPolicyRequest() {
  }

  public JsonPolicyRequest(Request request) {
    this.request = request;
  }

  public static class Request {
    @JsonProperty("ReturnPolicyIdList")
    public boolean returnPolicyIdList;

    @JsonProperty("CombinedDecision")
    public boolean combinedDecision;

    @JsonProperty("AccessSubject")
    public AttributeHolder accessSubject = new AttributeHolder();

    @JsonProperty("Resource")
    public AttributeHolder resource = new AttributeHolder();
  }

  public static class AttributeHolder {
    @JsonProperty("Attribute")
    public List<Attribute> attributes = new ArrayList<>();
  }

  public static class Attribute {
    @JsonProperty("AttributeId")
    public String attributeId;
    @JsonProperty("Value")
    public String value;

    public Attribute() {
    }

    public Attribute(String attributeId, String value) {
      this.attributeId = attributeId;
      this.value = value;
    }
  }

  @JsonIgnore
  public void deleteAttribute(String attributeId) {
    this.request.accessSubject.attributes = filter(request.accessSubject.attributes, attributeId);
    this.request.resource.attributes = filter(request.resource.attributes, attributeId);
  }

  private List<Attribute> filter(List<Attribute> attributes, String attributeId) {
    return attributes.stream().filter(attr -> !attr.attributeId.equalsIgnoreCase(attributeId)).collect(toList());
  }

  @JsonIgnore
  public void addOrReplaceResourceAttribute(String attributeId, String value) {
    this.deleteAttribute(attributeId);
    this.request.resource.attributes.add(new Attribute(attributeId, value));
  }

  @JsonIgnore
  public void addOrReplaceAccessSubjectAttribute(String attributeId, String value) {
    this.deleteAttribute(attributeId);
    this.request.accessSubject.attributes.add(new Attribute(attributeId, value));
  }

  @JsonIgnore
  public JsonPolicyRequest copy() {
    Request requestCopy = new Request();
    requestCopy.combinedDecision = this.request.combinedDecision;
    requestCopy.returnPolicyIdList = this.request.returnPolicyIdList;
    requestCopy.accessSubject.attributes = this.request.accessSubject.attributes.stream().map(attr -> new Attribute(attr.attributeId, attr.value)).collect(toList());
    requestCopy.resource.attributes = this.request.resource.attributes.stream().map(attr -> new Attribute(attr.attributeId, attr.value)).collect(toList());
    return new JsonPolicyRequest(requestCopy);
  }

}
