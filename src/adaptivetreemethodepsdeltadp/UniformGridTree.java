package adaptivetreemethodepsdeltadp;

import java.util.*;

/**
 *
 * @author raof
 */
public class UniformGridTree {

  List<Record> records;

  public UniformGridTreeNode root;
  double[] globalLowerBounds;
  double[] globalUpperBounds;
  int[] numbersOfIntervals;
  double originalW;
  int numberOfDimensions;
  List<UniformGridTreeNode> leafNodes;

  double epsilon;
  double delta;

  Random generator;

  public static class Builder {

    private List<Record> records;
    private double[] globalLowerBounds;
    private double[] globalUpperBounds;
    private int[] numbersOfIntervals;
    private double originalW;

    private double epsilon;
    private double delta;

    private Random generator;

    public Builder records(List<Record> inRecords) {
      records = new ArrayList<>();
      records.addAll(inRecords);
      return this;
    }

    public Builder globalLowerBounds(double[] inGlobalLowerBounds) {
      globalLowerBounds
              = Arrays.copyOf(inGlobalLowerBounds, inGlobalLowerBounds.length);
      return this;
    }

    public Builder globalUpperBounds(double[] inGlobalUpperBounds) {
      globalUpperBounds
              = Arrays.copyOf(inGlobalUpperBounds, inGlobalUpperBounds.length);
      return this;
    }

    public Builder numbersOfIntervals(int[] inNumbersOfIntervals) {
      numbersOfIntervals
              = Arrays.copyOf(inNumbersOfIntervals, inNumbersOfIntervals.length);
      return this;
    }

    public Builder originalW(double inOriginalW) {
      originalW = inOriginalW;
      return this;
    }

    public Builder epsilon(double inEpsilon) {
      epsilon = inEpsilon;
      return this;
    }

    public Builder delta(double inDelta) {
      delta = inDelta;
      return this;
    }

    public Builder generator(Random inGenerator) {
      generator = inGenerator;
      return this;
    }

    public UniformGridTree build() throws Exception {
      return new UniformGridTree(this);
    }

  }

  public UniformGridTree(Builder inBuilder) throws Exception {

    records = inBuilder.records;
    globalLowerBounds = inBuilder.globalLowerBounds;
    globalUpperBounds = inBuilder.globalUpperBounds;
    numbersOfIntervals = inBuilder.numbersOfIntervals;
    originalW = inBuilder.originalW;

    epsilon = inBuilder.epsilon;
    delta = inBuilder.delta;

    generator = inBuilder.generator;

    if (globalLowerBounds.length != globalUpperBounds.length) {
      throw new Exception("in constructor of Tree: "
              + "inGlobalLowerBounds.length != inGlobalUpperBounds.length");
    }
    numberOfDimensions = globalLowerBounds.length;

    leafNodes = new ArrayList<>();

    root = new UniformGridTreeNode.Builder()
            .depth(0)
            .localLowerBounds(globalLowerBounds)
            .localUpperBounds(globalUpperBounds)
            .records(records)
            .build();

  }


