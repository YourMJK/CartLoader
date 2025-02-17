package com.mjkrempl.cartchunkloader.Events;

import com.mjkrempl.cartchunkloader.ChunkManagement.GlobalChunkManager;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.logging.Level;

public class VehicleEventListener implements Listener {
	private final JavaPlugin plugin;
	private final GlobalChunkManager chunkManager;
	private final Set<EntityType> entityTypes;
	private final double speedThreshold;
	private final int updateInterval;
	
	public VehicleEventListener(JavaPlugin plugin, GlobalChunkManager chunkManager, Set<EntityType> entityTypes, double speedThreshold, int updateInterval) {
		this.plugin = plugin;
		this.chunkManager = chunkManager;
		this.entityTypes = entityTypes;
		this.speedThreshold = speedThreshold;
		this.updateInterval = updateInterval;
	}
	
	@EventHandler
	public void onVehicleMove(VehicleMoveEvent event) {
		Vehicle vehicle = event.getVehicle();
		// Ignore non-specified vehicles
		if (!entityTypes.contains(vehicle.getType())) return;
		
		double speed = vehicle.getVelocity().length();
		int ticks = vehicle.getTicksLived();
		
		// Only update chunks occasionally and only for significantly moving vehicles
		if (speed >= speedThreshold && ticks % updateInterval == 0) {
			chunkManager.onEntityActivity(vehicle);
		}
	}
}
