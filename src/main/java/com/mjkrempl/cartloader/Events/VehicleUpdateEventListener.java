package com.mjkrempl.cartloader.Events;

import com.mjkrempl.cartloader.CartLoader;
import com.mjkrempl.cartloader.ChunkManagement.GlobalChunkManager;

import com.mjkrempl.cartloader.Minecart.CustomMinecartEntityCache;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.logging.Level;

public class VehicleUpdateEventListener implements Listener {
	private final GlobalChunkManager chunkManager;
	private final @Nullable Set<EntityType> entityTypes;
	private final @Nullable CustomMinecartEntityCache entityCache;
	private final double speedThreshold;
	private final int updateInterval;
	
	public VehicleUpdateEventListener(GlobalChunkManager chunkManager, @Nullable Set<EntityType> entityTypes, @Nullable CustomMinecartEntityCache entityCache, double speedThreshold, int updateInterval) {
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
		if (!isValid(vehicle)) return;
		
		double speed = vehicle.getVelocity().length();
		int ticks = vehicle.getTicksLived();
		
		// Only update chunks occasionally and only for significantly moving vehicles
		if (speed >= speedThreshold && ticks % updateInterval == 0) {
			//plugin.getLogger().log(Level.INFO, "Moving " + vehicle.getUniqueId() + ": " + ticks + " " + Math.round(speed*100)/100.0 + " " + vehicle.getLocation());
			chunkManager.onEntityActivity(vehicle);
		}
	}
	
	@EventHandler
	public void onVehicleCreate(VehicleCreateEvent event) {
		Vehicle vehicle = event.getVehicle();
		if (!isValid(vehicle)) return;
		
		CartLoader.log(Level.INFO, "Created " + vehicle.getUniqueId());
		chunkManager.onEntityCreated(vehicle);
	}
	
	@EventHandler
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		Vehicle vehicle = event.getVehicle();
		
		// Remove vehicle from cache
		if (entityCache != null) {
			entityCache.remove(vehicle);
		}
		
		if (!isValid(vehicle)) return;
		
		CartLoader.log(Level.INFO, "Destroyed " + vehicle.getUniqueId());
		chunkManager.onEntityDestroyed(vehicle);
	}
	
	
	private boolean isValid(Vehicle vehicle) {
		return isSpecifiedMinecartType(vehicle) || isCustomMinecart(vehicle);
	}
	
	private boolean isSpecifiedMinecartType(Vehicle vehicle) {
		return entityTypes != null && entityTypes.contains(vehicle.getType());
	}
	
	private boolean isCustomMinecart(Vehicle vehicle) {
		return entityCache != null && entityCache.isCustomMinecart(vehicle);
	}
}
