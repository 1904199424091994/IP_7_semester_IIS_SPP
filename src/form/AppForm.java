package form;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by 1 on 09.11.2014.
 */
public class AppForm extends JFrame {
    //private JButton button1;
    private JPanel rootPanel;
    //private JLabel label1;
    private ImageEntry imageFrame;

    public AppForm() {
        super("Hello!");
        rootPanel.setLayout(new BoxLayout(rootPanel, BoxLayout.Y_AXIS));
        setContentPane(rootPanel);
        //pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(200,200,500,500);

        /*button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JOptionPane.showConfirmDialog(AppForm.this, "!!!");
                label1.setText("!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        });*/

        imageFrame = new ImageEntry();
        imageFrame.setBounds(100,100,100,100);
        getContentPane().add(imageFrame);
        imageFrame.repaint();
        imageFrame.setVisible(true);
        setVisible(true);

    }
}
