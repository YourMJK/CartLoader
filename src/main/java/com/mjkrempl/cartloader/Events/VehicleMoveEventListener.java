package com.mjkrempl.cartloader.Events;

import com.mjkrempl.cartloader.ChunkManagement.GlobalChunkManager;

import com.mjkrempl.cartloader.Minecart.CustomMinecartEntityCache;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import javax.annotation.Nullable;
import java.util.Set;

public class VehicleMoveEventListener implements Listener {
	private final GlobalChunkManager chunkManager;
	private final @Nullable Set<EntityType> entityTypes;
	private final @Nullable CustomMinecartEntityCache entityCache;
	private final double speedThreshold;
	private final int updateInterval;
	
	public VehicleMoveEventListener(GlobalChunkManager chunkManager, @Nullable Set<EntityType> entityTypes, @Nullable CustomMinecartEntityCache entityCache, double speedThreshold, int updateInterval) {
		this.chunkManager = chunkManager;
		this.entityTypes = entityTypes;
		this.entityCache = entityCache;
		this.speedThreshold = speedThreshold;
		this.updateInterval = updateInterval;
	}
	
	@EventHandler
	public void onVehicleMove(VehicleMoveEvent event) {
		Vehicle vehicle = event.getVehicle();
		// Ignore non-specified and non-custom vehicles
		if (!isSpecifiedMinecartType(vehicle) && !isCustomMinecart(vehicle)) return;
		
		double speed = vehicle.getVelocity().length();
		int ticks = vehicle.getTicksLived();
		
		// Only update chunks occasionally and only for significantly moving vehicles
		if (speed >= speedThreshold && ticks % updateInterval == 0) {
			chunkManager.onEntityActivity(vehicle);
		}
	}
	
	private boolean isSpecifiedMinecartType(Vehicle vehicle) {
		return entityTypes != null && entityTypes.contains(vehicle.getType());
	}
	
	private boolean isCustomMinecart(Vehicle vehicle) {
		return entityCache != null && entityCache.isCustomMinecart(vehicle);
	}
}
