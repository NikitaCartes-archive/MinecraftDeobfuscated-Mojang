package net.minecraft.data.info;

import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.flag.FeatureFlags;

public class CommandsReport implements DataProvider {
	private final PackOutput output;

	public CommandsReport(PackOutput packOutput) {
		this.output = packOutput;
	}

	@Override
	public void run(CachedOutput cachedOutput) throws IOException {
		Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("commands.json");
		CommandDispatcher<CommandSourceStack> commandDispatcher = new Commands(
				Commands.CommandSelection.ALL, new CommandBuildContext(BuiltinRegistries.createAccess(), FeatureFlags.REGISTRY.allFlags())
			)
			.getDispatcher();
		DataProvider.saveStable(cachedOutput, ArgumentUtils.serializeNodeToJson(commandDispatcher, commandDispatcher.getRoot()), path);
	}

	@Override
	public String getName() {
		return "Command Syntax";
	}
}
