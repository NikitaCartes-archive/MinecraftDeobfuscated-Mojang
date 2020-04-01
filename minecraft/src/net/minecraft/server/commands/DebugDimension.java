package net.minecraft.server.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.dimension.Dimension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugDimension {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Logger LOGGER = LogManager.getLogger();

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(Commands.literal("debugdim").executes(commandContext -> debugDim(commandContext.getSource())));
	}

	private static int debugDim(CommandSourceStack commandSourceStack) {
		Dimension dimension = commandSourceStack.getLevel().getDimension();
		File file = commandSourceStack.getLevel().getLevelStorage().getFolder();
		File file2 = new File(file, "debug");
		file2.mkdirs();
		Dynamic<JsonElement> dynamic = dimension.serialize(JsonOps.INSTANCE);
		int i = Registry.DIMENSION_TYPE.getId(dimension.getType());
		File file3 = new File(file2, "dim-" + i + ".json");

		try {
			Writer writer = Files.newBufferedWriter(file3.toPath());
			Throwable var8 = null;

			try {
				GSON.toJson(dynamic.getValue(), writer);
			} catch (Throwable var18) {
				var8 = var18;
				throw var18;
			} finally {
				if (writer != null) {
					if (var8 != null) {
						try {
							writer.close();
						} catch (Throwable var17) {
							var8.addSuppressed(var17);
						}
					} else {
						writer.close();
					}
				}
			}
		} catch (IOException var20) {
			LOGGER.warn("Failed to save file {}", file3.getAbsolutePath(), var20);
		}

		dimension.getKnownBiomes().forEach(biome -> {
			int ix = Registry.BIOME.getId(biome);
			Dynamic<JsonElement> dynamicx = biome.serialize(JsonOps.INSTANCE);
			File file2x = new File(file2, "biome-" + ix + ".json");

			try {
				Writer writer = Files.newBufferedWriter(file2x.toPath());
				Throwable var6x = null;

				try {
					GSON.toJson(dynamicx.getValue(), writer);
				} catch (Throwable var16) {
					var6x = var16;
					throw var16;
				} finally {
					if (writer != null) {
						if (var6x != null) {
							try {
								writer.close();
							} catch (Throwable var15) {
								var6x.addSuppressed(var15);
							}
						} else {
							writer.close();
						}
					}
				}
			} catch (IOException var18x) {
				LOGGER.warn("Failed to save file {}", file2x.getAbsolutePath(), var18x);
			}
		});
		commandSourceStack.sendSuccess(new TextComponent("Saved to file: " + file2), false);
		return 0;
	}
}
