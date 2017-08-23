package com.walmart.models;

import java.util.UUID;

/**
 * Class representing a executed reservation of a seat
 * 
 * @author wyn
 *
 */
public class Reservation extends BaseSeatHold {
  String reservationId;

  public Reservation(SeatHold seatHold) {
    super(seatHold.customerEmail);
    this.seats = seatHold.seats;
    this.reservationId = UUID.randomUUID().toString();
  }

  public String getReservationId() {
    return reservationId;
  }

  public void setReservationId(String reservationId) {
    this.reservationId = reservationId;
  }

  @Override
  public String toString() {
    return "Reservation [reservationId=" + reservationId + ", customerEmail=" + customerEmail
        + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", seats=" + seats + "]";
  }
}
