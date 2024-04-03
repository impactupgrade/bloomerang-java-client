package com.impactupgrade.integration.bloomerang;

import com.google.common.base.Strings;
import com.impactupgrade.integration.bloomerang.model.Constituent;
import com.impactupgrade.integration.bloomerang.model.ConstituentSearchResults;
import com.impactupgrade.integration.bloomerang.model.Donation;
import com.impactupgrade.integration.bloomerang.model.DonationResults;
import com.impactupgrade.integration.bloomerang.model.EmailResults;
import com.impactupgrade.integration.bloomerang.model.PhoneResults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class BloomerangClient {

  private static final Logger log = LogManager.getLogger(BloomerangClient.class);

  private static final String BLOOMERANG_URL = "https://api.bloomerang.co/v2/";

  private final String apiKey;

  public BloomerangClient(String apiKey) {
    this.apiKey = apiKey;
  }

  public Constituent getConstituentById(String id) {
    return get(BLOOMERANG_URL + "constituent/" + id, Constituent.class);
  }

  public List<Constituent> searchConstituents(String firstName, String lastName, String email, String _phone, Set<String> _keywords) {
    Set<String> keywords = new HashSet<>();

    String phone = _phone == null ? null : _phone.replaceAll("[\\D]", "");

    if (!Strings.isNullOrEmpty(email)) {
      keywords.add(email);
    }
    if (!Strings.isNullOrEmpty(phone)) {
      keywords.add(phone);
    }
    if (!Strings.isNullOrEmpty(firstName)) {
      keywords.add(firstName);
    }
    if (!Strings.isNullOrEmpty(lastName)) {
      keywords.add(lastName);
    }
    if (!keywords.isEmpty()) {
      keywords.addAll(_keywords);
    }

    String query = keywords.stream().map(k -> {
      k = k.trim();
      try {
        return URLEncoder.encode(k, StandardCharsets.UTF_8.toString());
      } catch (UnsupportedEncodingException e) {
        // will never happen
        return null;
      }
    }).filter(Objects::nonNull).collect(Collectors.joining("+"));

    ConstituentSearchResults constituentSearchResults = null;
    try {
      constituentSearchResults = get(BLOOMERANG_URL + "constituents/search?search=" + query, ConstituentSearchResults.class);
    } catch (Exception e) {
//      env.logJobError("search failed", e);
    }
    if (constituentSearchResults == null) {
      return Collections.emptyList();
    }

    for (Constituent constituent : constituentSearchResults.results) {
      if (constituent.emailIds.size() > 1) {
        List<String> emailIds = constituent.emailIds.stream().filter(id -> id != constituent.primaryEmail.id).map(Object::toString).toList();
        constituent.secondaryEmails = get(BLOOMERANG_URL + "emails?id=" + String.join("%7C", emailIds), EmailResults.class).results;
      }
      if (constituent.phoneIds.size() > 1) {
        List<String> phoneIds = constituent.phoneIds.stream().filter(id -> id != constituent.primaryPhone.id).map(Object::toString).toList();
        constituent.secondaryPhones = get(BLOOMERANG_URL + "phones?id=" + String.join("%7C", phoneIds), PhoneResults.class).results;
      }
    }

    // API appears to be doing SUPER forgiving fuzzy matches. If the search was by email/phone/name, verify those explicitly.
    // If it was a name search, make sure the name actually matches.
    return constituentSearchResults.results.stream()
        .filter(c -> Strings.isNullOrEmpty(email)
            || (c.primaryEmail != null && !Strings.isNullOrEmpty(c.primaryEmail.value) && c.primaryEmail.value.equalsIgnoreCase(email))
            || (c.secondaryEmails.stream().anyMatch(e -> e.value.equalsIgnoreCase(email))))
        .filter(c -> Strings.isNullOrEmpty(phone)
            || (c.primaryPhone != null && !Strings.isNullOrEmpty(c.primaryPhone.number) && c.primaryPhone.number.replaceAll("[\\D]", "").contains(phone))
            || (c.secondaryPhones.stream().anyMatch(p -> p.number.replaceAll("[\\D]", "").contains(phone))))
        .filter(c -> Strings.isNullOrEmpty(firstName) || firstName.equalsIgnoreCase(c.firstName))
        .filter(c -> Strings.isNullOrEmpty(lastName) || lastName.equalsIgnoreCase(c.lastName))
        .collect(Collectors.toList());
  }

  public String insertConstituent(Constituent constituent) {
    constituent = post(BLOOMERANG_URL + "constituent", constituent, APPLICATION_JSON, Constituent.class);

    if (constituent == null) {
      return null;
    }
    log.info("inserted constituent {}", constituent.id);
    return constituent.id + "";
  }

  public Optional<Donation> getDonation(String constituentId, List<String> donationTypes,
      String customFieldKey, String customFieldValue) {
    return getDonation(constituentId, donationTypes, customFieldKey, List.of(customFieldValue));
  }

  public Optional<Donation> getDonation(String constituentId, List<String> donationTypes,
      String customFieldKey, List<String> _customFieldValues) {
    if (Strings.isNullOrEmpty(customFieldKey)) {
      return Optional.empty();
    }

    List<String> customFieldValues = _customFieldValues.stream().filter(v -> !Strings.isNullOrEmpty(v)).collect(Collectors.toList());
    if (customFieldValues.isEmpty()) {
      return Optional.empty();
    }

    for (String donationType : donationTypes) {
      Optional<Donation> donation = getDonations(constituentId, donationType).results.stream().filter(d -> {
        String customFieldValue = getCustomFieldValue(d, customFieldKey);
        return customFieldValues.contains(customFieldValue);
      }).findFirst();
      if (donation.isPresent()) {
        return donation;
      }
    }

    return Optional.empty();
  }

  // type: Donation, Pledge, PledgePayment, RecurringDonation, RecurringDonationPayment
  public DonationResults getDonations(String contactId, String type) {
    // Assuming that the default page size of 50 is enough...
    return get(
        BLOOMERANG_URL + "transactions?type=" + type + "&accountId=" + contactId + "&orderBy=Date&orderDirection=Desc",
        DonationResults.class
    );
  }

  public Donation getDonation(String donationId) {
    return get(BLOOMERANG_URL + "transaction/" + donationId, Donation.class);
  }

  public String insertDonation(Donation donation) {
    donation = post(BLOOMERANG_URL + "transaction", donation, APPLICATION_JSON, Donation.class);

    if (donation == null) {
      return null;
    }
    log.info("inserted donation {}", donation.id);
    return donation.id + "";
  }

  public String insertRecurringDonation(Donation donation) {
    donation = post(BLOOMERANG_URL + "transaction", donation, APPLICATION_JSON, Donation.class);

    if (donation == null) {
      return null;
    }
    log.info("inserted recurring donation {}", donation.id);
    return donation.id + "";
  }

  public void updateRecurringDonation(Donation recurringDonation) {
    put(BLOOMERANG_URL + "transaction/" + recurringDonation.id, recurringDonation, APPLICATION_JSON, Donation.class);
  }

  public void closeRecurringDonation(Donation recurringDonation) {
    recurringDonation.designations.stream().filter(d -> !Strings.isNullOrEmpty(d.recurringDonationStatus))
        .forEach(rd -> {
          rd.recurringDonationStatus = "Closed";
          rd.recurringDonationEndDate = new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().getTime());

          // TODO: See the note on Donation.customFields. Since we currently have the response format and are about to push
          //  in the request format, simply clear them out since we don't need them.
          rd.customFields = null;
        });

    put(BLOOMERANG_URL + "transaction/" + recurringDonation.id, recurringDonation, APPLICATION_JSON, Donation.class);
  }

  public String getCustomFieldValue(Donation donation, String customFieldKey) {
    if (Strings.isNullOrEmpty(customFieldKey)) {
      return null;
    }
    int customFieldId = Integer.parseInt(customFieldKey);
    return donation.designations.stream().flatMap(designation -> designation.customFields.stream())
        .filter(jsonNode -> jsonNode.has("FieldId") && jsonNode.get("FieldId").asInt() == customFieldId)
        .map(jsonNode -> jsonNode.get("Value").get("Value").asText())
        .findFirst().orElse(null);
  }

  private <T> T get(String url, Class<T> clazz) {
    WebTarget webTarget = ClientBuilder.newClient().target(url);
    Response response = webTarget.request().header("X-API-KEY", apiKey).get();

    if (response.getStatus() < 300) {
      if (clazz != null) {
        return response.readEntity(clazz);
      }
    } else if (response.getStatus() == 404) {
      log.info("GET not found: url={}", url);
    } else {
      log.warn("GET failed: url={} code={} message={}", url, response.getStatus(), response.readEntity(String.class));
    }
    return null;
  }

  public <S, T> T post(String url, S entity, String mediaType, Class<T> clazz) {
    WebTarget webTarget = ClientBuilder.newClient().target(url);
    Response response = webTarget.request().header("X-API-KEY", apiKey).post(Entity.entity(entity, mediaType));

    if (response.getStatus() < 300) {
      if (clazz != null) {
        return response.readEntity(clazz);
      }
    } else {
      log.warn("POST failed: url={} code={} message={}", url, response.getStatus(), response.readEntity(String.class));
    }
    return null;
  }

  public <S, T> T put(String url, S entity, String mediaType, Class<T> clazz) {
    WebTarget webTarget = ClientBuilder.newClient().target(url);
    Response response = webTarget.request().header("X-API-KEY", apiKey).put(Entity.entity(entity, mediaType));

    if (response.getStatus() < 300) {
      if (clazz != null) {
        return response.readEntity(clazz);
      }
    } else {
      log.warn("PUT failed: url={} code={} message={}", url, response.getStatus(), response.readEntity(String.class));
    }
    return null;
  }
}
