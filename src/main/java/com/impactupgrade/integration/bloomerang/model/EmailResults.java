package com.impactupgrade.integration.bloomerang.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class EmailResults extends AbstractModel {

  @JsonProperty("Total")
  public int total;

  @JsonProperty("Results")
  public List<Email> results;
}
