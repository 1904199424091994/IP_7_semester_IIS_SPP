package Network;

import form.AppForm;

/**
 * Created by 1 on 29.11.2014.
 */
public class KohonenNeuralNetwork extends NeuralNetwork {
    protected double outputWeights[][]; //Веса нейронов на выходе
    protected double learnRate = 0.3; //Константа, используемая для настройки весов нейронов
    protected double quitError = 0.1; //Допустимый уровень ошибки. Если ошибка, рассчитываемая в ходе обучения меньше, чем значение данной константы, то алгоритм обучения сети завершается.
    protected int retries = 10000; //Предустановленное значение итераций цикла обучения нейронной сети.
    protected double reduction = 0.99; //Величина, на которую уменьшается learnRate в конце каждой итерации обучения.  В данном случае на 1% (rate *= this.reduction)
    protected AppForm form; //Форма, пользовательского интерфейса.
    protected Trainer trainer; //Экземпляр класса, который хранит наборы для обучения нейронной сети.
    public boolean halt = false; //Обозначает, что нейронная сеть завершила свое обучение

    /*
    *  Конструктор класса KohonenNeuralNetwork
    *  @inputCount --- число нейронов на входе
    *  @outputCount --- число нейронов на выходе
    *  @form --- связка с формой
    * */
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
    * */
    void normalizeInput(final double input[], double normfac[]/*, double synth[]*/) {
        double length;
        length = getVectorLength(input);

        //для случая очень малой длины (удалить наверное)
        if(length < 1.E-30)
            length = 1.E-30;

        normfac[0] = 1.0 / Math.sqrt(length);
        //synth[0] = 0.0;
    }

    /*
    * Нормализация весов
    *
    * @weights - массив входных весов
    * */
    void normalizeWeight(double[] vector) {
        double length;
        length = getVectorLength(vector);

        //для случая малой суммы квадратов
        if(length < 1.E-30)
            length = 1.E-30;

        double normfac = 1.0 / Math.sqrt(length);
        for(int i = 0; i < this.inputNeuronCount; i++)
            vector[i] *= normfac;
        vector[this.inputNeuronCount] = 0;
    }

    /*
    * Испытание входного рисунка изображения
    *
    * @input - входное изображение
    * */
    /*void trial(double input[]) {
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
    }*/

