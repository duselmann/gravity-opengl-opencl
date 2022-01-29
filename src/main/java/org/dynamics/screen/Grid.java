// Copyright (c) 2022 David Uselmann
package org.dynamics.screen;

import java.awt.Color;
import java.awt.Graphics2D;

import org.joml.Vector3f;

public class Grid {

	boolean display;
	Vector3f max = new Vector3f(36,36,36);
	Vector3f min = new Vector3f();

    public void draw(Graphics2D g) {
    	if (!display) {
    		return;
    	}
    	g.setColor(Color.yellow);
    	float dx = Math.max(1, (max.x-min.x)/36f);
    	float dy = Math.max(1, (max.y-min.y)/36f);
    	for (int x = (int)min.x; x<=max.x; x+=dx) {
        	for (int y = (int)min.y; y<=max.y; y+=dy) {
        		g.drawLine(x, y, x+(int)Math.ceil(dx), y);
        		if (x>=min.y && y>=min.x && x<=max.y && y<=max.x) {
        			g.drawLine(y, x, y, x+(int)Math.ceil(dy));
        		}
        	}
//    		g.drawLine(x, max.y, x+dx, max.y);
//    		g.drawLine(max, x, max, x+dy);
    	}
	}

	public void add(float x, float y, float z) {
		max.x = Math.max(x, max.x);
		max.y = Math.max(y, max.y);
		max.z = Math.max(z, max.z);
		min.x = Math.min(x, min.x);
		min.y = Math.min(y, min.y);
		min.z = Math.min(z, min.z);
//		System.out.println(max);
//		System.out.println(min);
	}

	public void setDisplay(boolean display) {
		this.display = display;
	}
}
