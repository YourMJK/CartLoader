package com.mjkrempl.cartloader.Events;

import com.mjkrempl.cartloader.Minecart.CustomMinecart;
import com.mjkrempl.cartloader.Minecart.MinecartType;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class VehicleDestroyEventListener implements Listener {
	@EventHandler
	public void onEntityDropItem(EntityDropItemEvent event) {
		// Ignore non-custom vehicles (not using cache)
		Entity entity = event.getEntity();
		if (!CustomMinecart.isEntity(entity)) return;
		
		// Replace item with custom minecart item
		Item item = event.getItemDrop();
		Material material = item.getItemStack().getType();
		MinecartType type = MinecartType.fromMaterial(material);
		if (type != null) {
			ItemStack customItemStack = CustomMinecart.getItem(type);
			item.setItemStack(customItemStack);
		}
	}
}
