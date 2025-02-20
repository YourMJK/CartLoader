package com.mjkrempl.cartloader;

import com.mjkrempl.cartloader.ChunkManagement.ChunkManagerConfiguration;
import com.mjkrempl.cartloader.ChunkManagement.GlobalChunkManager;
import com.mjkrempl.cartloader.Events.ChunkEventListener;
import com.mjkrempl.cartloader.Events.PlayerEventListener;
import com.mjkrempl.cartloader.Events.VehicleEventListener;

import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class CartLoader extends JavaPlugin {
	private Configuration config;
	private GlobalChunkManager chunkManager;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		config = new Configuration(this);
		
		// Don't set up if config wishes to disable functionality
		if (!config.enabled) return;
		
		// Setup chunk manager and entity types
		ChunkManagerConfiguration managerConfig = new ChunkManagerConfiguration(config.regionRadius, config.keepLastRegionLoadedTime, config.updateInterval);
		chunkManager = new GlobalChunkManager(this, managerConfig);
		
		Set<EntityType> vehicleEventEntityTypes = new HashSet<>();
		if (config.minecart) vehicleEventEntityTypes.add(EntityType.MINECART);
		if (config.minecartChest) vehicleEventEntityTypes.add(EntityType.MINECART_CHEST);
		if (config.minecartFurnace) vehicleEventEntityTypes.add(EntityType.MINECART_FURNACE);
		if (config.minecartTNT) vehicleEventEntityTypes.add(EntityType.MINECART_TNT);
		if (config.minecartHopper) vehicleEventEntityTypes.add(EntityType.MINECART_HOPPER);
		if (config.minecartSpawner) vehicleEventEntityTypes.add(EntityType.MINECART_MOB_SPAWNER);
		if (config.minecartCommandBlock) vehicleEventEntityTypes.add(EntityType.MINECART_COMMAND);
		
		// Register event handlers
		VehicleEventListener vehicleEventListener = new VehicleEventListener(this, chunkManager, vehicleEventEntityTypes, config.speedThreshold, config.updateInterval);
		getServer().getPluginManager().registerEvents(vehicleEventListener, this);
		ChunkEventListener chunkEventListener = new ChunkEventListener(this, chunkManager);
		getServer().getPluginManager().registerEvents(chunkEventListener, this);
		PlayerEventListener playerEventListener = new PlayerEventListener(this, chunkManager);
		getServer().getPluginManager().registerEvents(playerEventListener, this);
	}

	@Override
	public void onDisable() {
	
	}
	
}
