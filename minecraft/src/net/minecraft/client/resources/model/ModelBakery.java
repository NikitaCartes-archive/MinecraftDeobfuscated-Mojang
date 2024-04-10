package net.minecraft.client.resources.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ModelBakery {
	public static final Material FIRE_0 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/fire_0"));
	public static final Material FIRE_1 = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/fire_1"));
	public static final Material LAVA_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/lava_flow"));
	public static final Material WATER_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/water_flow"));
	public static final Material WATER_OVERLAY = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation("block/water_overlay"));
	public static final Material BANNER_BASE = new Material(Sheets.BANNER_SHEET, new ResourceLocation("entity/banner_base"));
	public static final Material SHIELD_BASE = new Material(Sheets.SHIELD_SHEET, new ResourceLocation("entity/shield_base"));
	public static final Material NO_PATTERN_SHIELD = new Material(Sheets.SHIELD_SHEET, new ResourceLocation("entity/shield_base_nopattern"));
	public static final int DESTROY_STAGE_COUNT = 10;
	public static final List<ResourceLocation> DESTROY_STAGES = (List<ResourceLocation>)IntStream.range(0, 10)
		.mapToObj(i -> new ResourceLocation("block/destroy_stage_" + i))
		.collect(Collectors.toList());
	public static final List<ResourceLocation> BREAKING_LOCATIONS = (List<ResourceLocation>)DESTROY_STAGES.stream()
		.map(resourceLocation -> new ResourceLocation("textures/" + resourceLocation.getPath() + ".png"))
		.collect(Collectors.toList());
	public static final List<RenderType> DESTROY_TYPES = (List<RenderType>)BREAKING_LOCATIONS.stream().map(RenderType::crumbling).collect(Collectors.toList());
	static final int SINGLETON_MODEL_GROUP = -1;
	private static final int INVISIBLE_MODEL_GROUP = 0;
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String BUILTIN_SLASH = "builtin/";
	private static final String BUILTIN_SLASH_GENERATED = "builtin/generated";
	private static final String BUILTIN_BLOCK_ENTITY = "builtin/entity";
	private static final String MISSING_MODEL_NAME = "missing";
	public static final ModelResourceLocation MISSING_MODEL_LOCATION = ModelResourceLocation.vanilla("builtin/missing", "missing");
	public static final FileToIdConverter BLOCKSTATE_LISTER = FileToIdConverter.json("blockstates");
	public static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");
	@VisibleForTesting
	public static final String MISSING_MODEL_MESH = ("{    'textures': {       'particle': '"
			+ MissingTextureAtlasSprite.getLocation().getPath()
			+ "',       'missingno': '"
			+ MissingTextureAtlasSprite.getLocation().getPath()
			+ "'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}")
		.replace('\'', '"');
	private static final Map<String, String> BUILTIN_MODELS = Maps.<String, String>newHashMap(ImmutableMap.of("missing", MISSING_MODEL_MESH));
	private static final Splitter COMMA_SPLITTER = Splitter.on(',');
	private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);
	public static final BlockModel GENERATION_MARKER = Util.make(
		BlockModel.fromString("{\"gui_light\": \"front\"}"), blockModel -> blockModel.name = "generation marker"
	);
	public static final BlockModel BLOCK_ENTITY_MARKER = Util.make(
		BlockModel.fromString("{\"gui_light\": \"side\"}"), blockModel -> blockModel.name = "block entity marker"
	);
	private static final StateDefinition<Block, BlockState> ITEM_FRAME_FAKE_DEFINITION = new StateDefinition.Builder<Block, BlockState>(Blocks.AIR)
		.add(BooleanProperty.create("map"))
		.create(Block::defaultBlockState, BlockState::new);
	static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
	private static final Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS = ImmutableMap.of(
		new ResourceLocation("item_frame"), ITEM_FRAME_FAKE_DEFINITION, new ResourceLocation("glow_item_frame"), ITEM_FRAME_FAKE_DEFINITION
	);
	private final BlockColors blockColors;
	private final Map<ResourceLocation, BlockModel> modelResources;
	private final Map<ResourceLocation, List<ModelBakery.LoadedJson>> blockStateResources;
	private final Set<ResourceLocation> loadingStack = Sets.<ResourceLocation>newHashSet();
	private final BlockModelDefinition.Context context = new BlockModelDefinition.Context();
	private final Map<ResourceLocation, UnbakedModel> unbakedCache = Maps.<ResourceLocation, UnbakedModel>newHashMap();
	final Map<ModelBakery.BakedCacheKey, BakedModel> bakedCache = Maps.<ModelBakery.BakedCacheKey, BakedModel>newHashMap();
	private final Map<ResourceLocation, UnbakedModel> topLevelModels = Maps.<ResourceLocation, UnbakedModel>newHashMap();
	private final Map<ResourceLocation, BakedModel> bakedTopLevelModels = Maps.<ResourceLocation, BakedModel>newHashMap();
	private int nextModelGroup = 1;
	private final Object2IntMap<BlockState> modelGroups = Util.make(
		new Object2IntOpenHashMap<>(), object2IntOpenHashMap -> object2IntOpenHashMap.defaultReturnValue(-1)
	);

	public ModelBakery(
		BlockColors blockColors, ProfilerFiller profilerFiller, Map<ResourceLocation, BlockModel> map, Map<ResourceLocation, List<ModelBakery.LoadedJson>> map2
	) {
		this.blockColors = blockColors;
		this.modelResources = map;
		this.blockStateResources = map2;
		profilerFiller.push("missing_model");

		try {
			this.unbakedCache.put(MISSING_MODEL_LOCATION, this.loadBlockModel(MISSING_MODEL_LOCATION));
			this.loadTopLevel(MISSING_MODEL_LOCATION);
		} catch (IOException var7) {
			LOGGER.error("Error loading missing model, should never happen :(", (Throwable)var7);
			throw new RuntimeException(var7);
		}

		profilerFiller.popPush("static_definitions");
		STATIC_DEFINITIONS.forEach(
			(resourceLocationx, stateDefinition) -> stateDefinition.getPossibleStates()
					.forEach(blockState -> this.loadTopLevel(BlockModelShaper.stateToModelLocation(resourceLocationx, blockState)))
		);
		profilerFiller.popPush("blocks");

		for (Block block : BuiltInRegistries.BLOCK) {
			block.getStateDefinition().getPossibleStates().forEach(blockState -> this.loadTopLevel(BlockModelShaper.stateToModelLocation(blockState)));
		}

		profilerFiller.popPush("items");

		for (ResourceLocation resourceLocation : BuiltInRegistries.ITEM.keySet()) {
			this.loadTopLevel(new ModelResourceLocation(resourceLocation, "inventory"));
		}

		profilerFiller.popPush("special");
		this.loadTopLevel(ItemRenderer.TRIDENT_IN_HAND_MODEL);
		this.loadTopLevel(ItemRenderer.SPYGLASS_IN_HAND_MODEL);
		this.topLevelModels.values().forEach(unbakedModel -> unbakedModel.resolveParents(this::getModel));
		profilerFiller.pop();
	}

	public void bakeModels(BiFunction<ResourceLocation, Material, TextureAtlasSprite> biFunction) {
		this.topLevelModels.keySet().forEach(resourceLocation -> {
			BakedModel bakedModel = null;

			try {
				bakedModel = new ModelBakery.ModelBakerImpl(biFunction, resourceLocation).bake(resourceLocation, BlockModelRotation.X0_Y0);
			} catch (Exception var5) {
				LOGGER.warn("Unable to bake model: '{}': {}", resourceLocation, var5);
			}

			if (bakedModel != null) {
				this.bakedTopLevelModels.put(resourceLocation, bakedModel);
			}
		});
	}

	private static Predicate<BlockState> predicate(StateDefinition<Block, BlockState> stateDefinition, String string) {
		Map<Property<?>, Comparable<?>> map = Maps.<Property<?>, Comparable<?>>newHashMap();

		for (String string2 : COMMA_SPLITTER.split(string)) {
			Iterator<String> iterator = EQUAL_SPLITTER.split(string2).iterator();
			if (iterator.hasNext()) {
				String string3 = (String)iterator.next();
				Property<?> property = stateDefinition.getProperty(string3);
				if (property != null && iterator.hasNext()) {
					String string4 = (String)iterator.next();
					Comparable<?> comparable = getValueHelper((Property<Comparable<?>>)property, string4);
					if (comparable == null) {
						throw new RuntimeException("Unknown value: '" + string4 + "' for blockstate property: '" + string3 + "' " + property.getPossibleValues());
					}

					map.put(property, comparable);
				} else if (!string3.isEmpty()) {
					throw new RuntimeException("Unknown blockstate property: '" + string3 + "'");
				}
			}
		}

		Block block = stateDefinition.getOwner();
		return blockState -> {
			if (blockState != null && blockState.is(block)) {
				for (Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
					if (!Objects.equals(blockState.getValue((Property)entry.getKey()), entry.getValue())) {
						return false;
					}
				}

				return true;
			} else {
				return false;
			}
		};
	}

	@Nullable
	static <T extends Comparable<T>> T getValueHelper(Property<T> property, String string) {
		return (T)property.getValue(string).orElse(null);
	}

	public UnbakedModel getModel(ResourceLocation resourceLocation) {
		if (this.unbakedCache.containsKey(resourceLocation)) {
			return (UnbakedModel)this.unbakedCache.get(resourceLocation);
		} else if (this.loadingStack.contains(resourceLocation)) {
			throw new IllegalStateException("Circular reference while loading " + resourceLocation);
		} else {
			this.loadingStack.add(resourceLocation);
			UnbakedModel unbakedModel = (UnbakedModel)this.unbakedCache.get(MISSING_MODEL_LOCATION);

			while (!this.loadingStack.isEmpty()) {
				ResourceLocation resourceLocation2 = (ResourceLocation)this.loadingStack.iterator().next();

				try {
					if (!this.unbakedCache.containsKey(resourceLocation2)) {
						this.loadModel(resourceLocation2);
					}
				} catch (ModelBakery.BlockStateDefinitionException var9) {
					LOGGER.warn(var9.getMessage());
					this.unbakedCache.put(resourceLocation2, unbakedModel);
				} catch (Exception var10) {
					LOGGER.warn("Unable to load model: '{}' referenced from: {}: {}", resourceLocation2, resourceLocation, var10);
					this.unbakedCache.put(resourceLocation2, unbakedModel);
				} finally {
					this.loadingStack.remove(resourceLocation2);
				}
			}

			return (UnbakedModel)this.unbakedCache.getOrDefault(resourceLocation, unbakedModel);
		}
	}

	private void loadModel(ResourceLocation resourceLocation) throws Exception {
		if (!(resourceLocation instanceof ModelResourceLocation modelResourceLocation)) {
			this.cacheAndQueueDependencies(resourceLocation, this.loadBlockModel(resourceLocation));
		} else {
			if (Objects.equals(modelResourceLocation.getVariant(), "inventory")) {
				ResourceLocation resourceLocation2 = resourceLocation.withPrefix("item/");
				BlockModel blockModel = this.loadBlockModel(resourceLocation2);
				this.cacheAndQueueDependencies(modelResourceLocation, blockModel);
				this.unbakedCache.put(resourceLocation2, blockModel);
			} else {
				ResourceLocation resourceLocation2 = new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath());
				StateDefinition<Block, BlockState> stateDefinition = (StateDefinition<Block, BlockState>)Optional.ofNullable(
						(StateDefinition)STATIC_DEFINITIONS.get(resourceLocation2)
					)
					.orElseGet(() -> BuiltInRegistries.BLOCK.get(resourceLocation2).getStateDefinition());
				this.context.setDefinition(stateDefinition);
				List<Property<?>> list = ImmutableList.copyOf(this.blockColors.getColoringProperties(stateDefinition.getOwner()));
				ImmutableList<BlockState> immutableList = stateDefinition.getPossibleStates();
				Map<ModelResourceLocation, BlockState> map = Maps.<ModelResourceLocation, BlockState>newHashMap();
				immutableList.forEach(blockState -> map.put(BlockModelShaper.stateToModelLocation(resourceLocation2, blockState), blockState));
				Map<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> map2 = Maps.<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>>newHashMap();
				ResourceLocation resourceLocation3 = BLOCKSTATE_LISTER.idToFile(resourceLocation);
				UnbakedModel unbakedModel = (UnbakedModel)this.unbakedCache.get(MISSING_MODEL_LOCATION);
				ModelBakery.ModelGroupKey modelGroupKey = new ModelBakery.ModelGroupKey(ImmutableList.of(unbakedModel), ImmutableList.of());
				Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair = Pair.of(unbakedModel, () -> modelGroupKey);

				try {
					for (Pair<String, BlockModelDefinition> pair2 : ((List)this.blockStateResources.getOrDefault(resourceLocation3, List.of()))
						.stream()
						.map(
							loadedJson -> {
								try {
									return Pair.of(loadedJson.source, BlockModelDefinition.fromJsonElement(this.context, loadedJson.data));
								} catch (Exception var4x) {
									throw new ModelBakery.BlockStateDefinitionException(
										String.format(
											Locale.ROOT, "Exception loading blockstate definition: '%s' in resourcepack: '%s': %s", resourceLocation3, loadedJson.source, var4x.getMessage()
										)
									);
								}
							}
						)
						.toList()) {
						BlockModelDefinition blockModelDefinition = pair2.getSecond();
						Map<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>> map3 = Maps.<BlockState, Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>>newIdentityHashMap();
						MultiPart multiPart;
						if (blockModelDefinition.isMultiPart()) {
							multiPart = blockModelDefinition.getMultiPart();
							immutableList.forEach(blockState -> map3.put(blockState, Pair.of(multiPart, () -> ModelBakery.ModelGroupKey.create(blockState, multiPart, list))));
						} else {
							multiPart = null;
						}

						blockModelDefinition.getVariants()
							.forEach(
								(string, multiVariant) -> {
									try {
										immutableList.stream()
											.filter(predicate(stateDefinition, string))
											.forEach(
												blockState -> {
													Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair2xx = (Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>)map3.put(
														blockState, Pair.of(multiVariant, () -> ModelBakery.ModelGroupKey.create(blockState, multiVariant, list))
													);
													if (pair2xx != null && pair2xx.getFirst() != multiPart) {
														map3.put(blockState, pair);
														throw new RuntimeException(
															"Overlapping definition with: "
																+ (String)((Entry)blockModelDefinition.getVariants()
																		.entrySet()
																		.stream()
																		.filter(entry -> entry.getValue() == pair2xx.getFirst())
																		.findFirst()
																		.get())
																	.getKey()
														);
													}
												}
											);
									} catch (Exception var12x) {
										LOGGER.warn(
											"Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}",
											resourceLocation3,
											pair2.getFirst(),
											string,
											var12x.getMessage()
										);
									}
								}
							);
						map2.putAll(map3);
					}
				} catch (ModelBakery.BlockStateDefinitionException var24) {
					throw var24;
				} catch (Exception var25) {
					throw new ModelBakery.BlockStateDefinitionException(
						String.format(Locale.ROOT, "Exception loading blockstate definition: '%s': %s", resourceLocation3, var25)
					);
				} finally {
					Map<ModelBakery.ModelGroupKey, Set<BlockState>> map5 = Maps.<ModelBakery.ModelGroupKey, Set<BlockState>>newHashMap();
					map.forEach((modelResourceLocationx, blockState) -> {
						Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>> pair2x = (Pair<UnbakedModel, Supplier<ModelBakery.ModelGroupKey>>)map2.get(blockState);
						if (pair2x == null) {
							LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", resourceLocation3, modelResourceLocationx);
							pair2x = pair;
						}

						this.cacheAndQueueDependencies(modelResourceLocationx, pair2x.getFirst());

						try {
							ModelBakery.ModelGroupKey modelGroupKeyx = (ModelBakery.ModelGroupKey)pair2x.getSecond().get();
							((Set)map5.computeIfAbsent(modelGroupKeyx, modelGroupKeyxx -> Sets.newIdentityHashSet())).add(blockState);
						} catch (Exception var9x) {
							LOGGER.warn("Exception evaluating model definition: '{}'", modelResourceLocationx, var9x);
						}
					});
					map5.forEach((modelGroupKeyx, set) -> {
						Iterator<BlockState> iterator = set.iterator();

						while (iterator.hasNext()) {
							BlockState blockState = (BlockState)iterator.next();
							if (blockState.getRenderShape() != RenderShape.MODEL) {
								iterator.remove();
								this.modelGroups.put(blockState, 0);
							}
						}

						if (set.size() > 1) {
							this.registerModelGroup(set);
						}
					});
				}
			}
		}
	}

	private void cacheAndQueueDependencies(ResourceLocation resourceLocation, UnbakedModel unbakedModel) {
		this.unbakedCache.put(resourceLocation, unbakedModel);
		this.loadingStack.addAll(unbakedModel.getDependencies());
	}

	private void loadTopLevel(ModelResourceLocation modelResourceLocation) {
		UnbakedModel unbakedModel = this.getModel(modelResourceLocation);
		this.unbakedCache.put(modelResourceLocation, unbakedModel);
		this.topLevelModels.put(modelResourceLocation, unbakedModel);
	}

	private void registerModelGroup(Iterable<BlockState> iterable) {
		int i = this.nextModelGroup++;
		iterable.forEach(blockState -> this.modelGroups.put(blockState, i));
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

	public Map<ResourceLocation, BakedModel> getBakedTopLevelModels() {
		return this.bakedTopLevelModels;
	}

	public Object2IntMap<BlockState> getModelGroups() {
		return this.modelGroups;
	}

	@Environment(EnvType.CLIENT)
	static record BakedCacheKey(ResourceLocation id, Transformation transformation, boolean isUvLocked) {
	}

	@Environment(EnvType.CLIENT)
	static class BlockStateDefinitionException extends RuntimeException {
		public BlockStateDefinitionException(String string) {
			super(string);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record LoadedJson(String source, JsonElement data) {
	}

	@Environment(EnvType.CLIENT)
	class ModelBakerImpl implements ModelBaker {
		private final Function<Material, TextureAtlasSprite> modelTextureGetter;

		ModelBakerImpl(final BiFunction<ResourceLocation, Material, TextureAtlasSprite> biFunction, final ResourceLocation resourceLocation) {
			this.modelTextureGetter = material -> (TextureAtlasSprite)biFunction.apply(resourceLocation, material);
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
				if (unbakedModel instanceof BlockModel blockModel && blockModel.getRootModel() == ModelBakery.GENERATION_MARKER) {
					return ModelBakery.ITEM_MODEL_GENERATOR
						.generateBlockModel(this.modelTextureGetter, blockModel)
						.bake(this, blockModel, this.modelTextureGetter, modelState, resourceLocation, false);
				}

				BakedModel bakedModel2 = unbakedModel.bake(this, this.modelTextureGetter, modelState, resourceLocation);
				ModelBakery.this.bakedCache.put(bakedCacheKey, bakedModel2);
				return bakedModel2;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class ModelGroupKey {
		private final List<UnbakedModel> models;
		private final List<Object> coloringValues;

		public ModelGroupKey(List<UnbakedModel> list, List<Object> list2) {
			this.models = list;
			this.coloringValues = list2;
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else {
				return !(object instanceof ModelBakery.ModelGroupKey modelGroupKey)
					? false
					: Objects.equals(this.models, modelGroupKey.models) && Objects.equals(this.coloringValues, modelGroupKey.coloringValues);
			}
		}

		public int hashCode() {
			return 31 * this.models.hashCode() + this.coloringValues.hashCode();
		}

		public static ModelBakery.ModelGroupKey create(BlockState blockState, MultiPart multiPart, Collection<Property<?>> collection) {
			StateDefinition<Block, BlockState> stateDefinition = blockState.getBlock().getStateDefinition();
			List<UnbakedModel> list = (List<UnbakedModel>)multiPart.getSelectors()
				.stream()
				.filter(selector -> selector.getPredicate(stateDefinition).test(blockState))
				.map(Selector::getVariant)
				.collect(ImmutableList.toImmutableList());
			List<Object> list2 = getColoringValues(blockState, collection);
			return new ModelBakery.ModelGroupKey(list, list2);
		}

		public static ModelBakery.ModelGroupKey create(BlockState blockState, UnbakedModel unbakedModel, Collection<Property<?>> collection) {
			List<Object> list = getColoringValues(blockState, collection);
			return new ModelBakery.ModelGroupKey(ImmutableList.of(unbakedModel), list);
		}

		private static List<Object> getColoringValues(BlockState blockState, Collection<Property<?>> collection) {
			return (List<Object>)collection.stream().map(blockState::getValue).collect(ImmutableList.toImmutableList());
		}
	}
}
