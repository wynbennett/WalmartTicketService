package com.walmart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.walmart.models.Seat;

public class VenueUtil {

  private static Logger logger = LoggerFactory.getLogger(VenueUtil.class);

  /**
   * Read seating layout from file
   * 
   * @param file
   * @return a list of Strings to be interpreted for a layout
   */
  public static List<String> readSeatingLayout(File file) {
    List<String> layout = new ArrayList<String>();
    try {
      BufferedReader b = new BufferedReader(new FileReader(file));
      String readLine = "";

      while ((readLine = b.readLine()) != null) {
        layout.add(readLine);
      }
    } catch (IOException e) {
      logger.error("Error reading file: " + file.getAbsolutePath(), e);
    }
    return layout;
  }

  /**
   * Read seating layout from file
   * 
   * @param file
   * @return a list of Strings to be interpreted for a layout
   */
  public static List<String> readSeatingLayoutFromResource(String filePath) {
    ClassLoader classLoader = VenueUtil.class.getClassLoader();
    File file = new File(classLoader.getResource(filePath).getFile());
    return readSeatingLayout(file);
  }

  /**
   * Gets the weighted distance to the center point of the stage from the seats location. Weights
   * distance based on the shape of the layout
   * 
   * @param centerStage - center stage point as int array with x,y
   * @param dimensions - dimensions of layout as int array x, y
   * @param seatX - x point of seat
   * @param seatY - y point of seat
   * @return
   */
  public static double distanceToCenterStage(int[] centerStage, int[] dimensions, int seatX,
      int seatY) {
    // Based on the shape of the stage give priority to the X or Y directions
    double xDimHalf = dimensions[0] * 0.5d;
    double yDimHalf = dimensions[1] * 0.5d;
    double xPull = 1 - (Math.abs(centerStage[1] - yDimHalf) / yDimHalf);
    double yPull = 1 - (Math.abs(centerStage[0] - xDimHalf) / xDimHalf);
    return Math.abs(
        Math.sqrt(Math.pow((centerStage[0] - seatX) * xPull, 2)
            + Math.pow((centerStage[1] - seatY) * yPull, 2)));
  }

  /**
   * Initializes all Seats for a given venueId and layout
   * 
   * @param venueId
   * @param layout - character-based seating layout with '-' being a stage portion and 's' being a
   *        seat, everything else is assumed to be spacing
   * @param centerStage - center point of stage as int[] {x, y}
   * @return
   */
  public static List<Seat> initializeSeats(int venueId, char[][] layout, int[] centerStage) {
    List<Seat> seats = new ArrayList<Seat>();
    int[] dimensions = new int[] {layout[0].length, layout.length};
    for (int y = 0; y < layout.length; y++) {
      for (int x = 0; x < layout[y].length; x++) {
        char c = layout[y][x];
        if (c == Constants.CHAR_SEAT) {
          Seat seat = new Seat(venueId, x, y);
          seat.setDistanceFromCenterStage(distanceToCenterStage(centerStage, dimensions, x, y));
          seats.add(seat);
        }
      }
    }
    return seats;
  }

  /**
   * Finds the center point of an arbitrarily laid out stage and seating arrangement. Center point
   * is rounded up.
   * 
   * @param layout - character-based seating layout with '-' being a stage portion and 's' being a
   *        seat, everything else is assumed to be spacing
   * @return center point as int array of x, y
   */
  public static int[] findCenterStage(char[][] layout) {
    int stageTopX = 0;
    int stageTopY = 0;
    int stageBottomX = 0;
    int stageBottomY = 0;
    for (int y = 0; y < layout.length; y++) {
      for (int x = 0; x < layout[y].length; x++) {
        char c = layout[y][x];
        if (c == '-') {
          if ((stageTopX == 0) || (x <= stageTopX)) {
            stageTopX = x;
          }
          if ((stageBottomX == 0) || (x >= stageBottomX)) {
            stageBottomX = x;
          }
          if ((stageTopY == 0) || (y <= stageTopY)) {
            stageTopY = y;
          }
          if ((stageBottomY == 0) || (y >= stageBottomY)) {
            stageBottomY = y;
          }
        }
      }
    }
    int centerX = (int) ((stageBottomX + stageTopX) / 2.0d);
    int centerY = (int) ((stageBottomY + stageTopY) / 2.0d);
    return new int[] {centerX, centerY};
  }

  /**
   * Comparator which sorts seats by seat distance
   * 
   * @author wyn
   *
   */
  public static class SeatDistanceComparator implements Comparator<Seat> {
    public int compare(Seat a, Seat b) {
      return Double.compare(a.getDistanceFromCenterStage(), b.getDistanceFromCenterStage());
    }
  }

}
