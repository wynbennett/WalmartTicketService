package com.walmart;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.walmart.models.Reservation;
import com.walmart.models.Seat;
import com.walmart.models.SeatHold;
import com.walmart.models.Venue;

public class App {

  private static Logger logger = LoggerFactory.getLogger(App.class);
  private TicketServiceManager manager;

  public App(CommandLine cmd) {
    int holdRemovalCheckInterval =
        Integer.parseInt(cmd.getOptionValue(Constants.HOLD_REMOVAL_CHECK_INTERVAL,
            String.valueOf(Constants.DEFAULT_REMOVAL_CHECK_INTERVAL_MS)));
    int holdTimeInSeconds = Integer.parseInt(cmd.getOptionValue(Constants.HOLD_TIME_SECONDS,
        String.valueOf(Constants.DEFAULT_SEAT_HOLD_TIME_SECS)));
    int numClients = Integer.parseInt(cmd.getOptionValue(Constants.CLIENTS, "1"));
    int maxNumberOfSeats = Integer.parseInt(cmd.getOptionValue(Constants.MAX_NUMBER_OF_SEATS, "2"));
    int maxWaitTimeMs = Integer.parseInt(cmd.getOptionValue(Constants.MAX_WAIT_TIME_MS, "5000"));
    boolean interactiveMode = cmd.hasOption(Constants.INTERACTIVE);

    manager = TicketServiceManager.getInstance(holdRemovalCheckInterval, holdTimeInSeconds);

    Venue venue = new Venue(1, "Standard Stage",
        VenueUtil.readSeatingLayoutFromResource("standard_stage.txt"));
    TicketService ticketService = new TicketServiceImpl(venue, holdTimeInSeconds);

    venue = new Venue(2, "Arena Stage", VenueUtil.readSeatingLayoutFromResource("arena_stage.txt"));
    ticketService = new TicketServiceImpl(venue, holdTimeInSeconds);

    venue =
        new Venue(3, "Thrust Stage", VenueUtil.readSeatingLayoutFromResource("thrust_stage.txt"));
    ticketService = new TicketServiceImpl(venue, holdTimeInSeconds);

    if (interactiveMode) {
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

      String command = "";
      while (!command.equals("exit")) {
        System.out.println("Enter a command (? for a list of commands): ");
        try {
          command = br.readLine();
          if (command.equals("?")) {
            System.out.println("Available commands:");
            System.out.println(
                "show venue <venue_id>                                shows the details of a particular venue");
            System.out.println(
                "hold seats <venue_id> <seats> <email> <contiguous>   holds a set of seats for a user");
            System.out.println(
                "reserve <venue_id> <seat_hold_id> <email>            reserves seat hold id");
            System.out.println(
                "show hold <seat_hold_id> <email>                     print details of a seat hold");
            System.out.println(
                "show reservation <reservation_id> <email>            print details of a reservation");
            System.out
                .println("exit                                                 exits the REPL");
            System.out.println("");
          } else if (command.startsWith("show venue")) {
            String[] split = command.split(" ");
            int venueId = Integer.parseInt(split[2]);

            showVenue(venueId);
          } else if (command.startsWith("hold seats")) {
            String[] split = command.split(" ");
            int venueId = Integer.parseInt(split[2]);
            int seats = Integer.parseInt(split[3]);
            String emailAddress = split[4];
            boolean contiguous = true;
            if (split.length == 6) {
              contiguous = Boolean.parseBoolean(split[5]);
            }

            holdSeats(venueId, seats, emailAddress, contiguous);
          } else if (command.startsWith("reserve")) {
            String[] split = command.split(" ");
            int venueId = Integer.parseInt(split[1]);
            int seatHoldId = Integer.parseInt(split[2]);
            String emailAddress = split[3];

            reserveSeats(venueId, seatHoldId, emailAddress);
          } else if (command.startsWith("show reservation")) {
            String[] split = command.split(" ");
            String reservationId = split[2];
            String emailAddress = split[3];

            showReservation(reservationId, emailAddress);
          } else if (command.startsWith("show hold")) {
            String[] split = command.split(" ");
            int seatHoldId = Integer.parseInt(split[2]);
            String emailAddress = split[3];

            showSeatHold(seatHoldId, emailAddress);
          }
        } catch (Exception e) {
          logger.error("Error with input", e);
        }
      }
    } else {
      // Run the simulation

      // A CountDownLatch to check when the client threads are done
      CountDownLatch doneSignal = new CountDownLatch(numClients);
      for (int t = 0; t < numClients; t++) {
        TicketClient client = new TicketClient(t + 1, maxNumberOfSeats, maxWaitTimeMs, doneSignal);
        client.start();
      }

      // When the threads finish filling the venues show the venues
      try {
        doneSignal.await();
        showVenue(1);
        showVenue(2);
        showVenue(3);
      } catch (InterruptedException e) {
        logger.error("Error waiting for threads to finish", e);
      }
    }
    manager.shutdown();
    System.exit(1);
  }