  //need to access the information in globalLowerBounds, globalUpperBounds, and
  //numberOfIntervals
  //tell the fanout from the current depth of the extracted TreeNode
  public void buildTree(int[] inRepresentativeDimensions) throws Exception {
    int numberOfLeafNodes = 0;
    ArrayDeque<UniformGridTreeNode> dq = new ArrayDeque<>();
    dq.add(root);

    while (dq.isEmpty() == false) {
      UniformGridTreeNode currentNode = dq.remove();

      int depthOfCurrentNode = currentNode.depth;
      //System.out.println("depthOfCurrentNode: " + depthOfCurrentNode);
      if (depthOfCurrentNode == inRepresentativeDimensions.length) {
        //stop the splitting, a leaf node is identified

        currentNode.isLeaf = true;
        leafNodes.add(currentNode);

        double noise;
        double shiftedMean
                = computeSmallestMeanForContinuousLaplaceRandom(epsilon, delta / 2.0);
        //System.out.println("shiftedMean: " + shiftedMean);
        noise = Utility.LaplaceRandom(shiftedMean, 1 / epsilon, generator);

        currentNode.noiseAdded = noise;
        currentNode.epsilonApplied = epsilon;
        currentNode.noisyCount = currentNode.records.size() + noise;

        numberOfLeafNodes = numberOfLeafNodes + 1;

      } else {//when depthOfCurrentNode < inStoppingDepth
        int currentNumberOfIntervals =
                numbersOfIntervals[inRepresentativeDimensions[depthOfCurrentNode]];
        //int currentNumberOfIntervals = numbersOfIntervals[depthOfCurrentNode];
//				Double currentIntervalWidth =
//								(globalUpperBounds[depthOfCurrentNode] - globalLowerBounds[depthOfCurrentNode]) / currentNumberOfIntervals;
        Double currentIntervalWidth =
                ((globalUpperBounds[inRepresentativeDimensions[depthOfCurrentNode]])
                        - (globalLowerBounds[inRepresentativeDimensions[depthOfCurrentNode]]))
                        / currentNumberOfIntervals;

        //create "currentNumberOfIntervals" child nodes
        for (int i = 0; i < currentNumberOfIntervals; i = i + 1) {
          double[] lowerBoundsForChild = new double[currentNode.localLowerBounds.length];
          double[] upperBoundsForChild = new double[currentNode.localUpperBounds.length];

          //child node inherits lowerBounds and upperBounds from its parent node
          for (int j = 0; j < globalLowerBounds.length; j = j + 1) {
            lowerBoundsForChild[j] = currentNode.localLowerBounds[j];
            upperBoundsForChild[j] = currentNode.localUpperBounds[j];
          }
          //but need to update the dimension of "depthOfCurrentNode"
          lowerBoundsForChild[inRepresentativeDimensions[depthOfCurrentNode]] =
                  globalLowerBounds[inRepresentativeDimensions[depthOfCurrentNode]]
                          + i * currentIntervalWidth;
          upperBoundsForChild[inRepresentativeDimensions[depthOfCurrentNode]] =
                  globalLowerBounds[inRepresentativeDimensions[depthOfCurrentNode]]
                          + (i + 1) * currentIntervalWidth;

          UniformGridTreeNode child
                  = new UniformGridTreeNode.Builder()
                  .depth(depthOfCurrentNode + 1)
                  .localLowerBounds(lowerBoundsForChild)
                  .localUpperBounds(upperBoundsForChild)
                  .build();
          currentNode.children.add(child);
        }

        //distribute the Records
        Iterator itr = currentNode.records.iterator();
        while (itr.hasNext()) {
          Record r = (Record) itr.next();
          double valueOfInterest = r.c.coordinates[inRepresentativeDimensions[depthOfCurrentNode]];
          int positionToDistribute =
                  (int) ((valueOfInterest
                          - globalLowerBounds[inRepresentativeDimensions[depthOfCurrentNode]])
                          / currentIntervalWidth);

          UniformGridTreeNode childToDistribute = currentNode.children.get(positionToDistribute);
          childToDistribute.records.add(r);
        }
        currentNode.records.clear();

        //add each child node to deque
        dq.addAll(currentNode.children);
      }
    }

    //System.out.println("numberOfLeafNodes: " + numberOfLeafNodes);
  }

