package form;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * Created by 1 on 09.11.2014.
 */
public class AppForm extends JFrame {
    //region Элементы формы
    private InputImage inputImage;
    private Grid recognizedSymbolGrid;
    private JPanel rootPanel;
    private JButton btnRecognizeInputImage;
    private JButton btnClear;
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
        inputImage.setBounds(10,10, 200, 200);
        container.add(inputImage);
        //endregion

        //region Кнопка для заполнения Grid
        btnRecognizeInputImage = new JButton("Распознать");
        btnRecognizeInputImage.setBounds(10, 220, 200, 30);
        container.add(btnRecognizeInputImage);
        btnRecognizeInputImage.addActionListener(new ActionListener() {
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
                inputImage.clear();
                recognizedSymbolGrid.gridData = new GridData(' ', 40, 40);
                recognizedSymbolGrid.repaint();
            }
        });
        //endregion

        //region Настройки для Grid
        recognizedSymbolGrid = new Grid(40,40);
        recognizedSymbolGrid.setBounds(10, 300, 200, 200);
        container.add(recognizedSymbolGrid);
        this.inputImage.grid = recognizedSymbolGrid;
        //endregion
    }
}
