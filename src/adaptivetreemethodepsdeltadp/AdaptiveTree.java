package adaptivetreemethodepsdeltadp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author raof
 */
public final class AdaptiveTree {

  public Random generator;
  public AdaptiveTreeNode root;
  List<Record> records;
  int numberOfDimensions;
  int initialSplittingDimension;
  List<AdaptiveTreeNode> uniformGridTreeRoots;
  List<AdaptiveTreeNode> internalNodes;
  double epsilon;
  double delta;
  double eta;
  double minPrivacyBudgetSpent;

  double totalPrivacyBudgetForProbing;
  double totalPrivacyBudgetForFinalCounting;

  double[] globalLowerBounds;
  double[] globalUpperBounds;
  int[] representativeDimensions;

  int numberOfUniformGridLeafNodes;

  int numberOfLeafNodes;
  int numberOfEmptyLeafNodes;
  int numberOfNonEmptyLeafNodes;
  double varianceOfLeafSize;
  double averageDepthOfLeafNodes;

  public static class Builder {

    private Random generator;
    private List<Record> records;
    private int numberOfDimensions;

    private double epsilon;
    private double delta;
    private double eta;
    private double minPrivacyBudgetSpent;

    private double[] globalLowerBounds;
    private double[] globalUpperBounds;
    private int[] representativeDimensions;

    //to check later
    public Builder generator(Random inGenerator) {
      generator = inGenerator;
      return this;
    }

    public Builder records(List<Record> inRecords) {
      records = new ArrayList<>();
      records.addAll(inRecords);
      return this;
    }

