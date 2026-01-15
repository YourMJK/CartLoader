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
import com.mjkrempl.cartloader.Configuration.Configuration;
import com.mjkrempl.cartloader.Events.VehicleDestroyEventListener;
import com.mjkrempl.cartloader.Events.VehicleUpdateEventListener;

import com.mjkrempl.cartloader.Minecart.CustomMinecart;
import com.mjkrempl.cartloader.Minecart.CustomMinecartEntityCache;
import com.mjkrempl.cartloader.Minecart.MinecartType;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.CraftingRecipe;
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
			log(Level.INFO, "Loading saved state");
			File stateStorageDirectory = new File(getDataFolder(), "saved-states");
			stateStorage = new StateStorage(stateStorageDirectory);
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
			log(Level.INFO, "Restored saved state");
			lastSavedState.worldStates.forEach((worldUID, worldState) -> {
				logRegionsInWorldState(worldUID, worldState, "Loading");
			});
		}
		
		// Setup chunk manager and entity types
		ChunkManagerConfiguration managerConfig = new ChunkManagerConfiguration(config.regionRadius, config.keepLastRegionLoadedTime, config.updateInterval);
		chunkManager = new GlobalChunkManager(this, lastSavedState, managerConfig);
		
		// Setup entity types
		Set<EntityType> vehicleEventEntityTypes = new HashSet<>();
		if (config.vanillaMinecarts.normal) vehicleEventEntityTypes.add(EntityType.MINECART);
		if (config.vanillaMinecarts.chest) vehicleEventEntityTypes.add(EntityType.CHEST_MINECART);
		if (config.vanillaMinecarts.furnace) vehicleEventEntityTypes.add(EntityType.FURNACE_MINECART);
		if (config.vanillaMinecarts.tnt) vehicleEventEntityTypes.add(EntityType.TNT_MINECART);
		if (config.vanillaMinecarts.hopper) vehicleEventEntityTypes.add(EntityType.HOPPER_MINECART);
		if (config.vanillaMinecarts.spawner) vehicleEventEntityTypes.add(EntityType.SPAWNER_MINECART);
		if (config.vanillaMinecarts.commandBlock) vehicleEventEntityTypes.add(EntityType.COMMAND_BLOCK_MINECART);
		
		// Determine whether vanilla and custom minecarts checks are needed at all, disable checks if not
		if (vehicleEventEntityTypes.isEmpty()) vehicleEventEntityTypes = null;
		CustomMinecartEntityCache vehicleEventEntityCache = config.customMinecarts.enabled ? new CustomMinecartEntityCache() : null;
		
		// Register event handlers
		registerEventListener(new VehicleUpdateEventListener(
			chunkManager,
			vehicleEventEntityTypes,
			vehicleEventEntityCache,
			config.speedThreshold,
			config.updateInterval
		));
		if (config.customMinecarts.enabled) {
			registerEventListener(new VehicleDestroyEventListener());
		}
		registerEventListener(new ChunkEventListener(this, chunkManager));
		registerEventListener(new PlayerEventListener(this, chunkManager));
		
		// Register commands
		String cmdLabel = "cartloader";
		CartLoaderCommand cmd = new CartLoaderCommand(this, cmdLabel);
		registerCommand(cmdLabel, cmd, cmd);
		cmd.registerSubcommand("help", new HelpSubcommand(cmd));
		if (config.customMinecarts.enabled) {
			cmd.registerSubcommand("give", new GiveSubcommand(getServer()));
		}
		
		// Register recipes
		if (config.customMinecarts.craftable) {
			Material ingredient = config.customMinecarts.craftingRecipeIngredient;
			for (MinecartType type : MinecartType.all) {
				registerRecipe(CustomMinecart.getRecipe(type, ingredient, this));
			}
		}
	}
	
	@Override
	public void onDisable() {
		if (!config.enabled) return;
		
		// Save current chunk manager states
		if (config.restoreRegionsAfterRestart && chunkManager != null && stateStorage != null) {
			log(Level.INFO, "Saving state");
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
	
	private void registerRecipe(CraftingRecipe recipe) {
		Bukkit.addRecipe(recipe);
	}
	
	
	// - Logging
	
	private static Logger logger;
	
	public static void log(Level level, Object... items) {
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
