package com.impactupgrade.integration.bloomerang.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Donation extends AbstractModel {

  @JsonProperty("AccountId")
  public Integer accountId;

  @JsonProperty("Amount")
  public Double amount;

  @JsonProperty("Method")
  public String method = "None";

  @JsonProperty("Date")
  public String date;

  @JsonProperty("Designations")
  public List<Designation> designations = new ArrayList<>();
}
