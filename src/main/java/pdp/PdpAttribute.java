package pdp;

import java.util.List;

public class PdpAttribute {

  private String name;
  private String value;

  public PdpAttribute() {
  }

  public PdpAttribute(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
