# Assumptions
1. The TicketService should be able to be initialized with a text-based venue layout file.
2. The layout file may contain any typical stage layout and seat placement and therefore the seat selection process should account for that.
3. The client requesting the seats would like those seats next to one another (contiguous).  Therefore, I created a way to try to maximize the number of seats next to each other while still trying to provide everyone in the group with the best available seat.
4. The TicketService interface should not be changed in any way.  Therefore, I extended it to allow for a non-contiguous seat holding override. In addition, I did not modify the interface to throw exceptions but rather logged them with a logback logger.
5. Clients do not need to communicate with the TicketService via RPC and may communicate using threads within the same process.
6. Clients cannot request specific seats but must always go through the "find best available" process.
7. Seat pricing is not a factor.
8. The TicketService should be able to handle thousands of clients without negatively affecting the performance of the machine.
9. The TicketService should be able to be interacted with in an interactive manner.
10. Minimize reliance on 3rd-party libraries to allow for easy extendability and integration into other projects.
11. One TicketService per venue.
12. One seat hold removal thread per TicketService.
13. Hold time and seat hold removal check interval should be configurable.

# Testing
1. Open a terminal and navigate to where you checked out this repository.
2. To run the tests run: 
`mvn test`

# Running the application
1. Open a terminal and navigate to where you checked out this repository.
  * To run the simulation run something like so from the terminal:
     
     `mvn exec:java -Dexec.args="-clients=10 -maxNumberOfSeats=10"`
     
     The simulation will run until all seats in every venue is reserved

  * To run the appliction in interactive mode run the following:
     
     `mvn exec:java -Dexec.args="-i"`
     
     Type `?` to show a list of commands for the interactive REPL

The arguments to the executable are the following:
```
usage: ticketservice
 -c,--clients <arg>                           number of clients to
                                              simulate at one time
                                              (defaults to 1)
 -h,--holdTimeSeconds <arg>                   time to hold a seat
                                              reservation for in seconds
                                              (defaults to 600 secconds)
 -i,--interactive                             runs in interactive REPL
                                              mode
 -m,--maxNumberOfSeats <arg>                  max number seats to randomly
                                              request from a client in simulation
                                              (defaults to 2)
 -r,--holdRemovalCheckInterval <arg>          interval to check if we
                                              should remove a seat hold
                                              in MS this should be less
                                              than the reservation time
                                              (defaults to 500ms)
 -w,--maxWaitTimeMs <arg>                     max wait time seats to
                                              randomly wait in simulation
                                              before making reservation
                                              (defaults to 5000ms)\
```
