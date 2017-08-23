package com.walmart.models;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class representing a hold on a set of seats
 * 
 * @author wyn
 *
 */
public class BaseSeatHold {
  String customerEmail;
  long createdAt;
  long updatedAt;
  Set<Seat> seats = new HashSet<Seat>();

  public BaseSeatHold(String customerEmail) {
    this.customerEmail = customerEmail;
    createdAt = System.currentTimeMillis();
    updatedAt = createdAt;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public void setCustomerEmail(String customerEmail) {
    this.customerEmail = customerEmail;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }

  public long getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(long updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Set<Seat> getSeats() {
    return seats;
  }

  public void setSeats(Set<Seat> seats) {
    this.seats = seats;
  }
}
