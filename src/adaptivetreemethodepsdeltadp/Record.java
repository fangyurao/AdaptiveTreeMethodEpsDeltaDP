package adaptivetreemethodepsdeltadp;

public class Record {
  public int recordID;
  public Coordinates c;

  public Record(int inRecordID, Coordinates inCoordinates) {
    recordID = inRecordID;
    c = inCoordinates;
  }

  public boolean isInBoundingBox(double[] inLowerBounds, double[] inUpperBounds) {
    int numberOfDimensions = inLowerBounds.length;
    double[] rawCoordinates = c.coordinates;
    for (int i = 0; i < numberOfDimensions; i = i + 1) {
      if ((inLowerBounds[i] <= rawCoordinates[i]) &&
              (rawCoordinates[i] < inUpperBounds[i])) {
        //do nothing, since this Record is in the range
      } else {
        return false;
      }
    }
    return true;
  }
}
