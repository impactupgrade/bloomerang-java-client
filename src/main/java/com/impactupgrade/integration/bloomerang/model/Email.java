package com.impactupgrade.integration.bloomerang.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Email extends AbstractModel {

  @JsonProperty("Type")
  public String type = "Home";

  @JsonProperty("IsPrimary")
  public boolean isPrimary = true;

  @JsonProperty("Value")
  public String value;
}
