package com.mjkrempl.cartloader;

import com.mjkrempl.cartloader.ChunkManagement.ChunkManagerConfiguration;
import com.mjkrempl.cartloader.ChunkManagement.GlobalChunkManager;
import com.mjkrempl.cartloader.ChunkManagement.GlobalSavedState;
import com.mjkrempl.cartloader.ChunkManagement.WorldSavedState;
import com.mjkrempl.cartloader.Events.VehicleEventListener;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public final class CartLoader extends JavaPlugin {
	private Configuration config;
	private StateStorage stateStorage;
	private GlobalSavedState lastSavedState;
	private GlobalChunkManager chunkManager;
	
	@Override
	public void onLoad() {
		saveDefaultConfig();
		Configuration.migrateIfNecessary(this);
		config = new Configuration(this);
		
		// Don't set up if config wishes to disable functionality
		if (!config.enabled) return;
		
		// Load last chunk manager states
		if (config.restoreRegionsAfterRestart) {
			getLogger().log(Level.INFO, "Loading saved state");
			File stateStorageDirectory = new File(getDataFolder(), "saved-states");
			stateStorage = new StateStorage(stateStorageDirectory);
			lastSavedState = stateStorage.load();
		}
	}
	
	@Override
	public void onEnable() {
		if (!config.enabled) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		// Log number of restored regions per world from saved state
		if (lastSavedState != null && !lastSavedState.worldStates.isEmpty()) {
			getLogger().log(Level.INFO, "Restored saved state");
			lastSavedState.worldStates.forEach((worldUID, worldState) -> {
				logRegionsInWorldState(worldUID, worldState, "Loading");
			});
		}
		
		// Setup chunk manager and entity types
		ChunkManagerConfiguration managerConfig = new ChunkManagerConfiguration(config.regionRadius, config.keepLastRegionLoadedTime, config.updateInterval);
		chunkManager = new GlobalChunkManager(this, lastSavedState, managerConfig);
		
		Set<EntityType> vehicleEventEntityTypes = new HashSet<>();
		if (config.minecart) vehicleEventEntityTypes.add(EntityType.MINECART);
		if (config.minecartChest) vehicleEventEntityTypes.add(EntityType.MINECART_CHEST);
		if (config.minecartFurnace) vehicleEventEntityTypes.add(EntityType.MINECART_FURNACE);
		if (config.minecartTNT) vehicleEventEntityTypes.add(EntityType.MINECART_TNT);
		if (config.minecartHopper) vehicleEventEntityTypes.add(EntityType.MINECART_HOPPER);
		if (config.minecartSpawner) vehicleEventEntityTypes.add(EntityType.MINECART_MOB_SPAWNER);
		if (config.minecartCommandBlock) vehicleEventEntityTypes.add(EntityType.MINECART_COMMAND);
		
		// Register event handlers
		VehicleEventListener vehicleEventListener = new VehicleEventListener(chunkManager, vehicleEventEntityTypes, config.speedThreshold, config.updateInterval);
		getServer().getPluginManager().registerEvents(vehicleEventListener, this);
	}
	
	@Override
	public void onDisable() {
		if (!config.enabled) return;
		
		// Save current chunk manager states
		if (config.restoreRegionsAfterRestart) {
			getLogger().log(Level.INFO, "Saving state");
			GlobalSavedState savedStates = chunkManager.getSavedStates();
			stateStorage.save(savedStates);
			
			// Log number of saved regions per world
			savedStates.worldStates.forEach((worldUID, worldState) -> {
				logRegionsInWorldState(worldUID, worldState, "Saved");
			});
		}
	}
	
	
	private void logRegionsInWorldState(UUID worldUID, WorldSavedState worldState, String prefix) {
		if (worldState.entityRegions.isEmpty()) return;
		
		int numberOfRegions = worldState.entityRegions.size();
		World world = getServer().getWorld(worldUID);
		String worldName = (world != null) ? world.getName() : worldUID.toString();
		
		getLogger().log(Level.INFO, prefix + " " + numberOfRegions + " regions in \"" + worldName + "\"");
	}
}
