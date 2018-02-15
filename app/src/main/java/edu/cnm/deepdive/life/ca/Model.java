package edu.cnm.deepdive.life.ca;

import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * This class implements a 2-dimensional cellular automaton on a square lattice. By default, the
 * rules used are Conway's B2/S23 rules, and the world is a 400 X 400 toroidal space; the rules and
 * size can be modified by specifying appropriate values in the constructor invocation.
 * <p>
 * For performance reasons, this class is <em>not</em> thread-safe. One important implication of
 * this is that if one thread is updating the CA (e.g. by invoking the {@link #advance()} method)
 * while another thread is reading the terrain (using {@link #getTerrain()}, there's no guarantee
 * that these operations will be performed in a consistent fashion. Thus, such multithreaded use
 * should be wrapped in the appropriate protection mechanisms, such as <code>synchronized</code>
 * blocks or methods.
 *
 * @author Nicholas Bennett &amp; Deep Dive Coding Java Cohort 3
 * @version 0.9.0
 */
public class Model {

  /** Default width of terrain. */
  public static final int DEFAULT_WIDTH = 400;
  /** Default height of terrain. */
  public static final int DEFAULT_HEIGHT = 400;

  private Rule rule;
  private int generation;
  private byte[][] terrain;
  private byte[][] mooreNeighbors;
  private byte[][] vonNeumannNeighbors;
  private Checksum checksum;
  private boolean checksumValid = true;

  /**
   * Initializes the <code>Model</code>instance using default values for the size and rule. Invoking
   * this constructor is equivalent to invoking {@link #Model(Rule) Model(Rule.CONWAY)}.
   */
  public Model() {
    this(new Conway());
  }

  /**
   * Initializes the <code>Model</code>instance using the specified rule set, with default values
   * for the size. Invoking this constructor is equivalent to invoking {@link #Model(int, int, Rule)
   * Model(DEFAULT_WIDTH, DEFAULT_HEIGHT, rule)}.
   *
   * @param rule  rule set to be used for creation of new cells and survival of existing cells.
   */
  public Model(Rule rule) {
    this(DEFAULT_WIDTH, DEFAULT_HEIGHT, rule);
  }

  /**
   * Initializes the <code>Model</code>instance using the specified size, with the default rule set.
   * Invoking this constructor is equivalent to invoking {@link #Model(int, int, Rule)
   * Model(width, height, Rule.CONWAY)}.
   *
   * @param width   horizontal size of toroidal terrain.
   * @param height  vertical size of toroidal terrain.
   */
  public Model(int width, int height) {
    this(width, height, new Conway());
  }

  /**
   * Initializes the <code>Model</code>instance using the specified size and rule set.
   *
   * @param width   horizontal size of toroidal terrain.
   * @param height  vertical size of toroidal terrain.
   * @param rule    rule set to be used for creation of new cells and survival of existing cells.
   */
  public Model(int width, int height, Rule rule) {
    this.rule = rule;
    terrain = new byte[height][width];
    mooreNeighbors = new byte[height][width];
    vonNeumannNeighbors = new byte[height][width];
    generation = 0;
    checksum = new CRC32(); //easier to look for checksum than it is to pattern
    checksumValid = true;
  }

  /**
   * Clears and populates the terrain randomly. Each cell is populated according to a Bernoulli
   * trial, with probability specified by the <code>density</code> parameter.
   *
   * @param density likelihood that a living cell will be created in each lattice point.
   */
  public void populate(double density) {
    generation = 0;
    checksum.reset();
    for (int i = 0; i < terrain.length; i++) {
      Arrays.fill(mooreNeighbors[i], (byte) 0);
      Arrays.fill(vonNeumannNeighbors[i], (byte) 0);
      for (int j = 0; j < terrain[i].length; j++) {
        terrain[i][j] = (byte) ((Math.random() < density) ? 1 : 0);
      }
      checksum.update(terrain[i], 0, terrain[i].length);
    }
    checksumValid = true;
  }

  /**
   * Advances the CA to the next generation by applying the rule set specified on instantiation.
   */
  public void advance() {
    countNeighbors();
    checksum.reset();
    for (int i = 0; i < terrain.length; i++) {
      for (int j = 0; j < terrain[i].length; j++) {
        byte ageNow = terrain[i][j];
        boolean aliveNow = (ageNow != 0);
        boolean aliveNext = rule.next(aliveNow, mooreNeighbors[i][j], vonNeumannNeighbors[i][j]);
        if (aliveNext) {
          if (!aliveNow) {
            terrain[i][j] = 1;
          } else if (ageNow < Byte.MAX_VALUE) {
            terrain[i][j]++;
          }
        } else if (aliveNow) {
          terrain[i][j] = 0;
        }
      }
      Arrays.fill(mooreNeighbors[i], (byte) 0);
      Arrays.fill(vonNeumannNeighbors[i], (byte) 0);
      checksum.update(terrain[i], 0, terrain[i].length);
    }
    checksumValid = true;
    generation++;
  }

  /**
   * Clears the specified location, removing a living cell if one is present.
   *
   * @param x   horizontal position.
   * @param y   vertical position.
   */
  public void clearCell(int x, int y) {
    setCell(x, y, false);
  }

  /**
   * Sets the specified location, creating a living cell there. The cell will be created with an age
   * of 1 generation.
   *
   * @param x   horizontal position.
   * @param y   vertical position.
   */
  public void setCell(int x, int y) {
    setCell(x, y, true);
  }

  /**
   * Sets or clears the specified location, creating or removing a living cell according to the
   * <code>alive</code> parameter. If a cell is created, it will have an age of 1 generation.
   *
   * @param x       horizontal position.
   * @param y       vertical position.
   * @param alive   live (<code>true</code>) or dead (<code>false</code>) state of the location.
   */
  public void setCell(int x, int y, boolean alive) {
    setCell(x, y, (byte) (alive ? 1 : 0));
  }

  /**
   * Sets or clears the specified location, creating or removing a living cell according to the
   * <code>age</code> parameter. If the value of that parameter is 0 (zero), the location is
   * cleared; otherwise, a living cell is created there. Note that ages in the range -128&hellip;-1
   * will be normalized to the range 0&hellip;127.
   *
   * @param x       horizontal position.
   * @param y       vertical position.
   * @param age     if non-zero, the number of generations the created cell is considered to have
   *                been alive.
   */
  public void setCell(int x, int y, byte age) {
    terrain[y][x] = (byte) (age & 0x7F);
    checksumValid = false;
  }

  /**
   * Copies and returns the contents of the terrain lattice. This approach is used so that UI
   * display updates can be performed using the copy, without having to synchronize that access. (As
   * noted {@link Model above}, however, this method is not itself synchronized.)
   *
   * @return  copy of terrain, with each zero element representing an unoccupied/dead location, and
   *          each non-zero element giving the age (in generations) of a living cell at that
   *          location.
   */
  public byte[][] getTerrain() {
    byte[][] safeCopy = new byte[terrain.length][terrain[0].length];
    for (int i = 0; i < terrain.length; i++) {
      System.arraycopy(terrain[i], 0, safeCopy[i], 0, terrain[i].length);
    }
    return safeCopy;
  }

  /**
   * Returns the number of generations that have elapsed since the <code>Model</code> instance was
   * instantiated, or since the most recent invocation of {@link #populate(double)}}.
   *
   * @return  generation count.
   */
  public int getGeneration() {
    return generation;
  }

  /**
   * Returns a CRC32C checksum computed from the terrain contents. If (for example) a rolling
   * history of these values is maintained by the caller, it can be used to check for cycles.
   *
   * @return  checksum value for most recently completed generation.
   */
  public long getChecksum() {
    if (!checksumValid) {
      recomputeChecksum();
    }
    return checksum.getValue();
  }

  private void countNeighbors() {
    int numRows = terrain.length;
    for (int i = 0; i < numRows; i++) {
      int numCols = terrain[i].length;
      for (int j = 0; j < numCols; j++) {
        if (terrain[i][j] != 0) {
          for (int row = i - 1; row <= i + 1; row++) {
            for (int col = j - 1; col <= j + 1; col++) {
              mooreNeighbors[(row + numRows) % numRows]
                  [(col + numCols) % numCols]++;
            }
          }
          mooreNeighbors[i][j]--;
          vonNeumannNeighbors[(i - 1 + numRows) % numRows][j]++;
          vonNeumannNeighbors[(i + 1) % numRows][j]++;
          vonNeumannNeighbors[i][(j - 1 + numCols) % numCols]++;
          vonNeumannNeighbors[i][(j + 1) % numCols]++;
        }
      }
    }
  }

  private void recomputeChecksum() {
    checksum.reset();
    for (int i = 0; i < terrain.length; i++) {
      checksum.update(terrain[i], 0, terrain[i].length);
    }
    checksumValid = true;
  }

}
