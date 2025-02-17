package com.mjkrempl.cartchunkloader.ChunkManagement;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChunkLoadVisualizer extends JFrame {
	private final int numberOfTiles;
	private final int tileSize;
	private final int topPadding;
	private final Map<Point, Integer> tileStates;
	private final Map<UUID, Point> entityPositions;
	private final Map<UUID, Point> playerPositions;
	private final Color colorLoaded = Color.green;
	private final Color colorUnloaded = Color.lightGray;
	private final Color colorAlwaysLoaded = new Color(0, 127, 0, 127);
	private final Color colorNotAlwaysLoaded = new Color(0, 0, 0, 0);
	private final Color colorEntitiesLoaded = new Color(0, 0, 255, 63);
	private final Color colorEntitiesUnloaded = new Color(0, 0, 255, 0);
	private final Color colorTicket = new Color(255, 0, 0, 127);
	private final Color colorNoTicket = new Color(255, 0, 0, 0);
	private final Color colorEntity = new Color(255, 255, 0);
	private final Color colorPlayer = new Color(255, 0, 255);
	
	public ChunkLoadVisualizer(int numberOfChunks, int pixelsPerChunk, String name) {
		this.numberOfTiles = numberOfChunks;
		this.tileSize = pixelsPerChunk;
		this.topPadding = 30;
		this.tileStates = Collections.synchronizedMap(new HashMap<>());
		this.entityPositions = new HashMap<>();
		this.playerPositions = new HashMap<>();
		
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
				Point p = coordinateToCanvasPoint(entry.getKey());
				int value = entry.getValue();
				paintRect(g, p.x, p.y, value, colorLoaded, colorUnloaded);
				value >>= 1;
				paintRect(g, p.x, p.y, value, colorAlwaysLoaded, colorNotAlwaysLoaded);
				value >>= 1;
				paintRect(g, p.x, p.y, value, colorEntitiesLoaded, colorEntitiesUnloaded);
				value >>= 1;
				paintRect(g, p.x, p.y, value, colorTicket, colorNoTicket);
			}
		}
		
		synchronized (entityPositions) {
			for (Point coord : entityPositions.values()) {
				Point p = coordinateToCanvasPoint(coord);
				g.setColor(colorEntity);
				g.fillRect(p.x, p.y, tileSize, tileSize);
			}
		}
		
		synchronized (playerPositions) {
			for (Point coord : playerPositions.values()) {
				Point p = coordinateToCanvasPoint(coord);
				g.setColor(colorPlayer);
				g.fillRect(p.x, p.y, tileSize, tileSize);
			}
		}
	}
	private void paintRect(Graphics g, int x, int y, int value, Color colorActive, Color colorInactive) {
		g.setColor((value & 1) == 1 ? colorActive : colorInactive);
		g.fillRect(x, y, tileSize, tileSize);
	}
	private Point coordinateToCanvasPoint(Point coord) {
		int x = (numberOfTiles / 2 + coord.x) * tileSize;
		int y = (numberOfTiles / 2 + coord.y) * tileSize + topPadding;
		return new Point(x, y);
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
	
	public void onEntityPositionUpdate(UUID entityUID, int x, int z) {
		boolean changed = false;
		synchronized (entityPositions) {
			Point pos = new Point(x, z);
			Point prevPos = entityPositions.put(entityUID, pos);
			changed = !pos.equals(prevPos);
		}
		if (changed) repaint();
	}
	public void onEntityRemoved(UUID entityUID) {
		synchronized (entityPositions) {
			entityPositions.remove(entityUID);
		}
		repaint();
	}
	
	public void onPlayerPositionUpdate(UUID playerUID, int x, int z) {
		boolean changed = false;
		synchronized (playerPositions) {
			Point pos = new Point(x, z);
			Point prevPos = playerPositions.put(playerUID, pos);
			changed = !pos.equals(prevPos);
		}
		if (changed) repaint();
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