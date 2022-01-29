// Copyright (c) 2022 David Uselmann
package org.dynamics.screen;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;


public class Window extends JFrame implements Runnable{
    private static final long serialVersionUID = 1L;
    private boolean running = true;
    public DisplayCanavs canvas;
    private List<ResizeListener> resizeListeners = new LinkedList<>();
    private CallBack exitCallBack;

    @FunctionalInterface
    public interface CallBack {
        void call();
    }

    private class WindowEventManager extends WindowAdapter {
        Window target;
        public WindowEventManager(Window target) {
            this.target = target;
        }
        @Override
		public void windowClosing(WindowEvent e) {
            target.stop();
            exitCallBack.call();
            System.exit(0);
        }
    }

    public Window(CallBack exitCallBack) {
        this.exitCallBack = exitCallBack;

        setName("Dynamics");
        setBounds(0,0, 1000,1000);

        Container container = getContentPane();
        canvas = new DisplayCanavs(this); // need to get resize events
        container.add(canvas);

        container.setBackground(Color.BLACK);
        setResizable(true);

        addWindowListener( new WindowEventManager(this) );
        addComponentListener(new ComponentAdapter() {
            @Override
			public void componentResized(ComponentEvent event) {
                int height = getHeight();
                int width  = getWidth();
                resizeListeners.stream()
                    .forEach(listener -> listener.updateSize(width, height));
            }
        });

        setVisible(true);
    }

    public void resizeListener(ResizeListener resizeListener) {
        resizeListeners.add(resizeListener);
    }

    public void stop() {
        running = false;
    }

    @Override
    public void paintComponents(Graphics g) {
        super.paintComponents(g);
    }

    @Override
	public void run() {
        while (running) {
            try {
                canvas.repaint();
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.exit(1);
    }
}
