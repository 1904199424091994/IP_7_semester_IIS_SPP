package Network;

import java.util.*;

/**
 * Created by 1 on 19.11.2014.
 */
abstract public class NeuralNetwork {
    public final static double NEURON_ON = 0.9;
    public final static double NEURON_OFF = 0.1;
    protected double output[];
    protected double totalError;
    protected int inputNeuronCount;
    protected int outputNeuronCount;
    protected Random random = new Random(System.currentTimeMillis());

    abstract public void learn() throws RuntimeException;
    abstract void trial(double []input);

    double[] getOutput() {
        return output;
    }

    double calculateTrialError(Trainer trainingSet) throws RuntimeException {
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
    }

    static double vectorLength(double v[]) {
        double rtn = 0.0;
        for(int i = 0; i < v.length; i++) {
            rtn += v[i] * v[i];
        }
        return rtn;
    }

    double dotProduct(double vec1[], double vec2[]) {
        int k,m,v;
        double rtn;

        rtn = 0.0;
        k = vec1.length / 4;
        m = vec1.length % 4;
        v = 0;

        while((k--) > 0) {
            rtn += (vec1[v] * vec2[v]);
            rtn += (vec1[v + 1] * vec2[v + 1]);
            rtn += (vec1[v + 2] * vec2[v + 2]);
            rtn += (vec1[v + 3] * vec2[v + 3]);
            v += 4;
        }

        while((m--) > 0) {
            rtn += vec1[v] * vec2[v];
            v++;
        }

        return rtn;
    }

    /*Случайность весов*/
    void randomizeWeights(double weight[][]) {
        double r;
        double sqrt12 = 3.464101615;
        int tmp = (int)(sqrt12 / (2.0 * Math.random()));

        for(int y = 0; y < weight.length; y++) {
            for(int x = 0; x < weight.length; x++) {
                r = (double) random.nextInt(Integer.MAX_VALUE) +
                    (double) random.nextInt(Integer.MAX_VALUE) -
                    (double) random.nextInt(Integer.MAX_VALUE) -
                    (double) random.nextInt(Integer.MAX_VALUE);
                weight[y][x] = tmp * r;
            }
        }
    }
}
