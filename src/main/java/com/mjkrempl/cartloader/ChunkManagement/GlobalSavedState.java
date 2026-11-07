package com.mjkrempl.cartloader.ChunkManagement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GlobalSavedState {
	public final Map<UUID, WorldSavedState> worldStates;
	
	public GlobalSavedState(Map<UUID, WorldSavedState> worldStates) {
		this.worldStates = worldStates;
	}
	
	public static GlobalSavedState fromManagers(Map<UUID, WorldChunkManager> managers) {
		// Retrieve saved state for each world. Copy eagerly because the manager map may be
		// modified by region threads while the server is shutting down.
		Map<UUID, WorldSavedState> worldStates = new HashMap<>(managers.size());
		managers.forEach((worldUID, manager) -> worldStates.put(worldUID, manager.getSavedState()));
		return new GlobalSavedState(worldStates);
	}
}
