package uk.gov.ons.census.notifyprocessor.model;

import lombok.Data;

@Data
public class Contact {

  private String title;
  private String forename;
  private String surname;
  private String telNo;
}
