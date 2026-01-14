package com.mjkrempl.cartloader.Commands;

import com.mjkrempl.cartloader.CartLoader;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.List;

public interface CartLoaderSubcommand {
	List<String> usage();
	@Nullable String action(CartLoader plugin, CommandSender sender, List<String> args) throws CommandException;
	@Nullable List<String> tabCompletion(CartLoader plugin, CommandSender sender, List<String> args);
}
