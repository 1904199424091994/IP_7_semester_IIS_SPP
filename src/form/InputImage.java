package form;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.PixelGrabber;

/**
 * Created by 1 on 09.11.2014.
 */
public class InputImage extends JPanel {
    protected Image image; //вводимое изображение
    protected Graphics graphics; //объект-графика
    protected Grid grid; //объект-сетка
    protected int x_InputSymbolLeft;
    protected int y_InputSymbolTop;
    protected int x_InputSymbolRight;
    protected int y_InputSymbolBottom;
    protected int lastX;
    protected int lastY;
    protected int[] pixelMap;
    protected double coefficientX;
    protected double coefficientY;
    private int symbPadding = 5;

    public InputImage() {
        enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK |
                AWTEvent.MOUSE_EVENT_MASK |
                AWTEvent.COMPONENT_EVENT_MASK);
    }

    protected void InitImage() {
        image = createImage(this.getWidth(), this.getHeight());
        graphics = image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public Grid getGrid() {
        return this.grid;
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        if (e.getID() != MouseEvent.MOUSE_DRAGGED)
            return;

        graphics.setColor(Color.black);
        graphics.drawLine(lastX, lastY, e.getX(), e.getY());
        getGraphics().drawImage(image, 0, 0, this);
        lastX = e.getX();
        lastY = e.getY();
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (e.getID() != MouseEvent.MOUSE_PRESSED)
            return;
        lastX = e.getX();
        lastY = e.getY();
    }

    @Override
    public void paint(Graphics graphics) {
        if (image == null) {
            InitImage();
        }
        graphics.drawImage(image, 0, 0, this);
        graphics.setColor(Color.black);
        graphics.drawRect(0, 0, this.getWidth(), this.getHeight());
        graphics.setColor(Color.red);
        graphics.drawRect(x_InputSymbolLeft, y_InputSymbolTop,
                x_InputSymbolRight - x_InputSymbolLeft,
                y_InputSymbolBottom - y_InputSymbolTop);
    }

    //Проверка: пустая ли горизонтальная линия изображения
    private boolean isHorizontalLineClear(int y) {
        int iWidth = this.image.getWidth(this);
        for (int i = 0; i < iWidth; i++) {
            if (pixelMap[(y * iWidth) + i] != -1) {
                return false;
            }
        }
        return true;
    }

    //Проверка: пустая ли вертикальная линия изображения
    private boolean isVerticalLineClear(int x) {
        int iWidth = this.image.getWidth(this);
        int iHeight = this.image.getHeight(this);
        for (int i = 0; i < iHeight; i++) {
            if (pixelMap[(i * iWidth) + x] != -1) {
                return false;
            }
        }
        return true;
    }

    //Поиск границы нарисованного символа
    private void findSymbolBounds(int iWidth, int iHeight) {
        //сверху-вниз
        for (int y = 0; y < iHeight; y++) {
            if (!isHorizontalLineClear(y)) {
                y_InputSymbolTop = y - symbPadding;
                if(y_InputSymbolTop < 0)
                    y_InputSymbolTop = 0;
                break;
            }
        }
        //снизу-вверх
        for (int y = iHeight - 1; y >= 0; y--) {
            if (!isHorizontalLineClear(y)) {
                y_InputSymbolBottom = y + symbPadding;
                if(y_InputSymbolBottom >= iHeight)
                    y_InputSymbolBottom = iHeight - 1;
                break;
            }
        }
        //слева-направо
        for (int x = 0; x < iWidth; x++) {
            if (!isVerticalLineClear(x)) {
                x_InputSymbolLeft = x - symbPadding;
                if(x_InputSymbolLeft < 0)
                    x_InputSymbolLeft = 0;
                break;
            }
        }
        //справо-налево
        for (int x = iWidth - 1; x >= 0; x--) {
            if (!isVerticalLineClear(x)) {
                x_InputSymbolRight = x + symbPadding;
                if(x_InputSymbolRight >= iWidth)
                    x_InputSymbolRight = iWidth - 1;
                break;
            }
        }
    }

    //Обрабатывает клетку изображения
    protected boolean handleSampleSquare(int x, int y) {
        int iWidth = this.image.getWidth(this);
        int startX = (int)(x_InputSymbolLeft + (x * coefficientX));
        int startY = (int)(y_InputSymbolTop + (y * coefficientY));
        int endX = (int)(startX + coefficientX);
        int endY = (int)(startY + coefficientY);

        for (int yy = startY; yy <= endY; yy++) {
            for (int xx = startX; xx <= endX; xx++) {
                int loc = xx + (yy * iWidth);
                if (pixelMap[loc] != -1)
                    return true;
            }
        }
        return false;
    }

    //Переносим изображение в Grid
    public void convertToGrid() {
        int iWidth = this.image.getWidth(this);
        int iHeight = this.image.getHeight(this);

        PixelGrabber grabber = new PixelGrabber(this.image, 0, 0,
                                                iWidth, iHeight,
                                                true);
        try {
            grabber.grabPixels();
            pixelMap = (int[]) grabber.getPixels();
            findSymbolBounds(iWidth, iHeight);

            GridData data = this.grid.getGridData();
            coefficientX = (double)(x_InputSymbolRight - x_InputSymbolLeft)
                            / (double) data.getWidth();
            coefficientY = (double)(y_InputSymbolBottom - y_InputSymbolTop)
                            / (double) data.getHeight();

            for (int y = 0; y < data.getHeight(); y++) {
                for (int x = 0; x < data.getWidth(); x++) {
                    if (handleSampleSquare(x, y))
                        data.setDataToGrid(x, y, true);
                    else
                        data.setDataToGrid(x, y, false);
                }
            }
            this.grid.repaint();
            this.repaint();
        } catch (InterruptedException e) { }
    }

    public void clear() {
        this.graphics.setColor(Color.white);
        this.graphics.fillRect(0, 0, getWidth(), getHeight());
        this.y_InputSymbolBottom = 0;
        this.y_InputSymbolBottom = 0;
        this.x_InputSymbolLeft = 0;
        this.x_InputSymbolRight = 0;
        this.repaint();
    }
}
    /*public void convertToGrid(Image textImage, int widthStart, int heightStart, int widthEnd, int heightEnd, int iW, int iH) {
        if(widthEnd == -1) {
            widthEnd = this.image.getWidth(this);
        }
        if(heightEnd == -1) {
            heightEnd = this.image.getHeight(this);
        }

        PixelGrabber pixelGrabber = new PixelGrabber(textImage, 0, 0, iW, iH, true);

        try {
            pixelGrabber.grabPixels();
            this.pixelMap = (int[])pixelGrabber.getPixels();
            GridData gridData = this.grid.getGridData();
            x_InputSymbolLeft = widthStart;
            x_InputSymbolRight = widthEnd + widthStart;
            y_InputSymbolBottom = heightStart + heightEnd;
            y_InputSymbolTop = heightStart + heightEnd;

        } catch(InterruptedException e) {

        }
    }*/

        /*protected boolean downSampleQuadrant(int x,int y, Image img)
    {
        int w = img.getWidth(this);
        int startX = (int)(x_InputSymbolLeft + (x * coefficientX));
        int startY = (int)(y_InputSymbolTop+(y * coefficientY));
        int endX = (int)(startX + coefficientX);
        int endY = (int)(startY + coefficientY);

        for ( int yy=startY;yy<=endY;yy++ ) {
            for ( int xx=startX;xx<=endX;xx++ ) {
                int loc = xx+(yy*w);

                int p = pixelMap[loc];
                int r = 0xff & (p >> 16);
                int g = 0xff & (p >> 8);
                int b = 0xff & (p);
                int intensity = (r + g + b)/3;
                Boolean white = false;
                if (intensity > 150) {
                    //return false;
                } else {
                    return true;
                }

            }
        }

        return false;
    }*/

