package com.mjkrempl.cartloader.ChunkManagement;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.UUID;

public final class GlobalSavedState {
	public final Map<UUID, WorldSavedState> worldStates;
	
	public GlobalSavedState(Map<UUID, WorldSavedState> worldStates) {
		this.worldStates = worldStates;
	}
	
	public static GlobalSavedState fromManagers(Map<UUID, WorldChunkManager> managers) {
		// Retrieve saved state for each world
		return new GlobalSavedState(Maps.transformValues(managers, WorldChunkManager::getSavedState));
	}
}
