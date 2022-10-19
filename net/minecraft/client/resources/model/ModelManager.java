/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.model;

import com.google.common.collect.HashMultimap;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ModelManager
implements PreparableReloadListener,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, AtlasSet.ResourceLister> VANILLA_ATLASES = Map.of(Sheets.BANNER_SHEET, resourceManager -> {
        HashMap map = new HashMap();
        SpriteLoader.addSprite(resourceManager, ModelBakery.BANNER_BASE.texture(), map::put);
        SpriteLoader.listSprites(resourceManager, "entity/banner", map::put);
        return map;
    }, Sheets.BED_SHEET, resourceManager -> SpriteLoader.listSprites(resourceManager, "entity/bed"), Sheets.CHEST_SHEET, resourceManager -> SpriteLoader.listSprites(resourceManager, "entity/chest"), Sheets.SHIELD_SHEET, resourceManager -> {
        HashMap map = new HashMap();
        SpriteLoader.addSprite(resourceManager, ModelBakery.SHIELD_BASE.texture(), map::put);
        SpriteLoader.addSprite(resourceManager, ModelBakery.NO_PATTERN_SHIELD.texture(), map::put);
        SpriteLoader.listSprites(resourceManager, "entity/shield", map::put);
        return map;
    }, Sheets.SIGN_SHEET, resourceManager -> SpriteLoader.listSprites(resourceManager, "entity/signs"), Sheets.SHULKER_SHEET, resourceManager -> SpriteLoader.listSprites(resourceManager, "entity/shulker"), TextureAtlas.LOCATION_BLOCKS, resourceManager -> {
        HashMap map = new HashMap();
        SpriteLoader.listSprites(resourceManager, "block", map::put);
        SpriteLoader.listSprites(resourceManager, "item", map::put);
        SpriteLoader.listSprites(resourceManager, "entity/conduit", map::put);
        SpriteLoader.addSprite(resourceManager, BellRenderer.BELL_RESOURCE_LOCATION.texture(), map::put);
        SpriteLoader.addSprite(resourceManager, EnchantTableRenderer.BOOK_LOCATION.texture(), map::put);
        return map;
    }, Sheets.HANGING_SIGN_SHEET, resourceManager -> SpriteLoader.listSprites(resourceManager, "entity/signs/hanging"));
    private Map<ResourceLocation, BakedModel> bakedRegistry;
    private final AtlasSet atlases;
    private final BlockModelShaper blockModelShaper;
    private final BlockColors blockColors;
    private int maxMipmapLevels;
    private BakedModel missingModel;
    private Object2IntMap<BlockState> modelGroups;

    public ModelManager(TextureManager textureManager, BlockColors blockColors, int i) {
        this.blockColors = blockColors;
        this.maxMipmapLevels = i;
        this.blockModelShaper = new BlockModelShaper(this);
        this.atlases = new AtlasSet(VANILLA_ATLASES, textureManager);
    }

    public BakedModel getModel(ModelResourceLocation modelResourceLocation) {
        return this.bakedRegistry.getOrDefault(modelResourceLocation, this.missingModel);
    }

    public BakedModel getMissingModel() {
        return this.missingModel;
    }

    public BlockModelShaper getBlockModelShaper() {
        return this.blockModelShaper;
    }

    @Override
    public final CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        profilerFiller.startTick();
        CompletableFuture<Map<ResourceLocation, BlockModel>> completableFuture = ModelManager.loadBlockModels(resourceManager, executor);
        CompletableFuture<Map<ResourceLocation, List<ModelBakery.LoadedJson>>> completableFuture2 = ModelManager.loadBlockStates(resourceManager, executor);
        CompletionStage completableFuture3 = completableFuture.thenCombineAsync(completableFuture2, (map, map2) -> new ModelBakery(this.blockColors, profilerFiller, (Map<ResourceLocation, BlockModel>)map, (Map<ResourceLocation, List<ModelBakery.LoadedJson>>)map2), executor);
        Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>> map3 = this.atlases.scheduleLoad(resourceManager, this.maxMipmapLevels, executor);
        return ((CompletableFuture)((CompletableFuture)((CompletableFuture)CompletableFuture.allOf((CompletableFuture[])Stream.concat(map3.values().stream(), Stream.of(completableFuture3)).toArray(CompletableFuture[]::new)).thenApplyAsync(arg_0 -> this.method_45885(profilerFiller, map3, (CompletableFuture)completableFuture3, arg_0), executor)).thenCompose(reloadState -> reloadState.readyForUpload.thenApply(void_ -> reloadState))).thenCompose(preparationBarrier::wait)).thenAcceptAsync(reloadState -> this.apply((ReloadState)reloadState, profilerFiller2), executor2);
    }

    private static CompletableFuture<Map<ResourceLocation, BlockModel>> loadBlockModels(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> ModelBakery.MODEL_LISTER.listMatchingResources(resourceManager), executor).thenCompose(map -> {
            ArrayList<CompletableFuture<Pair>> list2 = new ArrayList<CompletableFuture<Pair>>(map.size());
            for (Map.Entry entry : map.entrySet()) {
                list2.add(CompletableFuture.supplyAsync(() -> {
                    Pair<ResourceLocation, BlockModel> pair;
                    block8: {
                        BufferedReader reader = ((Resource)entry.getValue()).openAsReader();
                        try {
                            pair = Pair.of((ResourceLocation)entry.getKey(), BlockModel.fromStream(reader));
                            if (reader == null) break block8;
                        } catch (Throwable throwable) {
                            try {
                                if (reader != null) {
                                    try {
                                        ((Reader)reader).close();
                                    } catch (Throwable throwable2) {
                                        throwable.addSuppressed(throwable2);
                                    }
                                }
                                throw throwable;
                            } catch (IOException iOException) {
                                LOGGER.error("Failed to load model {}", entry.getKey(), (Object)iOException);
                                return null;
                            }
                        }
                        ((Reader)reader).close();
                    }
                    return pair;
                }, executor));
            }
            return Util.sequence(list2).thenApply(list -> list.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
        });
    }

    private static CompletableFuture<Map<ResourceLocation, List<ModelBakery.LoadedJson>>> loadBlockStates(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> ModelBakery.BLOCKSTATE_LISTER.listMatchingResourceStacks(resourceManager), executor).thenCompose(map -> {
            ArrayList<CompletableFuture<Pair>> list2 = new ArrayList<CompletableFuture<Pair>>(map.size());
            for (Map.Entry entry : map.entrySet()) {
                list2.add(CompletableFuture.supplyAsync(() -> {
                    List list = (List)entry.getValue();
                    ArrayList<ModelBakery.LoadedJson> list2 = new ArrayList<ModelBakery.LoadedJson>(list.size());
                    for (Resource resource : list) {
                        try {
                            BufferedReader reader = resource.openAsReader();
                            try {
                                JsonObject jsonObject = GsonHelper.parse(reader);
                                list2.add(new ModelBakery.LoadedJson(resource.sourcePackId(), jsonObject));
                            } finally {
                                if (reader == null) continue;
                                ((Reader)reader).close();
                            }
                        } catch (IOException iOException) {
                            LOGGER.error("Failed to load blockstate {} from pack {}", entry.getKey(), resource.sourcePackId(), iOException);
                        }
                    }
                    return Pair.of((ResourceLocation)entry.getKey(), list2);
                }, executor));
            }
            return Util.sequence(list2).thenApply(list -> list.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
        });
    }

    private ReloadState loadModels(ProfilerFiller profilerFiller, Map<ResourceLocation, AtlasSet.StitchResult> map, ModelBakery modelBakery) {
        profilerFiller.push("load");
        profilerFiller.popPush("baking");
        HashMultimap multimap = HashMultimap.create();
        modelBakery.bakeModels((resourceLocation, material) -> {
            AtlasSet.StitchResult stitchResult = (AtlasSet.StitchResult)map.get(material.atlasLocation());
            TextureAtlasSprite textureAtlasSprite = stitchResult.getSprite(material.texture());
            if (textureAtlasSprite != null) {
                return textureAtlasSprite;
            }
            multimap.put(resourceLocation, material);
            return stitchResult.missing();
        });
        multimap.asMap().forEach((resourceLocation, collection) -> LOGGER.warn("Missing textures in model {}:\n{}", resourceLocation, (Object)collection.stream().sorted(Material.COMPARATOR).map(material -> "    " + material.atlasLocation() + ":" + material.texture()).collect(Collectors.joining("\n"))));
        profilerFiller.popPush("dispatch");
        Map<ResourceLocation, BakedModel> map2 = modelBakery.getBakedTopLevelModels();
        BakedModel bakedModel = map2.get(ModelBakery.MISSING_MODEL_LOCATION);
        IdentityHashMap<BlockState, BakedModel> map3 = new IdentityHashMap<BlockState, BakedModel>();
        for (Block block : Registry.BLOCK) {
            block.getStateDefinition().getPossibleStates().forEach(blockState -> {
                ResourceLocation resourceLocation = blockState.getBlock().builtInRegistryHolder().key().location();
                BakedModel bakedModel2 = map2.getOrDefault(BlockModelShaper.stateToModelLocation(resourceLocation, blockState), bakedModel);
                map3.put((BlockState)blockState, bakedModel2);
            });
        }
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf((CompletableFuture[])map.values().stream().map(AtlasSet.StitchResult::readyForUpload).toArray(CompletableFuture[]::new));
        profilerFiller.pop();
        profilerFiller.endTick();
        return new ReloadState(modelBakery, bakedModel, map3, map, completableFuture);
    }

    private void apply(ReloadState reloadState, ProfilerFiller profilerFiller) {
        profilerFiller.startTick();
        profilerFiller.push("upload");
        reloadState.atlasPreparations.values().forEach(AtlasSet.StitchResult::upload);
        ModelBakery modelBakery = reloadState.modelBakery;
        this.bakedRegistry = modelBakery.getBakedTopLevelModels();
        this.modelGroups = modelBakery.getModelGroups();
        this.missingModel = reloadState.missingModel;
        profilerFiller.popPush("cache");
        this.blockModelShaper.replaceCache(reloadState.modelCache);
        profilerFiller.pop();
        profilerFiller.endTick();
    }

    public boolean requiresRender(BlockState blockState, BlockState blockState2) {
        int j;
        if (blockState == blockState2) {
            return false;
        }
        int i = this.modelGroups.getInt(blockState);
        if (i != -1 && i == (j = this.modelGroups.getInt(blockState2))) {
            FluidState fluidState2;
            FluidState fluidState = blockState.getFluidState();
            return fluidState != (fluidState2 = blockState2.getFluidState());
        }
        return true;
    }

    public TextureAtlas getAtlas(ResourceLocation resourceLocation) {
        return this.atlases.getAtlas(resourceLocation);
    }

    @Override
    public void close() {
        this.atlases.close();
    }

    public void updateMaxMipLevel(int i) {
        this.maxMipmapLevels = i;
    }

    private /* synthetic */ ReloadState method_45885(ProfilerFiller profilerFiller, Map map, CompletableFuture completableFuture, Void void_) {
        return this.loadModels(profilerFiller, map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> (AtlasSet.StitchResult)((CompletableFuture)entry.getValue()).join())), (ModelBakery)completableFuture.join());
    }

    @Environment(value=EnvType.CLIENT)
    record ReloadState(ModelBakery modelBakery, BakedModel missingModel, Map<BlockState, BakedModel> modelCache, Map<ResourceLocation, AtlasSet.StitchResult> atlasPreparations, CompletableFuture<Void> readyForUpload) {
    }
}

