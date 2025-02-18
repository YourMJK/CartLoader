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
	
	public void onEntityCreated(Entity entity) {
		UUID entityUID = entity.getUniqueId();
		UUID worldUID = entity.getWorld().getUID();
		
		Location location = entity.getLocation();
		int x = location.getBlockX() >> 4;
		int z = location.getBlockZ() >> 4;
		
		getManager(worldUID).onEntityCreated(entityUID, x, z);
	}
	
	public void onEntityDestroyed(Entity entity) {
		UUID entityUID = entity.getUniqueId();
		UUID worldUID = entity.getWorld().getUID();
		
		getManager(worldUID).onEntityDestroyed(entityUID);
	}
	
	public void onPlayerActivity(Entity entity) {
		UUID entityUID = entity.getUniqueId();
		UUID worldUID = entity.getWorld().getUID();
		
		Location location = entity.getLocation();
		int x = location.getBlockX() >> 4;
		int z = location.getBlockZ() >> 4;
		
		getManager(worldUID).onPlayerActivity(entityUID, x, z);
	}
	
	public void onChunkLoaded(int x, int z, UUID worldUID) {
		getManager(worldUID).onChunkLoaded(x, z);
	}
	public void onChunkUnloaded(int x, int z, UUID worldUID) {
		getManager(worldUID).onChunkUnloaded(x, z);
	}
	
	public void onEntitiesLoaded(int x, int z, UUID worldUID) {
		getManager(worldUID).onEntitiesLoaded(x, z);
	}
	public void onEntitiesUnloaded(int x, int z, UUID worldUID) {
		getManager(worldUID).onEntitiesUnloaded(x, z);
	}
	
	private WorldChunkManager getManager(UUID worldUID) {
		return managers.computeIfAbsent(worldUID, k -> {
			World world = plugin.getServer().getWorld(worldUID);
			assert world != null;
			return new WorldChunkManager(plugin, world, regionRadius, inactiveRegionLoadTime, updateInterval);
		});
	}
}
