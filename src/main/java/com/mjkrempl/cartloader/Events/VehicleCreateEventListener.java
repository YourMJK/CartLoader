package com.mjkrempl.cartloader.Events;

import com.mjkrempl.cartloader.Minecart.CustomMinecart;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public class VehicleCreateEventListener implements Listener {
	private record CustomMinecartDispenseEvent(long time, Location location) {}
	
	private @Nullable CustomMinecartDispenseEvent dispenseEvent;
	
	
	@EventHandler
	public void onVehicleCreate(VehicleCreateEvent event) {
		// Check if custom minecart was dispensed
		if (dispenseEvent == null) return;
		
		// Check if dispense event happened this tick, remove event if not
		Vehicle vehicle = event.getVehicle();
		long now = vehicle.getWorld().getGameTime();
		if (dispenseEvent.time != now) {
			dispenseEvent = null;
			return;
		}
		
		// Check if vehicle was created next to dispenser (i.e. distanceSquared == 1.0)
		Location vehicleLocation = roundToBlockCoordinates(vehicle.getLocation());
		if (dispenseEvent.location.getWorld() != vehicleLocation.getWorld()) return;
		double distanceSquared = dispenseEvent.location.distanceSquared(vehicleLocation);
		if (distanceSquared > 1.0) return;
		
		// Set entity to custom minecart and remove event
		CustomMinecart.setEntityData(vehicle);
		dispenseEvent = null;
	}
	
	@EventHandler
	public void onEntityPlace(EntityPlaceEvent event) {
		// Get item
		Player player = event.getPlayer();
		if (player == null) return;
		EquipmentSlot slot = event.getHand();
		ItemStack item = player.getInventory().getItem(slot);
		
		// Check if item is custom minecart
		if (CustomMinecart.isItem(item)) {
			// Set entity to custom minecart
			CustomMinecart.setEntityData(event.getEntity());
		}
	}
	
	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event) {
		ItemStack item = event.getItem();
		
		// Check if dispensed item is a custom minecart
		boolean isCustom = CustomMinecart.isItem(item);
		if (!isCustom) return;
		
		// Get dispenser location and current game time
		Location location = event.getBlock().getLocation();
		World world = location.getWorld();
		if (world == null) return;
		long time = world.getGameTime();
		
		// Remember dispense event for upcoming VehicleCreateEvent
		dispenseEvent = new CustomMinecartDispenseEvent(time, location);
	}
	
	
	private static Location roundToBlockCoordinates(Location loc) {
		loc.setX(loc.getBlockX());
		loc.setY(loc.getBlockY());
		loc.setZ(loc.getBlockZ());
		return loc;
	}
}
