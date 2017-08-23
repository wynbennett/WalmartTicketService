package com.walmart;

import com.walmart.models.SeatHold;

public interface ContiguousTicketService extends TicketService {

  /**
   * Find and hold the best available seats for a customer
   *
   * @param numSeats the number of seats to find and hold
   * @param customerEmail unique identifier for the customer
   * @param contiguous - indicator that we want seats next to each other
   * @return a SeatHold object identifying the specific seats and related information
   */
  public SeatHold findAndHoldSeats(int numSeats, String customerEmail, boolean contiguous);
}
