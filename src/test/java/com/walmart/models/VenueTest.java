package com.walmart.models;

import static org.junit.Assert.*;

import org.junit.Test;

import com.walmart.VenueUtil;
import com.walmart.models.Venue;

public class VenueTest {

  @Test
  public void testStandardStage() {
    Venue venue = new Venue(1, "Standard Stage",
        VenueUtil.readSeatingLayoutFromResource("standard_stage.txt"));
    assertArrayEquals(new int[] {16, 1}, venue.getCenterStage());
    assertEquals(297, venue.getSeats().size());
  }

  @Test
  public void testArenaStage() {
    Venue venue =
        new Venue(1, "Arena Stage", VenueUtil.readSeatingLayoutFromResource("arena_stage.txt"));
    assertArrayEquals(new int[] {17, 5}, venue.getCenterStage());
    assertEquals(324, venue.getSeats().size());
  }

  @Test
  public void testThrustStage() {
    Venue venue =
        new Venue(1, "Thrust Stage", VenueUtil.readSeatingLayoutFromResource("thrust_stage.txt"));
    assertArrayEquals(new int[] {16, 3}, venue.getCenterStage());
    assertEquals(283, venue.getSeats().size());
  }
}
