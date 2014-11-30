package Network;

import form.AppForm;
import form.InputImage;

/**
 * Created by 1 on 29.11.2014.
 */
public class KohonenNeuralNetwork extends NeuralNetwork {
    //веса выходных нейронов, рассчитывающиеся по весам входных нейронов
    double outputWeights[][];
    //метод тренировки
    protected int learnMethod = 1;
    //коэффициент обучения
    protected double learnRate = 0.3;
    //прерываем, если ошибка за пределами данного лимита
    protected double quitError = 0.1;
    //число циклов алгоритма перед выходом
    protected int retries = 10000;
    //фактор упрощения
    protected double reduction = 0.99;
    //связка с формой
    protected AppForm owner;
    //Тренер
    protected Trainer trainer;
    //Прерывает тренировку
    public boolean halt = false;
    /*
    *  Конструктор класса KohonenNeuralNetwork
    *  @inputCount --- число нейронов на входе
    *  @outputCount --- число нейронов на выходе
    *  @owner --- связка с формой
    * */
    public KohonenNeuralNetwork(int inputCount, int outputCount, AppForm owner) {
        this.totalError = 1.0;
        this.inputNeuronCount = inputCount;
        this.outputNeuronCount = outputCount;
        this.outputWeights = new double[outputNeuronCount][inputNeuronCount + 1];
        this.output = new double[outputNeuronCount];
        this.owner = owner;
    }

    /*Установка "тренера"*/
    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    /*Копирование весов одной сети Кохонена в другую*/
    public static void copyWeights(KohonenNeuralNetwork destinationNet, KohonenNeuralNetwork sourceNet) {
        for(int i = 0; i < sourceNet.outputWeights.length; i++) {
            System.arraycopy(sourceNet.outputWeights[i], 0,
                             destinationNet.outputWeights[i], 0,
                             sourceNet.outputWeights.length);
        }
    }

    /*Очистка весов*/
    public void clearWeights() {
        this.totalError = 1.0;
        for(int y = 0; y < outputWeights.length; y++) {
            for(int x = 0; x < outputWeights[0].length; x++) {
                outputWeights[y][x] = 0;
            }
        }
    }

    /*
    * Нормализация ввода
    * @input - входной массив представления рисунка
    * @normfac - результат
    * @synth - последний искусственный ввод
    * */
    void normalizeInput(final double input[], double normfac[], double synth[]) {
        double length, d;
        length = vectorLength(input);

        //для случая очень малой длины (удалить наверное)
        if(length < 1.E-30)
            length = 1.E-30;

        normfac[0] = 1.0 / Math.sqrt(length);
        synth[0] = 0.0;
    }

    /*
    * Нормализация весов
    *
    * @weights - массив входных весов
    * */
    void normalizeWeight(double[] weights) {
        double length;
        length = vectorLength(weights);

        //для случая очень малой длины (удалить наверное)
        if(length < 1.E-30)
            length = 1.E-30;

        length = 1.0 / Math.sqrt(length);
        for(int i = 0; i < this.inputNeuronCount; i++)
            weights[i] *= length;
        weights[this.inputNeuronCount] = 0;
    }

    /*
    * Испытание входного рисунка изображения
    *
    * @input - входное изображение
    * */
    void trial(double input[]) {
        double normfac[] = new double[1],
               synth[] = new double[1],
               optr[];

        normalizeInput(input, normfac, synth);

        for(int i = 0; i < this.outputNeuronCount; i++) {
            optr = this.outputWeights[i];
            this.output[i] = dotProduct(input, optr) * normfac[0]
                             + synth[0] * optr[this.inputNeuronCount];

            //ремап к биполярным (-1,1 к 0,1)
            this.output[i] = 0.5 * (output[i] + 1.0);
            //округление
            if(this.output[i] > 1.0)
                this.output[i] = 1.0;
            if(this.output[i] < 0.0)
                this.output[i] = 0.0;
        }
    }

