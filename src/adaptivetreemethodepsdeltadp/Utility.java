package adaptivetreemethodepsdeltadp;

import java.util.Random;

/**
 *
 * @author raof
 */
public class Utility {
  public static double naturalLogTwo = Math.log(2);

  public static double LaplaceRandom(double inMean, double inLambda, Random inGenerator) {
    double u = inGenerator.nextDouble();
    double randomNumber;
    if (u >= 0.5) {
      randomNumber = inLambda * (-1 * (Math.log(1 - u)) + -1 * (naturalLogTwo)) + inMean;
    } else {//the case when u < 0.5
      randomNumber = inLambda * ((naturalLogTwo) + Math.log(u)) + inMean;
    }
    return randomNumber;
  }

  public static double computeSampleAverage(double[] inArray) throws Exception {
    if ((inArray.length == 0) || inArray == null) {
      throw new Exception("in computeAverage: (inArray.length == 0) || inArray == null");
    }

    double sum = 0;
    for (int i = 0; i < inArray.length; i = i + 1) {
      double currentValue = inArray[i];
      sum = sum + currentValue;
    }
    return (sum / inArray.length);
  }

  public static double computeSampleVariance(double[] inArray) throws Exception {
    if ((inArray.length == 0) || (inArray.length == 1)
            || inArray == null) {
      if (inArray.length == 0) {
        throw new Exception("in computeVariance: (inArray.length == 0)");
      } else if (inArray.length == 1) {
        throw new Exception("in computeVariance: (inArray.length == 1)");
      } else {
        throw new Exception("in computeVariance: (inArray == null)");
      }
			/* throw new Exception("in computeVariance: (inArray.length == 0) || "
							+ "(inArray.length == 1) || inArray == null"); */
    }

    double sampleMean = computeSampleAverage(inArray);
    double sum = 0;
    double sampleVariance;
    for (int i = 0; i < inArray.length; i = i + 1) {
      sum = sum + (inArray[i] - sampleMean) * (inArray[i] - sampleMean);
    }
    sampleVariance = sum / (inArray.length - 1);
    return sampleVariance;
  }

  public static int ceiling(double inNumber) {
    if (inNumber >= 0) {
      int truncatedNumber = (int) inNumber;
      if (inNumber - (double) truncatedNumber != 0) {
        return (truncatedNumber + 1);
      } else {//inNumber is an integer
        return (int) inNumber;
      }
    } else {
      int truncatedNumber = (int) inNumber;
      return truncatedNumber;
    }
  }

  public static int floor(double inNumber) {
    if (inNumber >= 0) {
      return ((int) inNumber);
    } else {
      int truncatedNumber = (int) inNumber;
      if (inNumber - (double) truncatedNumber != 0) {
        return (truncatedNumber - 1);
      } else {//inNumber is an integer
        return (int) inNumber;
      }
    }
  }

  public static int mod(int inDividend, int inDivisor) throws Exception {
    if (inDivisor <= 0) {
      throw new Exception("inDivisor <= 0.");
    }

    if ((inDividend % inDivisor == 0) || (inDividend == 0)) {
      return 0;
    }

    int remainder;
    int quotient;
    if (inDividend > 0) {
      quotient = inDividend / inDivisor;
      remainder = inDividend - quotient * inDivisor;
    } else {//the case when inDividend < 0
      quotient = (inDividend / inDivisor) - 1;
      remainder = inDividend - quotient * inDivisor;
    }

    return remainder;
  }

  public static double abs(double inNumber) {
    if (inNumber >= 0.0) {
      return inNumber;
    } else {
      return (-1 * inNumber);
    }
  }
}
