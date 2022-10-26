/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.models;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModelProvider
implements DataProvider {
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider modelPathProvider;

    public ModelProvider(PackOutput packOutput) {
        this.blockStatePathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.modelPathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        HashMap map = Maps.newHashMap();
        Consumer<BlockStateGenerator> consumer = blockStateGenerator -> {
            Block block = blockStateGenerator.getBlock();
            BlockStateGenerator blockStateGenerator2 = map.put(block, blockStateGenerator);
            if (blockStateGenerator2 != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + block);
            }
        };
        HashMap map2 = Maps.newHashMap();
        HashSet set = Sets.newHashSet();
        BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer = (resourceLocation, supplier) -> {
            Supplier supplier2 = map2.put(resourceLocation, supplier);
            if (supplier2 != null) {
                throw new IllegalStateException("Duplicate model definition for " + resourceLocation);
            }
        };
        Consumer<Item> consumer2 = set::add;
        new BlockModelGenerators(consumer, biConsumer, consumer2).run();
        new ItemModelGenerators(biConsumer).run();
        List<Block> list = Registry.BLOCK.stream().filter(block -> !map.containsKey(block)).toList();
        if (!list.isEmpty()) {
            throw new IllegalStateException("Missing blockstate definitions for: " + list);
        }
        Registry.BLOCK.forEach(block -> {
            Item item = Item.BY_BLOCK.get(block);
            if (item != null) {
                if (set.contains(item)) {
                    return;
                }
                ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(item);
                if (!map2.containsKey(resourceLocation)) {
                    map2.put(resourceLocation, new DelegatedModel(ModelLocationUtils.getModelLocation(block)));
                }
            }
        });
        CompletableFuture[] completableFutureArray = new CompletableFuture[2];
        completableFutureArray[0] = this.saveCollection(cachedOutput, map, block -> this.blockStatePathProvider.json(block.builtInRegistryHolder().key().location()));
        completableFutureArray[1] = this.saveCollection(cachedOutput, map2, this.modelPathProvider::json);
        return CompletableFuture.allOf(completableFutureArray);
    }

    private <T> CompletableFuture<?> saveCollection(CachedOutput cachedOutput, Map<T, ? extends Supplier<JsonElement>> map, Function<T, Path> function) {
        return CompletableFuture.allOf((CompletableFuture[])map.entrySet().stream().map(entry -> {
            Path path = (Path)function.apply(entry.getKey());
            JsonElement jsonElement = (JsonElement)((Supplier)entry.getValue()).get();
            return DataProvider.saveStable(cachedOutput, jsonElement, path);
        }).toArray(CompletableFuture[]::new));
    }

    @Override
    public final String getName() {
        return "Model Definitions";
    }
}

