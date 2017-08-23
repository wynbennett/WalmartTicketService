package com.walmart.models;

public class Seat {
  String seatId;
  int venueId;
  String seatNumber;
  double distanceFromCenterStage;
  int seatPointX;
  int seatPointY;

  public Seat(int venueId, int seatPointX, int seatPointY) {
    this.venueId = venueId;
    this.seatNumber = String.format("%d_%d", seatPointX + 1, seatPointY + 1);
    this.seatId = String.format("%d_%s", venueId, seatNumber);
    this.seatPointX = seatPointX;
    this.seatPointY = seatPointY;
  }

  public String getSeatId() {
    return seatId;
  }

  public void setSeatId(String seatId) {
    this.seatId = seatId;
  }

  public int getVenueId() {
    return venueId;
  }

  public void setVenueId(int venueId) {
    this.venueId = venueId;
  }

  public String getSeatNumber() {
    return seatNumber;
  }

  public void setSeatNumber(String seatNumber) {
    this.seatNumber = seatNumber;
  }

  public int getSeatPointX() {
    return seatPointX;
  }

  public void setSeatPointX(int seatPointX) {
    this.seatPointX = seatPointX;
  }

  public int getSeatPointY() {
    return seatPointY;
  }

  public void setSeatPointY(int seatPointY) {
    this.seatPointY = seatPointY;
  }

  public double getDistanceFromCenterStage() {
    return distanceFromCenterStage;
  }

  public void setDistanceFromCenterStage(double distanceFromCenterStage) {
    this.distanceFromCenterStage = distanceFromCenterStage;
  }

  @Override
  public String toString() {
    return "Seat [seatId=" + seatId + ", venueId=" + venueId + ", seatNumber=" + seatNumber
        + ", distanceFromCenterStage=" + distanceFromCenterStage + ", seatPointX=" + seatPointX
        + ", seatPointY=" + seatPointY + "]";
  }
}
