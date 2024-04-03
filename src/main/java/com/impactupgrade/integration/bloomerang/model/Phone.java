package com.impactupgrade.integration.bloomerang.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Phone extends AbstractModel {

  @JsonProperty("Type")
  public String type = "Home";

  @JsonProperty("IsPrimary")
  public boolean isPrimary = true;

  @JsonProperty("Number")
  public String number;
}
