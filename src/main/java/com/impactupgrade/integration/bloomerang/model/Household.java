package com.impactupgrade.integration.bloomerang.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Household extends AbstractModel {

  @JsonProperty("FullName")
  public String fullName;
}
