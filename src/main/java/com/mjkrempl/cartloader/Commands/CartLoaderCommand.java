package com.mjkrempl.cartloader.Commands;

import com.mjkrempl.cartloader.CartLoader;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class CartLoaderCommand implements CommandExecutor, TabCompleter {
	private final CartLoader plugin;
	private final World defaultGameRuleWorld;
	private final String prefix;
	private final SequencedMap<String, CartLoaderSubcommand> subcommands;
	
	public CartLoaderCommand(CartLoader plugin, String label) {
		this.plugin = plugin;
		this.defaultGameRuleWorld = plugin.getServer().getWorlds().getFirst();
		this.prefix = "/" + label;
		this.subcommands = new LinkedHashMap<>();
	}
	
	
	@Override
	public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
		if (args.length == 0) return false;
		
		String subcommandLabel = args[0];
		CartLoaderSubcommand subcommand = subcommands.get(subcommandLabel);
		if (subcommand == null) return false;
		
		List<String> subArgs = Arrays.stream(args).skip(1).toList();
		try {
			// Execute subcommand
			String message = subcommand.action(plugin, sender, subArgs);
			if (message == null) return true;
			// Send result message
			if (shouldSendResultMessage(sender)) {
				sender.sendMessage(message);
			}
			sendAdminInfo(message, sender);
			return true;
		}
		catch (CommandException e) {
			// Send error message
			sender.sendMessage(ChatColor.RED + e.getMessage());
			sender.sendMessage(getSubcommandUsage(subcommandLabel, subcommand));
			return true;
		}
	}
	
	@Override
	public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
		if (args.length == 0) return null;
		if (args.length == 1) return subcommands.keySet().stream().sorted().toList();
		
		String subcommandLabel = args[0];
		CartLoaderSubcommand subcommand = subcommands.get(subcommandLabel);
		if (subcommand == null) return null;
		
		List<String> subArgs = Arrays.stream(args).skip(1).toList();
		return subcommand.tabCompletion(plugin, sender, subArgs);
	}
	
	
	// - Subcommands
	
	public void registerSubcommand(String label, CartLoaderSubcommand handler) {
		subcommands.put(label, handler);
	}
	
	public String getSubcommandsUsage() {
		return subcommands.sequencedEntrySet().stream()
			.map(entry -> getSubcommandUsage(entry.getKey(), entry.getValue()))
			.collect(Collectors.joining("\n"));
	}
	private String getSubcommandUsage(String label, CartLoaderSubcommand subcommand) {
		List<String> components = new ArrayList<>();
		components.add(prefix);
		components.add(label);
		components.addAll(subcommand.usage());
		return String.join(" ", components);
	}
	
	
	// - Game Rules
	
	private boolean getGameRuleValue(GameRule<Boolean> gameRule, CommandSender sender, Boolean defaultValue) {
		// Get world of sender (or null if console)
		World world = null;
		if (sender instanceof Player player) {
			world = player.getWorld();
		} else if (sender instanceof BlockCommandSender block) {
			world = block.getBlock().getWorld();
		} else if (defaultValue == null) {
			world = defaultGameRuleWorld;
		}
		// Default value
		boolean def = defaultValue != null ? defaultValue : false;
		// Get game rule value in world
		if (world == null) return def;
		Boolean value = world.getGameRuleValue(gameRule);
		if (value == null) return def;
		return value;
	}
	
	private boolean shouldSendResultMessage(CommandSender sender) {
		return getGameRuleValue(GameRule.SEND_COMMAND_FEEDBACK, sender, true);
	}
	private boolean shouldInformAdminsAboutAdminResults(CommandSender sender) {
		return getGameRuleValue(GameRule.LOG_ADMIN_COMMANDS, sender, null);
	}
	private boolean shouldInformAdminsAboutCommandBlockResults(CommandSender sender) {
		return getGameRuleValue(GameRule.COMMAND_BLOCK_OUTPUT, sender, null);
	}
	
	
	// - Info messages
	
	private void sendAdminInfo(String message, CommandSender sender) {
		// Check game rules if info should be sent
		if (sender instanceof BlockCommandSender) {
			if (!shouldInformAdminsAboutCommandBlockResults(sender)) return;
		} else {
			if (!shouldInformAdminsAboutAdminResults(sender)) return;
		}
		
		String opMessage = '[' + sender.getName() + ": " + message + ']';
		String opMessageFormatted = ChatColor.GRAY.toString() + ChatColor.ITALIC + opMessage;
		
		getOtherAdminCommandSenders(sender).forEach(s -> {
			if (s instanceof ConsoleCommandSender) {
				s.sendMessage(opMessage);
			} else {
				s.sendMessage(opMessageFormatted);
			}
		});
	}
	
	private Set<CommandSender> getOtherAdminCommandSenders(CommandSender sender) {
		Server server = plugin.getServer();
		Set<CommandSender> targets = new HashSet<>();
		// Add console
		targets.add(server.getConsoleSender());
		// Add all operators
		server.getOnlinePlayers().stream()
			.filter(Player::isOp)
			.forEach(targets::add);
		// Remove sender
		targets.remove(sender);
		return targets;
	}
}
