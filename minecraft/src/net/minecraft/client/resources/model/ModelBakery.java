package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.state.BlockState;
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
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String BUILTIN_SLASH = "builtin/";
	private static final String BUILTIN_SLASH_GENERATED = "builtin/generated";
	private static final String BUILTIN_BLOCK_ENTITY = "builtin/entity";
	private static final String MISSING_MODEL_NAME = "missing";
	public static final ResourceLocation MISSING_MODEL_LOCATION = ResourceLocation.withDefaultNamespace("builtin/missing");
	public static final ModelResourceLocation MISSING_MODEL_VARIANT = new ModelResourceLocation(MISSING_MODEL_LOCATION, "missing");
	public static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");
	@VisibleForTesting
	public static final String MISSING_MODEL_MESH = ("{    'textures': {       'particle': '"
			+ MissingTextureAtlasSprite.getLocation().getPath()
			+ "',       'missingno': '"
			+ MissingTextureAtlasSprite.getLocation().getPath()
			+ "'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}")
		.replace('\'', '"');
	private static final Map<String, String> BUILTIN_MODELS = Map.of("missing", MISSING_MODEL_MESH);
	public static final BlockModel GENERATION_MARKER = Util.make(
		BlockModel.fromString("{\"gui_light\": \"front\"}"), blockModel -> blockModel.name = "generation marker"
	);
	public static final BlockModel BLOCK_ENTITY_MARKER = Util.make(
		BlockModel.fromString("{\"gui_light\": \"side\"}"), blockModel -> blockModel.name = "block entity marker"
	);
	static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
	private final Map<ResourceLocation, BlockModel> modelResources;
	private final Set<ResourceLocation> loadingStack = new HashSet();
	private final Map<ResourceLocation, UnbakedModel> unbakedCache = new HashMap();
	final Map<ModelBakery.BakedCacheKey, BakedModel> bakedCache = new HashMap();
	private final Map<ModelResourceLocation, UnbakedModel> topLevelModels = new HashMap();
	private final Map<ModelResourceLocation, BakedModel> bakedTopLevelModels = new HashMap();
	private final UnbakedModel missingModel;
	private final Object2IntMap<BlockState> modelGroups;

	public ModelBakery(
		BlockColors blockColors,
		ProfilerFiller profilerFiller,
		Map<ResourceLocation, BlockModel> map,
		Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> map2
	) {
		this.modelResources = map;
		profilerFiller.push("missing_model");

		try {
			this.missingModel = this.loadBlockModel(MISSING_MODEL_LOCATION);
			this.registerModel(MISSING_MODEL_VARIANT, this.missingModel);
		} catch (IOException var8) {
			LOGGER.error("Error loading missing model, should never happen :(", (Throwable)var8);
			throw new RuntimeException(var8);
		}

		BlockStateModelLoader blockStateModelLoader = new BlockStateModelLoader(
			map2, profilerFiller, this.missingModel, blockColors, this::registerModelAndLoadDependencies
		);
		blockStateModelLoader.loadAllBlockStates();
		this.modelGroups = blockStateModelLoader.getModelGroups();
		profilerFiller.popPush("items");

		for (ResourceLocation resourceLocation : BuiltInRegistries.ITEM.keySet()) {
			this.loadItemModelAndDependencies(resourceLocation);
		}

		profilerFiller.popPush("special");
		this.loadSpecialItemModelAndDependencies(ItemRenderer.TRIDENT_IN_HAND_MODEL);
		this.loadSpecialItemModelAndDependencies(ItemRenderer.SPYGLASS_IN_HAND_MODEL);
		this.topLevelModels.values().forEach(unbakedModel -> unbakedModel.resolveParents(this::getModel));
		profilerFiller.pop();
	}

	public void bakeModels(ModelBakery.TextureGetter textureGetter) {
		this.topLevelModels.forEach((modelResourceLocation, unbakedModel) -> {
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

	UnbakedModel getModel(ResourceLocation resourceLocation) {
		if (this.unbakedCache.containsKey(resourceLocation)) {
			return (UnbakedModel)this.unbakedCache.get(resourceLocation);
		} else if (this.loadingStack.contains(resourceLocation)) {
			throw new IllegalStateException("Circular reference while loading " + resourceLocation);
		} else {
			this.loadingStack.add(resourceLocation);

			while (!this.loadingStack.isEmpty()) {
				ResourceLocation resourceLocation2 = (ResourceLocation)this.loadingStack.iterator().next();

				try {
					if (!this.unbakedCache.containsKey(resourceLocation2)) {
						UnbakedModel unbakedModel = this.loadBlockModel(resourceLocation2);
						this.unbakedCache.put(resourceLocation2, unbakedModel);
						this.loadingStack.addAll(unbakedModel.getDependencies());
					}
				} catch (Exception var7) {
					LOGGER.warn("Unable to load model: '{}' referenced from: {}: {}", resourceLocation2, resourceLocation, var7);
					this.unbakedCache.put(resourceLocation2, this.missingModel);
				} finally {
					this.loadingStack.remove(resourceLocation2);
				}
			}

			return (UnbakedModel)this.unbakedCache.getOrDefault(resourceLocation, this.missingModel);
		}
	}

	private void loadItemModelAndDependencies(ResourceLocation resourceLocation) {
		ModelResourceLocation modelResourceLocation = ModelResourceLocation.inventory(resourceLocation);
		ResourceLocation resourceLocation2 = resourceLocation.withPrefix("item/");
		UnbakedModel unbakedModel = this.getModel(resourceLocation2);
		this.registerModelAndLoadDependencies(modelResourceLocation, unbakedModel);
	}

	private void loadSpecialItemModelAndDependencies(ModelResourceLocation modelResourceLocation) {
		ResourceLocation resourceLocation = modelResourceLocation.id().withPrefix("item/");
		UnbakedModel unbakedModel = this.getModel(resourceLocation);
		this.registerModelAndLoadDependencies(modelResourceLocation, unbakedModel);
	}

	private void registerModelAndLoadDependencies(ModelResourceLocation modelResourceLocation, UnbakedModel unbakedModel) {
		for (ResourceLocation resourceLocation : unbakedModel.getDependencies()) {
			this.getModel(resourceLocation);
		}

		this.registerModel(modelResourceLocation, unbakedModel);
	}

	private void registerModel(ModelResourceLocation modelResourceLocation, UnbakedModel unbakedModel) {
		this.topLevelModels.put(modelResourceLocation, unbakedModel);
	}

	private BlockModel loadBlockModel(ResourceLocation resourceLocation) throws IOException {
		String string = resourceLocation.getPath();
		if ("builtin/generated".equals(string)) {
			return GENERATION_MARKER;
		} else if ("builtin/entity".equals(string)) {
			return BLOCK_ENTITY_MARKER;
		} else if (string.startsWith("builtin/")) {
			String string2 = string.substring("builtin/".length());
			String string3 = (String)BUILTIN_MODELS.get(string2);
			if (string3 == null) {
				throw new FileNotFoundException(resourceLocation.toString());
			} else {
				Reader reader = new StringReader(string3);
				BlockModel blockModel = BlockModel.fromStream(reader);
				blockModel.name = resourceLocation.toString();
				return blockModel;
			}
		} else {
			ResourceLocation resourceLocation2 = MODEL_LISTER.idToFile(resourceLocation);
			BlockModel blockModel2 = (BlockModel)this.modelResources.get(resourceLocation2);
			if (blockModel2 == null) {
				throw new FileNotFoundException(resourceLocation2.toString());
			} else {
				blockModel2.name = resourceLocation.toString();
				return blockModel2;
			}
		}
	}

	public Map<ModelResourceLocation, BakedModel> getBakedTopLevelModels() {
		return this.bakedTopLevelModels;
	}

	public Object2IntMap<BlockState> getModelGroups() {
		return this.modelGroups;
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

		@Override
		public UnbakedModel getModel(ResourceLocation resourceLocation) {
			return ModelBakery.this.getModel(resourceLocation);
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

		@Nullable
		BakedModel bakeUncached(UnbakedModel unbakedModel, ModelState modelState) {
			if (unbakedModel instanceof BlockModel blockModel && blockModel.getRootModel() == ModelBakery.GENERATION_MARKER) {
				return ModelBakery.ITEM_MODEL_GENERATOR
					.generateBlockModel(this.modelTextureGetter, blockModel)
					.bake(this, blockModel, this.modelTextureGetter, modelState, false);
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
