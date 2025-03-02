package com.mjkrempl.cartloader;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;

public class Configuration {
	public final boolean enabled;
	public final boolean minecart;
	public final boolean minecartChest;
	public final boolean minecartFurnace;
	public final boolean minecartTNT;
	public final boolean minecartHopper;
	public final boolean minecartSpawner;
	public final boolean minecartCommandBlock;
	public final int regionRadius;
	public final int keepLastRegionLoadedTime;
	public final boolean restoreRegionsAfterRestart;
	public final int updateInterval;
	public final double speedThreshold;
	public final int configVersion;
	
	private static final String configVersionKey = "config-version";
	private static final String filename = "config.yml";
	private static final String filenameBackup = filename + ".old";

	public Configuration(JavaPlugin plugin) {
		FileConfiguration config = plugin.getConfig();

		this.enabled = config.getBoolean("enabled", true);
		
		this.minecart = config.getBoolean("minecarts.normal", false);
		this.minecartChest = config.getBoolean("minecarts.chest", true);
		this.minecartFurnace = config.getBoolean("minecarts.furnace", true);
		this.minecartTNT = config.getBoolean("minecarts.tnt", true);
		this.minecartHopper = config.getBoolean("minecarts.hopper", true);
		this.minecartSpawner = config.getBoolean("minecarts.spawner", true);
		this.minecartCommandBlock = config.getBoolean("minecarts.command-block", true);
		
		this.regionRadius = getClampedInt(plugin, "region-radius", 2, 1);
		this.keepLastRegionLoadedTime = getClampedInt(plugin, "keep-last-region-loaded-time", 600, 0);
		this.restoreRegionsAfterRestart = config.getBoolean("restore-regions-after-restart", true);
		this.updateInterval = getClampedInt(plugin, "update-interval", 8, 1);
		this.speedThreshold = config.getDouble("speed-threshold", 0.001);
		this.configVersion = config.getInt("config-version");
	}
	
	private static int getClampedInt(JavaPlugin plugin, String path, int def, int min) {
		int value = plugin.getConfig().getInt(path, def);
		if (value >= min) return value;
		
		plugin.getLogger().log(Level.WARNING, "Config value \"" + path + "\" needs to be at least " + min + "!");
		return min;
	}
	
	
	public static void migrateIfNecessary(JavaPlugin plugin) {
		// Read the newest config-version from config file in resources
		InputStream newConfigStream = plugin.getResource(filename);
		if (newConfigStream == null) return;
		YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(newConfigStream, StandardCharsets.UTF_8));
		int newVersion = newConfig.getInt(configVersionKey);
		
		// Read current config-version from config file in data folder
		int currentVersion = plugin.getConfig().getInt(configVersionKey, 0);
		
		// Migrate if current version is older
		if (currentVersion >= newVersion) return;
		try {
			plugin.getLogger().log(Level.INFO, "Migrating config file from version " + currentVersion + " to " + newVersion);
			migrate(plugin);
		}
		catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE, "Couldn't migrate config file from version " + currentVersion + " to " + newVersion + ": " + e.getMessage());
		}
	}
	
	private static void migrate(JavaPlugin plugin) throws Exception {
		File config = new File(plugin.getDataFolder(), filename);
		File oldConfig = new File(plugin.getDataFolder(), filenameBackup);
		
		// Delete existing backup file if one exists
		if (oldConfig.exists() && !oldConfig.delete()) {
			throw new Exception("Couldn't delete existing config backup file " + oldConfig.getPath());
		}
		
		// Rename current config file to backup name
		if (!config.renameTo(oldConfig)) {
			throw new Exception("Couldn't rename config file " + config.getPath() + " to " + oldConfig.getPath());
		}
		
		// Copy new clean config file from resources
		plugin.saveResource(filename, false);
		
		// Copy all values (but config version) from old config file to new clean config file, preserving comments and format from latter
		YamlFile newConfigYaml = new YamlFile(config);
		YamlFile oldConfigYaml = new YamlFile(oldConfig);
		newConfigYaml.loadWithComments();
		oldConfigYaml.load();
		
		Map<String, Object> values = oldConfigYaml.getValues(true);
		values.remove(configVersionKey);
		values.forEach(newConfigYaml::set);
		
		newConfigYaml.save();
	}
}
