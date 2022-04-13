package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

public class CommandsReport implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private final DataGenerator generator;

	public CommandsReport(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	@Override
	public void run(CachedOutput cachedOutput) throws IOException {
		Path path = this.generator.getOutputFolder().resolve("reports/commands.json");
		CommandDispatcher<CommandSourceStack> commandDispatcher = new Commands(
				Commands.CommandSelection.ALL, new CommandBuildContext((RegistryAccess)RegistryAccess.BUILTIN.get())
			)
			.getDispatcher();
		DataProvider.save(GSON, cachedOutput, ArgumentUtils.serializeNodeToJson(commandDispatcher, commandDispatcher.getRoot()), path);
	}

	@Override
	public String getName() {
		return "Command Syntax";
	}
}
