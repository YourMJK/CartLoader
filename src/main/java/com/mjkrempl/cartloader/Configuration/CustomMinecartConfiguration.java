package com.mjkrempl.cartloader.Configuration;

import com.mjkrempl.cartloader.CartLoader;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public class CustomMinecartConfiguration {
	public final boolean enabled;
	public final boolean craftable;
	public final Material craftingRecipeIngredient;
	
	public CustomMinecartConfiguration(ConfigurationSection config) {
		this.enabled = config.getBoolean("enabled", true);
		
		boolean craftable = config.getBoolean("craftable", false);
		Material craftingRecipeIngredient = Configuration.getMaterialFromKey(config.getString("crafting-recipe-ingredient", "minecraft:map"));
		if (craftable && craftingRecipeIngredient == null) {
			CartLoader.log(Level.WARNING, "Disabling crafting recipe due to invalid ingredient.");
			craftable = false;
		}
		
		this.craftable = craftable;
		this.craftingRecipeIngredient = craftingRecipeIngredient;
	}
	
	@Override
	public String toString() {
		return "{" +
			"enabled=" + enabled +
			", craftable=" + craftable +
			", craftingRecipeIngredient=" + craftingRecipeIngredient +
			'}';
	}
}
