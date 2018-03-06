package adaptivetreemethodepsdeltadp;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package adaptivetreemethod;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author raof
 */
public class CollectedTreeMiscInformation {

  double parameter;
  List<TreeMiscInformation> results;

  double averageNumberOfLeafNodesA;
  double averageNumberOfEmptyLeafNodesA;
  double averageNumberOfNonEmptyLeafNodesA;
  double averageVarianceOfLeafSizeA;
  double avgAverageDepthOfLeafNodesA;

  double averageNumberOfLeafNodesB;
  double averageNumberOfEmptyLeafNodesB;
  double averageNumberOfNonEmptyLeafNodesB;
  double averageVarianceOfLeafSizeB;
  double avgAverageDepthOfLeafNodesB;

  double averageRateOfEmptyLeafNodesA;
  double averageRateOfEmptyLeafNodesB;

  public CollectedTreeMiscInformation(double inParameter) {
    parameter = inParameter;
    results = new ArrayList<TreeMiscInformation>();

    averageNumberOfLeafNodesA = Double.NaN;
    averageNumberOfEmptyLeafNodesA = Double.NaN;
    averageNumberOfNonEmptyLeafNodesA = Double.NaN;
    averageVarianceOfLeafSizeA = Double.NaN;
    avgAverageDepthOfLeafNodesA = Double.NaN;

    averageNumberOfLeafNodesB = Double.NaN;
    averageNumberOfEmptyLeafNodesB = Double.NaN;
    averageNumberOfNonEmptyLeafNodesB = Double.NaN;
    averageVarianceOfLeafSizeB = Double.NaN;
    avgAverageDepthOfLeafNodesB = Double.NaN;

    averageRateOfEmptyLeafNodesA = Double.NaN;
    averageRateOfEmptyLeafNodesB = Double.NaN;
  }

  public void computeAverage() throws Exception {
    if (results.isEmpty()) {
      throw new Exception("in computeAverage, results is empty.");
    }

    double sumOfNumberOfLeafNodesA = 0.0;
    double sumOfNumberOfEmptyLeafNodesA = 0.0;
    double sumOfNumberOfNonEmptyLeafNodesA = 0.0;
    double sumOfVarianceOfLeafSizeA = 0.0;
    double sumOfAverageDepthOfLeafNodesA = 0.0;

    double sumOfNumberOfLeafNodesB = 0.0;
    double sumOfNumberOfEmptyLeafNodesB = 0.0;
    double sumOfNumberOfNonEmptyLeafNodesB = 0.0;
    double sumOfVarianceOfLeafSizeB = 0.0;
    double sumOfAverageDepthOfLeafNodesB = 0.0;

    Iterator itr = results.iterator();
    int numberOfExperiments = results.size();

    while (itr.hasNext()) {
      TreeMiscInformation res = (TreeMiscInformation) itr.next();

      //do the summation
      sumOfNumberOfLeafNodesA = sumOfNumberOfLeafNodesA + (double) res.numberOfLeafNodesA;
      sumOfNumberOfEmptyLeafNodesA = sumOfNumberOfEmptyLeafNodesA
              + (double) res.numberOfEmptyLeafNodesA;
      sumOfNumberOfNonEmptyLeafNodesA = sumOfNumberOfNonEmptyLeafNodesA
              + (double) res.numberOfNonEmptyLeafNodesA;
      sumOfVarianceOfLeafSizeA = sumOfVarianceOfLeafSizeA
              + res.varianceOfLeafSizeA;
      sumOfAverageDepthOfLeafNodesA = sumOfAverageDepthOfLeafNodesA
              + res.averageDepthOfLeafNodesA;

      sumOfNumberOfLeafNodesB = sumOfNumberOfLeafNodesB + (double) res.numberOfLeafNodesB;
      sumOfNumberOfEmptyLeafNodesB = sumOfNumberOfEmptyLeafNodesB
              + (double) res.numberOfEmptyLeafNodesB;
      sumOfNumberOfNonEmptyLeafNodesB = sumOfNumberOfNonEmptyLeafNodesB
              + (double) res.numberOfNonEmptyLeafNodesB;
      sumOfVarianceOfLeafSizeB = sumOfVarianceOfLeafSizeB
              + res.varianceOfLeafSizeB;
      sumOfAverageDepthOfLeafNodesB = sumOfAverageDepthOfLeafNodesB
              + res.averageDepthOfLeafNodesB;
    }

    //compute the avg
    averageNumberOfLeafNodesA
            = sumOfNumberOfLeafNodesA / (double) numberOfExperiments;
    averageNumberOfEmptyLeafNodesA
            = sumOfNumberOfEmptyLeafNodesA / (double) numberOfExperiments;
    averageNumberOfNonEmptyLeafNodesA
            = sumOfNumberOfNonEmptyLeafNodesA / (double) numberOfExperiments;
    averageVarianceOfLeafSizeA
            = sumOfVarianceOfLeafSizeA / (double) numberOfExperiments;
    avgAverageDepthOfLeafNodesA
            = sumOfAverageDepthOfLeafNodesA / (double) numberOfExperiments;

    averageNumberOfLeafNodesB
            = sumOfNumberOfLeafNodesB / (double) numberOfExperiments;
    averageNumberOfEmptyLeafNodesB
            = sumOfNumberOfEmptyLeafNodesB / (double) numberOfExperiments;
    averageNumberOfNonEmptyLeafNodesB
            = sumOfNumberOfNonEmptyLeafNodesB / (double) numberOfExperiments;
    averageVarianceOfLeafSizeB
            = sumOfVarianceOfLeafSizeB / (double) numberOfExperiments;
    avgAverageDepthOfLeafNodesB
            = sumOfAverageDepthOfLeafNodesB / (double) numberOfExperiments;

    averageRateOfEmptyLeafNodesA = averageNumberOfEmptyLeafNodesA / averageNumberOfLeafNodesA;
    averageRateOfEmptyLeafNodesB = averageNumberOfEmptyLeafNodesB / averageNumberOfLeafNodesB;
  }

  public void outputResultsToFile() throws IOException {
    FileWriter fstreamA = new FileWriter("treeAMisc.txt", true);
    BufferedWriter outA = new BufferedWriter(fstreamA);

    outA.write(new Double(parameter).toString() + "\t"
            + new Double(averageNumberOfEmptyLeafNodesA).toString() + "\t"
            + new Double(averageNumberOfLeafNodesA).toString() + "\t"
            + new Double(averageRateOfEmptyLeafNodesA).toString() + "\t"
            + new Double(averageVarianceOfLeafSizeA).toString() + "\t"
            + new Double(avgAverageDepthOfLeafNodesA).toString());
    outA.newLine();
    outA.close();

    FileWriter fstreamB = new FileWriter("treeBMisc.txt", true);
    BufferedWriter outB = new BufferedWriter(fstreamB);
    outB.write(new Double(parameter).toString() + "\t"
            + new Double(averageNumberOfEmptyLeafNodesB).toString() + "\t"
            + new Double(averageNumberOfLeafNodesB).toString() + "\t"
            + new Double(averageRateOfEmptyLeafNodesB).toString() + "\t"
            + new Double(averageVarianceOfLeafSizeB).toString() + "\t"
            + new Double(avgAverageDepthOfLeafNodesB).toString());
    outB.newLine();
    outB.close();

  }
}
