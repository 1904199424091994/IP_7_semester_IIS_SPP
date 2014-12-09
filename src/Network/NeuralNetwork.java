package Network;

import java.util.*;

/**
 * Created by 1 on 19.11.2014.
 */
abstract public class NeuralNetwork {
    protected double output[];
    protected double totalError;
    protected int inputNeuronCount;
    protected int outputNeuronCount;
    protected Random random = new Random(System.currentTimeMillis());

    abstract public void train() throws RuntimeException;

    public double[] getOutput() {
        return output;
    }

    public static double getVectorLength(double vector[]) {
        double length = 0.0;
        for(int i = 0; i < vector.length; i++) {
            length += vector[i] * vector[i];
        }
        return length;
    }

    public double getDotProduct(double vec1[], double vec2[]) {
        double result = 0.0;

        for(int i = 0; i < vec1.length; i++) {
            result += (vec1[i] * vec2[i]);
        }
        return result;
    }

    /*Задает случайным образом выходные веса*/
    public void setRandomToWeights(double outputWeights[][]) {
        double r;
        int tmp = (int)(Math.sqrt(12) / (2.0 * Math.random() * (1 - 1.E-30) + 1.E-30));

        for(int i = 0; i < outputWeights.length; i++) {
            for(int j = 0; j < outputWeights.length; j++) {
                r = (double) random.nextInt(Integer.MAX_VALUE) +
                    (double) random.nextInt(Integer.MAX_VALUE) -
                    (double) random.nextInt(Integer.MAX_VALUE) -
                    (double) random.nextInt(Integer.MAX_VALUE);
                outputWeights[i][j] = tmp * r;
            }
        }
    }
}