  //to test
  public Double rangeCount(Double[] inQueryLowerBounds, Double[] inQueryUpperBounds) throws Exception {
    //again, use a deque to accomplish the task...
    if (inQueryLowerBounds.length != numberOfDimensions
            || inQueryUpperBounds.length != numberOfDimensions) {
      throw new Exception("in rangeCount: inQueryLowerBounds.length != numberOfDimensions || "
              + "inQueryUpperBounds.length != numberOfDimensions");
    }

    Double count = 0.0;

    ArrayDeque<UniformGridTreeNode> dq = new ArrayDeque<UniformGridTreeNode>();
    dq.add(root);

    while (dq.isEmpty() == false) {
      UniformGridTreeNode currentNode = dq.remove();

      //if currentNode is a leaf node
      if (currentNode.isLeaf == true) {
        //estimate the counts
        //for each dimension, compute the intersected ratio
        Double productOfIntersectedRatios = 1.0;
        for (int i = 0; i < numberOfDimensions; i = i + 1) {
          Double currentRegionLowerBound = currentNode.localLowerBounds[i];
          Double currentRegionUpperBound = currentNode.localUpperBounds[i];
          Double currentQueryLowerBound = inQueryLowerBounds[i];
          Double currentQueryUpperBound = inQueryUpperBounds[i];
          //start from here, to debug...
          Double length = intersectedLength(currentRegionLowerBound, currentRegionUpperBound,
                  currentQueryLowerBound, currentQueryUpperBound);
          Double currentRatio = length / (currentRegionUpperBound - currentRegionLowerBound);
          productOfIntersectedRatios = productOfIntersectedRatios * currentRatio;
        }
        //System.out.println("productOfIntersectedRatio: " + productOfIntersectedRatios);
        count = count + currentNode.records.size() * productOfIntersectedRatios;

      } else {//currentNode is an internal node
        //insert only the related children into deque.
        int dimensionOfInterest = currentNode.depth;
        Iterator itr = currentNode.children.iterator();
        while (itr.hasNext()) {
          UniformGridTreeNode child = (UniformGridTreeNode) itr.next();
          Double currentLowerBound = child.localLowerBounds[dimensionOfInterest];
          Double currentUpperBound = child.localUpperBounds[dimensionOfInterest];
          if (isIntersected(currentLowerBound, currentUpperBound,
                  inQueryLowerBounds[dimensionOfInterest],
                  inQueryUpperBounds[dimensionOfInterest])) {
            //insert related child into deque
            dq.add(child);
          } else {
            //do nothing since child is not related
          }
        }
      }
    }


    return count;
  }

  public Double noisyRangeCount(Double[] inQueryLowerBounds, Double[] inQueryUpperBounds) throws Exception {
    //again, use a deque to accomplish the task...
    if (inQueryLowerBounds.length != numberOfDimensions
            || inQueryUpperBounds.length != numberOfDimensions) {
      throw new Exception("in rangeCount: inQueryLowerBounds.length != numberOfDimensions || "
              + "inQueryUpperBounds.length != numberOfDimensions");
    }

    //Double count = 0.0;
    Double noisyCount = 0.0;

    ArrayDeque<UniformGridTreeNode> dq = new ArrayDeque<UniformGridTreeNode>();
    dq.add(root);

    while (dq.isEmpty() == false) {
      UniformGridTreeNode currentNode = dq.remove();

      //if currentNode is a leaf node
      if (currentNode.isLeaf == true) {
        //estimate the counts
        //for each dimension, compute the intersected ratio
        Double productOfIntersectedRatios = 1.0;
        for (int i = 0; i < numberOfDimensions; i = i + 1) {
          Double currentRegionLowerBound = currentNode.localLowerBounds[i];
          Double currentRegionUpperBound = currentNode.localUpperBounds[i];
          Double currentQueryLowerBound = inQueryLowerBounds[i];
          Double currentQueryUpperBound = inQueryUpperBounds[i];
          //start from here, to debug...
          Double length = intersectedLength(currentRegionLowerBound, currentRegionUpperBound,
                  currentQueryLowerBound, currentQueryUpperBound);
          Double currentRatio = length / (currentRegionUpperBound - currentRegionLowerBound);
          productOfIntersectedRatios = productOfIntersectedRatios * currentRatio;
        }
        //System.out.println("productOfIntersectedRatio: " + productOfIntersectedRatios);
        noisyCount = noisyCount + currentNode.noisyCount * productOfIntersectedRatios;
      } else {//currentNode is an internal node
        //insert only the related children into deque.
        int dimensionOfInterest = currentNode.depth;
        Iterator itr = currentNode.children.iterator();
        while (itr.hasNext()) {
          UniformGridTreeNode child = (UniformGridTreeNode) itr.next();
          Double currentLowerBound = child.localLowerBounds[dimensionOfInterest];
          Double currentUpperBound = child.localUpperBounds[dimensionOfInterest];
          if (isIntersected(currentLowerBound, currentUpperBound,
                  inQueryLowerBounds[dimensionOfInterest],
                  inQueryUpperBounds[dimensionOfInterest])) {
            //insert related child into deque
            dq.add(child);
          } else {
            //do nothing since child is not related
          }
        }
      }
    }

    return noisyCount;
  }

