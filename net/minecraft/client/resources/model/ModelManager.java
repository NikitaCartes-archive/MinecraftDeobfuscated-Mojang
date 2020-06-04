/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources.model;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ModelManager
extends SimplePreparableReloadListener<ModelBakery>
implements AutoCloseable {
    private Map<ResourceLocation, BakedModel> bakedRegistry;
    @Nullable
    private AtlasSet atlases;
    private final BlockModelShaper blockModelShaper;
    private final TextureManager textureManager;
    private final BlockColors blockColors;
    private int maxMipmapLevels;
    private BakedModel missingModel;
    private Object2IntMap<BlockState> modelGroups;

    public ModelManager(TextureManager textureManager, BlockColors blockColors, int i) {
        this.textureManager = textureManager;
        this.blockColors = blockColors;
        this.maxMipmapLevels = i;
        this.blockModelShaper = new BlockModelShaper(this);
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
    protected ModelBakery prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        profilerFiller.startTick();
        ModelBakery modelBakery = new ModelBakery(resourceManager, this.blockColors, profilerFiller, this.maxMipmapLevels);
        profilerFiller.endTick();
        return modelBakery;
    }

    @Override
    protected void apply(ModelBakery modelBakery, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        profilerFiller.startTick();
        profilerFiller.push("upload");
        if (this.atlases != null) {
            this.atlases.close();
        }
        this.atlases = modelBakery.uploadTextures(this.textureManager, profilerFiller);
        this.bakedRegistry = modelBakery.getBakedTopLevelModels();
        this.modelGroups = modelBakery.getModelGroups();
        this.missingModel = this.bakedRegistry.get(ModelBakery.MISSING_MODEL_LOCATION);
        profilerFiller.popPush("cache");
        this.blockModelShaper.rebuildCache();
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
        if (this.atlases != null) {
            this.atlases.close();
        }
    }

    public void updateMaxMipLevel(int i) {
        this.maxMipmapLevels = i;
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }
}

