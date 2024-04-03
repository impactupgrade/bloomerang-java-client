package com.impactupgrade.integration.bloomerang.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Constituent extends AbstractModel {

  @JsonProperty("Type")
  public String type = "Individual";

  @JsonProperty("FirstName")
  public String firstName;

  @JsonProperty("LastName")
  public String lastName;

  // TODO: Annoying issue. We may need to set this for Organization constituents. But the API won't let you set this
  //  for Individuals. Makes sense, but it's giving that same error even when this is null. We might need to extend the class...
//    @JsonProperty("FullName")
//    public String fullName;

  @JsonProperty("HouseholdId")
  public Integer householdId;

  @JsonProperty("PrimaryEmail")
  public Email primaryEmail;

  @JsonProperty("PrimaryPhone")
  public Phone primaryPhone;

  @JsonProperty("PrimaryAddress")
  public Address primaryAddress;

  @JsonProperty("EmailIds")
  public List<Integer> emailIds = new ArrayList<>();

  @JsonProperty("PhoneIds")
  public List<Integer> phoneIds = new ArrayList<>();

  // transient
  public List<Email> secondaryEmails = new ArrayList<>();
  public List<Phone> secondaryPhones = new ArrayList<>();
}
