/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptivetreemethodepsdeltadp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 *
 * @author raof
 */
public class AdaptiveTreeMethodEpsDeltaDP {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {
    // TODO code application logic here

    long sourceSeed = 11;
    int numberOfParameters = 10;
    int numberOfSeedsEachParameter = 4;

    long[][] seeds
            = generateSeedsRevised(sourceSeed, numberOfParameters, numberOfSeedsEachParameter);

    double[] lowerBounds = new double[5];
    double[] upperBounds = new double[5];
    readAgreedGridInformation("agreedGridInformation.txt", lowerBounds, upperBounds);

    int[] representativeDimensions = {0, 1};//default is 0, 1

    List<Record> recordsA = readRecordsFromFile("A_transformed_9000.txt");
    List<Record> recordsB = readRecordsFromFile("B_transformed_9000.txt");

    //double[] epsilonArray = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
    //added on 2018/02/15 (Thur)
    double[] epsilonArray = {0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0};
    double[] deltaArray = {0.0001};

    double[] etaArray = {0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.10, 0.11, 0.12, 0.13};
    double[] minBudgetArray = {0.000, 0.025, 0.050, 0.075, 0.100, 0.125, 0.150, 0.175, 0.200};
    double[] betaArray = {0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45};

    double defaultEpsilon = 1.0;
    double defaultDelta = 1.0;
    double defaultEta = 0.04;
    //0.04 for decision rule 1 (exact matching) and 0.07 for decision rule 2
    double defaultMinPrivacyBudgetSpent = 0.006;//should be 0.003

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < epsilonArray.length; i = i + 1) {
    //for (int i = 6; i < 7; i = i + 1) {

      CollectedTreeMiscInformation collectedResults
              = new CollectedTreeMiscInformation(epsilonArray[i]);

      for (int j = 0; j < numberOfSeedsEachParameter; j = j + 1) {
      //for (int j = 3; j < 4; j = j + 1) {

        System.out.println("epsilon: " + epsilonArray[i]);
        TreeMiscInformation res = new TreeMiscInformation(seeds[i][j], epsilonArray[i]);

        System.out.println("seeds[" + i + "][" + j + "]: " + seeds[i][j]);

        Parameters param = new Parameters.Builder()
                .seed(seeds[i][j])
                .epsilon(epsilonArray[i])
                .delta(defaultDelta)
                .eta(defaultEta)
                .minPrivacyBudget(defaultMinPrivacyBudgetSpent)
                .lowerBounds(lowerBounds)
                .upperBounds(upperBounds)
                .recordsA(recordsA)
                .recordsB(recordsB)
                .numberOfDimensions(5)
                .representativeDimensions(representativeDimensions)
                .showingDetailsWhenDisplaying(true)
                .res(res)
                .build();

        conductExperiment(param);

        collectedResults.results.add(res);
      }
      collectedResults.computeAverage();
      collectedResults.outputResultsToFile();
    }

    long endTime = System.currentTimeMillis();
    long timeElapsed = endTime - startTime;
    System.out.println("timeElapsed: " + timeElapsed + " (ms).");

