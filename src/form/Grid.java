package form;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 1 on 16.11.2014.
 */
public class Grid extends JPanel {
    GridData gridData;
    public Grid(int width, int height) {
        gridData = new GridData(' ', width, height);
    }

    public GridData getGridData() {
        return gridData;
    }
    public void setGridData(GridData data) {
        this.gridData = data;
    }

    @Override
    public void paint(Graphics g) {
        if(this.gridData == null)
            return;
        int cellWidth = this.getWidth()/this.gridData.getWidth();
        int cellHeight = this.getHeight()/this.gridData.getHeight();
        drawBase(g);
        drawLattice(g, cellWidth, cellHeight);
        drawGridData(g, cellWidth, cellHeight);

    }

    //Прямоугольник под Grid
    private void drawBase(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0,0,this.getWidth(), this.getHeight());
        g.setColor(Color.black);
        System.out.println("-1???? что?");
        g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
    }

    //Отрисовка сетки
    private void drawLattice(Graphics g, int cellWidth, int cellHeight) {
        g.setColor(Color.black);
        for(int x = 0; x < this.gridData.getWidth(); x++) {
            g.drawLine(x * cellHeight, 0, x * cellHeight, this.getHeight());
        }
        for(int y = 0; y < this.gridData.getHeight(); y++) {
            g.drawLine(0, y * cellHeight, this.getWidth(), y * cellHeight);
        }
    }

    //Отображение данных gridData
    private void drawGridData(Graphics g, int cellWidth, int cellHeight) {
        for(int x = 0; x < this.gridData.getWidth(); x++) {
            for(int y = 0; y < this.gridData.getHeight(); y++) {
                if(this.gridData.getDataFromGrid(x, y)) {
                    g.fillRect(x * cellHeight, y * cellWidth, cellHeight, cellWidth);
                }
            }
        }
    }
}
