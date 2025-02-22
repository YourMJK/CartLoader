package com.mjkrempl.cartloader;

import com.google.common.io.Files;
import com.mjkrempl.cartloader.ChunkManagement.GlobalSavedState;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.common.reflect.TypeToken;
import com.mjkrempl.cartloader.ChunkManagement.WorldSavedState;

public final class StateStorage {
	private final File directory;
	private final Logger logger;
	
	private static final Gson gson = new Gson();;
	private static final Type mapType = new TypeToken<Map<UUID, Long>>(){}.getType();
	private static final String jsonExtension = ".json";
	private static final FilenameFilter jsonFilenameFilter = (dir, name) -> name.endsWith(jsonExtension);
	
	public StateStorage(File directory, Logger logger) {
		this.directory = directory;
		this.logger = logger;
		
		// Create directory if it doesn't exist already
		logger.log(Level.INFO, "Creating directory");
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
			logger.log(Level.INFO, "Found file " + file.getPath());
			// Get world's UUID from file name
			String fileName = file.getName();
			String worldUIDString = fileName.substring(0, fileName.length() - jsonExtension.length());
			logger.log(Level.INFO, "UUID string: " + worldUIDString);
			UUID worldUID = UUID.fromString(worldUIDString);
			
			// Load world state from file and add to global states
			WorldSavedState state = loadWorldState(file);
			states.put(worldUID, state);
		}
		
		return new GlobalSavedState(states);
	}
	
	
	private void clear() {
		logger.log(Level.INFO, "Clearing directory");
		// Delete contents of directory
		File[] contents = directory.listFiles();
		if (contents == null) return;
		for (File file : contents) {
			boolean success = file.delete();
		}
	}
	
	private void saveWorldState(UUID worldUID, WorldSavedState state) {
		// Skip if state is empty
		if (state.entityRegions.isEmpty()) {
			logger.log(Level.INFO, "Skipping because empty: " + worldUID);
			return;
		}
		
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
		logger.log(Level.INFO, "Wrote contents: " + json);
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
		logger.log(Level.INFO, "Read contents: " + json);
		
		// Parse JSON string
		Map<UUID, Long> entityRegions = gson.fromJson(json, mapType);
		
		return new WorldSavedState(entityRegions);
	}
}
