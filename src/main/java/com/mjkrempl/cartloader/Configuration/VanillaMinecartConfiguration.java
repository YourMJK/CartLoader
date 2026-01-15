package com.mjkrempl.cartloader.Configuration;

import org.bukkit.configuration.ConfigurationSection;

public class VanillaMinecartConfiguration {
	public final boolean normal;
	public final boolean chest;
	public final boolean furnace;
	public final boolean tnt;
	public final boolean hopper;
	public final boolean spawner;
	public final boolean commandBlock;
	
	public VanillaMinecartConfiguration(ConfigurationSection config) {
		this.normal = config.getBoolean("normal", false);
		this.chest = config.getBoolean("chest", true);
		this.furnace = config.getBoolean("furnace", true);
		this.tnt = config.getBoolean("tnt", true);
		this.hopper = config.getBoolean("hopper", true);
		this.spawner = config.getBoolean("spawner", true);
		this.commandBlock = config.getBoolean("command-block", true);
	}
	
	@Override
	public String toString() {
		return "{" +
			"normal=" + normal +
			", chest=" + chest +
			", furnace=" + furnace +
			", tnt=" + tnt +
			", hopper=" + hopper +
			", spawner=" + spawner +
			", commandBlock=" + commandBlock +
			'}';
	}
}
