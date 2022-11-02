package net.minecraft.data.info;

import com.mojang.brigadier.CommandDispatcher;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public class CommandsReport implements DataProvider {
	private final PackOutput output;
	private final CompletableFuture<HolderLookup.Provider> registries;

	public CommandsReport(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
		this.output = packOutput;
		this.registries = completableFuture;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		Path path = this.output.getOutputFolder(PackOutput.Target.REPORTS).resolve("commands.json");
		return this.registries
			.thenCompose(
				provider -> {
					CommandDispatcher<CommandSourceStack> commandDispatcher = new Commands(Commands.CommandSelection.ALL, Commands.createValidationContext(provider))
						.getDispatcher();
					return DataProvider.saveStable(cachedOutput, ArgumentUtils.serializeNodeToJson(commandDispatcher, commandDispatcher.getRoot()), path);
				}
			);
	}

	@Override
	public final String getName() {
		return "Command Syntax";
	}
}
