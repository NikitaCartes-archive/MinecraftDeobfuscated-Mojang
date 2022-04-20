/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
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

public class ServerFunctionLibrary
implements PreparableReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_EXTENSION = ".mcfunction";
    private static final int PATH_PREFIX_LENGTH = "functions/".length();
    private static final int PATH_SUFFIX_LENGTH = ".mcfunction".length();
    private volatile Map<ResourceLocation, CommandFunction> functions = ImmutableMap.of();
    private final TagLoader<CommandFunction> tagsLoader = new TagLoader(this::getFunction, "tags/functions");
    private volatile Map<ResourceLocation, Tag<CommandFunction>> tags = Map.of();
    private final int functionCompilationLevel;
    private final CommandDispatcher<CommandSourceStack> dispatcher;

    public Optional<CommandFunction> getFunction(ResourceLocation resourceLocation) {
        return Optional.ofNullable(this.functions.get(resourceLocation));
    }

    public Map<ResourceLocation, CommandFunction> getFunctions() {
        return this.functions;
    }

    public Tag<CommandFunction> getTag(ResourceLocation resourceLocation) {
        return this.tags.getOrDefault(resourceLocation, Tag.empty());
    }

    public Iterable<ResourceLocation> getAvailableTags() {
        return this.tags.keySet();
    }

    public ServerFunctionLibrary(int i, CommandDispatcher<CommandSourceStack> commandDispatcher) {
        this.functionCompilationLevel = i;
        this.dispatcher = commandDispatcher;
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        CompletableFuture<Map> completableFuture = CompletableFuture.supplyAsync(() -> this.tagsLoader.load(resourceManager), executor);
        CompletionStage completableFuture2 = CompletableFuture.supplyAsync(() -> resourceManager.listResources("functions", resourceLocation -> resourceLocation.getPath().endsWith(FILE_EXTENSION)), executor).thenCompose(map -> {
            HashMap<ResourceLocation, CompletableFuture<CommandFunction>> map2 = Maps.newHashMap();
            CommandSourceStack commandSourceStack = new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, null, this.functionCompilationLevel, "", CommonComponents.EMPTY, null, null);
            for (Map.Entry entry : map.entrySet()) {
                ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
                String string = resourceLocation.getPath();
                ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), string.substring(PATH_PREFIX_LENGTH, string.length() - PATH_SUFFIX_LENGTH));
                map2.put(resourceLocation2, CompletableFuture.supplyAsync(() -> {
                    List<String> list = ServerFunctionLibrary.readLines((Resource)entry.getValue());
                    return CommandFunction.fromLines(resourceLocation2, this.dispatcher, commandSourceStack, list);
                }, executor));
            }
            CompletableFuture[] completableFutures = map2.values().toArray(new CompletableFuture[0]);
            return CompletableFuture.allOf(completableFutures).handle((void_, throwable) -> map2);
        });
        return ((CompletableFuture)((CompletableFuture)completableFuture.thenCombine(completableFuture2, Pair::of)).thenCompose(preparationBarrier::wait)).thenAcceptAsync(pair -> {
            Map map = (Map)pair.getSecond();
            ImmutableMap.Builder builder = ImmutableMap.builder();
            map.forEach((resourceLocation, completableFuture) -> ((CompletableFuture)completableFuture.handle((commandFunction, throwable) -> {
                if (throwable != null) {
                    LOGGER.error("Failed to load function {}", resourceLocation, throwable);
                } else {
                    builder.put(resourceLocation, commandFunction);
                }
                return null;
            })).join());
            this.functions = builder.build();
            this.tags = this.tagsLoader.build((Map)pair.getFirst());
        }, executor2);
    }

    private static List<String> readLines(Resource resource) {
        List<String> list;
        block8: {
            BufferedReader bufferedReader = resource.openAsReader();
            try {
                list = bufferedReader.lines().toList();
                if (bufferedReader == null) break block8;
            } catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (IOException iOException) {
                    throw new CompletionException(iOException);
                }
            }
            bufferedReader.close();
        }
        return list;
    }
}

