package com.walmart;

import static org.junit.Assert.*;

import org.junit.Test;

import com.walmart.models.SeatHold;
import com.walmart.models.Venue;

public class TicketServiceImplTest {

  @Test
  public void testNumSeatsAvailable() {
    TicketServiceManager ticketServiceManager = null;
    try {
      ticketServiceManager = TicketServiceManager.getInstance(
          Constants.DEFAULT_REMOVAL_CHECK_INTERVAL_MS, Constants.DEFAULT_SEAT_HOLD_TIME_SECS);
      Venue venue = new Venue(1, "Standard Stage",
          VenueUtil.readSeatingLayoutFromResource("standard_stage.txt"));
      TicketService ticketService = new TicketServiceImpl(venue);
      assertEquals(297, ticketService.numSeatsAvailable());
    } finally {
      shutdown(ticketServiceManager);
    }
  }

  @Test
  public void testFindAndHoldSeats() {
    TicketServiceManager ticketServiceManager = null;
    try {
      ticketServiceManager = TicketServiceManager.getInstance(
          Constants.DEFAULT_REMOVAL_CHECK_INTERVAL_MS, Constants.DEFAULT_SEAT_HOLD_TIME_SECS);
      Venue venue = new Venue(1, "Standard Stage",
          VenueUtil.readSeatingLayoutFromResource("standard_stage.txt"));
      TicketService ticketService = new TicketServiceImpl(venue);
      assertEquals(297, ticketService.numSeatsAvailable());

      String emailAddress = "test@walmart.com";
      SeatHold seatHold = ticketService.findAndHoldSeats(2, emailAddress);
      assertEquals(2, seatHold.getSeats().size());
      assertEquals(emailAddress, seatHold.getCustomerEmail());
    } finally {
      shutdown(ticketServiceManager);
    }
  }

  @Test
  public void testReserveSeats() {
    TicketServiceManager ticketServiceManager = null;
    try {
      ticketServiceManager = TicketServiceManager.getInstance(
          Constants.DEFAULT_REMOVAL_CHECK_INTERVAL_MS, Constants.DEFAULT_SEAT_HOLD_TIME_SECS);
      Venue venue = new Venue(1, "Standard Stage",
          VenueUtil.readSeatingLayoutFromResource("standard_stage.txt"));
      TicketService ticketService = new TicketServiceImpl(venue);
      assertEquals(297, ticketService.numSeatsAvailable());

      String emailAddress = "test@walmart.com";
      SeatHold seatHold = ticketService.findAndHoldSeats(2, emailAddress);
      assertEquals(2, seatHold.getSeats().size());
      assertEquals(emailAddress, seatHold.getCustomerEmail());

      String reservationId = ticketService.reserveSeats(seatHold.getSeatHoldId(), emailAddress);
      assertNotNull(reservationId);
      assertTrue(ticketServiceManager.getSeatHolds().isEmpty());
    } finally {
      shutdown(ticketServiceManager);
    }
  }

  @Test
  public void testExpireSeatsViaThread() throws InterruptedException {
    TicketServiceManager ticketServiceManager = null;
    try {
      // Make the expiration thread run every 500ms to expire the hold before we try to reserve
      ticketServiceManager = TicketServiceManager.getInstance(
          Constants.DEFAULT_REMOVAL_CHECK_INTERVAL_MS, Constants.DEFAULT_SEAT_HOLD_TIME_SECS);
      Venue venue = new Venue(1, "Standard Stage",
          VenueUtil.readSeatingLayoutFromResource("standard_stage.txt"));
      TicketService ticketService = new TicketServiceImpl(venue, 1);
      assertEquals(297, ticketService.numSeatsAvailable());

      String emailAddress = "test@walmart.com";
      SeatHold seatHold = ticketService.findAndHoldSeats(2, emailAddress);
      assertEquals(2, seatHold.getSeats().size());
      assertEquals(emailAddress, seatHold.getCustomerEmail());

      Thread.sleep(1001);
      String reservationId = ticketService.reserveSeats(seatHold.getSeatHoldId(), emailAddress);
      assertNull(reservationId);
      assertTrue(ticketServiceManager.getSeatHolds().isEmpty());
    } finally {
      shutdown(ticketServiceManager);
    }
  }

  @Test
  public void testExpireSeatsEagerly() throws InterruptedException {
    TicketServiceManager ticketServiceManager = null;
    try {
      // Make the expiration thread run every 5000ms to not let it trigger before we try to reserve
      ticketServiceManager = TicketServiceManager.getInstance(
          5000, Constants.DEFAULT_SEAT_HOLD_TIME_SECS);
      Venue venue = new Venue(1, "Standard Stage",
          VenueUtil.readSeatingLayoutFromResource("standard_stage.txt"));
      TicketService ticketService = new TicketServiceImpl(venue, 1);
      assertEquals(297, ticketService.numSeatsAvailable());

      String emailAddress = "test@walmart.com";
      SeatHold seatHold = ticketService.findAndHoldSeats(2, emailAddress);
      assertEquals(2, seatHold.getSeats().size());
      assertEquals(emailAddress, seatHold.getCustomerEmail());

      Thread.sleep(1001);
      String reservationId = ticketService.reserveSeats(seatHold.getSeatHoldId(), emailAddress);
      assertNull(reservationId);
    } finally {
      shutdown(ticketServiceManager);
    }
  }

  private void shutdown(TicketServiceManager ticketServiceManager) {
    if (ticketServiceManager != null) {
      ticketServiceManager.shutdown();
    }
  }
}
