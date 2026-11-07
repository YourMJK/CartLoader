package com.mjkrempl.cartloader.Minecart;

import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CustomMinecartEntityCache {
	private final Map<UUID, Boolean> isCustomMap = new ConcurrentHashMap<>();
	
	public boolean isCustomMinecart(Entity entity) {
		UUID entityUID = entity.getUniqueId();
		return isCustomMap.computeIfAbsent(entityUID, key -> CustomMinecart.isEntity(entity));
	}
	
	public void remove(Entity entity) {
		UUID entityUID = entity.getUniqueId();
		isCustomMap.remove(entityUID);
	}
}
