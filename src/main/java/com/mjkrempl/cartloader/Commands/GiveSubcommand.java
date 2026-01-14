package com.mjkrempl.cartloader.Commands;

import com.mjkrempl.cartloader.CartLoader;
import com.mjkrempl.cartloader.CustomMinecart;
import com.mjkrempl.cartloader.MinecartType;
import org.bukkit.Server;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class GiveSubcommand implements CartLoaderSubcommand {
	private static final List<String> usage = Arrays.asList("<targets>", "<item>", "[<amount>]");
	
	private final Server server;
	private final List<String> minecartTypeIdentifiers;
	private final Map<String, MinecartType> minecartTypeMap;
	
	private @Nullable List<String> targetsList;
	
	public GiveSubcommand(Server server) {
		this.server = server;
		
		minecartTypeIdentifiers = new ArrayList<>(MinecartType.all.length);
		minecartTypeMap = new HashMap<>(MinecartType.all.length);
		for (var type : MinecartType.all) {
			minecartTypeIdentifiers.add(type.identifier);
			minecartTypeMap.put(type.identifier, type);
		}
	}
	
	
	@Override
	public List<String> usage() {
		return usage;
	}
	
	@Override
	public @Nullable String action(CartLoader plugin, CommandSender sender, List<String> args) throws CommandException {
		resetTargetsList();
		if (args.size() < 2) throw new CommandException("Incomplete command");
		
		// Target
		String targetString = args.get(0);
		List<Player> targets = PlayerSelector.getTargets(targetString, sender, server);
		
		// Minecart type
		String minecartTypeIdentifier = args.get(1);
		MinecartType minecartType = minecartTypeMap.get(minecartTypeIdentifier);
		if (minecartType == null) throw new CommandException("No such minecart type \"" + minecartTypeIdentifier + "\"");
		
		// Amount
		int amount = 1;
		if (args.size() >= 3) {
			String amountString = args.get(2);
			boolean valid;
			try {
				amount = Integer.parseInt(amountString);
				valid = amount >= 1;
			} catch (NumberFormatException e) {
				valid = false;
			}
			if (!valid) throw new CommandException("Invalid amount \"" + amountString + "\"");
		}
		
		// Give item
		ItemStack item = CustomMinecart.getItem(minecartType, amount);
		targets.forEach(player -> player.getInventory().addItem(item));
		
		// Build info message
		StringBuilder message = new StringBuilder()
			.append("Gave ")
			.append(amount)
			.append(" [")
			.append(CustomMinecart.getName(minecartType))
			.append("] to ")
			.append(targets.getFirst().getName());
		targets.stream().skip(1).forEach(player -> {
			message.append(", ");
			message.append(player.getName());
		});
		return message.toString();
	}
	
	@Override
	public @Nullable List<String> tabCompletion(CartLoader plugin, CommandSender sender, List<String> args) {
		return switch (args.size()) {
			// Target
			case 1 -> {
				// Cache targets list during typing, only reload when sender reached/cleared this argument
				if (args.getFirst().isEmpty()) resetTargetsList();
				yield getTargetsList();
			}
			
			// Minecart type
			case 2 -> minecartTypeIdentifiers;
			
			default -> null;
		};
	}
	
	
	private List<String> getTargetsList() {
		if (targetsList == null) {
			targetsList = PlayerSelector.getAllAvailableTargetValues(server);
		}
		return targetsList;
	}
	private void resetTargetsList() {
		targetsList = null;
	}
}
