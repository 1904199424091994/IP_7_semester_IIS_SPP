package form;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by 1 on 09.11.2014.
 */
public class ImageEntry extends JPanel {
    protected Image entryImage;
    protected Graphics entryGraphics;
   /* protected int downImageLeft = 10;
    protected int downImageRight = 10;
    protected int downImageTop = 100;
    protected int downImageBottom = 100;*/
    protected int lastX = -1;
    protected int lastY = -1;

    ImageEntry() {
        enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK |
                AWTEvent.MOUSE_EVENT_MASK |
                AWTEvent.COMPONENT_EVENT_MASK);
    }

    protected void InitImage() {
        entryImage = createImage(this.getWidth(), this.getHeight());
        entryGraphics = entryImage.getGraphics();
        entryGraphics.setColor(Color.WHITE);
        entryGraphics.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e)
    {
        if ( e.getID()!=MouseEvent.MOUSE_DRAGGED )
            return;

        entryGraphics.setColor(Color.black);
        entryGraphics.drawLine(lastX,lastY,e.getX(),e.getY());
        getGraphics().drawImage(entryImage,0,0,this);
        lastX = e.getX();
        lastY = e.getY();
    }

    @Override
    public void paint(Graphics graphics) {
        if(entryImage == null) {
            InitImage();
        }
        graphics.drawImage(entryImage, 0, 0, this);
        graphics.setColor(Color.black);
        graphics.drawRect(0, 0, this.getWidth(), this.getHeight());
        graphics.setColor(Color.red);
        /*graphics.drawRect(downImageLeft, downImageTop,
                          downImageRight - downImageLeft,
                          downImageBottom - downImageTop);*/
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        if(e.getID() != MouseEvent.MOUSE_DRAGGED) {
            return;
        }
        System.out.println("!!!!!");
        entryGraphics.setColor(Color.red);
        entryGraphics.drawLine(lastX, lastY, e.getX(), e.getY());
        this.getGraphics().drawImage(entryImage, 0, 0, this);
        lastX = e.getX();
        lastY = e.getY();
    }
}