    System.out.println("successfully reach the end of program");
  }

  public static long[][] generateSeedsRevised(long inSourceSeed,
                                              int inNumberOfParameters,
                                              int inNumberOfSeedsEachParameter) throws Exception {
    long[][] seeds = new long[inNumberOfParameters][inNumberOfSeedsEachParameter];
    Random g = new Random(inSourceSeed);

    //for the first row, i.e., row 0
    for (int j = 0; j < inNumberOfSeedsEachParameter; j = j + 1) {
      seeds[0][j] = g.nextLong();
      System.out.println("seeds[0][" + j + "]: " + seeds[0][j]);
    }

    //for the rest of rows
    for (int i = 1; i < inNumberOfParameters; i = i + 1) {
      for (int j = 0; j < inNumberOfSeedsEachParameter; j = j + 1) {
        seeds[i][j] = seeds[i - 1][j];
        System.out.println("seeds[" + i + "][" + j + "]: " + seeds[i][j]);
      }
    }

    return seeds;
  }

  public static void readAgreedGridInformation(String inFileName,
                                               double[] outLowerBounds, double[] outUpperBounds)
          throws FileNotFoundException, IOException {
    BufferedReader input = new BufferedReader(new FileReader(inFileName));
    String str;
    String delimiter = "\t";
    str = input.readLine();
    int numberOfDimensions = (new Integer(str)).intValue();

    System.out.println("numberOfDimensions: " + numberOfDimensions);

    //outLowerBounds = new Double[numberOfDimensions];
    //outUpperBounds = new Double[numberOfDimensions];
    //outNumberOfIntervals = new int[numberOfDimensions];
    for (int i = 0; i < numberOfDimensions; i = i + 1) {
      str = input.readLine();
      String[] token = str.split(delimiter);
      double currentLowerBound = new Double(token[0]);
      double currentUpperBound = new Double(token[1]);
      outLowerBounds[i] = currentLowerBound;
      outUpperBounds[i] = currentUpperBound;
    }
    input.close();
  }

  public static List<Record> readRecordsFromFile(String inFileName)
          throws FileNotFoundException, IOException, Exception {
    List<Record> records = new ArrayList<>();

    BufferedReader input = new BufferedReader(new FileReader(inFileName));
    String str;
    String delimiter = ", ";

    while ((str = input.readLine()) != null) {
      String[] token = str.split(delimiter);
      int currentRID = (new Integer(token[0])).intValue();
      double[] currentRawCoordinates = new double[token.length - 1];
      for (int i = 1; i < token.length; i = i + 1) {
        currentRawCoordinates[i - 1] = new Double(token[i]);
      }
      Coordinates currentEncapsulatedCoordinates
              = new Coordinates(token.length - 1, currentRawCoordinates);
      Record currentRecord = new Record(currentRID, currentEncapsulatedCoordinates);
      records.add(currentRecord);
    }
    input.close();

    return records;
  }

  /**
   * Created on 2018/02/13
   * @param inParam
   */
  public static void conductExperiment(Parameters inParam) throws Exception {

    System.out.println("inParam.seed: " + inParam.seed);

    long seed = inParam.seed;
    double epsilon = inParam.epsilon;
    double delta = inParam.delta;
    double eta = inParam.eta;
    double minPrivacyBudgetSpent = inParam.minPrivacyBudgetSpent;
    double[] lowerBounds = inParam.lowerBounds;
    double[] upperBounds = inParam.upperBounds;
    List<Record> recordsA = inParam.recordsA;
    List<Record> recordsB = inParam.recordsB;
    int numberOfDimensions = inParam.numberOfDimensions;
    int[] representativeDimensions = inParam.representativeDimensions;
    boolean showingDetailsWhenDisplaying = inParam.showingDetailsWhenDisplaying;
    TreeMiscInformation res = inParam.res;

    System.out.println("in conductExperiment, seed: " + seed);
    Random generator = new Random(seed);

    System.out.println("recordsA.size(): " + recordsA.size());
    AdaptiveTree searchTreeA = new AdaptiveTree.Builder()
            .globalLowerBounds(lowerBounds)
            .globalUpperBounds(upperBounds)
            .records(recordsA)
            .epsilon(epsilon)
            .delta(delta)
            .eta(eta)
            .numberOfDimensions(numberOfDimensions)
            .representativeDimensions(representativeDimensions)
            .generator(generator)
            .minPrivacyBudgetSpent(minPrivacyBudgetSpent)
            .build();
    searchTreeA.buildTree();
    System.out.println("searchTreeA.numberOfUniformGridLeafNodes: "
            + searchTreeA.numberOfUniformGridLeafNodes);

    System.out.println("");
    System.out.println("recordsB.size(): " + recordsB.size());
    AdaptiveTree searchTreeB = new AdaptiveTree.Builder()
            .globalLowerBounds(lowerBounds)
            .globalUpperBounds(upperBounds)
            .records(recordsB)
            .epsilon(epsilon)
            .delta(delta)
            .eta(eta)
            .numberOfDimensions(numberOfDimensions)
            .representativeDimensions(representativeDimensions)
            .generator(generator)
            .minPrivacyBudgetSpent(minPrivacyBudgetSpent)
            .build();
    searchTreeB.buildTree();
    System.out.println("searchTreeB.numberOfUniformGridLeafNodes: "
            + searchTreeB.numberOfUniformGridLeafNodes);

    //to prepare and produce the output
    String fileNamePrefixA;
    fileNamePrefixA = "leafNodesA"
            + "_eps" + new Double(epsilon).toString()
            + "_delta" + new Double(delta).toString()
            + "_eta" + new Double(eta).toString()
            + "_minBudget" + new Double(minPrivacyBudgetSpent).toString();
    fileNamePrefixA = fileNamePrefixA + "_seed" + new Long(seed);
    searchTreeA.prepareRecordIDsForLeafNodes();
    searchTreeA.outputLeafNodesToFile(fileNamePrefixA + ".txt");

    String fileNamePrefixB;
    fileNamePrefixB = "leafNodesB"
            + "_eps" + new Double(epsilon).toString()
            + "_delta" + new Double(delta).toString()
            + "_eta" + new Double(eta).toString()
            + "_minBudget" + new Double(minPrivacyBudgetSpent).toString();
    fileNamePrefixB = fileNamePrefixB + "_seed" + new Long(seed);
    searchTreeB.prepareRecordIDsForLeafNodes();
    searchTreeB.outputLeafNodesToFile(fileNamePrefixB + ".txt");

    //
    System.out.println("");
    System.out.println("Tree A information: ");
    searchTreeA.displayLeafCounts(showingDetailsWhenDisplaying);
    searchTreeA.displyInternalNodeCounts(showingDetailsWhenDisplaying);

    int numberOfRecordsA = numberOfRecords(searchTreeA);
    System.out.println("numberOfRecordsA: " + numberOfRecordsA);

    System.out.println("");
    boolean allInBoxA = allNodesInBox(searchTreeA.uniformGridTreeRoots);
    System.out.println("allInBoxA: " + allInBoxA);

    System.out.println("");
    System.out.println("Tree B information: ");
    searchTreeB.displayLeafCounts(showingDetailsWhenDisplaying);
    searchTreeB.displyInternalNodeCounts(showingDetailsWhenDisplaying);

    int numberOfRecordsB = numberOfRecords(searchTreeB);
    System.out.println("numberOfRecordsB: " + numberOfRecordsB);

    System.out.println("");
    boolean allInBoxB = allNodesInBox(searchTreeB.uniformGridTreeRoots);
    System.out.println("allInBoxB: " + allInBoxB);

    searchTreeA.fillOutLeafNodesInformation();
    searchTreeB.fillOutLeafNodesInformation();
    res.fillOutMiscInformation(searchTreeA, searchTreeB);
  }

  public static int numberOfRecords(AdaptiveTree inTree) {

    List<AdaptiveTreeNode> uniformGridTreeRoots = inTree.uniformGridTreeRoots;
    System.out.println("uniformGridTreeRoots.size(): " + uniformGridTreeRoots.size());
    int numberOfRecords = 0;
    for (AdaptiveTreeNode currentNode : uniformGridTreeRoots) {
      numberOfRecords = numberOfRecords + currentNode.records.size();
    }
    return numberOfRecords;

  }

  public static boolean allNodesInBox(List<AdaptiveTreeNode> inNodes) {
    boolean result = true;
    int numberOfRecordsInNodes = 0;
    Iterator itr = inNodes.iterator();
    int numberOfNodesNotInBox = 0;
    int numberOfNodesInBox = 0;
    int numberOfEmptyNodesInBox = 0;
    while (itr.hasNext()) {
      AdaptiveTreeNode node = (AdaptiveTreeNode) itr.next();
      numberOfRecordsInNodes = numberOfRecordsInNodes + node.records.size();
      //leaf.displayBoundingBoxes(); System.out.println("");
      boolean allRecordsInBox = node.allRecordsInBoundingBoxes();
      if (allRecordsInBox == false) {
        System.out.println("not in box!");
        numberOfNodesNotInBox = numberOfNodesNotInBox + 1;
        result = false;
      } else {
        //System.out.println("in box.");
        numberOfNodesInBox = numberOfNodesInBox + 1;
        if (node.records.isEmpty()) {
          numberOfEmptyNodesInBox = numberOfEmptyNodesInBox + 1;
        }
      }
    }
    System.out.println("numberOfRecordsInNodes: " + numberOfRecordsInNodes);
    System.out.println("numberOfNodesNotInBox: " + numberOfNodesNotInBox);
    System.out.println("numberOfNodesInBox: " + numberOfNodesInBox);
    System.out.println("nubmerOfEmptyNodesInBox: " + numberOfEmptyNodesInBox);

    return result;
  }

}