  public Double intersectedLength(Double inRegionLowerBound, Double inRegionUpperBound,
                                  Double inQueryLowerBound, Double inQueryUpperBound) throws Exception {
    Double result;

    //need to rule out the disjoint cases:
    if (inQueryUpperBound <= inRegionLowerBound) {
      return 0.0;
    }
    if (inRegionUpperBound <= inQueryLowerBound) {
      return 0.0;
    }

    //there are four possible cases:
    //System.out.println("this line?");
    if (inRegionLowerBound <= inQueryLowerBound) {
      if (inRegionUpperBound <= inQueryUpperBound) {
        result = inRegionUpperBound - inQueryLowerBound;
      } else {//(inRegionUpperBound > inQueryUpperBound)
        result = inQueryUpperBound - inQueryLowerBound;
      }
    } else {//(inRegionLowerBound > inQueryLowerBound)
      if (inRegionUpperBound <= inQueryUpperBound) {
        result = inRegionUpperBound - inRegionLowerBound;
      } else {//(inRegionUpperBound > inQueryUpperBound)
        result = inQueryUpperBound - inRegionLowerBound;
      }
    }

    if (result < 0) {
      System.out.println("inRegionLowerBound: " + inRegionLowerBound);
      System.out.println("inRegionUpperBound: " + inRegionUpperBound);
      System.out.println("inQueryLowerBound: " + inQueryLowerBound);
      System.out.println("inQueryUpperBound: " + inQueryUpperBound);
      throw new Exception("intersected length < 0.");
    }
    return result;
  }

  public boolean isIntersected(Double inRegionLowerBound, Double inRegionUpperBound,
                               Double inQueryLowerBound, Double inQueryUpperBound) {
    if (inRegionUpperBound <= inQueryLowerBound) {
      return false;
    } else if (inQueryUpperBound <= inRegionLowerBound) {
      return false;
    } else {
      return true;
    }
  }

  //this method might not be used.
  public boolean isRelated(UniformGridTreeNode inNode,
                           double[] inQueryLowerBounds, double[] inQueryUpperBounds) {
    double[] lowerBounds = inNode.localLowerBounds;
    double[] upperBounds = inNode.localUpperBounds;
    for (int i = 0; i < numberOfDimensions; i = i + 1) {
      if ((isIntersected(lowerBounds[i], upperBounds[i],
              inQueryLowerBounds[i], inQueryUpperBounds[i])) == false) {
        return false;
      }
    }
    return true;
  }

