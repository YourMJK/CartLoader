package com.mjkrempl.cartloader.ChunkManagement;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.UUID;

public final class WorldSavedState {
	public final Map<UUID, Long> entityRegions;
	
	public WorldSavedState(Map<UUID, Long> entityRegions) {
		this.entityRegions = entityRegions;
	}
	
	public static WorldSavedState fromRegions(Map<UUID, ChunkCoord> entityActiveRegions) {
		// Save the coordinates of all recently active entities
		return new WorldSavedState(Maps.transformValues(entityActiveRegions, ChunkCoord::getPairValue));
	}
}
