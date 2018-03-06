package adaptivetreemethodepsdeltadp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author raof
 */
public class UniformGridTreeNode {

  int depth;
  double[] localLowerBounds;
  double[] localUpperBounds;
  List<Record> records;
  List<UniformGridTreeNode> children;
  //this field will be modified when it is removed from ArrayDeque
  boolean isLeaf;
  double noisyCount;
  double noiseAdded;
  Double epsilonApplied;

  List<Integer> suppressedRecordIDs;
  List<Integer> fakeRecordIDs;
  List<Integer> preservedRecordIDs;

  public static class Builder {

    private int depth;
    private double[] localLowerBounds;
    private double[] localUpperBounds;
    private List<Record> records;

    public Builder depth(int inDepth) {
      depth = inDepth;
      return this;
    }

    public Builder localLowerBounds(double[] inLocalLowerBounds) {
      localLowerBounds
              = Arrays.copyOf(inLocalLowerBounds, inLocalLowerBounds.length);
      return this;
    }

    public Builder localUpperBounds(double[] inLocalUpperBounds) {
      localUpperBounds
              = Arrays.copyOf(inLocalUpperBounds, inLocalUpperBounds.length);
      return this;
    }

    public Builder records(List<Record> inRecords) {
      records = new ArrayList<>();
      records.addAll(inRecords);
      return this;
    }

    public UniformGridTreeNode build() {
      return new UniformGridTreeNode(this);
    }
  }

  public UniformGridTreeNode(Builder inBuilder) {

    depth = inBuilder.depth;
    localLowerBounds = inBuilder.localLowerBounds;
    localUpperBounds = inBuilder.localUpperBounds;
    records =  inBuilder.records;

    if (records == null) {
      records = new ArrayList<>();
    }

    children = new ArrayList<>();
    suppressedRecordIDs = new ArrayList<>();
    fakeRecordIDs = new ArrayList<>();
    preservedRecordIDs = new ArrayList<>();

    isLeaf = false;
    noisyCount = Double.NaN;
    noiseAdded = Double.NaN;
    epsilonApplied = Double.NaN;

  }

  public void displayBoundingBoxes() {
    int numberOfDimensions = localLowerBounds.length;
    for (int i = 0; i < numberOfDimensions; i = i + 1) {
      System.out.println("[" + localLowerBounds[i] + ", " + localUpperBounds[i] + ")");
    }
  }

  //a method to test the correctness of the partitioning process
  public boolean allRecordsInBoundingBoxes() {

    Iterator itr = records.iterator();
    while (itr.hasNext()) {
      Record currentRecord = (Record) itr.next();
      boolean isInRange = currentRecord.isInBoundingBox(localLowerBounds, localUpperBounds);
      if (isInRange) {
        //do nothing
      } else {
        return false;
      }
    }

    return true;
  }


}
