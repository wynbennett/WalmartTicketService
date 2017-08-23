package com.walmart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.walmart.models.Reservation;
import com.walmart.models.Seat;
import com.walmart.models.SeatHold;
import com.walmart.models.Venue;

public class TicketServiceManager {
  private static Logger logger = LoggerFactory.getLogger(TicketServiceManager.class);
  private static final List<TicketServiceManager> instances = new ArrayList<TicketServiceManager>();

  private Map<Integer, Venue> venues = new ConcurrentHashMap<Integer, Venue>();
  private Map<Venue, TicketService> ticketServices = new ConcurrentHashMap<Venue, TicketService>();
  private Map<Integer, List<Seat>> availableSeats = new ConcurrentHashMap<Integer, List<Seat>>();
  private AtomicInteger seatHoldIds = new AtomicInteger();
  private Map<Integer, SeatHold> seatHolds = new ConcurrentHashMap<Integer, SeatHold>();
  private Map<String, Reservation> reservations = new ConcurrentHashMap<String, Reservation>();

  private SeatHoldRemovalThread seatHoldRemovalThread;
  private int removalCheckIntervalMs;
  private int holdTimeSeconds;

  protected TicketServiceManager(int removalCheckIntervalMs, int holdTimeSeconds) {
    this.removalCheckIntervalMs = removalCheckIntervalMs;
    this.holdTimeSeconds = holdTimeSeconds;
    seatHoldRemovalThread = new SeatHoldRemovalThread();
    seatHoldRemovalThread.start();
  }

  public synchronized void shutdown() {
    seatHoldRemovalThread.interrupt();
    instances.remove(this);
  }

  public synchronized static TicketServiceManager getInstance(int removalCheckIntervalMs,
      int holdTimeSeconds) {
    for (TicketServiceManager instance : instances) {
      if ((instance.holdTimeSeconds == holdTimeSeconds)
          && (instance.removalCheckIntervalMs == removalCheckIntervalMs)) {
        return instance;
      }
    }
    TicketServiceManager instance =
        new TicketServiceManager(removalCheckIntervalMs, holdTimeSeconds);
    instances.add(instance);
    return instance;
  }

  public synchronized static TicketServiceManager getInstance() {
    return getInstance(Constants.DEFAULT_REMOVAL_CHECK_INTERVAL_MS,
        Constants.DEFAULT_SEAT_HOLD_TIME_SECS);
  }

  public Map<Integer, Venue> getVenues() {
    return venues;
  }

  public Map<Venue, TicketService> getTicketServices() {
    return ticketServices;
  }

  public Map<Integer, List<Seat>> getAvailableSeats() {
    return availableSeats;
  }

  public AtomicInteger getSeatHoldIds() {
    return seatHoldIds;
  }

  public Map<Integer, SeatHold> getSeatHolds() {
    return seatHolds;
  }

  public Map<String, Reservation> getReservations() {
    return reservations;
  }

  class SeatHoldRemovalThread extends Thread {
    @Override
    public void run() {
      while (!this.isInterrupted()) {
        for (SeatHold seatHold : seatHolds.values()) {
          long currentTime = System.currentTimeMillis();
          long diff = currentTime - seatHold.getCreatedAt();
          if (diff >= (holdTimeSeconds * 1000)) {
            logger.info("Evicting {}", seatHold.getSeatHoldId());
            seatHolds.remove(seatHold.getSeatHoldId());
            int venueId = seatHold.getSeats().iterator().next().getVenueId();
            List<Seat> venueSeats = availableSeats.get(venueId);
            // Allow the seats to become available again
            synchronized (venueSeats) {
              venueSeats.addAll(seatHold.getSeats());
            }
          }
        }
        try {
          Thread.sleep(removalCheckIntervalMs);
        } catch (InterruptedException e) {
          // NOOP move on gracefully
        }
      }
    }
  }
}
