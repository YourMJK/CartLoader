package com.mjkrempl.cartloader.Commands;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public enum PlayerSelector {
	ALL("@a"),
	NEAREST("@p"),
	RANDOM("@r"),
	CURRENT("@s");
	
	public static final List<String> allValues = Arrays.asList(
		ALL.value,
		NEAREST.value,
		RANDOM.value,
		CURRENT.value
	);
	
	
	public final String value;
	
	PlayerSelector(String value) {
		this.value = value;
	}
	
	public static @Nullable PlayerSelector fromValue(String value) {
		return switch (value) {
			case "@a" -> ALL;
			case "@p" -> NEAREST;
			case "@r" -> RANDOM;
			case "@s" -> CURRENT;
			default -> null;
		};
	}
	
	
	public static List<String> getAllAvailableTargetValues(Server server) {
		List<String> onlinePlayerNames = server.getOnlinePlayers().stream().map(Player::getName).toList();
		return Stream.concat(PlayerSelector.allValues.stream(), onlinePlayerNames.stream()).toList();
	}
	
	
	public static List<Player> getTargets(String targetString, CommandSender sender, Server server) {
		return getTargetsNative(targetString, sender, server);
	}
	
	private static List<Player> getTargetsNative(String targetString, CommandSender sender, Server server) {
		List<Player> targets = new ArrayList<>();
		
		PlayerSelector selector = PlayerSelector.fromValue(targetString);
		if (selector != null) {
			// Get player from selector
			server.selectEntities(sender, selector.value).forEach(entity -> {
				if (entity instanceof Player player) {
					targets.add(player);
				}
			});
		}
		else {
			// Get player by name
			Player target = server.getPlayerExact(targetString);
			if (target == null) throw new CommandException("No player named \"" + targetString + "\" was found");
			targets.add(target);
		}
		
		if (targets.isEmpty()) throw new CommandException("No player was found");
		
		return targets;
	}
	
	private static List<Player> getTargetsCustom(String targetString, CommandSender sender, Server server) {
		List<Player> targets = new ArrayList<>();
		
		switch (PlayerSelector.fromValue(targetString)) {
			case ALL -> targets.addAll(server.getOnlinePlayers());
			
			case NEAREST -> {
				if (sender instanceof Player player) {
					// If sender is a player, target sender
					targets.add(player);
				}
				else if (sender instanceof BlockCommandSender blockSender) {
					// If sender is a block, target nearest player
					Block block = blockSender.getBlock();
					Location location = block.getLocation();
					List<Player> players = block.getWorld().getPlayers();
					
					players.stream()
						.min(Comparator.comparing(player -> player.getLocation().distanceSquared(location)))
						.ifPresent(targets::add);
				}
				else {
					throw new CommandException("Command sender must be a player or block for selector \"" + targetString + "\"");
				}
			}
			
			case RANDOM -> {
				ArrayList<Player> players = new ArrayList<>(server.getOnlinePlayers());
				if (players.isEmpty()) break;
				Collections.shuffle(players);
				targets.add(players.getFirst());
			}
			
			case CURRENT -> {
				if (!(sender instanceof Player player)) {
					throw new CommandException("Command sender must be a player for selector \"" + targetString + "\"");
				}
				targets.add(player);
			}
			
			case null, default -> {
				// Get player by name
				Player target = server.getPlayerExact(targetString);
				if (target == null) throw new CommandException("No player named \"" + targetString + "\" was found");
				targets.add(target);
			}
		}
		
		if (targets.isEmpty()) throw new CommandException("No player was found");
		
		return targets;
	}
	
	
	@Override
	public String toString() {
		return value;
	}
}
