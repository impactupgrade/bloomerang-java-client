package com.impactupgrade.integration.bloomerang.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Address extends AbstractModel {

  @JsonProperty("Type")
  public String type = "Home";

  @JsonProperty("IsPrimary")
  public boolean isPrimary = true;

  @JsonProperty("Street")
  public String street;

  @JsonProperty("City")
  public String city;

  @JsonProperty("State")
  public String state;

  @JsonProperty("PostalCode")
  public String postalCode;

  @JsonProperty("Country")
  public String country;
}
