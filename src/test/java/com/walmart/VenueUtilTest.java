package com.walmart;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.walmart.models.Seat;

public class VenueUtilTest {

  @Test
  public void testReadSeatingLayout() {
    List<String> layout = VenueUtil.readSeatingLayoutFromResource("standard_stage.txt");
    assertEquals(11, layout.size());
    assertEquals("-----------[[ STAGE ]]-----------", layout.get(0));
    assertEquals("sssssssssssssssssssssssssssssssss", layout.get(layout.size() - 1));
  }

  @Test
  public void testDistanceToCenterStage() {
    int[] centerStage = new int[] {6, 6};
    int[] dimensions = new int[] {11, 11};
    int seatX = 6;
    int seatY = 0;
    double dist = VenueUtil.distanceToCenterStage(centerStage, dimensions, seatX, seatY);
    assertEquals(5.454545d, dist, 0.00001);
  }

  @Test
  public void testFindCenterStage() {
    char[][] layout =
        new char[][] {new char[] {'-', '-', '-', '-', '-'}, new char[] {'s', 's', 's', 's', 's'}};
    int[] centerStage = VenueUtil.findCenterStage(layout);
    assertEquals(2, centerStage[0]);
    assertEquals(0, centerStage[1]);
  }

  @Test
  public void testInitializeSeats() {
    char[][] layout = new char[][] { new char[] {'-', ' '}, new char[] { ' ','s'} };
    int[] centerStage = new int[] { 0, 0 };
    List<Seat> seats = VenueUtil.initializeSeats(1, layout, centerStage);
    assertEquals(1, seats.size());
    Seat seat = seats.get(0);
    assertEquals("2_2", seat.getSeatNumber());
    assertEquals(1, seat.getSeatPointX());
    assertEquals(1, seat.getSeatPointY());
    assertEquals(1, seat.getVenueId());
  }
}
