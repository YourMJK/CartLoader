package com.mjkrempl.cartchunkloader;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public final class CartChunkLoader extends JavaPlugin {

	@Override
	public void onEnable() {
		getLogger().log(Level.INFO, "Plugin enabled!");
	}

	@Override
	public void onDisable() {
		getLogger().log(Level.INFO, "Plugin disabled.");
	}

}
