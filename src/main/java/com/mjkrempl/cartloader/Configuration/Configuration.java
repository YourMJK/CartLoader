package com.mjkrempl.cartloader.Configuration;

import com.mjkrempl.cartloader.CartLoader;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.simpleyaml.configuration.file.YamlFile;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;

public class Configuration {
	public final boolean enabled;
	public final VanillaMinecartConfiguration vanillaMinecarts;
	public final CustomMinecartConfiguration customMinecarts;
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
		
		this.vanillaMinecarts = new VanillaMinecartConfiguration(getConfigurationSection(config, "minecarts"));
		this.customMinecarts = new CustomMinecartConfiguration(getConfigurationSection(config, "custom-minecarts"));
		
		this.regionRadius = getClampedInt(plugin, "region-radius", 2, 1);
		this.keepLastRegionLoadedTime = getClampedInt(plugin, "keep-last-region-loaded-time", 600, 0);
		this.restoreRegionsAfterRestart = config.getBoolean("restore-regions-after-restart", true);
		this.updateInterval = getClampedInt(plugin, "update-interval", 8, 1);
		this.speedThreshold = config.getDouble("speed-threshold", 0.001);
		
		this.configVersion = config.getInt("config-version");
	}
	
	public static ConfigurationSection getConfigurationSection(ConfigurationSection config, String path) {
		ConfigurationSection section = config.getConfigurationSection(path);
		if (section == null) return new MemoryConfiguration();
		return section;
	}
	
	public static int getClampedInt(JavaPlugin plugin, String path, int def, int min) {
		int value = plugin.getConfig().getInt(path, def);
		if (value >= min) return value;
		
		CartLoader.log(Level.WARNING, "Config value \"" + path + "\" needs to be at least " + min + "!");
		return min;
	}
	
	public static @Nullable Material getMaterialFromKey(String keyString) {
		NamespacedKey key = NamespacedKey.fromString(keyString);
		if (key == null) return null;
		Material material = Registry.MATERIAL.get(key);
		if (material != null) return material;
		
		CartLoader.log(Level.SEVERE, "Invalid item ID \"" + keyString + "\"! Must be a valid resource location like \"minecraft:map\".");
		return null;
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
	
	
	@Override
	public String toString() {
		return "{" +
			"enabled=" + enabled +
			", vanillaMinecarts=" + vanillaMinecarts +
			", customMinecarts=" + customMinecarts +
			", regionRadius=" + regionRadius +
			", keepLastRegionLoadedTime=" + keepLastRegionLoadedTime +
			", restoreRegionsAfterRestart=" + restoreRegionsAfterRestart +
			", updateInterval=" + updateInterval +
			", speedThreshold=" + speedThreshold +
			", configVersion=" + configVersion +
			'}';
	}
}
