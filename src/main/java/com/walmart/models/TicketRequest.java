package com.walmart.models;

public class TicketRequest {
  private int venueId;
  private int numberOfSeats;
  private String customerEmail;

  public int getVenueId() {
    return venueId;
  }

  public void setVenueId(int venueId) {
    this.venueId = venueId;
  }

  public int getNumberOfSeats() {
    return numberOfSeats;
  }

  public void setNumberOfSeats(int numberOfSeats) {
    this.numberOfSeats = numberOfSeats;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public void setCustomerEmail(String customerEmail) {
    this.customerEmail = customerEmail;
  }
}