    /*
    * На основе входных данных получает выигравший нейрон
    *
    * @input --- входные данные
    * @normfac --- результат
    * @synth --- последний ввод ???
    *
    * @return --- выигравший нейрон
    * */
    public int getWinner(double input[], double normfac[]/*, double synth[]*/) {
        int win = 0;
        double biggest, optr[];

        normalizeInput(input, normfac/*, synth*/);
        biggest = -1.E30;

        for(int i = 0; i < this.outputNeuronCount; i++) {
            optr = outputWeights[i];
            this.output[i] = getDotProduct(input, optr) * normfac[0];
                             //+ synth[0] * optr[this.inputNeuronCount];

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

    private void evaluateErrors(double rate, /*int learnMethod,*/ int won[],
                        double error[], double corrections[][]
                        /*double work[]*/) throws  RuntimeException {
        int best, size, tset;
        double dptr[], normfac[] = new double[1];
        double /*synth[] = new double[1],*/ cptr[], wptr[], length, diff;

        //сбрасываем коррекцию и счетчик побед
        for(int y = 0; y < corrections.length; y++) {
            for(int x = 0; x < corrections[0].length; x++) {
                corrections[y][x] = 0;
            }
        }
        for(int i = 0; i < won.length; i++) {
            won[i] = 0;
        }

        error[0] = 0.0;

        for(tset = 0; tset < this.trainer.getTrainingSetCount(); tset++) {
            dptr = this.trainer.getInputSet(tset);
            best = getWinner(dptr, normfac/*, synth*/);
            won[best]++;
            wptr = this.outputWeights[best];
            cptr = corrections[best];
            length = 0.0;

            for(int i = 0; i < this.inputNeuronCount; i++) {
                diff = dptr[i] * normfac[0] - wptr[i];
                length += (diff *diff);
                //if(learnMethod != 0)
                    cptr[i] += diff;
                //else
                //    work[i] = rate * dptr[i] * normfac[0] + wptr[i];
            }

            diff = /*synth[0]*/ (-1) * wptr[this.inputNeuronCount];
            length += (diff * diff);

            //if(learnMethod != 0)
                cptr[this.inputNeuronCount] += diff;
            //else
            //    work[this.inputNeuronCount] = rate * synth[0] + wptr[this.inputNeuronCount];

            if(length > error[0])
                error[0] = length;

            /*if(learnMethod == 0) {
                normalizeWeight(work);
                for(int i = 0; i < this.inputNeuronCount; i++)
                    cptr[i] += work[i] - wptr[i];
            }*/
        }
        error[0] = Math.sqrt(error[0]);
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
    private void adjustWeights(double rate, //int learnMethod,
                       int[] won, double error[],
                       double corrections[][]) {
        double corr, correction[], weight[], length, factor;

        error[0] = 0.0;

        for(int i = 0; i < this.outputNeuronCount; i++) {
            //пропускаем ни разу не выигравшие нейроны
            if(won[i] == 0)
                continue;

            weight = this.outputWeights[i];
            correction = corrections[i];

            factor = 1.0 / (double)won[i];

            //if(learnMethod != 0)
            factor *= rate;

            length = 0.0;

            for(int j = 0; j < this.inputNeuronCount; j++) {
                corr = factor * correction[j];
                weight[j] += corr;
                length += (corr * corr);
            }

            if(length > error[0])
                error[0] = length;
        }
        error[0] = Math.sqrt(error[0]) / rate;
    }

    /*
    * Если среди нейронов нет победителей, то делаем победителя!
    *
    * @won - счетчик побед нейронов
    * */
    private void forceWin(int won[]) throws RuntimeException {
        int best, size, which = 0;
        double dptr[], normfac[] = new double[1];
        double /*synth[] = new double[1],*/ dist, optr[];

        size = this.inputNeuronCount + 1;
        dist = 1.E30;

        for(int tset = 0; tset < this.trainer.getTrainingSetCount(); tset++) {
            dptr = this.trainer.getInputSet(tset);
            best = getWinner(dptr, normfac/*, synth*/);
            if(this.output[best] < dist) {
                dist = output[best];
                which = tset;
            }
        }

        dptr = this.trainer.getInputSet(which);
        best = getWinner(dptr, normfac/*, synth*/);

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
        optr[this.inputNeuronCount] = 0.0;//synth[0] / normfac[0];
        normalizeWeight(optr);
    }

    /*
    * Данный метод тренирует нейронную сеть.
    * */
    public void learn() throws RuntimeException {
        int n_retry;
        int won[], winners;
        double /*work[], */corrections[][], rate, best_err, dptr[];
        double bigerr[] = new double[1];
        double bigcorr[] = new double[1];
        KohonenNeuralNetwork bestNet; //сохраняем лучшую сюда

        this.totalError = 1.0;

        for(int tset = 0; tset < this.trainer.getTrainingSetCount(); tset++) {
            dptr = this.trainer.getInputSet(tset); //входной вектор символа
            if(getVectorLength(dptr) < 1.E-30) //сумма квадратов строки вектора
                throw (new RuntimeException("Нулевой случай тренировки"));
        }

        bestNet = new KohonenNeuralNetwork(this.inputNeuronCount, this.outputNeuronCount, this.form);
        won = new int[this.outputNeuronCount];
        corrections = new double[this.outputNeuronCount][this.inputNeuronCount + 1];

        //if(learnMethod == 0)
        //    work = new double[inputNeuronCount + 1];
        //else
            //work = null;

        rate = this.learnRate;

        /*ШАГ 4. Инициализация нейронной сети - чистка весов,
        *        их рандомизирование и нормирование
        * */
        initialize();

        best_err = 1.e30; //ошибка
        n_retry = 0;  //текущая итерация

        for(int i = 0;; i++) {
            //Расчет ошибки
            evaluateErrors(rate/*, this.learnMethod*/, won, bigerr,
                           corrections/*, work*/);
            this.totalError = bigerr[0];

            if(this.totalError < best_err) {
                best_err = this.totalError;
                copyWeights(bestNet, this);
            }

            winners = 0;

            for(int j = 0; j < won.length; j++) {
                if(won[j] != 0)
                    winners++;
            }

            //Условие выхода из цикла - если ошибка меньше заданного уровня, то break
            if(bigerr[0] < quitError)
                break;

            if(winners < this.outputNeuronCount /*&& (winners < this.trainer.getTrainingSetCount())*/) {
                forceWin(won);
                continue;
            }

            adjustWeights(rate/*, this.learnMethod*/, won, bigcorr, corrections);

            form.updateStats(n_retry, this.totalError, best_err);
            if(this.halt) {
                form.updateStats(n_retry, this.totalError, best_err);
                break;
            }
            Thread.yield();

            if(bigcorr[0] < 1E-5) {
                if(++n_retry > this.retries)
                    break;
                initialize();
                i = -1;
                rate = this.learnRate;
                continue;
            }

            if(rate > 0.01)
                rate *= this.reduction;
        }

        copyWeights(this, bestNet);

        for(int i = 0; i < this.outputNeuronCount; i++)
            normalizeWeight(this.outputWeights[i]);

        this.halt = true;
        n_retry++;
        form.updateStats(n_retry, this.totalError, best_err);
    }

    /*Инициализация нейронной сети Кохонена*/
    public void initialize() {
        double vector[];

        clearWeights();
        setRandomToWeights(this.outputWeights);
        for(int i = 0; i < this.outputNeuronCount; i++) {
            vector = this.outputWeights[i];
            normalizeWeight(vector);
        }
    }
   }
