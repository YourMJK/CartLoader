package com.mjkrempl.cartchunkloader;

import com.mjkrempl.cartchunkloader.ChunkManagement.GlobalChunkManager;
import com.mjkrempl.cartchunkloader.Events.VehicleEventListener;

import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class CartChunkLoader extends JavaPlugin {
	private Configuration configuration;
	private GlobalChunkManager chunkManager;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		configuration = new Configuration(this);
		
		// Don't set up if config wishes to disable functionality
		if (!configuration.enabled) return;
		
		// Setup chunk manager and entity types
		chunkManager = new GlobalChunkManager(this, configuration.regionRadius, configuration.keepLastRegionLoadedTime, configuration.updateInterval);
		
		Set<EntityType> vehicleEventEntityTypes = new HashSet<>();
		if (configuration.minecart) vehicleEventEntityTypes.add(EntityType.MINECART);
		if (configuration.minecartChest) vehicleEventEntityTypes.add(EntityType.MINECART_CHEST);
		if (configuration.minecartFurnace) vehicleEventEntityTypes.add(EntityType.MINECART_FURNACE);
		if (configuration.minecartTNT) vehicleEventEntityTypes.add(EntityType.MINECART_TNT);
		if (configuration.minecartHopper) vehicleEventEntityTypes.add(EntityType.MINECART_HOPPER);
		if (configuration.minecartSpawner) vehicleEventEntityTypes.add(EntityType.MINECART_MOB_SPAWNER);
		if (configuration.minecartCommandBlock) vehicleEventEntityTypes.add(EntityType.MINECART_COMMAND);
		
		// Register event handlers
		VehicleEventListener vehicleEventListener = new VehicleEventListener(this, chunkManager, vehicleEventEntityTypes, configuration.speedThreshold, configuration.updateInterval);
		getServer().getPluginManager().registerEvents(vehicleEventListener, this);
	}
}
