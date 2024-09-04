package net.minecraft.client.resources.model;

import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ModelBakery {
	public static final Material FIRE_0 = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/fire_0"));
	public static final Material FIRE_1 = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/fire_1"));
	public static final Material LAVA_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/lava_flow"));
	public static final Material WATER_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/water_flow"));
	public static final Material WATER_OVERLAY = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/water_overlay"));
	public static final Material BANNER_BASE = new Material(Sheets.BANNER_SHEET, ResourceLocation.withDefaultNamespace("entity/banner_base"));
	public static final Material SHIELD_BASE = new Material(Sheets.SHIELD_SHEET, ResourceLocation.withDefaultNamespace("entity/shield_base"));
	public static final Material NO_PATTERN_SHIELD = new Material(Sheets.SHIELD_SHEET, ResourceLocation.withDefaultNamespace("entity/shield_base_nopattern"));
	public static final int DESTROY_STAGE_COUNT = 10;
	public static final List<ResourceLocation> DESTROY_STAGES = (List<ResourceLocation>)IntStream.range(0, 10)
		.mapToObj(i -> ResourceLocation.withDefaultNamespace("block/destroy_stage_" + i))
		.collect(Collectors.toList());
	public static final List<ResourceLocation> BREAKING_LOCATIONS = (List<ResourceLocation>)DESTROY_STAGES.stream()
		.map(resourceLocation -> resourceLocation.withPath((UnaryOperator<String>)(string -> "textures/" + string + ".png")))
		.collect(Collectors.toList());
	public static final List<RenderType> DESTROY_TYPES = (List<RenderType>)BREAKING_LOCATIONS.stream().map(RenderType::crumbling).collect(Collectors.toList());
	static final Logger LOGGER = LogUtils.getLogger();
	static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
	final Map<ModelBakery.BakedCacheKey, BakedModel> bakedCache = new HashMap();
	private final Map<ModelResourceLocation, BakedModel> bakedTopLevelModels = new HashMap();
	private final Map<ModelResourceLocation, UnbakedModel> topModels;
	final Map<ResourceLocation, UnbakedModel> unbakedModels;
	final UnbakedModel missingModel;

	public ModelBakery(Map<ModelResourceLocation, UnbakedModel> map, Map<ResourceLocation, UnbakedModel> map2, UnbakedModel unbakedModel) {
		this.topModels = map;
		this.unbakedModels = map2;
		this.missingModel = unbakedModel;
	}

	public void bakeModels(ModelBakery.TextureGetter textureGetter) {
		this.topModels.forEach((modelResourceLocation, unbakedModel) -> {
			BakedModel bakedModel = null;

			try {
				bakedModel = new ModelBakery.ModelBakerImpl(textureGetter, modelResourceLocation).bakeUncached(unbakedModel, BlockModelRotation.X0_Y0);
			} catch (Exception var6) {
				LOGGER.warn("Unable to bake model: '{}': {}", modelResourceLocation, var6);
			}

			if (bakedModel != null) {
				this.bakedTopLevelModels.put(modelResourceLocation, bakedModel);
			}
		});
	}

	public Map<ModelResourceLocation, BakedModel> getBakedTopLevelModels() {
		return this.bakedTopLevelModels;
	}

	@Environment(EnvType.CLIENT)
	static record BakedCacheKey(ResourceLocation id, Transformation transformation, boolean isUvLocked) {
	}

	@Environment(EnvType.CLIENT)
	class ModelBakerImpl implements ModelBaker {
		private final Function<Material, TextureAtlasSprite> modelTextureGetter;

		ModelBakerImpl(final ModelBakery.TextureGetter textureGetter, final ModelResourceLocation modelResourceLocation) {
			this.modelTextureGetter = material -> textureGetter.get(modelResourceLocation, material);
		}

		private UnbakedModel getModel(ResourceLocation resourceLocation) {
			UnbakedModel unbakedModel = (UnbakedModel)ModelBakery.this.unbakedModels.get(resourceLocation);
			if (unbakedModel == null) {
				ModelBakery.LOGGER.warn("Requested a model that was not discovered previously: {}", resourceLocation);
				return ModelBakery.this.missingModel;
			} else {
				return unbakedModel;
			}
		}

		@Override
		public BakedModel bake(ResourceLocation resourceLocation, ModelState modelState) {
			ModelBakery.BakedCacheKey bakedCacheKey = new ModelBakery.BakedCacheKey(resourceLocation, modelState.getRotation(), modelState.isUvLocked());
			BakedModel bakedModel = (BakedModel)ModelBakery.this.bakedCache.get(bakedCacheKey);
			if (bakedModel != null) {
				return bakedModel;
			} else {
				UnbakedModel unbakedModel = this.getModel(resourceLocation);
				BakedModel bakedModel2 = this.bakeUncached(unbakedModel, modelState);
				ModelBakery.this.bakedCache.put(bakedCacheKey, bakedModel2);
				return bakedModel2;
			}
		}

		BakedModel bakeUncached(UnbakedModel unbakedModel, ModelState modelState) {
			if (unbakedModel instanceof BlockModel blockModel && blockModel.getRootModel() == SpecialModels.GENERATED_MARKER) {
				return ModelBakery.ITEM_MODEL_GENERATOR.generateBlockModel(this.modelTextureGetter, blockModel).bake(this.modelTextureGetter, modelState, false);
			}

			return unbakedModel.bake(this, this.modelTextureGetter, modelState);
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface TextureGetter {
		TextureAtlasSprite get(ModelResourceLocation modelResourceLocation, Material material);
	}
}