  /**
   * Get the venue by venueId and reserve the seats held in seatHoldId
   * 
   * @param venueId - id of venue
   * @param seatHoldId - seat hold identifier
   * @param emailAddress - customer email
   */
  private void reserveSeats(int venueId, int seatHoldId, String emailAddress) {
    Venue selectedVenue = manager.getVenues().get(venueId);
    ContiguousTicketService ticketService =
        (ContiguousTicketService) manager.getTicketServices().get(selectedVenue);
    String reservation = ticketService.reserveSeats(seatHoldId, emailAddress);
    System.out.println(manager.getReservations().get(reservation));
  }

  /**
   * Get the venue by venueId and hold the seats with the given parameters
   * 
   * @param venueId - id of venue
   * @param seats - number of seats
   * @param emailAddress - customer email
   * @param contiguous - flag indicating if the seats should be next to each other wherever possible
   */
  private void holdSeats(int venueId, int seats, String emailAddress, boolean contiguous) {
    Venue selectedVenue = manager.getVenues().get(venueId);
    ContiguousTicketService ticketService =
        (ContiguousTicketService) manager.getTicketServices().get(selectedVenue);
    SeatHold seatHold = ticketService.findAndHoldSeats(seats, emailAddress, contiguous);
    System.out.println(seatHold);
  }

  /**
   * Prints a textual representation of a venue to standard out with 's' representing an open seat,
   * 'h' as a hold, and 'r' as a reservation
   * 
   * @param venueId - id of venue
   */
  private void showVenue(int venueId) {
    System.out.println("Venue: " + venueId);
    Venue selectedVenue = manager.getVenues().get(venueId);
    for (int y = 0; y < selectedVenue.getLayout().length; y++) {
      for (int x = 0; x < selectedVenue.getLayout()[y].length; x++) {
        char layoutItem = selectedVenue.getLayout()[y][x];
        // If the original layout is a seat
        if (layoutItem == Constants.CHAR_SEAT) {
          boolean found = false;
          // Check for which seats are reserved
          for (Reservation reservation : manager.getReservations().values()) {
            for (Seat seat : reservation.getSeats()) {
              if ((seat.getVenueId() == venueId) && (seat.getSeatPointX() == x)
                  && (seat.getSeatPointY() == y)) {
                System.out.print(Constants.CHAR_RESERVATION);
                found = true;
                break;
              }
            }
          }

          if (!found) {
            // Check for which seats are held
            for (SeatHold seatHold : manager.getSeatHolds().values()) {
              for (Seat seat : seatHold.getSeats()) {
                if ((seat.getVenueId() == venueId) && (seat.getSeatPointX() == x)
                    && (seat.getSeatPointY() == y)) {
                  System.out.print(Constants.CHAR_HOLD);
                  found = true;
                  break;
                }
              }
            }
          }

          // Otherwise, print s for available seat
          if (!found) {
            System.out.print(Constants.CHAR_SEAT);
          }
        } else {
          System.out.print(layoutItem);
        }
      }
      System.out.print('\n');
    }
  }

  /**
   * Prints out a reservation with a given venueId, reservationId, and emailAddress
   * 
   * @param reservationId - reservation identifier
   * @param emailAddress - customer email
   */
  private void showReservation(String reservationId, String emailAddress) {
    Reservation reservation = manager.getReservations().get(reservationId);
    if ((reservation != null) && reservation.getCustomerEmail().equals(emailAddress)) {
      System.out.println(reservation);
    } else {
      System.out.println("Unable to find reservation with id: " + reservationId);
    }
  }

