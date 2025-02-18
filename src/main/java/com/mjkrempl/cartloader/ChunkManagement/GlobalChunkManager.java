package com.mjkrempl.cartloader.ChunkManagement;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class GlobalChunkManager {
	private final JavaPlugin plugin;
	private final Map<UUID, WorldChunkManager> managers;
	private final int regionRadius;
	private final int inactiveRegionLoadTime;
	private final int updateInterval;
	
	public GlobalChunkManager(JavaPlugin plugin, int regionRadius, int inactiveRegionLoadTime, int updateInterval) {
		this.plugin = plugin;
		this.managers = new HashMap<>();
		this.regionRadius = regionRadius;
		this.inactiveRegionLoadTime = inactiveRegionLoadTime;
		this.updateInterval = updateInterval;
		
		for (World world : plugin.getServer().getWorlds()) {
			managers.put(world.getUID(), new WorldChunkManager(plugin, world, regionRadius, inactiveRegionLoadTime, updateInterval));
		}
	}
	
	
	public void onEntityActivity(Entity entity) {
		UUID entityUID = entity.getUniqueId();
		UUID worldUID = entity.getWorld().getUID();
		
		// Calculate chunk coordinates through division by 16
		Location location = entity.getLocation();
		int x = location.getBlockX() >> 4;
		int z = location.getBlockZ() >> 4;
		
		getManager(worldUID).onEntityActivity(entityUID, x, z);
	}
	
	
	private WorldChunkManager getManager(UUID worldUID) {
		return managers.computeIfAbsent(worldUID, k -> {
			World world = plugin.getServer().getWorld(worldUID);
			assert world != null;
			return new WorldChunkManager(plugin, world, regionRadius, inactiveRegionLoadTime, updateInterval);
		});
	}
}
