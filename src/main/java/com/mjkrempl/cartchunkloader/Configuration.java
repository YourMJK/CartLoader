package com.mjkrempl.cartchunkloader;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

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
		
		ConfigurationSection minecartsSection = config.getConfigurationSection("minecarts");
		this.minecart = minecartsSection.getBoolean("normal", false);
		this.minecartChest = minecartsSection.getBoolean("chest", true);
		this.minecartFurnace = minecartsSection.getBoolean("furnace", true);
		this.minecartTNT = minecartsSection.getBoolean("tnt", true);
		this.minecartHopper = minecartsSection.getBoolean("hopper", true);
		this.minecartSpawner = minecartsSection.getBoolean("spawner", true);
		this.minecartCommandBlock = minecartsSection.getBoolean("command-block", true);
		
		this.regionRadius = config.getInt("region-radius", 2);
		this.keepLastRegionLoadedTime = config.getInt("keep-last-region-loaded-time", 600);
		this.updateInterval = config.getInt("update-interval", 8);
		this.speedThreshold = config.getDouble("speed-threshold", 0.001);
		this.configVersion = config.getInt("config-version");
	}
}
