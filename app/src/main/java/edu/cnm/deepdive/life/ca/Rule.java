package edu.cnm.deepdive.life.ca;

/**
 * This interface declares a {@link #next(boolean, int, int)} method for implementing a CA rule
 * set.
 *
 * @author Nicholas Bennett &amp; Deep Dive Coding Java Cohort 3
 * @version 0.9.0
 */
public interface Rule {

  /**
   * Computes and returns the state of a cell for the next generation, based on the current
   * state and the current number of living neighbors, in both the Moore (directly and diagonally
   * adjacent) and Von Neumann (directly adjacent only) neighborhoods.
   *
   * @param current             live/dead state in current generation.
   * @param mooreNeighbors      number of directly or diagonally adjacent living neighbors.
   * @param vonNeumannNeighbors number of directly adjacent living neighbors.
   * @return                    live/dead state in next generation.
   */
  boolean next(boolean current, int mooreNeighbors, int vonNeumannNeighbors);

}