    /*
    * На основе входных данных получает выигравший нейрон
    *
    * @input --- входные данные
    * @normfac --- результат
    * @synth --- последний ввод ???
    *
    * @return --- выигравший нейрон
    * */
    public int getWinner(double input[], double normfac[], double synth[]) {
        int win = 0;
        double biggest, optr[];

        normalizeInput(input, normfac, synth);
        biggest = -1.E30;

        for(int i = 0; i < this.outputNeuronCount; i++) {
            optr = outputWeights[i];
            this.output[i] = dotProduct(input, optr) * normfac[0]
                             + synth[0] * optr[this.inputNeuronCount];

            //К биполярным (-1,1 ---> 0,1)
            this.output[i] = 0.5 * (this.output[i] + 1.0);

            if(this.output[i] > biggest) {
                biggest = this.output[i];
                win = i;
            }

            //округление
            if(this.output[i] > 1.0)
                this.output[i] = 1.0;
            if(this.output[i] < 0.0)
                this.output[i] = 0.0;
        }

        return win;
    }

    /*
    * Один из самых важных методов для тренировки нейронной сети.
    * Метод оценивает веса в соответствии с "тренером"
    *
    * @rate --- коэффициент обучения
    * @learnMethod --- метод обучения
    * @won --- учитывает сколько раз данный нейрон выигрывает
    * @errors --- для возвращения ошибки
    * @corrections --- для возвращения коррекции
    * @work --- область работ
    * */

    void evaluateErrors(double rate, int learnMethod, int won[],
                        double errors[], double corrections[][],
                        double work[]) throws  RuntimeException {
        int best, size, tset;
        double dptr[], normfac[] = new double[1];
        double synth[] = new double[1], cptr[], wptr[], length, diff;

        //сбрасываем коррекцию и счетчик побед
        for(int y = 0; y < corrections.length; y++) {
            for(int x = 0; x < corrections[0].length; x++) {
                corrections[y][x] = 0;
            }
        }

        for(int i = 0; i < won.length; i++) {
            won[i] = 0;
        }

        errors[0] = 0.0;

        //бежим через всех "тренеров" чтобы определить коррекцию
        for(tset = 0; tset < this.trainer.getTrainingSetCount(); tset++) {
            dptr = this.trainer.getInputSet(tset);
            best = getWinner(dptr, normfac, synth);
            won[best]++;
            wptr = this.outputWeights[best];
            cptr = corrections[best];
            length = 0.0;

            for(int i = 0; i < this.inputNeuronCount; i++) {
                diff = dptr[i] * normfac[0] - wptr[i];
                length += (diff *diff);
                if(learnMethod != 0)
                    cptr[i] += diff;
                else
                    work[i] = rate * dptr[i] * normfac[0] + wptr[i];
            }

            diff = synth[0] - wptr[this.inputNeuronCount];
            length += (diff * diff);

            if(learnMethod != 0)
                cptr[this.inputNeuronCount] += diff;
            else
                work[this.inputNeuronCount] = rate * synth[0] + wptr[this.inputNeuronCount];

            if(length > errors[0])
                errors[0] = length;

            if(learnMethod == 0) {
                normalizeWeight(work);
                for(int i = 0; i < this.inputNeuronCount; i++)
                    cptr[i] += work[i] - wptr[i];
            }
        }
        errors[0] = Math.sqrt(errors[0]);
    }

    /*
    * Метод вызывается в конце тренировки
    * Данный метод приводит в порядок веса, основанные на тренировке
    *
    * @rate --- коэффициент обучения
    * @learnMethod --- (0 - сложение, 1 = вычитания)
    * @won --- счетчик учета побед каждым нейроном
    * @errors --- ошибки
    * @corrections --- коррекции
    * */
    void adjustWeights(double rate, int learnMethod,
                       int[] won, double errors[],
                       double corrections[][]) {
        double corr, cptr[], wptr[], length, f;

        errors[0] = 0.0;

        for(int i = 0; i < this.outputNeuronCount; i++) {
            //пропускаем ни разу не выигравшие нейроны
            if(won[i] == 0)
                continue;

            wptr = this.outputWeights[i];
            cptr = corrections[i];

            f = 1.0 / (double)won[i];

            if(learnMethod != 0)
                f *= rate;

            length = 0.0;

            for(int j = 0; j < this.inputNeuronCount; j++) {
                corr = f * cptr[j];
                wptr[j] += corr;
                length += (corr * corr);
            }

            if(length > errors[0])
                errors[0] = length;
        }
        //масштабируем коррекцию
        errors[0] = Math.sqrt(errors[0]) / rate;
    }