  /**
   * Prints out a seat hold with a given seatHoldId, and emailAddress
   * 
   * @param seatHoldId - seat hold identifier
   * @param emailAddress - customer email
   */
  private void showSeatHold(int seatHoldId, String emailAddress) {
    SeatHold seatHold = manager.getSeatHolds().get(seatHoldId);
    if ((seatHold != null) && seatHold.getCustomerEmail().equals(emailAddress)) {
      System.out.println(seatHold);
    } else {
      System.out.println("Unable to find seat hold with id: " + seatHoldId);
    }
  }

  public static void main(String[] args) {
    Options options = new Options();
    options.addOption(new Option("r", Constants.HOLD_REMOVAL_CHECK_INTERVAL, true,
        "interval to check if we should remove a seat hold in MS this should be less than the reservation time (defaults to 500ms)"));
    options.addOption(new Option("h", Constants.HOLD_TIME_SECONDS, true,
        "time to hold a seat reservation for in seconds (defaults to 600 secconds)"));
    options.addOption(new Option("c", Constants.CLIENTS, true,
        "number of clients to simulate at one time (defaults to 1)"));
    options.addOption(new Option("m", Constants.MAX_NUMBER_OF_SEATS, true,
        "max number seats to randomly request from a client in simulation (defaults to 2)"));
    options.addOption(new Option("w", Constants.MAX_WAIT_TIME_MS, true,
        "max wait time seats to randomly wait in simulation before making reservation (defaults to 5000ms)"));
    options
        .addOption(new Option("i", Constants.INTERACTIVE, false, "runs in interactive REPL mode "));

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd;

    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("ticketservice", options);
      System.exit(1);
      return;
    }

    App app = new App(cmd);
  }

  class TicketClient extends Thread {

    private String clientEmail;
    private int clientId;
    private int maxNumberOfSeats;
    private int maxWaitTimeMs;
    private CountDownLatch doneSignal;

    public TicketClient(int clientId, int maxNumberOfSeats, int maxWaitTimeMs,
        CountDownLatch doneSignal) {
      this.clientId = clientId;
      this.maxNumberOfSeats = maxNumberOfSeats;
      this.maxWaitTimeMs = maxWaitTimeMs;
      this.doneSignal = doneSignal;
      clientEmail = String.format("%d@%d.com", clientId, clientId);
    }

    @Override
    public void run() {
      while (!this.isInterrupted()) {
        // Setup random variables
        int venueId = ThreadLocalRandom.current().nextInt(1, 4);
        int numberOfSeats = ThreadLocalRandom.current().nextInt(1, maxNumberOfSeats + 1);
        int waitTimeMs = ThreadLocalRandom.current().nextInt(1000, maxWaitTimeMs + 1);

        List<Seat> availableSeats = manager.getAvailableSeats().get(venueId);
        synchronized (availableSeats) {
          if (availableSeats.isEmpty() && manager.getSeatHolds().isEmpty()) {
            logger.info("Thread {} exiting, no more available seats", clientId);
            break;
          }
        }

        Venue venue = manager.getVenues().get(venueId);
        TicketService ticketService = manager.getTicketServices().get(venue);
        SeatHold seatHold = ticketService.findAndHoldSeats(numberOfSeats, clientEmail);
        if (seatHold == null) {
          logger.info("Thread {} unable to hold {} seats contiguously trying non-contiguous",
              clientId, numberOfSeats);
          seatHold = ((ContiguousTicketService) ticketService).findAndHoldSeats(numberOfSeats,
              clientEmail, false);
          if (seatHold == null) {
            logger.info("Thread {} unable to hold {} seats non-contiguously", clientId,
                numberOfSeats);
            continue;
          }
        }

        logger.info("Thread {} holding {} seats of venue {} with id of {} for {}ms", clientId,
            numberOfSeats, venueId, seatHold.getSeatHoldId(), waitTimeMs);
        manager.getSeatHolds().put(seatHold.getSeatHoldId(), seatHold);
        try {
          Thread.sleep(waitTimeMs);
        } catch (InterruptedException e) {
          // NOOP move on gracefully
        }
        String reservationId = ticketService.reserveSeats(seatHold.getSeatHoldId(), clientEmail);
        if (reservationId != null) {
          logger.info("Thread {} reserved with reservation id {}", clientId, reservationId);
        }
      }
      doneSignal.countDown();
    }
  }
}
