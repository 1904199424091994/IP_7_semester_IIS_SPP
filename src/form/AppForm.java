package form;

import Network.KohonenNeuralNetwork;
import Network.Trainer;
import Statistics.UpdateStats;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

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
    public static JLabel lblTries;
    public static JLabel lblLastError;
    public static JLabel lblBestError;
    private JScrollPane jListScrollPane;
    private JList lstLetters;
    //endregion

    //region переменные
    private Thread trainThread = null;
    private KohonenNeuralNetwork network;// = new KohonenNeuralNetwork(10,10,this);
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
                //updateStats(1, 1, 1);
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

        //region Кнопка для тренировки сети
        btnBeginTraining = new JButton("Тренировать");
        btnBeginTraining.setBounds(250, 260, 180, 30);
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
        btnRecognize.setBounds(250, 300, 200, 200);
        container.add(btnRecognize);
        btnRecognize.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recognize();
            }
        });
        //endregion

        //region Кнопка "Добавить"
        btnAddNewSymbol = new JButton("Добавить");
        btnAddNewSymbol.setBounds(250, 220, 90, 30);
        container.add(btnAddNewSymbol);
        btnAddNewSymbol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewLetter();
            }
        });
        //endregion

        //region "Удалить"
        btnDeleteItem = new JButton("Удалить");
        btnDeleteItem.setBounds(340, 220, 90, 30);
        container.add(btnDeleteItem);
        btnDeleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedListItem();
            }
        });
        //endregion

        //region Список для отображения символов (для распознавания)
        jListScrollPane = new JScrollPane();
        jListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jListScrollPane.setOpaque(false);
        jListScrollPane.setBounds(250, 10, 200, 200);
        container.add(jListScrollPane);
        lstLetters = new JList();
        lstLetters.setBounds(0, 0, 196, 196);
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
        if ((((trial % 100) != 0) || (trial == 10)) && !network.halt)
            return;

        if (network.halt) {
            trainThread = null;
            btnBeginTraining.setText("Begin Training");
            JOptionPane.showMessageDialog(this,
                    "Training has completed.", "Training",
                    JOptionPane.PLAIN_MESSAGE);
        }

        UpdateStats stats = new UpdateStats();
        stats.tries = trial;
        stats.lastError = error;
        stats.bestError = best;

        try {
            SwingUtilities.invokeAndWait(stats);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e, "Training",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

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

    //Тренировать нейронную сеть
    private void train() {
        if (trainThread == null) {
            //train.setText("Stop Training");
            //train.repaint();
            trainThread = new Thread(this);
            trainThread.start();
        } else {
            network.halt = true;
        }
    }

    public void run() {
        try {
            int inputNeuron = AppForm.gridWidth * AppForm.gridHeight;
            int outputNeuron = letterListModel.size();
            Trainer trainer = new Trainer(inputNeuron, outputNeuron);
            trainer.setTrainingSetCount(letterListModel.size());

            for (int t = 0; t < letterListModel.size(); t++) {
                int idx = 0;
                GridData ds = (GridData) letterListModel.getElementAt(t);
                for (int y = 0; y < ds.getHeight(); y++) {
                    for (int x = 0; x < ds.getWidth(); x++) {
                        trainer.setInput(t, idx++, ds.getDataFromGrid(x, y) ? .5 : -.5);
                    }
                }
            }

            network = new KohonenNeuralNetwork(inputNeuron, outputNeuron, this);
            network.setTrainer(trainer);
            network.learn();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + e,
                    "Тренировка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void recognize() {
        if ( network == null ) {
            JOptionPane.showMessageDialog(this,
                    "Затренируй меня полностью!","Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        inputImage.convertToGrid();

        double input[] = new double[this.gridWidth * this.gridHeight];
        int idx=0;
        GridData ds = recognizedSymbolGrid.getGridData();
        for ( int y=0;y<ds.getHeight();y++ ) {
            for ( int x=0;x<ds.getWidth();x++ ) {
                input[idx++] = ds.getDataFromGrid(x,y)?.5:-.5;
            }
        }

        double normfac[] = new double[1];
        double synth[] = new double[1];

        int best = network.getWinner ( input , normfac , synth ) ;
        char map[] = mapNeurons();
        JOptionPane.showMessageDialog(this,
                "  That letter is " + map[best], "Recognition Successful",
                JOptionPane.PLAIN_MESSAGE);
        clear();
    }

    private char []mapNeurons()
    {
        char map[] = new char[letterListModel.size()];
        double normfac[] = new double[1];
        double synth[] = new double[1];

        for ( int i=0;i<map.length;i++ )
            map[i]='?';
        for ( int i=0;i<letterListModel.size();i++ ) {
            double input[] = new double[this.gridWidth * this.gridHeight];
            int idx=0;
            GridData ds = (GridData)letterListModel.getElementAt(i);
            for ( int y=0;y<ds.getHeight();y++ ) {
                for ( int x=0;x<ds.getWidth();x++ ) {
                    input[idx++] = ds.getDataFromGrid(x,y)?.5:-.5;
                }
            }

            int best = network.getWinner ( input , normfac , synth ) ;

            map[best] = ds.getSymbol();
        }
        return map;
    }

    //Очистить
    private void clear() {
        inputImage.clear();
        recognizedSymbolGrid.getGridData().clear();
        recognizedSymbolGrid.repaint();
    }
}
