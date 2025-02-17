package com.mjkrempl.cartchunkloader.Events;

import com.mjkrempl.cartchunkloader.ChunkManagement.GlobalChunkManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class PlayerEventListener implements Listener {
	private final JavaPlugin plugin;
	private final GlobalChunkManager chunkManager;
	
	public PlayerEventListener(JavaPlugin plugin, GlobalChunkManager chunkManager) {
		this.plugin = plugin;
		this.chunkManager = chunkManager;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		chunkManager.setPlayerActive(player);
	}
}
