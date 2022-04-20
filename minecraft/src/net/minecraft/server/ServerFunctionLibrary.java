package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class ServerFunctionLibrary implements PreparableReloadListener {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String FILE_EXTENSION = ".mcfunction";
	private static final int PATH_PREFIX_LENGTH = "functions/".length();
	private static final int PATH_SUFFIX_LENGTH = ".mcfunction".length();
	private volatile Map<ResourceLocation, CommandFunction> functions = ImmutableMap.of();
	private final TagLoader<CommandFunction> tagsLoader = new TagLoader<>(this::getFunction, "tags/functions");
	private volatile Map<ResourceLocation, Tag<CommandFunction>> tags = Map.of();
	private final int functionCompilationLevel;
	private final CommandDispatcher<CommandSourceStack> dispatcher;

	public Optional<CommandFunction> getFunction(ResourceLocation resourceLocation) {
		return Optional.ofNullable((CommandFunction)this.functions.get(resourceLocation));
	}

	public Map<ResourceLocation, CommandFunction> getFunctions() {
		return this.functions;
	}

	public Tag<CommandFunction> getTag(ResourceLocation resourceLocation) {
		return (Tag<CommandFunction>)this.tags.getOrDefault(resourceLocation, Tag.empty());
	}

	public Iterable<ResourceLocation> getAvailableTags() {
		return this.tags.keySet();
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
		CompletableFuture<Map<ResourceLocation, Tag.Builder>> completableFuture = CompletableFuture.supplyAsync(() -> this.tagsLoader.load(resourceManager), executor);
		CompletableFuture<Map<ResourceLocation, CompletableFuture<CommandFunction>>> completableFuture2 = CompletableFuture.supplyAsync(
				() -> resourceManager.listResources("functions", resourceLocation -> resourceLocation.getPath().endsWith(".mcfunction")), executor
			)
			.thenCompose(
				map -> {
					Map<ResourceLocation, CompletableFuture<CommandFunction>> map2 = Maps.<ResourceLocation, CompletableFuture<CommandFunction>>newHashMap();
					CommandSourceStack commandSourceStack = new CommandSourceStack(
						CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, this.functionCompilationLevel, "", CommonComponents.EMPTY, null, null
					);

					for (Entry<ResourceLocation, Resource> entry : map.entrySet()) {
						ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
						String string = resourceLocation.getPath();
						ResourceLocation resourceLocation2 = new ResourceLocation(
							resourceLocation.getNamespace(), string.substring(PATH_PREFIX_LENGTH, string.length() - PATH_SUFFIX_LENGTH)
						);
						map2.put(resourceLocation2, CompletableFuture.supplyAsync(() -> {
							List<String> list = readLines((Resource)entry.getValue());
							return CommandFunction.fromLines(resourceLocation2, this.dispatcher, commandSourceStack, list);
						}, executor));
					}

					CompletableFuture<?>[] completableFutures = (CompletableFuture<?>[])map2.values().toArray(new CompletableFuture[0]);
					return CompletableFuture.allOf(completableFutures).handle((void_, throwable) -> map2);
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
			this.tags = this.tagsLoader.build((Map<ResourceLocation, Tag.Builder>)pair.getFirst());
		}, executor2);
	}

	private static List<String> readLines(Resource resource) {
		try {
			BufferedReader bufferedReader = resource.openAsReader();

			List var2;
			try {
				var2 = bufferedReader.lines().toList();
			} catch (Throwable var5) {
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (Throwable var4) {
						var5.addSuppressed(var4);
					}
				}

				throw var5;
			}

			if (bufferedReader != null) {
				bufferedReader.close();
			}

			return var2;
		} catch (IOException var6) {
			throw new CompletionException(var6);
		}
	}
}
