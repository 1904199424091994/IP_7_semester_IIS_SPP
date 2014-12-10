package Network;

import form.AppForm;

/**
 * Created by 1 on 29.11.2014.
 */
public class KohonenNeuralNetwork extends NeuralNetwork {
    protected double outputWeights[][]; //Веса нейронов на выходе
    protected double learnRate = 0.3; //Константа, используемая для настройки весов нейронов
    protected double breakError = 0.1; //Допустимый уровень ошибки. Если ошибка, рассчитываемая в ходе обучения меньше, чем значение данной константы, то алгоритм обучения сети завершается.
    protected int iterations = 10000; //Предустановленное значение итераций цикла обучения нейронной сети.
    protected double reduction = 0.99; //Величина, на которую уменьшается learnRate в конце каждой итерации обучения.  В данном случае на 1% (rate *= this.reduction)
    protected AppForm form; //Форма, пользовательского интерфейса.
    protected Trainer trainer; //Экземпляр класса, который хранит наборы для обучения нейронной сети.
    public boolean halt = false; //Обозначает, что нейронная сеть завершила свое обучение

    public KohonenNeuralNetwork(int inputCount, int outputCount, AppForm form) {
        this.totalError = 1.0;
        this.inputNeuronCount = inputCount;
        this.outputNeuronCount = outputCount;
        this.outputWeights = new double[outputNeuronCount][inputNeuronCount + 1];
        this.output = new double[outputNeuronCount];
        this.form = form;
    }

    /*Установка "тренера"*/
    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    /*Копирование весов одной сети Кохонена в другую*/
    private static void copyWeights(KohonenNeuralNetwork destinationNet, KohonenNeuralNetwork sourceNet) {
        for(int i = 0; i < sourceNet.outputWeights.length; i++)
            System.arraycopy(sourceNet.outputWeights[i], 0,
                             destinationNet.outputWeights[i], 0,
                             sourceNet.outputWeights.length);
    }

    /*Очистка весов*/
    private void clearWeights() {
        this.totalError = 1.0;
        for(int i = 0; i < outputWeights.length; i++)
            for(int j = 0; j < outputWeights[0].length; j++)
                outputWeights[i][j] = 0;
    }

    /*Нормализация ввода*/
    private void normalizeInput(double input[], double normalizationFactor[]) {
        double length;
        length = getVectorLength(input);

        if(length < 1.E-30)
            length = 1.E-30;

        normalizationFactor[0] = 1.0 / Math.sqrt(length);
    }

    /*Нормализация весов*/
    private void normalizeWeights(double[] vector) {
        double length;
        length = getVectorLength(vector);

        //для случая сверх малой суммы квадратов
        if(length < 1.E-30)
            length = 1.E-30;

        double normalizationFactor = 1.0 / Math.sqrt(length);

        for(int i = 0; i < inputNeuronCount; i++)
            vector[i] *= normalizationFactor;

        vector[inputNeuronCount] = 0;
    }

    /*Получает нейрон-победитель*/
    public int getWinner(double input[], double normalizationFactor[]) {
        int winnerIndex = 0;
        double max, outputWeightsVector[];

        normalizeInput(input, normalizationFactor);
        max = -1.E30;

        for(int i = 0; i < outputNeuronCount; i++) {
            outputWeightsVector = outputWeights[i];
            output[i] = getDotProduct(input, outputWeightsVector) * normalizationFactor[0];

            //Перевод в биполярные координаты (-1,1 ---> 0,1)
            output[i] = 0.5 * (output[i] + 1.0);

            if(output[i] > max) {
                max = output[i];
                winnerIndex = i;
            }

            //округление
            if(output[i] > 1.0)
                output[i] = 1.0;
            if(output[i] < 0.0)
                output[i] = 0.0;
        }

        return winnerIndex;
    }

    /*Оценка эффективности обучения*/
    private void estimateErrors(int wonNeurons[], double error[], double corrections[][]) throws  RuntimeException {
        int bestNeuronIndex;
        double inputSet[], normalizationFactor[] = new double[1];
        double correctionsVector[], outputWeightsVector[], length, delta;

        //сбрасываем коррекцию и счетчик побед
        for(int i = 0; i < corrections.length; i++)
            for(int j = 0; j < corrections[0].length; j++)
                corrections[i][j] = 0;

        for(int i = 0; i < wonNeurons.length; i++)
            wonNeurons[i] = 0;

        error[0] = 0.0;

        for(int trainingSet = 0; trainingSet < trainer.getTrainingSetCount(); trainingSet++) {
            inputSet = trainer.getInputSet(trainingSet);
            bestNeuronIndex = getWinner(inputSet, normalizationFactor);
            wonNeurons[bestNeuronIndex]++;
            outputWeightsVector = outputWeights[bestNeuronIndex];
            correctionsVector = corrections[bestNeuronIndex];
            length = 0.0;

            for(int i = 0; i < inputNeuronCount; i++) {
                delta = inputSet[i] * normalizationFactor[0] - outputWeightsVector[i];
                length += (delta * delta);
                correctionsVector[i] += delta;
            }

            delta = (-1) * outputWeightsVector[inputNeuronCount];
            length += (delta * delta);
            correctionsVector[inputNeuronCount] += delta;

            if(length > error[0])
                error[0] = length;
        }
        error[0] = Math.sqrt(error[0]);
    }

