package com.walmart;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.walmart.VenueUtil.SeatDistanceComparator;
import com.walmart.models.Reservation;
import com.walmart.models.Seat;
import com.walmart.models.SeatHold;
import com.walmart.models.Venue;

public class TicketServiceImpl implements ContiguousTicketService {

  private static Logger logger = LoggerFactory.getLogger(App.class);

  private Venue venue;
  private TicketServiceManager manager;
  private int holdTimeSeconds;
  private List<Seat> availableSeats;
  private SeatDistanceComparator seatDistanceComparator = new VenueUtil.SeatDistanceComparator();

  public TicketServiceImpl(Venue venue) {
    this(venue, Constants.DEFAULT_SEAT_HOLD_TIME_SECS);
  }

  public TicketServiceImpl(Venue venue, int holdTimeSeconds) {
    this.venue = venue;
    this.holdTimeSeconds = holdTimeSeconds;

    manager = TicketServiceManager.getInstance();
    manager.getTicketServices().put(venue, this);
    manager.getVenues().put(venue.getVenueId(), venue);

    // Sort the availableSeats by seat distance closest to the center of the stage
    availableSeats = new CopyOnWriteArrayList<Seat>(venue.getSeats());
    manager.getAvailableSeats().put(venue.getVenueId(), availableSeats);
    availableSeats.sort(seatDistanceComparator);
  }

  public synchronized int numSeatsAvailable() {
    synchronized (availableSeats) {
      return this.availableSeats.size();
    }
  }

  public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
    return findAndHoldSeats(numSeats, customerEmail, true);
  }

  public SeatHold findAndHoldSeats(int numSeats, String customerEmail, boolean contiguous) {
    SeatHold seatHold = new SeatHold(manager.getSeatHoldIds().incrementAndGet(), customerEmail);
    // Just add the seats in a non-contiguous manner
    if (!contiguous) {
      synchronized (availableSeats) {
        Iterator<Seat> itr = availableSeats.iterator();
        while (itr.hasNext() && seatHold.getSeats().size() < numSeats) {
          Seat seat = itr.next();
          seatHold.getSeats().add(seat);
        }
        availableSeats.removeAll(seatHold.getSeats());
      }
    } else {
      synchronized (availableSeats) {
        Iterator<Seat> itr = availableSeats.iterator();
        int seen = 0;
        while ((seatHold.getSeats().size() < numSeats) && (seen < availableSeats.size() - 1)) {
          seen++;
          while (itr.hasNext()) {
            Seat seat = itr.next();
            // for (Seat seat : tmpAvailableSeats) {
            boolean foundFriend = false;
            for (Seat friendSeat : seatHold.getSeats()) {
              // Try to get seats next to each other in the X direction (same row)
              if ((friendSeat.getSeatPointY() == seat.getSeatPointY())
                  && (((friendSeat.getSeatPointX() - 1) == seat.getSeatPointX())
                      || ((friendSeat.getSeatPointX() + 1) == seat.getSeatPointX()))) {
                seatHold.getSeats().add(seat);
                foundFriend = true;
                break;
              }
              // If not in the X direction try the Y direction
              else if ((friendSeat.getSeatPointX() == seat.getSeatPointX())
                  && (((friendSeat.getSeatPointY() - 1) == seat.getSeatPointY())
                      || ((friendSeat.getSeatPointY() + 1) == seat.getSeatPointY()))) {
                seatHold.getSeats().add(seat);
                foundFriend = true;
                break;
              }
            }

            if (seatHold.getSeats().size() == 0) {
              seatHold.getSeats().add(seat);
              break;
            } else if (foundFriend) {
              break;
            } else if (!foundFriend) {
              logger.debug("Skipping {},{} not near friend.", seat.getSeatPointX(),
                  seat.getSeatPointY());
            }
          }
          availableSeats.removeAll(seatHold.getSeats());
          availableSeats.sort(new VenueUtil.SeatDistanceComparator());
        }
      }
    }

    if (seatHold.getSeats().size() != numSeats) {
      synchronized (availableSeats) {
        // Put the seats back
        availableSeats.addAll(seatHold.getSeats());
      }
      return null;
    }
    manager.getSeatHolds().put(seatHold.getSeatHoldId(), seatHold);
    synchronized (availableSeats) {
      availableSeats.removeAll(seatHold.getSeats());
    }
    return seatHold;
  }

  public String reserveSeats(int seatHoldId, String customerEmail) {
    SeatHold seatHold = manager.getSeatHolds().get(seatHoldId);
    // Check that the seatHoldId, the email address is valid
    if ((seatHold != null) && seatHold.getCustomerEmail().equals(customerEmail)) {
      long currentTime = System.currentTimeMillis();
      long diff = currentTime - seatHold.getCreatedAt();
      // Check that the hold time is still valid
      if (diff < (holdTimeSeconds * 1000)) {
        Reservation reservation = new Reservation(seatHold);
        manager.getReservations().put(reservation.getReservationId(), reservation);
        manager.getSeatHolds().remove(seatHoldId);
        return reservation.getReservationId();
      }
      // Eagerly remove the seatHold if it is beyond the expiration time
      else {
        logger.info("Evicting {}", seatHold.getSeatHoldId());
        manager.getSeatHolds().remove(seatHoldId);
        // Make the seats available again
        synchronized (availableSeats) {
          this.availableSeats.addAll(seatHold.getSeats());
          this.availableSeats.sort(seatDistanceComparator);
        }
      }
    }
    return null;
  }
}
