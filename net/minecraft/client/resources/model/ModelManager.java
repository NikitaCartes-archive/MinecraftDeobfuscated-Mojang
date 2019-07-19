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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@Environment(value=EnvType.CLIENT)
public class ModelManager
extends SimplePreparableReloadListener<ModelBakery> {
    private Map<ResourceLocation, BakedModel> bakedRegistry;
    private final TextureAtlas terrainAtlas;
    private final BlockModelShaper blockModelShaper;
    private final BlockColors blockColors;
    private BakedModel missingModel;
    private Object2IntMap<BlockState> modelGroups;

    public ModelManager(TextureAtlas textureAtlas, BlockColors blockColors) {
        this.terrainAtlas = textureAtlas;
        this.blockColors = blockColors;
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
        ModelBakery modelBakery = new ModelBakery(resourceManager, this.terrainAtlas, this.blockColors, profilerFiller);
        profilerFiller.endTick();
        return modelBakery;
    }

    @Override
    protected void apply(ModelBakery modelBakery, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        profilerFiller.startTick();
        profilerFiller.push("upload");
        modelBakery.uploadTextures(profilerFiller);
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

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }
}

