package com.mjkrempl.cartloader.Commands;

import com.mjkrempl.cartloader.CartLoader;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class HelpSubcommand implements CartLoaderSubcommand {
	private final CartLoaderCommand mainCommand;
	
	public HelpSubcommand(CartLoaderCommand mainCommand) {
		this.mainCommand = mainCommand;
	}
	
	@Override
	public List<String> usage() {
		return Collections.emptyList();
	}
	
	@Override
	public @Nullable String action(CartLoader plugin, CommandSender sender, List<String> args) throws CommandException {
		sender.sendMessage(mainCommand.getSubcommandsUsage());
		return null;
	}
	
	@Override
	public @Nullable List<String> tabCompletion(CartLoader plugin, CommandSender sender, List<String> args) {
		return null;
	}
}
