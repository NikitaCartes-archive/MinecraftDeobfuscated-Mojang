package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.ProfileResults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugCommand {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(new TranslatableComponent("commands.debug.notRunning"));
	private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.debug.alreadyRunning")
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("debug")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(Commands.literal("start").executes(commandContext -> start(commandContext.getSource())))
				.then(Commands.literal("stop").executes(commandContext -> stop(commandContext.getSource())))
				.then(
					Commands.literal("function")
						.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
						.then(
							Commands.argument("name", FunctionArgument.functions())
								.suggests(FunctionCommand.SUGGEST_FUNCTION)
								.executes(commandContext -> traceFunction(commandContext.getSource(), FunctionArgument.getFunctions(commandContext, "name")))
						)
				)
		);
	}

	private static int start(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		if (minecraftServer.isTimeProfilerRunning()) {
			throw ERROR_ALREADY_RUNNING.create();
		} else {
			minecraftServer.startTimeProfiler();
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.debug.started"), true);
			return 0;
		}
	}

	private static int stop(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		if (!minecraftServer.isTimeProfilerRunning()) {
			throw ERROR_NOT_RUNNING.create();
		} else {
			ProfileResults profileResults = minecraftServer.stopTimeProfiler();
			double d = (double)profileResults.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
			double e = (double)profileResults.getTickDuration() / d;
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d), profileResults.getTickDuration(), String.format("%.2f", e)),
				true
			);
			return (int)e;
		}
	}

	private static int traceFunction(CommandSourceStack commandSourceStack, Collection<CommandFunction> collection) {
		int i = 0;
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		String string = "debug-trace-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".txt";

		try {
			Path path = minecraftServer.getFile("debug").toPath();
			Files.createDirectories(path);
			Writer writer = Files.newBufferedWriter(path.resolve(string), StandardCharsets.UTF_8);

			try {
				PrintWriter printWriter = new PrintWriter(writer);

				for (CommandFunction commandFunction : collection) {
					printWriter.println(commandFunction.getId());
					DebugCommand.Tracer tracer = new DebugCommand.Tracer(printWriter);
					i += commandSourceStack.getServer().getFunctions().execute(commandFunction, commandSourceStack.withSource(tracer).withMaximumPermission(2), tracer);
				}
			} catch (Throwable var12) {
				if (writer != null) {
					try {
						writer.close();
					} catch (Throwable var11) {
						var12.addSuppressed(var11);
					}
				}

				throw var12;
			}

			if (writer != null) {
				writer.close();
			}
		} catch (IOException | UncheckedIOException var13) {
			LOGGER.warn("Tracing failed", (Throwable)var13);
			commandSourceStack.sendFailure(new TranslatableComponent("commands.debug.function.traceFailed"));
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.debug.function.success.single", i, ((CommandFunction)collection.iterator().next()).getId(), string), true
			);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.debug.function.success.multiple", i, collection.size(), string), true);
		}

		return i;
	}

	static class Tracer implements CommandSource, ServerFunctionManager.TraceCallbacks {
		public static final int INDENT_OFFSET = 1;
		private final PrintWriter output;
		private int lastIndent;
		private boolean waitingForResult;

		Tracer(PrintWriter printWriter) {
			this.output = printWriter;
		}

		private void indentAndSave(int i) {
			this.printIndent(i);
			this.lastIndent = i;
		}

		private void printIndent(int i) {
			for (int j = 0; j < i + 1; j++) {
				this.output.write("    ");
			}
		}

		private void newLine() {
			if (this.waitingForResult) {
				this.output.println();
				this.waitingForResult = false;
			}
		}

		@Override
		public void onCommand(int i, String string) {
			this.newLine();
			this.indentAndSave(i);
			this.output.print("[C] ");
			this.output.print(string);
			this.waitingForResult = true;
		}

		@Override
		public void onReturn(int i, String string, int j) {
			if (this.waitingForResult) {
				this.output.print(" -> ");
				this.output.println(j);
				this.waitingForResult = false;
			} else {
				this.indentAndSave(i);
				this.output.print("[R = ");
				this.output.print(j);
				this.output.print("] ");
				this.output.println(string);
			}
		}

		@Override
		public void onCall(int i, ResourceLocation resourceLocation, int j) {
			this.newLine();
			this.indentAndSave(i);
			this.output.print("[F] ");
			this.output.print(resourceLocation);
			this.output.print(" size=");
			this.output.println(j);
		}

		@Override
		public void onError(int i, String string) {
			this.newLine();
			this.indentAndSave(i + 1);
			this.output.print("[E] ");
			this.output.print(string);
		}

		@Override
		public void sendMessage(Component component, UUID uUID) {
			this.newLine();
			this.printIndent(this.lastIndent + 1);
			this.output.print("[M] ");
			if (uUID != Util.NIL_UUID) {
				this.output.print(uUID);
				this.output.print(": ");
			}

			this.output.println(component.getString());
		}

		@Override
		public boolean acceptsSuccess() {
			return true;
		}

		@Override
		public boolean acceptsFailure() {
			return true;
		}

		@Override
		public boolean shouldInformAdmins() {
			return false;
		}

		@Override
		public boolean alwaysAccepts() {
			return true;
		}
	}
}
