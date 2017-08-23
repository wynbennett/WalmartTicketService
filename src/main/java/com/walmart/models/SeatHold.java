package com.walmart.models;

/**
 * Class representing a hold on a
 * 
 * @author wyn
 *
 */
public class SeatHold extends BaseSeatHold {

  int seatHoldId;

  public SeatHold(int seatHoldId, String customerEmail) {
    super(customerEmail);
    this.seatHoldId = seatHoldId;
  }

  public int getSeatHoldId() {
    return seatHoldId;
  }

  public void setSeatHoldId(int seatHoldId) {
    this.seatHoldId = seatHoldId;
  }

  @Override
  public String toString() {
    return "SeatHold [seatHoldId=" + seatHoldId + ", customerEmail=" + customerEmail
        + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", seats=" + seats + "]";
  }
}
