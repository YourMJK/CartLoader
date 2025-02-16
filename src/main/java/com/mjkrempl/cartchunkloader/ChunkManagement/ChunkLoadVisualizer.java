package com.mjkrempl.cartchunkloader.ChunkManagement;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChunkLoadVisualizer extends JFrame {
	private final int numberOfTiles;
	private final int tileSize;
	private final int topPadding;
	private final Map<Point, Integer> tileStates;
	private final Color colorLoaded = Color.green;
	private final Color colorUnloaded = Color.lightGray;
	private final Color colorAlwaysLoaded = new Color(0, 127, 0, 127);
	private final Color colorNotAlwaysLoaded = new Color(0, 0, 0, 0);
	private final Color colorEntities = new Color(0, 0, 255, 63);
	private final Color colorNoEntities = new Color(0, 0, 255, 0);
	private final Color colorTicket = new Color(255, 0, 0, 127);
	private final Color colorNoTicket = new Color(255, 0, 0, 0);
	
	public ChunkLoadVisualizer(int numberOfChunks, int pixelsPerChunk, String name) {
		this.numberOfTiles = numberOfChunks;
		this.tileSize = pixelsPerChunk;
		this.topPadding = 30;
		this.tileStates = Collections.synchronizedMap(new HashMap<>());
		
		int frameSize = numberOfChunks * pixelsPerChunk;
		setSize(frameSize, frameSize + topPadding);
		setTitle("Chunk Load Visualizer (" + name + ")");
		setBackground(Color.white);
		setVisible(true);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		synchronized (tileStates) {
			for (HashMap.Entry<Point, Integer> entry : tileStates.entrySet()) {
				Point point = entry.getKey();
				int x = (numberOfTiles / 2 + point.x) * tileSize;
				int y = (numberOfTiles / 2 + point.y) * tileSize + topPadding;
				int value = entry.getValue();
				paintRect(g, x, y, value, colorLoaded, colorUnloaded);
				value >>= 1;
				paintRect(g, x, y, value, colorAlwaysLoaded, colorNotAlwaysLoaded);
				value >>= 1;
				paintRect(g, x, y, value, colorEntities, colorNoEntities);
				value >>= 1;
				paintRect(g, x, y, value, colorTicket, colorNoTicket);
			}
		}
	}
	private void paintRect(Graphics g, int x, int y, int value, Color colorActive, Color colorInactive) {
		g.setColor((value & 1) == 1 ? colorActive : colorInactive);
		g.fillRect(x, y, tileSize, tileSize);
	}
	
	public void onChunkAlwaysLoaded(int x, int z) {
		updateTileState(x, z, true, 1);
	}
	
	public void onChunkLoaded(int x, int z) {
		updateTileState(x, z, true, 0);
	}
	public void onChunkUnloaded(int x, int z) {
		updateTileState(x, z, false, 0);
	}
	
	public void onEntitiesLoaded(int x, int z) {
		updateTileState(x, z, true, 2);
	}
	public void onEntitiesUnloaded(int x, int z) {
		updateTileState(x, z, false, 2);
	}
	
	public void onChunkTicketAdd(int x, int z) {
		updateTileState(x, z, true, 3);
	}
	public void onChunkTicketRemove(int x, int z) {
		updateTileState(x, z, false, 3);
	}
	
	private void updateTileState(int x, int z, boolean state, int bit) {
		synchronized (tileStates) {
			Point point = new Point(x, z);
			int value = tileStates.getOrDefault(point, 0);
			int mask = 1 << bit;
			if (state) {
				value |= mask;
			} else {
				value &= ~mask;
			}
			tileStates.put(point, value);
		}
		repaint();
	}
}