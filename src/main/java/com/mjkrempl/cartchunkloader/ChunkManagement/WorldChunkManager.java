package com.mjkrempl.cartchunkloader.ChunkManagement;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;

import com.google.common.collect.*;

public final class WorldChunkManager {
	private final JavaPlugin plugin;
	private final World world;
	private final int regionRadius;
	private final Map<UUID, ChunkCoord> entityActiveRegions;
	private final Map<UUID, Long> entityActiveTimes;
	private final Multiset<ChunkCoord> chunkTickets;
	private final ChunkLoadVisualizer chunkLoadVisualizer;
	
	public WorldChunkManager(JavaPlugin plugin, World world, int regionRadius, int inactiveRegionLoadTime, int updateInterval) {
		this.plugin = plugin;
		this.world = world;
		this.regionRadius = regionRadius;
		this.entityActiveRegions = new HashMap<>();
		this.entityActiveTimes = new HashMap<>();
		this.chunkTickets = HashMultiset.create();
		
		// Visualizer
		this.chunkLoadVisualizer = new ChunkLoadVisualizer(96, 8, world.getName());
		// Identify already loaded chunks as always loaded (spawn chunks)
		Chunk[] loadedChunks = world.getLoadedChunks();
		for (Chunk chunk : loadedChunks) {
			chunkLoadVisualizer.onChunkAlwaysLoaded(chunk.getX(), chunk.getZ());
		}
		
		// Setup periodic runnable to unload each entity's last active region after `inactiveRegionLoadTime` of inactivity
		new BukkitRunnable() {
			@Override
			public void run() {
				long now = world.getGameTime();
				
				entityActiveTimes.entrySet().removeIf(entry -> {
					long entityActiveTime = entry.getValue();
					if ((now - entityActiveTime) < inactiveRegionLoadTime) return false;
					// Entity was inactive for longer than `inactiveRegionLoadTime`, remove last active region
					ChunkCoord coord = entityActiveRegions.remove(entry.getKey());
					removeRegion(coord);
					return true;
				});
			}
		}.runTaskTimer(plugin, updateInterval, updateInterval);
	}
	
	
	public void onEntityActivity(UUID entityUID, int x, int z) {
		// Update last active time for entity
		entityActiveTimes.put(entityUID, world.getGameTime());
		
		// Update position of active region for entity
		ChunkCoord newCoord = new ChunkCoord(x, z);
		ChunkCoord oldCoord = entityActiveRegions.put(entityUID, newCoord);
		chunkLoadVisualizer.onEntityPositionUpdate(entityUID, x, z);
		
		if (newCoord.equals(oldCoord)) return;
		// Position changed, update chunk tickets
		addRegion(newCoord);
		
		if (oldCoord == null) return;
		// No previous position, no chunk tickets to remove
		removeRegion(oldCoord);
	}
	
	public void onEntityCreated(UUID entityUID, int x, int z) {
		chunkLoadVisualizer.onEntityPositionUpdate(entityUID, x, z);
	}
	public void onEntityDestroyed(UUID entityUID) {
		chunkLoadVisualizer.onEntityRemoved(entityUID);
	}
	
	public void onPlayerActivity(UUID playerUID, int x, int z) {
		chunkLoadVisualizer.onPlayerPositionUpdate(playerUID, x, z);
	}
	
	private void addRegion(ChunkCoord coord) {
		Iterator<ChunkCoord> region = createRegionIterator(coord);
		region.forEachRemaining(this::addChunkTicket);
	}
	private void removeRegion(ChunkCoord coord) {
		Iterator<ChunkCoord> region = createRegionIterator(coord);
		region.forEachRemaining(this::removeChunkTicket);
	}
	
	private void addChunkTicket(ChunkCoord coord) {
		boolean isFirstOccurrence = chunkTickets.add(coord, 1) == 0;
		if (isFirstOccurrence) {
			// New chunk to keep loaded, add ticket
			int x = coord.getX();
			int z = coord.getZ();
			world.addPluginChunkTicket(x, z, plugin);
			onChunkTicketAdd(x, z);
		}
	}
	private void removeChunkTicket(ChunkCoord coord) {
		boolean wasLastOccurrence = chunkTickets.remove(coord, 1) == 1;
		if (wasLastOccurrence) {
			// Chunk not required to keep loaded, remove ticket
			int x = coord.getX();
			int z = coord.getZ();
			world.removePluginChunkTicket(x, z, plugin);
			onChunkTicketRemove(x, z);
		}
	}
	
	private Iterator<ChunkCoord> createRegionIterator(ChunkCoord center) {
		return new Iterator<ChunkCoord>() {
			private final int x = center.getX();
			private final int z = center.getZ();
			private final int r = regionRadius;
			private int i = -r;
			private int j = -r;
			
			@Override
			public boolean hasNext() {
				return j <= r;
			}
			@Override
			public ChunkCoord next() {
				ChunkCoord coord = new ChunkCoord(x+i, z+j);
				i++;
				if (i > r) {
					i = -r;
					j++;
				}
				return coord;
			}
		};
	}
	
	
	public void onChunkLoaded(int x, int z) {
		chunkLoadVisualizer.onChunkLoaded(x, z);
	}
	public void onChunkUnloaded(int x, int z) {
		chunkLoadVisualizer.onChunkUnloaded(x, z);
	}
	
	public void onEntitiesLoaded(int x, int z) {
		chunkLoadVisualizer.onEntitiesLoaded(x, z);
	}
	public void onEntitiesUnloaded(int x, int z) {
		chunkLoadVisualizer.onEntitiesUnloaded(x, z);
	}
	
	public void onChunkTicketAdd(int x, int z) {
		plugin.getLogger().log(Level.INFO, "ADD " + x + " " + z);
		chunkLoadVisualizer.onChunkTicketAdd(x, z);
	}
	public void onChunkTicketRemove(int x, int z) {
		plugin.getLogger().log(Level.INFO, "REMOVE " + x + " " + z);
		chunkLoadVisualizer.onChunkTicketRemove(x, z);
	}
}
