package com.mjkrempl.cartloader.ChunkManagement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WorldSavedState {
	public final Map<UUID, Long> entityRegions;
	
	public WorldSavedState(Map<UUID, Long> entityRegions) {
		this.entityRegions = entityRegions;
	}
	
	public static WorldSavedState fromRegions(Map<UUID, ChunkCoord> entityActiveRegions) {
		// Save the coordinates of all recently active entities. Copy eagerly so the result is
		// independent of the live map, which may be modified by region threads afterwards.
		Map<UUID, Long> entityRegions = new HashMap<>(entityActiveRegions.size());
		entityActiveRegions.forEach((entityUID, coord) -> entityRegions.put(entityUID, coord.getPairValue()));
		return new WorldSavedState(entityRegions);
	}
}
