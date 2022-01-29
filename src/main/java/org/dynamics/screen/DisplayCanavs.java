// Copyright (c) 2022 David Uselmann
package org.dynamics.screen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.function.Function;

import javax.swing.JPanel;

import org.dynamics.math.Vector;

public class DisplayCanavs extends JPanel implements HoldCenter  {

    public FloatBuffer bodies;

    private static final long serialVersionUID = 1L;

    private BufferedImage buffer;
    private Color background = Color.BLACK;
    private boolean holdCenter = false;
    Function<Vector,Float> radiusCallBack;
    private float zoom=1; // 0.5 for 4k displays
    private Point translation;
    private Point point;

    public float scaleDistance = 2;
    public float scaleRadius = 1;
    public boolean _3D = false;

    public final Color BLUE = new Color(0,100,100);
    public final Color RED  = new Color(100,0,0);

    Grid grid = new Grid();

    public void makeBuffer(int w, int h) {
        buffer = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB);
        if (translation == null) {
            translation = new Point(w/2,h/2);
        }
    }

    public DisplayCanavs(Window window) {
        startNewDynamic();

        setBackground(background);
        setSize(1000, 1000);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                point = e.getPoint();
            }
        });
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoom*= Math.pow(0.9, e.getWheelRotation());
            }
        });
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                double dx = e.getX()-point.getX();
                double dy = e.getY()-point.getY();
                translation.setLocation(translation.getX() + dx, translation.getY() + dy);
                point.setLocation(e.getX(), e.getY());
            }
        });

        makeBuffer(1000,1000);
        window.resizeListener((width,height)-> {
            makeBuffer(width,height);
        });
    }

    private void startNewDynamic() {
    }

    @Override
	public void setHoldCenter(boolean holdCenter) {
        this.holdCenter = holdCenter;
    }
    public boolean isHoldCenter() {
        return holdCenter;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        Graphics2D buffferedGraphic = buffer.createGraphics();
        buffferedGraphic.setBackground(background);
        buffferedGraphic.clearRect(0, 0, buffer.getWidth(),buffer.getHeight());

        buffferedGraphic.translate(translation.getX(), translation.getY());
        buffferedGraphic.scale(zoom,zoom); // for 4k displays
        buffferedGraphic.setColor(Color.RED);
        buffferedGraphic.setStroke(new BasicStroke(1));

        if (bodies != null) {
            bodies.rewind();
            float delta = 0;

            while (bodies.hasRemaining()) {
                float x = bodies.get();
                float y = bodies.get();
                float z = bodies.get();
                x *= scaleDistance;
                y *= scaleDistance;
                z *= scaleDistance;
                grid.add(x,y,z);

                float m = bodies.get();
                float r = m; // mass as radius
                if (radiusCallBack != null) {
                    r = scaleRadius * radiusCallBack.apply(new Vector(r,0,z));
                }
                if (m>1000) {
                    r = Math.max(r, 3);
                } else {
                    r = (float)Math.max(r, 0.5);
                }
                r = Math.min(r, 50);
                float d = r * 2;
                if (_3D) {
                    delta = z/25;
                    buffferedGraphic.setColor(BLUE);
                    buffferedGraphic.draw( new Ellipse2D.Double(x-r-delta, y-r, d, d) );
                }
                buffferedGraphic.setColor(RED);
                buffferedGraphic.draw( new Ellipse2D.Double(x-r+delta, y-r, d, d) );
            }
            bodies.rewind();
            grid.draw(buffferedGraphic);
        }

        buffferedGraphic.scale(1,1); // for 4k displays

        g2.drawImage(buffer,0,0,this);
    }


	public void setRadiusCallBack(Function<Vector, Float> radiusCallBack) {
        this.radiusCallBack = radiusCallBack;
    }

    public void setTranslation(int x, int y) {
        translation.setLocation(x, y);
    }
}
