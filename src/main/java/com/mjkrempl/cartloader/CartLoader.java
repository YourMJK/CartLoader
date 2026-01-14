package com.mjkrempl.cartloader;

import com.mjkrempl.cartloader.ChunkManagement.ChunkManagerConfiguration;
import com.mjkrempl.cartloader.ChunkManagement.GlobalChunkManager;
import com.mjkrempl.cartloader.ChunkManagement.GlobalSavedState;
import com.mjkrempl.cartloader.Events.ChunkEventListener;
import com.mjkrempl.cartloader.Events.PlayerEventListener;
import com.mjkrempl.cartloader.ChunkManagement.WorldSavedState;
import com.mjkrempl.cartloader.Commands.CartLoaderCommand;
import com.mjkrempl.cartloader.Commands.GiveSubcommand;
import com.mjkrempl.cartloader.Commands.HelpSubcommand;
import com.mjkrempl.cartloader.Events.VehicleDestroyEventListener;
import com.mjkrempl.cartloader.Events.VehicleUpdateEventListener;

import com.mjkrempl.cartloader.Minecart.CustomMinecartEntityCache;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class CartLoader extends JavaPlugin {
	private Configuration config;
	private StateStorage stateStorage;
	private GlobalSavedState lastSavedState;
	private GlobalChunkManager chunkManager;
	
	public CartLoader() {
		super();
		logger = getLogger();
	}
	
	
	// - Plugin
	
	@Override
	public void onLoad() {
		saveDefaultConfig();
		Configuration.migrateIfNecessary(this);
		config = new Configuration(this);
		
		// Don't set up if config wishes to disable functionality
		if (!config.enabled) return;
		
		// Load last chunk manager states
		if (config.restoreRegionsAfterRestart) {
			getLogger().log(Level.INFO, "Loading saved state");
			File stateStorageDirectory = new File(getDataFolder(), "saved-states");
			stateStorage = new StateStorage(stateStorageDirectory, getLogger());
			lastSavedState = stateStorage.load();
		}
	}
	
	@Override
	public void onEnable() {
		if (!config.enabled) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		// Log number of restored regions per world from saved state
		if (lastSavedState != null && !lastSavedState.worldStates.isEmpty()) {
			getLogger().log(Level.INFO, "Restored saved state");
			lastSavedState.worldStates.forEach((worldUID, worldState) -> {
				logRegionsInWorldState(worldUID, worldState, "Loading");
			});
		}
		
		// Setup chunk manager and entity types
		ChunkManagerConfiguration managerConfig = new ChunkManagerConfiguration(config.regionRadius, config.keepLastRegionLoadedTime, config.updateInterval);
		chunkManager = new GlobalChunkManager(this, lastSavedState, managerConfig);
		
		Set<EntityType> vehicleEventEntityTypes = new HashSet<>();
		CustomMinecartEntityCache vehicleEventEntityCache = new CustomMinecartEntityCache();
		if (config.minecart) vehicleEventEntityTypes.add(EntityType.MINECART);
		if (config.minecartChest) vehicleEventEntityTypes.add(EntityType.CHEST_MINECART);
		if (config.minecartFurnace) vehicleEventEntityTypes.add(EntityType.FURNACE_MINECART);
		if (config.minecartTNT) vehicleEventEntityTypes.add(EntityType.TNT_MINECART);
		if (config.minecartHopper) vehicleEventEntityTypes.add(EntityType.HOPPER_MINECART);
		if (config.minecartSpawner) vehicleEventEntityTypes.add(EntityType.SPAWNER_MINECART);
		if (config.minecartCommandBlock) vehicleEventEntityTypes.add(EntityType.COMMAND_BLOCK_MINECART);
		
		// Register event handlers
		registerEventListener(new VehicleUpdateEventListener(
			chunkManager,
			vehicleEventEntityTypes,
			vehicleEventEntityCache,
			config.speedThreshold,
			config.updateInterval
		));
		registerEventListener(new VehicleDestroyEventListener());
		registerEventListener(new ChunkEventListener(this, chunkManager));
		registerEventListener(new PlayerEventListener(this, chunkManager));
		
		// Register commands
		final String cmdLabel = "cartloader";
		CartLoaderCommand cmd = new CartLoaderCommand(this, cmdLabel);
		registerCommand(cmdLabel, cmd, cmd);
		cmd.registerSubcommand("help", new HelpSubcommand(cmd));
		cmd.registerSubcommand("give", new GiveSubcommand(getServer()));
	}
	
	@Override
	public void onDisable() {
		if (!config.enabled) return;
		
		// Save current chunk manager states
		if (config.restoreRegionsAfterRestart) {
			getLogger().log(Level.INFO, "Saving state");
			GlobalSavedState savedStates = chunkManager.getSavedStates();
			stateStorage.save(savedStates);
			
			// Log number of saved regions per world
			savedStates.worldStates.forEach((worldUID, worldState) -> {
				logRegionsInWorldState(worldUID, worldState, "Saved");
			});
		}
	}
	
	
	// - Helpers
	
	private void registerEventListener(Listener listener) {
		getServer().getPluginManager().registerEvents(listener, this);
	}
	
	private void registerCommand(String label, CommandExecutor executor, TabCompleter completer) {
		PluginCommand command = Objects.requireNonNull(getCommand(label));
		command.setExecutor(executor);
		command.setTabCompleter(completer);
	}
	
	
	// - Logging
	
	private static Logger logger;
	
	public static void log(java.util.logging.Level level, Object... items) {
		String message = Arrays.stream(items)
			.map(o -> o == null ? "(null)" : o)
			.map(Object::toString)
			.collect(Collectors.joining(" "));
		logger.log(level, message);
	}
	
	private void logRegionsInWorldState(UUID worldUID, WorldSavedState worldState, String prefix) {
		if (worldState.entityRegions.isEmpty()) return;
		
		int numberOfRegions = worldState.entityRegions.size();
		World world = getServer().getWorld(worldUID);
		String worldName = (world != null) ? world.getName() : worldUID.toString();
		
		log(Level.INFO, prefix + " " + numberOfRegions + " regions in \"" + worldName + "\"");
	}
}
