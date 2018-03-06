package adaptivetreemethodepsdeltadp;

import java.util.Arrays;

/**
 *
 * @author raof
 */
public class Coordinates {

  public int numberOfDimensions;
  public double[] coordinates;

  public Coordinates(int inNumberOfDimensions, double[] inCoordinates) throws Exception {
    if (inCoordinates.length != inNumberOfDimensions) {
      throw new Exception("inCoordinates.length != inNumberOfDimensions");
    }
    numberOfDimensions = inNumberOfDimensions;
    coordinates = inCoordinates;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 89 * hash + this.numberOfDimensions;
    hash = 89 * hash + Arrays.hashCode(this.coordinates);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Coordinates other = (Coordinates) obj;
    if (this.numberOfDimensions != other.numberOfDimensions) {
      return false;
    }
    if (!Arrays.equals(this.coordinates, other.coordinates)) {
      return false;
    }
    return true;
  }

  public void display() {
    System.out.print("(");
    for (int i = 0; i < numberOfDimensions; i = i + 1) {
      if (i < numberOfDimensions - 1) {
        System.out.print(coordinates[i] + ", ");
      } else {
        System.out.print(coordinates[i] + ")");
      }
    }
  }
}
