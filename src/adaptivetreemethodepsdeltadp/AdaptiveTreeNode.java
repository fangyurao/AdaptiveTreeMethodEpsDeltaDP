package adaptivetreemethodepsdeltadp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author raof
 */
public class AdaptiveTreeNode {

  int depth;
  double[] localLowerBounds;
  double[] localUpperBounds;
  List<Record> records;
  AdaptiveTreeNode leftChild;
  AdaptiveTreeNode rightChild;
  UniformGridTree uGTree;
  boolean isRootOfUniformGridTree;
  double noiseAdded;
  double epsilonApplied;
  double noisyCount;
  int trueCount;
  double privacyBudgetForChildren;

  public static class Builder {

    private int depth;
    private double[] lowerBounds;
    private double[] upperBounds;
    private List<Record> records;
    private double privacyBudgetForChildren;

    public Builder depth(int inDepth) {
      depth = inDepth;
      return this;
    }

    public Builder lowerBounds(double[] inLowerBounds) {
      lowerBounds
              = Arrays.copyOf(inLowerBounds, inLowerBounds.length);
      return this;
    }

    public Builder upperBounds(double[] inUpperBounds) {
      upperBounds
              = Arrays.copyOf(inUpperBounds, inUpperBounds.length);
      return this;
    }

    public Builder records(List<Record> inRecords) {
      records = new ArrayList<>();
      records.addAll(inRecords);
      return this;
    }

    public Builder privacyBudgetForChildren(double inPrivacyBudgetForChildren) {
      privacyBudgetForChildren = inPrivacyBudgetForChildren;
      return this;
    }

    public AdaptiveTreeNode build() {
      return new AdaptiveTreeNode(this);
    }

  }

  /**
   * created on 2018/02/13
   * @param inBuilder
   */
  public AdaptiveTreeNode(Builder inBuilder) {

    depth = inBuilder.depth;
    localLowerBounds = inBuilder.lowerBounds;
    localUpperBounds = inBuilder.upperBounds;
    records = inBuilder.records;
    privacyBudgetForChildren = inBuilder.privacyBudgetForChildren;

    leftChild = null;
    rightChild = null;
    isRootOfUniformGridTree = false;

    noiseAdded = Double.NaN;
    epsilonApplied = Double.NaN;
    noisyCount = Double.NaN;
    trueCount = records.size();

  }

  /*
   * Note:
   * We do not need allRecordsInBoundingBoxes() here since
   * all Records will be stored in UniformGridTreeNodes
   */
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

  public void displayBoundingBoxes() {
    int numberOfDimensions = localLowerBounds.length;
    for (int i = 0; i < numberOfDimensions; i = i + 1) {
      System.out.println("[" + localLowerBounds[i] + ", " + localUpperBounds[i] + ")");
    }
  }
}
