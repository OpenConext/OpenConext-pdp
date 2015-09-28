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
    @JsonProperty("AccessSubject")
    public List<AttributeHolder> accessSubject = new ArrayList<>();
    @JsonProperty("Resource")
    public List<AttributeHolder> resource = new ArrayList<>();
  }

  public static class AttributeHolder {
    @JsonProperty("Attribute")
    public Attribute attribute;

    public AttributeHolder() {
    }

    public AttributeHolder(Attribute attribute) {
      this.attribute = attribute;
    }
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
    this.request.accessSubject = filter(request.accessSubject, attributeId);
    this.request.resource = filter(request.resource, attributeId);
  }

  private List<AttributeHolder> filter(List<AttributeHolder> attributeHolders, String attributeId) {
    return attributeHolders.stream().filter(attributeHolder -> !attributeHolder.attribute.attributeId.equalsIgnoreCase(attributeId)).collect(toList());
  }

  @JsonIgnore
  public void addOrReplaceResourceAttribute(String attributeId, String value) {
    this.deleteAttribute(attributeId);
    this.request.resource.add(new AttributeHolder(new Attribute(attributeId, value)));
  }

  @JsonIgnore
  public void addOrReplaceAccessSubjectAttribute(String attributeId, String value) {
    this.deleteAttribute(attributeId);
    this.request.accessSubject.add(new AttributeHolder(new Attribute(attributeId, value)));
  }

  @JsonIgnore
  public JsonPolicyRequest copy() {
    return new JsonPolicyRequest(this.request);
  }

}
