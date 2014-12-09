package Network;

/**
 * Created by 1 on 19.11.2014.
 */
public class Trainer {
    private int inputCount;
    private int outputCount;
    private double[][] input;
    private double[][] output;
    //private double[] classify;
    private int trainingSetCount;

    public Trainer(int inputCount, int outputCount) {
        this.inputCount = inputCount;
        this.outputCount = outputCount;
        trainingSetCount = 0;
    }

    public int getInputCount() {
        return inputCount;
    }

    public int getOutputCount() {
        return outputCount;
    }

    public void setTrainingSetCount(int trainingSetCount) {
        this.trainingSetCount = trainingSetCount;
        input = new double[trainingSetCount][inputCount];
        output = new double[trainingSetCount][outputCount];
        //classify = new double[trainingSetCount];
    }

    public int getTrainingSetCount() {
        return trainingSetCount;
    }

    //Установка i,j значения Grid-a в input
    public void setInput(int set, int index, double value) throws RuntimeException
    {
        if((set < 0) || (set >= trainingSetCount))
            throw (new RuntimeException("Параметр set находится вне массива:" + set));
        if((index < 0) || (index >= inputCount))
            throw (new RuntimeException("Параметр index находится вне массива:" + index));

        input[set][index] = value;
    }

    public void setOutput(int set,int index,double value)
            throws RuntimeException
    {
        if ((set < 0) || (set >= trainingSetCount))
            throw(new RuntimeException("Параметр set находится вне массива:" + set ));
        if ((index < 0) || (set >= outputCount))
            throw(new RuntimeException("Параметр index находится вне массива:" + index ));
        output[set][index] = value;
    }

    /*public void setClassify(int set,double value)
            throws RuntimeException
    {
        if ((set<0) || (set>=trainingSetCount))
            throw(new RuntimeException("Параметр set находится вне массива:" + set ));
        //classify[set] = value;
    }*/



    public double getInput(int set,int index)
            throws RuntimeException
    {
        if ((set < 0) || (set >= trainingSetCount))
            throw(new RuntimeException("Параметр set находится вне массива:" + set ));
        if ((index < 0) || (index >= inputCount))
            throw(new RuntimeException("Параметр index находится вне массива:" + index ));
        return input[set][index];
    }


    public double getOutput(int set,int index)
            throws RuntimeException
    {
        if ((set < 0) || (set >= trainingSetCount))
            throw(new RuntimeException("Параметр set находится вне массива:" + set ));
        if ((index < 0) || (set >= outputCount))
            throw(new RuntimeException("Параметр index находится вне массива:" + index ));
        return output[set][index];
    }


    /*public double getClassify(int set)
            throws RuntimeException
    {
        if ((set < 0) || (set >= trainingSetCount))
            throw(new RuntimeException("Параметр set находится вне массива:" + set ));
        return classify[set];
    }*/

    public double[] getOutputSet(int set)
            throws RuntimeException
    {
        if ((set < 0) || (set >= trainingSetCount))
            throw(new RuntimeException("Параметр set находится вне массива:" + set ));
        return output[set];
    }


    public double[] getInputSet(int set)
            throws RuntimeException
    {
        if ((set < 0) || (set >= trainingSetCount))
            throw(new RuntimeException("Параметр set находится вне массива:" + set ));
        return input[set];
    }
}
