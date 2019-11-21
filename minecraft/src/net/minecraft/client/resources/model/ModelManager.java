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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@Environment(EnvType.CLIENT)
public class ModelManager extends SimplePreparableReloadListener<ModelBakery> implements AutoCloseable {
	private Map<ResourceLocation, BakedModel> bakedRegistry;
	private AtlasSet atlases;
	private final BlockModelShaper blockModelShaper;
	private final TextureManager textureManager;
	private final BlockColors blockColors;
	private final int mipmapLevels;
	private BakedModel missingModel;
	private Object2IntMap<BlockState> modelGroups;

	public ModelManager(TextureManager textureManager, BlockColors blockColors, int i) {
		this.textureManager = textureManager;
		this.blockColors = blockColors;
		this.mipmapLevels = i;
		this.blockModelShaper = new BlockModelShaper(this);
	}

	public BakedModel getModel(ModelResourceLocation modelResourceLocation) {
		return (BakedModel)this.bakedRegistry.getOrDefault(modelResourceLocation, this.missingModel);
	}

	public BakedModel getMissingModel() {
		return this.missingModel;
	}

	public BlockModelShaper getBlockModelShaper() {
		return this.blockModelShaper;
	}

	protected ModelBakery prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		profilerFiller.startTick();
		ModelBakery modelBakery = new ModelBakery(resourceManager, this.blockColors, profilerFiller, this.mipmapLevels);
		profilerFiller.endTick();
		return modelBakery;
	}

	protected void apply(ModelBakery modelBakery, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		profilerFiller.startTick();
		profilerFiller.push("upload");
		this.atlases = modelBakery.uploadTextures(this.textureManager, this.mipmapLevels, profilerFiller);
		this.bakedRegistry = modelBakery.getBakedTopLevelModels();
		this.modelGroups = modelBakery.getModelGroups();
		this.missingModel = (BakedModel)this.bakedRegistry.get(ModelBakery.MISSING_MODEL_LOCATION);
		profilerFiller.popPush("cache");
		this.blockModelShaper.rebuildCache();
		profilerFiller.pop();
		profilerFiller.endTick();
	}

	public boolean requiresRender(BlockState blockState, BlockState blockState2) {
		if (blockState == blockState2) {
			return false;
		} else {
			int i = this.modelGroups.getInt(blockState);
			if (i != -1) {
				int j = this.modelGroups.getInt(blockState2);
				if (i == j) {
					FluidState fluidState = blockState.getFluidState();
					FluidState fluidState2 = blockState2.getFluidState();
					return fluidState != fluidState2;
				}
			}

			return true;
		}
	}

	public TextureAtlas getAtlas(ResourceLocation resourceLocation) {
		return this.atlases.getAtlas(resourceLocation);
	}

	public void close() {
		this.atlases.close();
	}

	public void updateMaxMipLevel(int i) {
		this.atlases.updateMaxMipLevel(this.textureManager, i);
	}
}