    /*
    * Если среди нейронов нет победителей, то делаем победителя!
    *
    * @won - счетчик побед нейронов
    * */
    void forceWin(int won[]) throws RuntimeException {
        int best, size, which = 0;
        double dptr[], normfac[] = new double[1];
        double synth[] = new double[1], dist, optr[];

        size = this.inputNeuronCount + 1;
        dist = 1.E30;

        for(int tset = 0; tset < this.trainer.getTrainingSetCount(); tset++) {
            dptr = this.trainer.getInputSet(tset);
            best = getWinner(dptr, normfac, synth);
            if(this.output[best] < dist) {
                dist = output[best];
                which = tset;
            }
        }

        dptr = this.trainer.getInputSet(which);
        best = getWinner(dptr, normfac, synth);

        dist = -1.E30;
        int i = this.outputNeuronCount;

        while((i--) > 0) {
            if(won[i] != 0)
                continue;
            if(this.output[i] > dist) {
                dist = this.output[i];
                which = i;
            }
        }

        optr = this.outputWeights[which];
        System.arraycopy(dptr, 0, optr, 0, dptr.length);
        optr[this.inputNeuronCount] = synth[0] / normfac[0];
        normalizeWeight(optr);
    }

    /*
    * Данный метод тренирует нейронную сеть. Может очень долго работать,
    * но отображает прогресс работы в owner-форму
    *
    * */
    public void learn() throws RuntimeException {
        int key, iter, n_retry, nwts;
        int won[], winners;
        double work[], corrections[][], rate, best_err, dptr[];
        double bigerr[] = new double[1];
        double bigcorr[] = new double[1];
        KohonenNeuralNetwork bestNet; //сохраняем лучшую сюда

        this.totalError = 1.0;

        for(int tset = 0; tset < this.trainer.getTrainingSetCount(); tset++) {
            dptr = this.trainer.getInputSet(tset);
            if(vectorLength(dptr) < 1.E-30)
                throw (new RuntimeException("Multiplicative normalization has null training case"));
        }

        bestNet = new KohonenNeuralNetwork(this.inputNeuronCount, this.outputNeuronCount, this.owner);

        won = new int[this.outputNeuronCount];
        corrections = new double[this.outputNeuronCount][this.inputNeuronCount + 1];

        if(learnMethod == 0)
            work = new double[inputNeuronCount + 1];
        else
            work = null;

        rate = this.learnRate;

        initialize();

        best_err = 1.e30;
        n_retry = 0;

        for(iter = 0;; iter++) {
            evaluateErrors(rate, this.learnMethod, won, bigerr,
                           corrections, work);
            this.totalError = bigerr[0];

            if(this.totalError < best_err) {
                best_err = this.totalError;
                copyWeights(bestNet, this);
            }

            winners = 0;

            for(int i = 0; i < won.length; i++) {
                if(won[i] != 0)
                    winners++;
            }

            if(bigerr[0] < quitError)
                break;

            if((winners < this.outputNeuronCount) && (winners < this.trainer.getTrainingSetCount())) {
                forceWin(won);
                continue;
            }

            adjustWeights(rate, this.learnMethod, won, bigcorr, corrections);

            owner.updateStats(n_retry, this.totalError, best_err);
            if(this.halt) {
                owner.updateStats(n_retry, this.totalError, best_err);
                break;
            }
            Thread.yield();

            if(bigcorr[0] < 1E-5) {
                if(++n_retry > this.retries)
                    break;
                initialize();
                iter = -1;
                rate = this.learnRate;
                continue;
            }

            if(rate > 0.01)
                rate *= this.reduction;
        }

        copyWeights(this, bestNet);

        for(int i = 0; i < this.outputNeuronCount; i++) {
            normalizeWeight(this.outputWeights[i]);
        }

        this.halt = true;
        n_retry++;
        owner.updateStats(n_retry, this.totalError, best_err);
    }

    /*Инициализация нейронной сети Кохонена*/
    public void initialize() {
        double optr[];

        clearWeights();
        randomizeWeights(this.outputWeights);
        for(int i = 0; i < this.outputNeuronCount; i++) {
            optr = this.outputWeights[i];
            normalizeWeight(optr);
        }
    }
   }
