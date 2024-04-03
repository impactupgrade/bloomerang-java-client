package com.impactupgrade.integration.bloomerang.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class Designation extends AbstractModel {

  @JsonProperty("Amount")
  public Double amount;

  @JsonProperty("NonDeductibleAmount")
  public Double nonDeductibleAmount = 0.0;

  @JsonProperty("Type")
  public String type;

  @JsonProperty("FundId")
  public Integer fundId;

  @JsonProperty("RecurringDonationId")
  public Integer recurringDonationId;

  // Active, Closed, Overdue
  @JsonProperty("RecurringDonationStatus")
  public String recurringDonationStatus;

  @JsonProperty("RecurringDonationStartDate")
  public String recurringDonationStartDate;

  @JsonProperty("RecurringDonationEndDate")
  public String recurringDonationEndDate;

  @JsonProperty("RecurringDonationNextInstallmentDate")
  public String recurringDonationNextDate;

  // Weekly, EveryOtherWeekly, TwiceMonthly, Monthly, EveryOtherMonthly, Quarterly, Yearly
  @JsonProperty("RecurringDonationFrequency")
  public String recurringDonationFrequency;

  @JsonProperty("IsExtraPayment")
  public Boolean isExtraPayment;

  // TODO: The following is frustrating. Bloomerang uses one CustomField structure for requests, a different one
  //  for responses, and validation on their side isn't forgiving :(
  @JsonProperty("CustomValues")
  public List<JsonNode> customFields = new ArrayList<>();
}
