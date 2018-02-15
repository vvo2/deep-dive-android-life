package edu.cnm.deepdive.life.ca;

/**
 * This class is an implementation of Conway's "classic" rules &ndash; namely, that a cell is
 * born in an empty space with exactly 3 living neighbors, and a living cell survives if (and
 * only if) it has 2 or 3 living neighbors.
 *
 * @author Nicholas Bennett &amp; Deep Dive Coding Java Cohort 3
 * @version 0.9.0
 */
public class Conway implements Rule {

  /**
   * Computes and returns the state of a cell for the next generation, using Conway's B3/S23
   * rules.
   */
  @Override
  public boolean next(boolean current, int mooreNeighbors, int vonNeumannNeighbors) {
    return (mooreNeighbors == 3 || (current && mooreNeighbors == 2));
  }

}
