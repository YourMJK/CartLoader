package com.mjkrempl.cartloader;

import com.mjkrempl.cartloader.ChunkManagement.GlobalSavedState;
import com.mjkrempl.cartloader.ChunkManagement.WorldSavedState;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.common.reflect.TypeToken;
import com.google.common.io.Files;

public final class StateStorage {
	private final File directory;
	
	private static final Gson gson = new Gson();;
	private static final Type mapType = new TypeToken<Map<UUID, Long>>(){}.getType();
	private static final String jsonExtension = ".json";
	private static final FilenameFilter jsonFilenameFilter = (dir, name) -> name.endsWith(jsonExtension);
	
	public StateStorage(File directory) {
		this.directory = directory;
		
		// Create directory if it doesn't exist already
		if (!directory.isDirectory()) {
			boolean success = directory.mkdir();
			if (!success) {
				throw new RuntimeException("Couldn't create saved states directory at " + directory.getPath());
			}
		}
	}
	
	
	public void save(GlobalSavedState state) {
		clear();
		state.worldStates.forEach(this::saveWorldState);
	}
	
	public GlobalSavedState load() {
		// Get all JSON files in directory
		File[] files = directory.listFiles(jsonFilenameFilter);
		if (files == null) {
			throw new RuntimeException("Couldn't list files in saved states directory");
		}
		if (files.length == 0) return null;
		
		// Load world states from files
		Map<UUID, WorldSavedState> states = new HashMap<>();
		for (File file : files) {
			// Get world's UUID from file name
			String fileName = file.getName();
			String worldUIDString = fileName.substring(0, fileName.length() - jsonExtension.length());
			UUID worldUID = UUID.fromString(worldUIDString);
			
			// Load world state from file and add to global states
			WorldSavedState state = loadWorldState(file);
			states.put(worldUID, state);
		}
		
		return new GlobalSavedState(states);
	}
	
	
	private void clear() {
		// Delete contents of directory
		File[] contents = directory.listFiles();
		if (contents == null) return;
		for (File file : contents) {
			boolean success = file.delete();
		}
	}
	
	private void saveWorldState(UUID worldUID, WorldSavedState state) {
		// Skip if state is empty
		if (state.entityRegions.isEmpty()) return;
		
		// Create JSON string
		String json = gson.toJson(state.entityRegions);
		
		// Write JSON to new file
		String fileName = worldUID + jsonExtension;
		File file = new File(directory, fileName);
		
		try {
			Files.asCharSink(file, StandardCharsets.UTF_8).write(json);
		}
		catch (IOException e) {
			throw new RuntimeException("Couldn't save state file as " + file.getPath() + ": " + e.getMessage());
		}
	}
	
	private WorldSavedState loadWorldState(File file) {
		// Read file contents
		String json;
		try {
			json = Files.asCharSource(file, StandardCharsets.UTF_8).read();
		}
		catch (IOException e) {
			throw new RuntimeException("Couldn't read state file " + file.getPath() + ": " + e.getMessage());
		}
		
		// Parse JSON string
		Map<UUID, Long> entityRegions = gson.fromJson(json, mapType);
		
		return new WorldSavedState(entityRegions);
	}
}
