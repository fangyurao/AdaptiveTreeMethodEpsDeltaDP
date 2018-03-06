package adaptivetreemethodepsdeltadp;

/**
 *  Fang-Yu on 2018/02/13: also need to add a Builder inner class for this class
 *
 * @author raof
 */
public class TreeMiscInformation {

  long seedUsed;
  int integralParameter;
  double doubleParameter;

  int numberOfLeafNodesA;
  int numberOfEmptyLeafNodesA;
  int numberOfNonEmptyLeafNodesA;
  double varianceOfLeafSizeA;
  double averageDepthOfLeafNodesA;

  int numberOfLeafNodesB;
  int numberOfEmptyLeafNodesB;
  int numberOfNonEmptyLeafNodesB;
  double varianceOfLeafSizeB;
  double averageDepthOfLeafNodesB;

  public TreeMiscInformation(long inSeed, int inIntegralParameter) {
    seedUsed = inSeed;
    integralParameter = inIntegralParameter;
    doubleParameter = Double.NaN;
  }

  public TreeMiscInformation(long inSeed, double inDoubleParameter) {
    seedUsed = inSeed;
    integralParameter = -1;
    doubleParameter = inDoubleParameter;
  }

  public void fillOutMiscInformation(AdaptiveTree inSearchTreeA, AdaptiveTree inSearchTreeB) {

    numberOfLeafNodesA = inSearchTreeA.numberOfLeafNodes;
    numberOfEmptyLeafNodesA = inSearchTreeA.numberOfEmptyLeafNodes;
    numberOfNonEmptyLeafNodesA = inSearchTreeA.numberOfNonEmptyLeafNodes;
    varianceOfLeafSizeA = inSearchTreeA.varianceOfLeafSize;
    averageDepthOfLeafNodesA = inSearchTreeA.averageDepthOfLeafNodes;

    numberOfLeafNodesB = inSearchTreeB.numberOfLeafNodes;
    numberOfEmptyLeafNodesB = inSearchTreeB.numberOfEmptyLeafNodes;
    numberOfNonEmptyLeafNodesB = inSearchTreeB.numberOfNonEmptyLeafNodes;
    varianceOfLeafSizeB = inSearchTreeB.varianceOfLeafSize;
    averageDepthOfLeafNodesB = inSearchTreeB.averageDepthOfLeafNodes;
  }

}
