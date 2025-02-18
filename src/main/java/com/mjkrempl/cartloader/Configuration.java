package com.mjkrempl.cartloader;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

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
	public final int updateInterval;
	public final double speedThreshold;
	public final int configVersion;

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
}