  public void prepareRecordIDsForLeafNode(UniformGridTreeNode inTreeNode) throws Exception {

    double noise = inTreeNode.noiseAdded;

    int roundedNoise;
    if (noise == 0.0) {
      roundedNoise = 0;
    } else if (noise > 0.0) {
      roundedNoise = Utility.ceiling(noise);
    } else if (noise < 0.0) {//the case when noise < 0
      roundedNoise = Utility.floor(noise);
    } else {
      throw new Exception("in prepareRecordIDsForLeafNode: noise is NaN.");
    }

    List<Integer> allRecordIDs = new ArrayList<Integer>();
    //add all Record IDs to allRecordIDs first
    Iterator itr = inTreeNode.records.iterator();
    while (itr.hasNext()) {
      Record currentRecord = (Record) itr.next();
      Integer currentRID = new Integer(currentRecord.recordID);
      allRecordIDs.add(currentRID);
    }

    if (roundedNoise == 0) {
      //add all Record IDs to inPreservedRecordIDs
      inTreeNode.preservedRecordIDs.addAll(allRecordIDs);
      allRecordIDs.clear();
    } else if (roundedNoise > 0) {
      //add roundedNoise "-1" to inFakeRecordIDs
      //add all Record IDs to inTreeNode.preservedRecordIDs as well
      for (int i = 0; i < roundedNoise; i = i + 1) {
        inTreeNode.fakeRecordIDs.add(new Integer(-1));
      }
      inTreeNode.preservedRecordIDs.addAll(allRecordIDs);
      allRecordIDs.clear();
    } else {//when roundedNoise < 0
      //randomly choose -1 * inRoundedNoise Record IDs to suppress
      int numberOfRecordsToSuppress = (-1 * roundedNoise);
      //start from here, need to take care of the case when
      //numberOfRecordsToSuppress > allRecordIDs.size()...
      if (allRecordIDs.size() < numberOfRecordsToSuppress) {
        numberOfRecordsToSuppress = allRecordIDs.size();
      }

      for (int i = 0; i < numberOfRecordsToSuppress; i = i + 1) {
        int positionOfSuppressedRecordIDs =
                Utility.mod(generator.nextInt(), allRecordIDs.size());
        Integer removedID = allRecordIDs.remove(positionOfSuppressedRecordIDs);
        inTreeNode.suppressedRecordIDs.add(removedID);
      }
      inTreeNode.preservedRecordIDs.addAll(allRecordIDs);
      allRecordIDs.clear();
    }

    //check consistency
    if (inTreeNode.preservedRecordIDs.size() + inTreeNode.suppressedRecordIDs.size()
            != inTreeNode.records.size()) {
      throw new Exception("inTreeNode.preservedRecordIDs.size() + "
              + "inTreeNode.suppressedRecordIDs.size() != inTreeNode.records.size()");
    } else {
      //System.out.println("consistency checked.");
    }
  }

  public void prepareRecordIDsForUniformGridTree() throws Exception {
    Iterator itr = leafNodes.iterator();
    while (itr.hasNext()) {
      UniformGridTreeNode leaf = (UniformGridTreeNode) itr.next();
      prepareRecordIDsForLeafNode(leaf);
    }
  }

  public double computeShiftedMean(double inEpsilon, double inBeta, double inL) throws Exception {
    double mu;
    double lambda = 1.0 / inEpsilon;
    double naturalLogOfTwo = Math.log(2.0);
    double naturalLogOfBeta = Math.log(inBeta);
    mu = -1.0 * lambda * (naturalLogOfTwo + naturalLogOfBeta) - inL;

    if (mu < 0.0) {
      throw new Exception("in cmputeShiftedMean: mu < 0.0.");
    }

    return mu;
  }

  /**
   * This method is created on 2017/09/02
   * This method is pasted on 2018/02/15 (Fri)
   *
   * Fang-Yu: Notice the sensitivity is with respect to any pair of databases
   * (or sets) S and S' such that S' = S U {r} or S' = S \ {r} for some records
   * r. That is, their size differs only by 1. Using this parameter setting, it
   * can be guaranteed that the output of the function f satisfies the following
   * inequality.
   *
   * Pr[f(S') = O] <= e^{epsilon} * Pr[f(S) = O] + delta
   *
   * If in total there will be two subsets in a database that may be different
   * after replacing one non-mathcing records with another distinct non-matching
   * record, then the difference in probability would be the following:
   *
   * Pr[f(D') = O] <= e^{2 * epsilon} * Pr[f(D) = O] + (2 * delta),
   *
   * where D' = S' U ... and D = S U ...
   *
   *
   * Fang-Yu: when calling this method, inEpsilon corresponds to the privacy budget
   * left for the current (leaf) node.
   *
   * Fang-Yu: as for inDelta, it corresponds to "this.delta / 2.0", since there are
   * at most 2 leaf affected leaf nodes when we replace one non-matching record
   * with another distinct non-matching record.
   *
   * @param inEpsilon
   * @param inDelta
   * @return
   */
  public static double computeSmallestMeanForContinuousLaplaceRandom(
          double inEpsilon, double inDelta) {
    double smallestSatisfyingMean;

    double lambda = 1.0 / inEpsilon;//sensitivity is equal to 1.0
    smallestSatisfyingMean = (-1.0) * lambda * Math.log(2.0 * inDelta);

    return smallestSatisfyingMean;
  }
}
