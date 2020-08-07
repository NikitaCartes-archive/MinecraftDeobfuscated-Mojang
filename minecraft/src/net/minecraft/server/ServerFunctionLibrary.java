package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerFunctionLibrary implements PreparableReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int PATH_PREFIX_LENGTH = "functions/".length();
	private static final int PATH_SUFFIX_LENGTH = ".mcfunction".length();
	private volatile Map<ResourceLocation, CommandFunction> functions = ImmutableMap.of();
	private final TagLoader<CommandFunction> tagsLoader = new TagLoader<>(this::getFunction, "tags/functions", "function");
	private volatile TagCollection<CommandFunction> tags = TagCollection.empty();
	private final int functionCompilationLevel;
	private final CommandDispatcher<CommandSourceStack> dispatcher;

	public Optional<CommandFunction> getFunction(ResourceLocation resourceLocation) {
		return Optional.ofNullable(this.functions.get(resourceLocation));
	}

	public Map<ResourceLocation, CommandFunction> getFunctions() {
		return this.functions;
	}

	public TagCollection<CommandFunction> getTags() {
		return this.tags;
	}

	public Tag<CommandFunction> getTag(ResourceLocation resourceLocation) {
		return this.tags.getTagOrEmpty(resourceLocation);
	}

	public ServerFunctionLibrary(int i, CommandDispatcher<CommandSourceStack> commandDispatcher) {
		this.functionCompilationLevel = i;
		this.dispatcher = commandDispatcher;
	}

	@Override
	public CompletableFuture<Void> reload(
		PreparableReloadListener.PreparationBarrier preparationBarrier,
		ResourceManager resourceManager,
		ProfilerFiller profilerFiller,
		ProfilerFiller profilerFiller2,
		Executor executor,
		Executor executor2
	) {
		CompletableFuture<Map<ResourceLocation, Tag.Builder>> completableFuture = this.tagsLoader.prepare(resourceManager, executor);
		CompletableFuture<Map<ResourceLocation, CompletableFuture<CommandFunction>>> completableFuture2 = CompletableFuture.supplyAsync(
				() -> resourceManager.listResources("functions", string -> string.endsWith(".mcfunction")), executor
			)
			.thenCompose(
				collection -> {
					Map<ResourceLocation, CompletableFuture<CommandFunction>> map = Maps.<ResourceLocation, CompletableFuture<CommandFunction>>newHashMap();
					CommandSourceStack commandSourceStack = new CommandSourceStack(
						CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, this.functionCompilationLevel, "", TextComponent.EMPTY, null, null
					);

					for (ResourceLocation resourceLocation : collection) {
						String string = resourceLocation.getPath();
						ResourceLocation resourceLocation2 = new ResourceLocation(
							resourceLocation.getNamespace(), string.substring(PATH_PREFIX_LENGTH, string.length() - PATH_SUFFIX_LENGTH)
						);
						map.put(resourceLocation2, CompletableFuture.supplyAsync(() -> {
							List<String> list = readLines(resourceManager, resourceLocation);
							return CommandFunction.fromLines(resourceLocation2, this.dispatcher, commandSourceStack, list);
						}, executor));
					}

					CompletableFuture<?>[] completableFutures = (CompletableFuture<?>[])map.values().toArray(new CompletableFuture[0]);
					return CompletableFuture.allOf(completableFutures).handle((void_, throwable) -> map);
				}
			);
		return completableFuture.thenCombine(completableFuture2, Pair::of).thenCompose(preparationBarrier::wait).thenAcceptAsync(pair -> {
			Map<ResourceLocation, CompletableFuture<CommandFunction>> map = (Map<ResourceLocation, CompletableFuture<CommandFunction>>)pair.getSecond();
			Builder<ResourceLocation, CommandFunction> builder = ImmutableMap.builder();
			map.forEach((resourceLocation, completableFuturex) -> completableFuturex.handle((commandFunction, throwable) -> {
					if (throwable != null) {
						LOGGER.error("Failed to load function {}", resourceLocation, throwable);
					} else {
						builder.put(resourceLocation, commandFunction);
					}

					return null;
				}).join());
			this.functions = builder.build();
			this.tags = this.tagsLoader.load((Map<ResourceLocation, Tag.Builder>)pair.getFirst());
		}, executor2);
	}

	private static List<String> readLines(ResourceManager resourceManager, ResourceLocation resourceLocation) {
		try {
			Resource resource = resourceManager.getResource(resourceLocation);
			Throwable var3 = null;

			List var4;
			try {
				var4 = IOUtils.readLines(resource.getInputStream(), StandardCharsets.UTF_8);
			} catch (Throwable var14) {
				var3 = var14;
				throw var14;
			} finally {
				if (resource != null) {
					if (var3 != null) {
						try {
							resource.close();
						} catch (Throwable var13) {
							var3.addSuppressed(var13);
						}
					} else {
						resource.close();
					}
				}
			}

			return var4;
		} catch (IOException var16) {
			throw new CompletionException(var16);
		}
	}
}