    public Builder numberOfDimensions(int inNumberOfDimensions) {
      numberOfDimensions = inNumberOfDimensions;
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

    public Builder eta(double inEta) {
      eta = inEta;
      return this;
    }

    public Builder minPrivacyBudgetSpent(double inMinPrivacyBudgetSpent) {
      minPrivacyBudgetSpent = inMinPrivacyBudgetSpent;
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

    public Builder representativeDimensions(int[] inRepresentativeDimensions) {
      representativeDimensions
              = Arrays.copyOf(inRepresentativeDimensions, inRepresentativeDimensions.length);
      return this;
    }

    public AdaptiveTree build() {
      return new AdaptiveTree(this);
    }

  }

  /**
   * created on 2018/02/13
   * @param inBuilder
   */
  public AdaptiveTree(Builder inBuilder) {

    generator = inBuilder.generator;
    records = inBuilder.records;
    numberOfDimensions = inBuilder.numberOfDimensions;

    epsilon = inBuilder.epsilon;
    delta = inBuilder.delta;
    eta = inBuilder.eta;
    minPrivacyBudgetSpent = inBuilder.minPrivacyBudgetSpent;

    globalLowerBounds = inBuilder.globalLowerBounds;
    globalUpperBounds = inBuilder.globalUpperBounds;
    representativeDimensions = inBuilder.representativeDimensions;

    totalPrivacyBudgetForProbing = (epsilon / 2.0) / 2;
    totalPrivacyBudgetForFinalCounting
            = (epsilon / 2.0) - totalPrivacyBudgetForProbing;
    System.out.println("totalPrivacyBudgetForProbing: " + totalPrivacyBudgetForProbing);
    System.out.println("totalPrivacyBudgetForFinalCounting: " + totalPrivacyBudgetForFinalCounting);

    root = new AdaptiveTreeNode.Builder()
            .depth(0)
            .lowerBounds(globalLowerBounds)
            .upperBounds(globalUpperBounds)
            .records(records)
            .privacyBudgetForChildren(totalPrivacyBudgetForProbing)
            .build();
    uniformGridTreeRoots = new ArrayList<>();
    internalNodes = new ArrayList<>();

    root.noisyCount = root.records.size();
    root.noiseAdded = 0.0;
    root.epsilonApplied = Double.POSITIVE_INFINITY;//since we are using the true count
    root.trueCount = root.records.size();

    initialSplittingDimension = 0;
    System.out.println("initialSplittingDimension: " + initialSplittingDimension);

    //the following 3 fields are filled out in fillOutLeafNodesInformation()
    numberOfLeafNodes = -1;//orig. -1
    numberOfEmptyLeafNodes = -1;//orig. -1
    numberOfNonEmptyLeafNodes = -1;//orig. -1

    //the following field is filled out when an AdaptiveTreeNode stops growing child nodes
    numberOfUniformGridLeafNodes = 0;

    varianceOfLeafSize = Double.NaN;
    averageDepthOfLeafNodes = Double.NaN;
  }

  /*
   * when privacy budget is not enough, switch to UniformGridTree.
   */
  public void buildTree() throws Exception {

    ArrayDeque<AdaptiveTreeNode> dq = new ArrayDeque<>();

    dq.add(root);
    while (dq.isEmpty() == false) {
      AdaptiveTreeNode currentNode = dq.remove();

      internalNodes.add(currentNode);//added on 2013/07/07

      Double requiredPrivacyBudget = 2.0 / (eta * currentNode.noisyCount);

      if (requiredPrivacyBudget < 0.0) {
        //to avoid the case when noisyCount is negative so that we will directly
        //use uniform grid to partition, i.e., go to else part directly
        requiredPrivacyBudget = Double.POSITIVE_INFINITY;
      }
      //add a statement to specify a minimum budget spent
      //added on 2013/07/08
      if (requiredPrivacyBudget < minPrivacyBudgetSpent) {
        requiredPrivacyBudget = minPrivacyBudgetSpent;
      }

      if (requiredPrivacyBudget <= currentNode.privacyBudgetForChildren) {

        int dimensionToSplit = (currentNode.depth + initialSplittingDimension) % numberOfDimensions;
        Double currentMidPoint = (currentNode.localLowerBounds[dimensionToSplit]
                + currentNode.localUpperBounds[dimensionToSplit]) / 2;

        //create left child and right child
        //need to prepare the corresponding lowerBounds and upperBounds
        double[] lowerBoundsForLeftChild = new double[numberOfDimensions];
        double[] upperBoundsForLeftChild = new double[numberOfDimensions];
        for (int i = 0; i < numberOfDimensions; i = i + 1) {
          lowerBoundsForLeftChild[i] = currentNode.localLowerBounds[i];
          upperBoundsForLeftChild[i] = currentNode.localUpperBounds[i];
        }
        upperBoundsForLeftChild[dimensionToSplit] = currentMidPoint;

        double[] lowerBoundsForRightChild = new double[numberOfDimensions];
        double[] upperBoundsForRightChild = new double[numberOfDimensions];
        for (int i = 0; i < numberOfDimensions; i = i + 1) {
          lowerBoundsForRightChild[i] = currentNode.localLowerBounds[i];
          upperBoundsForRightChild[i] = currentNode.localUpperBounds[i];
        }
        lowerBoundsForRightChild[dimensionToSplit] = currentMidPoint;

        //distribute Records to children nodes
        List<Record> recordsForLeftChild = new ArrayList<>();
        List<Record> recordsForRightChild = new ArrayList<>();
        Iterator itr = currentNode.records.iterator();
        while (itr.hasNext()) {
          Record currentRecord = (Record) itr.next();
          if (currentRecord.c.coordinates[dimensionToSplit] < currentMidPoint) {
            recordsForLeftChild.add(currentRecord);
          } else if (currentRecord.c.coordinates[dimensionToSplit] >= currentMidPoint) {
            recordsForRightChild.add(currentRecord);
          } else {
            throw new Exception("in buildTree(), should not be here.");
          }
        }

        //need to rule out the possibility that a produced child by definition
        //does not contain any record.
        //i.e., we would like to make sure that any node addded to deque by definition
        //could possibly contain some records.
        if (anyDimensionDoesNotContainIntegralPoint(lowerBoundsForLeftChild,
                upperBoundsForLeftChild) == true) {
          //do nothing
          //System.out.println("some dim does not contain integral point.");
        } else {

          AdaptiveTreeNode leftChild = new AdaptiveTreeNode.Builder()
                  .depth(currentNode.depth + 1)
                  .lowerBounds(lowerBoundsForLeftChild)
                  .upperBounds(upperBoundsForLeftChild)
                  .records(recordsForLeftChild)
                  .privacyBudgetForChildren(currentNode.privacyBudgetForChildren - requiredPrivacyBudget)
                  .build();

          currentNode.leftChild = leftChild;
          double noiseForLeftChild = Utility.LaplaceRandom(0.0, 1 / requiredPrivacyBudget, generator);
          leftChild.noisyCount = leftChild.records.size() + noiseForLeftChild;
          leftChild.noiseAdded = noiseForLeftChild;
          leftChild.epsilonApplied = requiredPrivacyBudget;

				System.out.println("noiseForLeftChild: " + noiseForLeftChild);
				System.out.println("leftChild.records.size(): " + leftChild.records.size());

          dq.add(leftChild);
        }

        if (anyDimensionDoesNotContainIntegralPoint(lowerBoundsForRightChild,
                upperBoundsForRightChild) == true) {
          //do nothing
          //System.out.println("some dim does not contain integral point.");
        } else {

          AdaptiveTreeNode rightChild = new AdaptiveTreeNode.Builder()
                  .depth(currentNode.depth + 1)
                  .lowerBounds(lowerBoundsForRightChild)
                  .upperBounds(upperBoundsForRightChild)
                  .records(recordsForRightChild)
                  .privacyBudgetForChildren(currentNode.privacyBudgetForChildren - requiredPrivacyBudget)
                  .build();

          currentNode.rightChild = rightChild;
          double noiseForRightChild = Utility.LaplaceRandom(0.0, 1 / requiredPrivacyBudget, generator);
          rightChild.noisyCount = rightChild.records.size() + noiseForRightChild;
          rightChild.noiseAdded = noiseForRightChild;
          rightChild.epsilonApplied = requiredPrivacyBudget;

				System.out.println("noiseForRightChild: " + noiseForRightChild);
				System.out.println("rightChild.records.size(): " + rightChild.records.size());

          dq.add(rightChild);
        }

        //the code below is used to debug...
//				System.out.println("");
//				System.out.println("currentNode.noisyCount: " + currentNode.noisyCount);
//				System.out.println("requiredPrivacyBudget: " + requiredPrivacyBudget);
//				System.out.println("lambda used: " + (1 / requiredPrivacyBudget));
        currentNode.records.clear();
      } else {//currentNode does not have enough requiredPrivacyBudget

        //added on 2018/02/16 (Fri) for debugging
        System.out.println("currentNode.noisyCount: " + currentNode.noisyCount);
        System.out.println("not enough budget, requiredPrivacyBudget: " + requiredPrivacyBudget);

        uniformGridTreeRoots.add(currentNode);
        currentNode.isRootOfUniformGridTree = true;

        Double originalW
                = currentNode.noisyCount * (currentNode.privacyBudgetForChildren + totalPrivacyBudgetForFinalCounting) * eta;
//
// 	System.out.println("");
//				System.out.println("currentNode.noisyCount: " + currentNode.noisyCount);
//				System.out.println("currentNode.records.size(): " + currentNode.records.size());
//				System.out.println("currentNode.privacyBudgetForChildren: " + currentNode.privacyBudgetForChildren);
//				System.out.println("originalW: " + originalW);
        int truncatedW = Utility.floor(originalW);
        if (truncatedW <= 1) {
          truncatedW = 1;//truncatedW has to be at least 1.
        }
        //System.out.println("truncatedW: " + truncatedW);
        int numberOfIntervalsInEachRepresentativeDimension
                = Utility.floor(Math.pow((double) truncatedW, (1.0 / representativeDimensions.length)));
//				System.out.println("numberOfIntervalsInEachRepresentativeDimension: " +
//								numberOfIntervalsInEachRepresentativeDimension);

        //now come up with numbersOfIntervals for those representativeDimensions.
        int[] numbersOfIntervals = new int[numberOfDimensions];
        for (int i = 0; i < numberOfDimensions; i = i + 1) {
          numbersOfIntervals[i] = -1;
        }
        for (int i = 0; i < representativeDimensions.length; i = i + 1) {
          int dimensionOfInterest = representativeDimensions[i];
          int numberOfIntegralPoints
                  = numberOfIntegralValuesWithinRange(
                  currentNode.localLowerBounds[dimensionOfInterest],
                  currentNode.localUpperBounds[dimensionOfInterest]);
          if (numberOfIntegralPoints < numberOfIntervalsInEachRepresentativeDimension) {
            numbersOfIntervals[dimensionOfInterest] = numberOfIntegralPoints;
          } else {//the case when numberOfIntegralPoints >= numberOfIntervalsInEachRepresentativeDimension
            numbersOfIntervals[dimensionOfInterest] = numberOfIntervalsInEachRepresentativeDimension;
          }
        }

//        System.out.println("currentNode.privacyBudgetForChildren: "
//                + currentNode.privacyBudgetForChildren);
//        System.out.println("totalPrivacyBudgetForFinalCounting: "
//                + totalPrivacyBudgetForFinalCounting);
//        System.out.println("(currentNode.privacyBudgetForChildren + totalPrivacyBudgetForFinalCounting): "
//                + (currentNode.privacyBudgetForChildren + totalPrivacyBudgetForFinalCounting));
        UniformGridTree currentUGTree
                = new UniformGridTree.Builder()
                .records(currentNode.records)
                .globalLowerBounds(currentNode.localLowerBounds)
                .globalUpperBounds(currentNode.localUpperBounds)
                .originalW(originalW)
                .numbersOfIntervals(numbersOfIntervals)
                .epsilon((currentNode.privacyBudgetForChildren + totalPrivacyBudgetForFinalCounting))
                .delta(delta)
                .generator(generator)
                .build();
        currentUGTree.buildTree(representativeDimensions);

        //note that an internal AdaptiveTreeNode will not have a non-null uGTree field
        currentNode.uGTree = currentUGTree;
        numberOfUniformGridLeafNodes
                = numberOfUniformGridLeafNodes + currentNode.uGTree.leafNodes.size();

      }
    }
  }

  /*
   * disply all the leaf nodes
   */
  public void displayLeafNodes() {
    Iterator itr = uniformGridTreeRoots.iterator();
    while (itr.hasNext()) {
      AdaptiveTreeNode currentAdaptiveTreeNode = (AdaptiveTreeNode) itr.next();
      UniformGridTree currentUGTree = currentAdaptiveTreeNode.uGTree;
      List<UniformGridTreeNode> leafNodes = currentUGTree.leafNodes;

      Iterator innerItr = leafNodes.iterator();
      while (innerItr.hasNext()) {
        UniformGridTreeNode leaf = (UniformGridTreeNode) innerItr.next();
        leaf.displayBoundingBoxes();
        System.out.println("");
      }
    }
  }

  public void displayLeafCounts(boolean inShowingDetails) {

    Iterator outerItr = uniformGridTreeRoots.iterator();
    int numberOfLeafNodes = 0;
    int numberOfRecords = 0;

    double L2DistanceRealAndNoisy = 0.0;

    Double sumOfOriginalW = 0.0;//added on 2013/08/07
    double sumOfNumberOfSupposedLeafNodes = 0.0;//added on 2013/08/07
    while (outerItr.hasNext()) {
      AdaptiveTreeNode node = (AdaptiveTreeNode) outerItr.next();
      UniformGridTree currentUGTree = node.uGTree;

      sumOfOriginalW = sumOfOriginalW + currentUGTree.originalW;

      numberOfLeafNodes = numberOfLeafNodes + currentUGTree.leafNodes.size();
      Iterator innerItr = currentUGTree.leafNodes.iterator();

      //below added on 2013/08/07
      int truncatedW = Utility.floor(currentUGTree.originalW);
      int numberOfIntervalsInEachRepresentativeDimension
              = Utility.floor(Math.pow((double) truncatedW, (1.0 / representativeDimensions.length)));
      double numberOfSupposedLeafNodes = Math.pow((double) numberOfIntervalsInEachRepresentativeDimension,
              (double) representativeDimensions.length);
      sumOfNumberOfSupposedLeafNodes = sumOfNumberOfSupposedLeafNodes
              + numberOfSupposedLeafNodes;
      if (inShowingDetails == true) {
        System.out.format("originalW: %10.3f, leafNodes.size(): %5d",
                currentUGTree.originalW, currentUGTree.leafNodes.size());
        System.out.println("");
      }
      //above added on 2013/08/07

      while (innerItr.hasNext()) {
        //numberOfLeafNodes = numberOfLeafNodes + 1;
        UniformGridTreeNode leaf = (UniformGridTreeNode) innerItr.next();
        numberOfRecords = numberOfRecords + leaf.records.size();

        double absDifferenceBetweenRealAndNoisy = Utility.abs((double) leaf.records.size() - leaf.noisyCount);

        L2DistanceRealAndNoisy = L2DistanceRealAndNoisy
                + absDifferenceBetweenRealAndNoisy * absDifferenceBetweenRealAndNoisy;

        if (inShowingDetails == true) {
          System.out.format(
                  "real: %5d, noisy: %10.3f, depth: %3d, eps: %5.5f",
                  leaf.records.size(), leaf.noisyCount,
                  (node.depth + 1), leaf.epsilonApplied);
          System.out.println("");

        }

        //code below added on 2013/07/30 to debug
//				System.out.println("leaf related:");
//				for (int i = 0; i < numberOfDimensions; i = i + 1) {
//					System.out.println("[" + leaf.localLowerBounds[i] + ", " + leaf.localUpperBounds[i] + ")");
//				}
        //code above added on 2013/07/30 to debug
      }
    }
    System.out.println("uniformGridTreeRoots.size(): " + uniformGridTreeRoots.size());//added on 2013/08/17
    System.out.println("sumOfOriginalW: " + sumOfOriginalW);//added on 2013/08/17
    System.out.println("sumOfNumberOfSuposedLeafNodes: " + sumOfNumberOfSupposedLeafNodes);//added on 2013/08/17

    System.out.println("numberOfLeafNodes: " + numberOfLeafNodes);
    System.out.println("numberOfRecords: " + numberOfRecords);
    System.out.println("L2DistanceRealAndNoisy: " + L2DistanceRealAndNoisy);
  }

  /*
   * also good to compute the L2 norm.
   */
  public void displyInternalNodeCounts(boolean inShowingDetails) {
    int numberOfInternalNodes = internalNodes.size();
    double L2DistanceRealAndNoisy = 0.0;

    Iterator itr = internalNodes.iterator();
    while (itr.hasNext()) {
      AdaptiveTreeNode node = (AdaptiveTreeNode) itr.next();

      double absDifferenceBetweenRealAndNoisy = Utility.abs((double) node.trueCount - node.noisyCount);

      L2DistanceRealAndNoisy = L2DistanceRealAndNoisy
              + absDifferenceBetweenRealAndNoisy * absDifferenceBetweenRealAndNoisy;

      if (inShowingDetails == true) {
        System.out.format(
                "real: %5d, noisy: %10.3f, depth: %3d, eps: %5.5f",
                node.trueCount, node.noisyCount, (node.depth), node.epsilonApplied);
        System.out.println("");
      }

      //code below added on 2013/07/30 to debug
//			System.out.println("internal node related: ");
//			for (int i = 0; i < numberOfDimensions; i = i + 1) {
//				System.out.println("[" + node.localLowerBounds[i] + ", " + node.localUpperBounds[i] + ")");
//			}
      //code above added on 2013/07/30 to debug
    }

    System.out.println("numberOfInternalNodes: " + numberOfInternalNodes);
    System.out.println("L2DistanceRealAndNoisy: " + L2DistanceRealAndNoisy);
  }

  public void outputLeafNodesToFile(String inFileName) throws IOException {
    int numberOfTrueRecordsProcessed = 0;
    FileWriter fstream = new FileWriter(inFileName, false);
    BufferedWriter out = new BufferedWriter(fstream);

    out.write((new Integer(numberOfUniformGridLeafNodes)).toString());
    out.newLine();
    out.write((new Integer(numberOfDimensions)).toString());
    out.newLine();

    Iterator outerItr = uniformGridTreeRoots.iterator();
    while (outerItr.hasNext()) {
      AdaptiveTreeNode currentAdaptiveTreeNode = (AdaptiveTreeNode) outerItr.next();
      UniformGridTree currentUGTree = currentAdaptiveTreeNode.uGTree;
      Iterator innerItr = currentUGTree.leafNodes.iterator();
      while (innerItr.hasNext()) {
        UniformGridTreeNode leaf = (UniformGridTreeNode) innerItr.next();
        for (int i = 0; i < numberOfDimensions; i = i + 1) {
          out.write((new Double(leaf.localLowerBounds[i])).toString());
          out.write("\t");
          out.write((new Double(leaf.localUpperBounds[i])).toString());
          out.newLine();
        }

        int numberOfSuppressedRecordIDs = leaf.suppressedRecordIDs.size();
        int numberOfFakeRecordIDs = leaf.fakeRecordIDs.size();
        int numberOfPreservedRecordIDs = leaf.preservedRecordIDs.size();

        //sort the Record IDs to facilitate the matching experiment later
        //added on 2013/07/05
        Collections.sort(leaf.suppressedRecordIDs);
        Collections.sort(leaf.preservedRecordIDs);

        if (numberOfSuppressedRecordIDs == 0) {
          out.write("");
          out.newLine();
        } else {
          for (int i = 0; i < numberOfSuppressedRecordIDs; i = i + 1) {
            numberOfTrueRecordsProcessed = numberOfTrueRecordsProcessed + 1;
            if (i < numberOfSuppressedRecordIDs - 1) {
              out.write(leaf.suppressedRecordIDs.get(i).toString());
              out.write(", ");
            } else {//the last Record ID
              out.write(leaf.suppressedRecordIDs.get(i).toString());
              out.newLine();
            }
          }
        }

        if (numberOfFakeRecordIDs == 0) {
          out.write("");
          out.newLine();
        } else {
          for (int i = 0; i < numberOfFakeRecordIDs; i = i + 1) {
            if (i < numberOfFakeRecordIDs - 1) {
              out.write(leaf.fakeRecordIDs.get(i).toString());
              out.write(", ");
            } else {//the last Record ID
              out.write(leaf.fakeRecordIDs.get(i).toString());
              out.newLine();
            }
          }
        }

        if (numberOfPreservedRecordIDs == 0) {
          out.write("");
          out.newLine();
        } else {
          for (int i = 0; i < numberOfPreservedRecordIDs; i = i + 1) {
            numberOfTrueRecordsProcessed = numberOfTrueRecordsProcessed + 1;
            if (i < numberOfPreservedRecordIDs - 1) {
              out.write(leaf.preservedRecordIDs.get(i).toString());
              out.write(", ");
            } else {//the last Record IDs
              out.write(leaf.preservedRecordIDs.get(i).toString());
              out.newLine();
            }
          }
        }

      }
    }

    out.close();
    System.out.println("numberOfTrueRecordsProcessed: " + numberOfTrueRecordsProcessed);
  }

  public void prepareRecordIDsForLeafNodes() throws Exception {
    Iterator itr = uniformGridTreeRoots.iterator();
    while (itr.hasNext()) {
      AdaptiveTreeNode currentATTreeNode = (AdaptiveTreeNode) itr.next();
      UniformGridTree currentUGTree = currentATTreeNode.uGTree;
      currentUGTree.prepareRecordIDsForUniformGridTree();
    }
  }


  /*
   * Note: added on 2013/07/24. This method might not be used.
   */
  public boolean anyDimensionDoesNotContainIntegralPoint(
          double[] inLowerBounds, double[] inUpperBounds) {
    for (int i = 0; i < numberOfDimensions; i = i + 1) {
      Double currentLowerBound = inLowerBounds[i];
      Double currentUpperBound = inUpperBounds[i];

      double roundedLowerBound = (double) Utility.ceiling(currentLowerBound);
      double difference = currentUpperBound - (double) Utility.floor(currentUpperBound.doubleValue());
      double roundedUpperBound;
      if (difference == 0.0) {
        roundedUpperBound = (double) Utility.floor(currentUpperBound.doubleValue()) - 1.0;
      } else {
        roundedUpperBound = (double) Utility.floor(currentUpperBound.doubleValue());
      }

      if (roundedLowerBound > roundedUpperBound) {
        return true;
      }
    }
    return false;
  }

  public int numberOfIntegralValuesWithinRange(Double inLowerBound,
                                               Double inUpperBound) throws Exception {
    int rightShiftedIntegralLowerBound = Utility.ceiling(inLowerBound.doubleValue());
    int leftShiftedIntegralUpperBound = Utility.floor(inUpperBound);
    double difference = inUpperBound.doubleValue() - (double) leftShiftedIntegralUpperBound;
    int result;

    if (leftShiftedIntegralUpperBound < rightShiftedIntegralLowerBound) {
      throw new Exception("in numberOfIntegralValuesWithinRange: "
              + "leftShiftedIntegralUpperBound < rightShiftedIntegralLowerBound");
    }

    if (difference == 0.0) {
      result = leftShiftedIntegralUpperBound - rightShiftedIntegralLowerBound;
    } else {//difference > 0.0
      result = leftShiftedIntegralUpperBound - rightShiftedIntegralLowerBound + 1;
    }
    return result;
  }

  public void fillOutLeafNodesInformation() throws Exception {

    int numberOfRecords = 0;

    int numberOfUGTreeLeafNodes = 0;
    int numberOfEmptyUGTreeLeafNodes = 0;
    int numberOfNonEmptyUGTreeLeafNodes = 0;

    List<Integer> leafSizes = new ArrayList<Integer>();
    List<Integer> depths = new ArrayList<Integer>();//added on 2013/08/19

    Iterator outerItr = this.uniformGridTreeRoots.iterator();
    while (outerItr.hasNext()) {
      AdaptiveTreeNode uGTreeRoot = (AdaptiveTreeNode) outerItr.next();
      int currentDepth = uGTreeRoot.depth;//added on 2013/08/19
      UniformGridTree uGTree = uGTreeRoot.uGTree;
      Iterator innerItr = uGTree.leafNodes.iterator();
      numberOfUGTreeLeafNodes = numberOfUGTreeLeafNodes + uGTree.leafNodes.size();
      while (innerItr.hasNext()) {
        UniformGridTreeNode leaf = (UniformGridTreeNode) innerItr.next();
        numberOfRecords = numberOfRecords + leaf.records.size();

        if (leaf.records.isEmpty()) {
          numberOfEmptyUGTreeLeafNodes = numberOfEmptyUGTreeLeafNodes + 1;
        } else {
          numberOfNonEmptyUGTreeLeafNodes
                  = numberOfNonEmptyUGTreeLeafNodes + 1;
        }

        leafSizes.add(new Integer(leaf.records.size()));
        depths.add(new Integer(currentDepth + 1));
      }
    }

    if (numberOfUGTreeLeafNodes
            != (numberOfEmptyUGTreeLeafNodes + numberOfNonEmptyUGTreeLeafNodes)) {
      throw new Exception("in fillOutLeafNodesInformation, "
              + "numberOfUGTreeLeafNodes != (numberOfEmptyUGTreeLeafNodes + numberOfNonEmptyUGTreeLeafNodes)");
    }

    System.out.println("fillOutLeafNodesInformation, numberOfEmptyUGTreeLeafNodes: "
            + numberOfEmptyUGTreeLeafNodes);
    System.out.println("fillOutLeafNodesInformation, numberOfNonEmptyUGTreeLeafNodes: "
            + numberOfNonEmptyUGTreeLeafNodes);
    System.out.println("fillOutLeafNodesInformation, numberOfUGTreeLeafNodes: "
            + numberOfUGTreeLeafNodes);

    this.numberOfLeafNodes = numberOfUGTreeLeafNodes;
    this.numberOfEmptyLeafNodes = numberOfEmptyUGTreeLeafNodes;
    this.numberOfNonEmptyLeafNodes = numberOfNonEmptyUGTreeLeafNodes;

    //to see if numberOfLeafNodes is equal to numberOfUniformGridLeafNodes
    //it is a sanity check
    if (numberOfLeafNodes != numberOfUniformGridLeafNodes) {
      throw new Exception("in fillOutLeafNodesInformation: " +
              "numberOfLeafNodes != numberOfUniformGridLeafNodes");
    } else {
      //System.out.println("sanity check passed");
    }

    if (numberOfUGTreeLeafNodes != leafSizes.size()) {
      throw new Exception("in fillOutLeafNodesInformation: "
              + "numberOfUGTreeLeafNodes != leafSizes.size()");
    }
    double[] leafSizeArray = new double[numberOfUGTreeLeafNodes];
    for (int i = 0; i < numberOfUGTreeLeafNodes; i = i + 1) {
      leafSizeArray[i] = leafSizes.get(i).doubleValue();
    }
    double variance = Utility.computeSampleVariance(leafSizeArray);
    this.varianceOfLeafSize = variance;

    if (numberOfUGTreeLeafNodes != depths.size()) {
      throw new Exception("in fillOutLeafNodesInformation: "
              + "numberOfUGTreeLeafNodes != depths.size()");
    }
    double[] depthArray = new double[numberOfUGTreeLeafNodes];
    for (int i = 0; i < numberOfUGTreeLeafNodes; i = i + 1) {
      depthArray[i] = depths.get(i).doubleValue();
    }
    double avg = Utility.computeSampleAverage(depthArray);
    this.averageDepthOfLeafNodes = avg;
  }

}
