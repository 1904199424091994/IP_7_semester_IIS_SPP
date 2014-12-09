package Network;

import java.util.*;

/**
 * Created by 1 on 19.11.2014.
 */
//ЭТОТ ПРОЕКТ
abstract public class NeuralNetwork {
    public final static double NEURON_ON = 0.9;
    public final static double NEURON_OFF = 0.1;
    protected double output[];
    protected double totalError;
    protected int inputNeuronCount;
    protected int outputNeuronCount;
    protected Random random = new Random(System.currentTimeMillis());

    abstract public void learn() throws RuntimeException;
    //abstract void trial(double[] input);

    public double[] getOutput() {
        return output;
    }

    /*double calculateTrialError(Trainer trainingSet) throws RuntimeException {
        int size, tset, tclass;
        double diff;

        for(int i = 0; i < trainingSet.getTrainingSetCount(); i++) {
            trial(trainingSet.getOutputSet(i));
            tclass = (int)(trainingSet.getClassify(trainingSet.getInputCount() - 1));

            for(int j = 0; j < trainingSet.getOutputCount(); j++) {
                if(tclass == j)
                    diff = NEURON_OFF - output[j];
                else
                    diff = NEURON_ON - output[j];
                totalError += (diff * diff);
            }

            for(int j = 0; j < trainingSet.getOutputCount(); j++) {
                diff = trainingSet.getOutput(i, j) - output[j];
                totalError += (diff * diff);
            }
        }

        totalError /= (double)trainingSet.getTrainingSetCount();

        return  totalError;
    }*/

    public static double getVectorLength(double vector[]) {
        double length = 0.0;
        for(int i = 0; i < vector.length; i++) {
            length += vector[i] * vector[i];
        }
        return length;
    }

    public double getDotProduct(double vec1[], double vec2[]) {
        //int whole_part, remainder, v;
        double result = 0.0;
        /*rtn = 0.0;
        whole_part = vec1.length / 4;
        remainder = vec1.length % 4;
        v = 0;

        //int i = whole_part + remainder;

        while((whole_part--) > 0) {
            rtn += (vec1[v] * vec2[v]);
            rtn += (vec1[v + 1] * vec2[v + 1]);
            rtn += (vec1[v + 2] * vec2[v + 2]);
            rtn += (vec1[v + 3] * vec2[v + 3]);
            v += 4;
        }

        while((remainder--) > 0) {
            rtn += vec1[v] * vec2[v];
            v++;
        }*/
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
