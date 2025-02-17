package com.mjkrempl.cartchunkloader.ChunkManagement;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import com.google.common.collect.*;

public final class WorldChunkManager {
	private final JavaPlugin plugin;
	private final World world;
	private final int regionRadius;
	private final Map<UUID, ChunkCoord> entityActiveRegions;
	private final Map<UUID, Long> entityActiveTimes;
	private final Multiset<ChunkCoord> chunkTickets;
	
	public WorldChunkManager(JavaPlugin plugin, World world, int regionRadius, int inactiveRegionLoadTime, int updateInterval) {
		this.plugin = plugin;
		this.world = world;
		this.regionRadius = regionRadius;
		this.entityActiveRegions = new HashMap<>();
		this.entityActiveTimes = new HashMap<>();
		this.chunkTickets = HashMultiset.create();
		
		// Setup periodic runnable to unload each entity's last active region after `inactiveRegionLoadTime` of inactivity
		new BukkitRunnable() {
			@Override
			public void run() {
				long now = world.getGameTime();
				
				// Enumerate previously active entities and the time of their last activity
				entityActiveTimes.entrySet().removeIf(entry -> {
					long entityActiveTime = entry.getValue();
					if ((now - entityActiveTime) < inactiveRegionLoadTime) return false;
					
					// Entity was inactive for longer than `inactiveRegionLoadTime`, remove last active region and forget entity
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
		
		// If position changed, update chunk tickets
		if (newCoord.equals(oldCoord)) return;
		addRegion(newCoord);
		
		// If no previous position, no chunk tickets to remove
		if (oldCoord == null) return;
		removeRegion(oldCoord);
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
		int previousCount = chunkTickets.add(coord, 1);
		boolean isFirstOccurrence = (previousCount == 0);
		if (isFirstOccurrence) {
			// Is new chunk to be kept loaded, add ticket
			int x = coord.getX();
			int z = coord.getZ();
			world.addPluginChunkTicket(x, z, plugin);
		}
	}
	private void removeChunkTicket(ChunkCoord coord) {
		int previousCount = chunkTickets.remove(coord, 1);
		boolean wasLastOccurrence = (previousCount == 1);
		if (wasLastOccurrence) {
			// Chunk isn't required to be kept loaded anymore, remove ticket
			int x = coord.getX();
			int z = coord.getZ();
			world.removePluginChunkTicket(x, z, plugin);
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
				// Iterate coordinates of a square with center at (x,z) and side length (2*r+1),
				// starting at top left (x-r,z-r) and moving left-to-right (+x) and top-to-bottom (+z).
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
}
