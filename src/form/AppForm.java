package form;


import Network.KohonenNeuralNetwork;
import Network.Trainer;
import Statistics.UpdateStats;
import Helpers.Helper;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * Created by 1 on 09.11.2014.
 */
public class AppForm extends JFrame implements Runnable {
    private static int gridWidth = 40;
    private static int gridHeight = 40;

    //region Элементы формы
    private InputImage inputImage;
    private Grid recognizedSymbolGrid;
    private JPanel rootPanel;
    private JButton btnSchemeInputImage;
    private JButton btnClear;
    private JButton btnBeginTraining;
    private JButton btnAddNewSymbol;
    private JButton btnDeleteItem;
    private JButton btnRecognize;
    private JButton btnSave;
    private JButton btnLoad;
    public static JLabel lblTries;
    public static JLabel lblLastError;
    public static JLabel lblBestError;
    private JScrollPane jListScrollPane;
    private JList lstLetters;
    //endregion

    //region переменные
    private Thread trainThread = null;
    private KohonenNeuralNetwork network;
    private DefaultListModel letterListModel = new DefaultListModel();
    //endregion

    public AppForm() {
        //region Настройки формы
        super("NewForm");
        this.setLayout(null);
        this.setResizable(false);
        this.setBounds(200, 200, 800, 600);
        this.setVisible(true);
        Container container = this.getContentPane();
        //endregion

        //region Настройки рисовального блока
        inputImage = new InputImage();
        inputImage.setBounds(10, 10, 200, 200);
        container.add(inputImage);
        //endregion

        //region Кнопка для заполнения Grid
        btnSchemeInputImage = new JButton("Нарисовать макет");
        btnSchemeInputImage.setBounds(10, 220, 200, 30);
        container.add(btnSchemeInputImage);
        btnSchemeInputImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputImage.convertToGrid();
            }
        });
        //endregion

        //region Кнопка для очистки Grid и изображения
        btnClear = new JButton("Очистить");
        btnClear.setBounds(10, 260, 200, 30);
        container.add(btnClear);
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               clear();
            }
        });
        //endregion

        //region Настройки для Grid
        recognizedSymbolGrid = new Grid(gridWidth, gridHeight);
        recognizedSymbolGrid.setBounds(10, 300, gridWidth * 5, gridHeight * 5);
        container.add(recognizedSymbolGrid);
        this.inputImage.grid = recognizedSymbolGrid;
        //endregion

        //region Кнопка "Добавить"
        btnAddNewSymbol = new JButton("Добавить");
        btnAddNewSymbol.setBounds(250, 220, 100, 30);
        container.add(btnAddNewSymbol);
        btnAddNewSymbol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewLetter();
            }
        });
        //endregion

        //region Кнопка "Удалить"
        btnDeleteItem = new JButton("Удалить");
        btnDeleteItem.setBounds(350, 220, 100, 30);
        container.add(btnDeleteItem);
        btnDeleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedListItem();
            }
        });
        //endregion

        //region Кнопка "Загрузить"
        btnLoad = new JButton("Загрузить");
        btnLoad.setBounds(250, 260, 100, 30);
        container.add(btnLoad);
        btnLoad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFromFile();
            }
        });
        //endregion

        //region Кнопка "Сохранить"
        btnSave = new JButton("Сохранить");
        btnSave.setBounds(350, 260, 100, 30);
        container.add(btnSave);
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveToFile();
            }
        });
        //endregion

        //region Кнопка для тренировки сети
        btnBeginTraining = new JButton("Тренировать");
        btnBeginTraining.setBounds(250, 300, 200, 100);
        container.add(btnBeginTraining);
        btnBeginTraining.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                train();
            }
        });
        //endregion

        //region Кнопка для запуска алгоритма распознавания
        btnRecognize = new JButton("Распознать");
        btnRecognize.setBounds(250, 400, 200, 100);
        container.add(btnRecognize);
        btnRecognize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recognize();
            }
        });
        //endregion

        //region Список для отображения символов (для распознавания)
        jListScrollPane = new JScrollPane();
        jListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jListScrollPane.setOpaque(false);
        jListScrollPane.setBounds(250, 10, 200, 200);
        jListScrollPane.setBorder(new LineBorder(Color.black, 1));
        container.add(jListScrollPane);
        lstLetters = new JList();
        lstLetters.setBorder(new LineBorder(Color.black, 1));
        lstLetters.setBounds(0, 0, 172, 197);
        jListScrollPane.getViewport().add(lstLetters);
        lstLetters.setModel(letterListModel);
        //endregion

        //region Labels для статистики
        lblTries = new JLabel("tries...");
        lblTries.setBounds(500, 10, 100, 30);
        container.add(lblTries);

        lblBestError = new JLabel("bestError...");
        lblBestError.setBounds(500, 50, 100, 30);
        container.add(lblBestError);

        lblLastError = new JLabel("lastError...");
        lblLastError.setBounds(500, 90, 100, 30);
        container.add(lblLastError);
        //endregion
    }

    //Вывод статистики
    public void updateStats(long trial, double error, double best) {
        /*if ((((trial % 100) != 0) || (trial == 10)) && !network.halt)
            return;*/

        if (network.halt) {
            trainThread = null;
            btnBeginTraining.setText("Тренировать");
            JOptionPane.showMessageDialog(this,
                    "Обучение нейронной сети завершено!", "Тренировка",
                    JOptionPane.PLAIN_MESSAGE);
        }

        /*UpdateStats stats = new UpdateStats();
        stats.tries = trial;
        stats.lastError = error;
        stats.bestError = best;

        try {
            SwingUtilities.invokeAndWait(stats);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e, "Training",
                    JOptionPane.ERROR_MESSAGE);
        }*/
    }

    //region Функционал списка
    private void addNewLetter() {
        //Панель
        JPanel panel = new JPanel();
        //Текстбокс для букв
        JTextField txtLetters = new JTextField("", 20);
        txtLetters.setEditable(true);
        panel.add(txtLetters);

        String letter = new String();
        if (JOptionPane.showConfirmDialog(null, panel, "Добавить новую букву", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            letter = (String) txtLetters.getText();
        } else {
            return;
        }

        //Проверки на верность ввода символа буквы
        if (letter == null || letter.length() == 0) {
            JOptionPane.showMessageDialog(this, "Вы не выбрали ни одной буквы...", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (letter.length() > 1) {
            JOptionPane.showMessageDialog(this, "Ввели не букву...", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //Сохраняем элемент
        inputImage.convertToGrid();
        GridData gridData = (GridData) recognizedSymbolGrid.getGridData().clone();
        gridData.setSymbol(letter.charAt(0));

        int i;
        for (i = 0; i < letterListModel.size(); i++) {
            Comparable symb = (Comparable) letterListModel.getElementAt(i);
            if (symb.compareTo(gridData) > 0) {
                letterListModel.add(i, gridData);
                return;
            }
        }

        letterListModel.add(letterListModel.size(), gridData);
        lstLetters.setSelectedIndex(i);

        inputImage.clear();
        recognizedSymbolGrid.getGridData().clear();
        recognizedSymbolGrid.repaint();
    }

    //Удаление из списка символа
    private void deleteSelectedListItem() {
        int i = lstLetters.getSelectedIndex();
        if (i == -1) {
            JOptionPane.showMessageDialog(this,
                    "Выберите символ из списка", "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        letterListModel.remove(i);
    }
    //endregion

    //region Тренировка нейронной сети
    private void train() {
        if (trainThread == null) {
            trainThread = new Thread(this);
            trainThread.start();
        } else {
            network.halt = true;
        }
    }

    //Метод run() потока тренировки нейронной сети
    public void run() {
        try {
            int inputNeuron = AppForm.gridWidth * AppForm.gridHeight; //произведение, потому что в вектор
            int outputNeuron = letterListModel.size(); //выходное число нейронов = числу обучаемых букв
            Trainer trainer = new Trainer(inputNeuron, outputNeuron);
            trainer.setTrainingSetCount(letterListModel.size()); //число наборов для тренировки = числу символов
            /*
            *   ШАГ 1. Формирование входных данных. Каждый набор представляет собой соответствие между буквой и набором
            *   GridData-ы всех введенных символов  переконвертируются в одномерные массивы типа double
                для "тренера" нейронной сети
            * */
            for (int i = 0; i < letterListModel.size(); i++) {
                int j = 0;
                GridData gridData = (GridData) letterListModel.getElementAt(i);
                System.out.println("GridData буквы");
                Helper.outGridData(gridData);
                for (int m = 0; m < gridData.getHeight(); m++) {
                    for (int n = 0; n < gridData.getWidth(); n++) {
                        //Устанавливаем  тренировочные наборы - если есть отметка = 0.5, иначе -0.5
                        trainer.setInput(i, j++, gridData.getDataFromGrid(n, m) ? 0.5 : -0.5);
                    }
                }
            }
            /*
            * ШАГ 2. Создание экземпляра нейронной сети Кохонена
            * */
            network = new KohonenNeuralNetwork(inputNeuron, outputNeuron, this);
            network.setTrainer(trainer);

            /*
            * ШАГ 3. Начало обучения нейронной сети
            * */
            network.learn(); //обучаем нейронную сеть
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e,
                    "Тренировка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    //endregion

    //region Распознавание изображения
    public void recognize() {
        if ( network == null ) {
            JOptionPane.showMessageDialog(this,
                    "Необходимо обучить нейронную сеть!","Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        inputImage.convertToGrid();

        double input[] = new double[this.gridWidth * this.gridHeight];
        int idx = 0;
        GridData gridData = recognizedSymbolGrid.getGridData();
        for ( int j=0;j<gridData.getHeight();j++ ) {
            for ( int i=0;i<gridData.getWidth();i++ ) {
                input[idx++] = gridData.getDataFromGrid(i, j)?.5:-.5;
            }
        }

        double normfac[] = new double[1];
        //double synth[] = new double[1];

        int best = network.getWinner(input, normfac/*, synth*/);
        char map[] = mapNeurons();
        JOptionPane.showMessageDialog(this,
                "  That letter is " + map[best], "Recognition Successful",
                JOptionPane.PLAIN_MESSAGE);
        clear();
    }

    private char[] mapNeurons()
    {
        char map[] = new char[letterListModel.size()];
        double normfac[] = new double[1];
        //double synth[] = new double[1];

        for (int i = 0; i < map.length; i++ )
            map[i] = '?';

        for (int i = 0; i < letterListModel.size(); i++) {
            double input[] = new double[this.gridWidth * this.gridHeight];
            int idx = 0;
            GridData gridData = (GridData)letterListModel.getElementAt(i);
            for (int m = 0; m < gridData.getHeight(); m++ ) {
                for (int n=0; n<gridData.getWidth(); n++) {
                    input[idx++] = gridData.getDataFromGrid(n,m)?.5:-.5;
                }
            }
            int best = network.getWinner(input, normfac/*, synth*/) ;
            map[best] = gridData.getSymbol();
        }
        return map;
    }
    //endregion

    //region Загрузить/сохранить в файл
    public void saveToFile() {
        try {
            BufferedWriter outStreamWriter = new BufferedWriter (new OutputStreamWriter(new FileOutputStream("./sample.dat"), "UTF8"));

            for (int i = 0; i < letterListModel.size(); i++) {
                GridData gridData = (GridData)letterListModel.elementAt(i);
                outStreamWriter.write(gridData.getSymbol() + ":");
                for(int y = 0; y < gridData.getHeight(); y++) {
                    for (int x = 0; x < gridData.getWidth(); x++) {
                        outStreamWriter.write(gridData.getDataFromGrid(x, y) ? "1" : "0");
                    }
                }
                outStreamWriter.newLine();
            }
            outStreamWriter.close();
            clear();
            JOptionPane.showMessageDialog(this,
                    "Сохранено в 'sample.dat'.",
                    "Сохранение",
                    JOptionPane.PLAIN_MESSAGE);

        } catch ( Exception e ) {
            JOptionPane.showMessageDialog(this,"Ошибка: " + e, "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadFromFile() {
        try {
            BufferedReader inputStreamReader;// used to read the file line by line
            inputStreamReader = new BufferedReader(new InputStreamReader(new FileInputStream ("./sample.dat"), "UTF8"));
            String line;
            int i = 0;
            letterListModel.clear();

            while ((line = inputStreamReader.readLine()) !=null) {
                GridData gridData = new GridData(line.charAt(0), AppForm.gridWidth, AppForm.gridHeight);
                letterListModel.add(i++, gridData);
                int idx = 2;
                for (int y = 0; y < gridData.getHeight(); y++) {
                    for (int x = 0; x < gridData.getWidth(); x++) {
                        gridData.setDataToGrid(x,y,line.charAt(idx++)=='1');
                    }
                }
            }
            inputStreamReader.close();
            clear();
            JOptionPane.showMessageDialog(this,
                    "Загружено из 'sample.dat'.","Загрузка",
                    JOptionPane.PLAIN_MESSAGE);

        } catch ( Exception e ) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка: " + e,"Загрузка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    //endregion

    //Очистить grid
    private void clear() {
        inputImage.clear();
        recognizedSymbolGrid.getGridData().clear();
        recognizedSymbolGrid.repaint();
    }
}
