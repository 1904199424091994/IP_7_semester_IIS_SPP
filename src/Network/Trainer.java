package Network;

/**
 * Created by 1 on 19.11.2014.
 */
public class Trainer {
    private int inputCount;
    private int outputCount;
    private double[][] input;
    private double[][] output;
    private double[] classify;
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
        classify = new double[trainingSetCount];
    }

    public int getTrainingSetCount() {
        return trainingSetCount;
    }

    //Самое интересное
    public void setInput(int set, int index, double value) throws RuntimeException
    {
        if((set < 0) || (set >= trainingSetCount)) {
            throw (new RuntimeException("Training set is out of range:" + set));
        }
        if((index < 0) || (index >= inputCount)) {
            throw (new RuntimeException("Training input index out of range:" + index));
        }
        input[set][index] = value;
    }

    public void setOutput(int set,int index,double value)
            throws RuntimeException
    {
        if ( (set<0) || (set>=trainingSetCount) )
            throw(new RuntimeException("Training set out of range:" + set ));
        if ( (index<0) || (set>=outputCount) )
            throw(new RuntimeException("Training input index out of range:" + index ));
        output[set][index] = value;
    }

    public void setClassify(int set,double value)
            throws RuntimeException
    {
        if ( (set<0) || (set>=trainingSetCount) )
            throw(new RuntimeException("Training set out of range:" + set ));
        classify[set] = value;
    }



    public double getInput(int set,int index)
            throws RuntimeException
    {
        if ( (set<0) || (set>=trainingSetCount) )
            throw(new RuntimeException("Training set out of range:" + set ));
        if ( (index<0) || (index>=inputCount) )
            throw(new RuntimeException("Training input index out of range:" + index ));
        return input[set][index];
    }


    public double getOutput(int set,int index)
            throws RuntimeException
    {
        if ( (set<0) || (set>=trainingSetCount) )
            throw(new RuntimeException("Training set out of range:" + set ));
        if ( (index<0) || (set>=outputCount) )
            throw(new RuntimeException("Training input index out of range:" + index ));
        return output[set][index];
    }


    public double getClassify(int set)
            throws RuntimeException
    {
        if ( (set<0) || (set>=trainingSetCount) )
            throw(new RuntimeException("Training set out of range:" + set ));
        return classify[set];
    }

    public double[] getOutputSet(int set)
            throws RuntimeException
    {
        if ( (set<0) || (set>=trainingSetCount) )
            throw(new RuntimeException("Training set out of range:" + set ));
        return output[set];
    }


    double []getInputSet(int set)
            throws RuntimeException
    {
        if ( (set<0) || (set>=trainingSetCount) )
            throw(new RuntimeException("Training set out of range:" + set ));
        return input[set];
    }

    void calculateClass(int c) {
        for(int i = 0; i <= trainingSetCount; i++) {
            classify[i] = c + 0.1;
        }
    }
}
