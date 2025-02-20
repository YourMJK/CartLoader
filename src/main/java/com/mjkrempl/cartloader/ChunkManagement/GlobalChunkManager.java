package com.mjkrempl.cartloader.ChunkManagement;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class GlobalChunkManager {
	private final JavaPlugin plugin;
	private final Map<UUID, WorldChunkManager> managers;
	private final ChunkManagerConfiguration configuration;
	
	public GlobalChunkManager(JavaPlugin plugin, ChunkManagerConfiguration configuration) {
		this.plugin = plugin;
		this.managers = new HashMap<>();
		this.configuration = configuration;
		
		// Create a new chunk manager for each world
		for (World world : plugin.getServer().getWorlds()) {
			WorldChunkManager manager = new WorldChunkManager(plugin, world, configuration);
			managers.put(world.getUID(), manager);
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
			return new WorldChunkManager(plugin, world, configuration);
		});
	}
}
