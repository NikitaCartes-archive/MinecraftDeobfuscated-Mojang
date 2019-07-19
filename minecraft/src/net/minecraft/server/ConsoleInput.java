package net.minecraft.server;

import net.minecraft.commands.CommandSourceStack;

public class ConsoleInput {
	public final String msg;
	public final CommandSourceStack source;

	public ConsoleInput(String string, CommandSourceStack commandSourceStack) {
		this.msg = string;
		this.source = commandSourceStack;
	}
}