    /*Метод корректировки весов*/
    private void adjustWeights(double learnRate, int[] wonNeurons, double error[], double corrections[][]) {
        double delta, correctionsVector[], outputWeightsVector[], length, factor;

        error[0] = 0.0;

        for(int i = 0; i < outputNeuronCount; i++) {
            if(wonNeurons[i] == 0)
                continue;

            outputWeightsVector = outputWeights[i];
            correctionsVector = corrections[i];

            factor = 1.0 / (double)wonNeurons[i];
            factor *= learnRate;
            length = 0.0;

            for(int j = 0; j < inputNeuronCount; j++) {
                delta = factor * correctionsVector[j];
                outputWeightsVector[j] += delta;
                length += (delta * delta);
            }

            if(length > error[0])
                error[0] = length;
        }
        error[0] = Math.sqrt(error[0]) / learnRate;
    }

    /*Заставляем нейрон выиграть*/
    private void makeItWin(int wonNeurons[]) throws RuntimeException {
        int bestNeuronIndex, which = 0;
        double inputSet[], normalizationFactor[] = new double[1];
        double val, outputWeightsVector[];

        val = 1.E30;

        for(int tset = 0; tset < trainer.getTrainingSetCount(); tset++) {
            inputSet = trainer.getInputSet(tset);
            bestNeuronIndex = getWinner(inputSet, normalizationFactor);
            if(this.output[bestNeuronIndex] < val) {
                val = output[bestNeuronIndex];
                which = tset;
            }
        }

        inputSet = trainer.getInputSet(which);

        val = -1.E30;
        int i = outputNeuronCount;

        while((i--) > 0) {
            if(wonNeurons[i] != 0)
                continue;

            if(output[i] > val) {
                val = output[i];
                which = i;
            }
        }

        outputWeightsVector = outputWeights[which];
        System.arraycopy(inputSet, 0, outputWeightsVector, 0, inputSet.length);
        outputWeightsVector[inputNeuronCount] = 0.0;
        normalizeWeights(outputWeightsVector);
    }

    /*Тренирует нейронную сеть.*/
    public void train() throws RuntimeException {
        int iteration = 0;
        int wonNeurons[], winners;
        double corrections[][], learnRate, bestError, inputSet[];
        double currentError[] = new double[1];
        double error[] = new double[1];
        KohonenNeuralNetwork bestNet;

        totalError = 1.0;

        for(int tset = 0; tset < this.trainer.getTrainingSetCount(); tset++) {
            inputSet = trainer.getInputSet(tset);
            if(getVectorLength(inputSet) < 1.E-30)
                throw (new RuntimeException("Слишком маленькая длина вектора!"));
        }

        bestNet = new KohonenNeuralNetwork(inputNeuronCount, outputNeuronCount, form);
        wonNeurons = new int[outputNeuronCount];
        corrections = new double[outputNeuronCount][inputNeuronCount + 1];
        learnRate = this.learnRate;
        initialize();
        bestError = 1.e30;

        for(int i = 0;; i++) {
            estimateErrors(wonNeurons, currentError, corrections);
            totalError = currentError[0];

            if(totalError < bestError) {
                bestError = totalError;
                copyWeights(bestNet, this);
            }
            winners = 0;

            for(int j = 0; j < wonNeurons.length; j++)
                if(wonNeurons[j] != 0)
                    winners++;

            if(currentError[0] < breakError)
                break;

            if(winners < outputNeuronCount) {
                makeItWin(wonNeurons);
                continue;
            }

            adjustWeights(learnRate, wonNeurons, error, corrections);

            if(this.halt)
                break;

            Thread.yield();

            if(error[0] < 1E-5) {
                if(++iteration > iterations)
                    break;
                initialize();
                i = -1;
                learnRate = this.learnRate;
                continue;
            }

            if(learnRate > 0.01)
                learnRate *= reduction;
        }

        copyWeights(this, bestNet);

        for(int i = 0; i < outputNeuronCount; i++)
            normalizeWeights(outputWeights[i]);

        halt = true;
        iteration++;
        form.trainingLog();
    }

    /*Инициализация нейронной сети Кохонена*/
    private void initialize() {
        double vector[];

        clearWeights();
        setRandomToWeights(this.outputWeights);
        for(int i = 0; i < this.outputNeuronCount; i++) {
            vector = this.outputWeights[i];
            normalizeWeights(vector);
        }
    }
}
