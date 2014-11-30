package Statistics;

import form.AppForm;

import javax.swing.*;

/**
 * Created by 1 on 30.11.2014.
 */
public class UpdateStats implements Runnable {
    public long tries;
    public double lastError;
    public double bestError;
    public static JLabel lblTries = AppForm.lblTries;
    public static JLabel lblLastError = AppForm.lblLastError;
    public static JLabel lblBestError = AppForm.lblBestError;

    public UpdateStats() { }

    public void update() {
        lblTries.setText("" + this.tries);
        lblLastError.setText("" + this.lastError);
        lblBestError.setText("" + this.bestError);
    }

    public void run() {
        update();
    }
}
