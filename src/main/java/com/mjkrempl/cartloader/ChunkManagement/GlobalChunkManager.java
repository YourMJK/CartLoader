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
	
	public GlobalChunkManager(JavaPlugin plugin, GlobalSavedState globalState, ChunkManagerConfiguration configuration) {
		this.plugin = plugin;
		this.managers = new HashMap<>();
		this.configuration = configuration;
		
		// Create a new chunk manager for each world with its saved state
		for (World world : plugin.getServer().getWorlds()) {
			UUID worldUID = world.getUID();
			
			// Inject saved state if present
			WorldSavedState worldState = null;
			if (globalState != null) {
				worldState = globalState.worldStates.get(worldUID);
			}
			
			WorldChunkManager manager = new WorldChunkManager(plugin, world, worldState, configuration);
			managers.put(worldUID, manager);
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
	
	public GlobalSavedState getSavedStates() {
		return GlobalSavedState.fromManagers(managers);
	}
	
	
	private WorldChunkManager getManager(UUID worldUID) {
		return managers.computeIfAbsent(worldUID, k -> {
			World world = plugin.getServer().getWorld(worldUID);
			assert world != null;
			return new WorldChunkManager(plugin, world, null, configuration);
		});
	}
}
