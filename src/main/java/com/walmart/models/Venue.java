package com.walmart.models;

import java.util.ArrayList;
import java.util.List;

import com.walmart.VenueUtil;

public class Venue {

  int venueId;
  String venueName;
  char[][] layout;
  int[] centerStage;
  List<Seat> seats = new ArrayList<Seat>();

  public Venue(int venueId, String venueName, List<String> layoutStr) {
    this.venueId = venueId;
    this.venueName = venueName;
    initializeLayout(layoutStr);
    seats = VenueUtil.initializeSeats(venueId, layout, centerStage);
  }

  private void initializeLayout(List<String> strLayout) {
    layout = new char[strLayout.size()][];
    for (int i = 0; i < strLayout.size(); i++) {
      String layoutLine = strLayout.get(i);
      layout[i] = layoutLine.toCharArray();
    }
    centerStage = VenueUtil.findCenterStage(layout);
  }

  public int getVenueId() {
    return venueId;
  }

  public void setVenueId(int venueId) {
    this.venueId = venueId;
  }

  public String getVenueName() {
    return venueName;
  }

  public void setVenueName(String venueName) {
    this.venueName = venueName;
  }

  public List<Seat> getSeats() {
    return seats;
  }

  public void setSeats(List<Seat> seats) {
    this.seats = seats;
  }

  public char[][] getLayout() {
    return layout;
  }

  public void setLayout(char[][] layout) {
    this.layout = layout;
  }

  public void setLayout(List<String> layoutStr) {
    initializeLayout(layoutStr);
  }

  public int[] getCenterStage() {
    return centerStage;
  }

  public void setCenterStage(int[] centerStage) {
    this.centerStage = centerStage;
  }
}
