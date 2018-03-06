package adaptivetreemethodepsdeltadp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parameters {

  double epsilon;
  double delta;
  double eta;

  //the following are the required fields for conductExperiment
  long seed;
  double minPrivacyBudgetSpent;
  double beta;//will be used in a different from when (eps, delta)-DPRL is applied
  double[] lowerBounds;
  double[] upperBounds;
  List<Record> recordsA;
  List<Record> recordsB;
  int numberOfDimensions;
  int[] representativeDimensions;
  boolean showingDetailsWhenDisplaying;
  TreeMiscInformation res;

  public static class Builder {

    private double epsilon;
    private double delta;
    private double eta;

    private long seed;
    private double minPrivacyBudgetSpent;
    private double beta;//will be used in a different from when (eps, delta)-DPRL is applied
    private double[] lowerBounds;
    private double[] upperBounds;
    private List<Record> recordsA;
    private List<Record> recordsB;
    private int numberOfDimensions;
    private int[] representativeDimensions;
    private boolean showingDetailsWhenDisplaying;
    private TreeMiscInformation res;

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

    public Builder seed(long inSeed) {
      seed = inSeed;
      return this;
    }

    public Builder minPrivacyBudget(double inMinPrivacyBudget) {
      minPrivacyBudgetSpent = inMinPrivacyBudget;
      return this;
    }

    public Builder beta(double inBeta) {
      beta = inBeta;
      return this;
    }

    public Builder lowerBounds(double[] inLowerBounds) {
      lowerBounds = Arrays.copyOf(inLowerBounds, inLowerBounds.length);
      return this;
    }

    public Builder upperBounds(double[] inUpperBounds) {
      upperBounds = Arrays.copyOf(inUpperBounds, inUpperBounds.length);
      return this;
    }

    public Builder recordsA(List<Record> inRecordsA) {
      recordsA = new ArrayList<>();
      recordsA.addAll(inRecordsA);
      return this;
    }

    public Builder recordsB(List<Record> inRecordsB) {
      recordsB = new ArrayList<>();
      recordsB.addAll(inRecordsB);
      return this;
    }

    public Builder numberOfDimensions(int inNumberOfDimensions) {
      numberOfDimensions = inNumberOfDimensions;
      return this;
    }

    public Builder representativeDimensions(int[] inRepresentativeDimensions) {
      representativeDimensions
              = Arrays.copyOf(inRepresentativeDimensions, inRepresentativeDimensions.length);
      return this;
    }

    public Builder showingDetailsWhenDisplaying(boolean inShowingDetailsWhenDisplaying) {
      showingDetailsWhenDisplaying = inShowingDetailsWhenDisplaying;
      return this;
    }

    //to check later
    public Builder res(TreeMiscInformation inRes) {
      res = inRes;
      return this;
    }

    public Parameters build() {
      return new Parameters(this);
    }

  }

  public Parameters(Builder inBuilder) {

    epsilon = inBuilder.epsilon;
    delta = inBuilder.delta;
    eta = inBuilder.eta;

    seed = inBuilder.seed;
    minPrivacyBudgetSpent = inBuilder.minPrivacyBudgetSpent;
    beta = inBuilder.beta;//will be used in a different from when (eps, delta)-DPRL is applied
    lowerBounds = inBuilder.lowerBounds;
    upperBounds = inBuilder.upperBounds;
    recordsA = inBuilder.recordsA;
    recordsB = inBuilder.recordsB;
    numberOfDimensions = inBuilder.numberOfDimensions;
    representativeDimensions = inBuilder.representativeDimensions;
    showingDetailsWhenDisplaying = inBuilder.showingDetailsWhenDisplaying;
    res = inBuilder.res;

  }

}
