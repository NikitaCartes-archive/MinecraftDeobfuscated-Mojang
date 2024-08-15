package net.minecraft.client.resources.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ModelManager implements PreparableReloadListener, AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final FileToIdConverter BLOCKSTATE_LISTER = FileToIdConverter.json("blockstates");
	private static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");
	private static final Map<ResourceLocation, ResourceLocation> VANILLA_ATLASES = Map.of(
		Sheets.BANNER_SHEET,
		ResourceLocation.withDefaultNamespace("banner_patterns"),
		Sheets.BED_SHEET,
		ResourceLocation.withDefaultNamespace("beds"),
		Sheets.CHEST_SHEET,
		ResourceLocation.withDefaultNamespace("chests"),
		Sheets.SHIELD_SHEET,
		ResourceLocation.withDefaultNamespace("shield_patterns"),
		Sheets.SIGN_SHEET,
		ResourceLocation.withDefaultNamespace("signs"),
		Sheets.SHULKER_SHEET,
		ResourceLocation.withDefaultNamespace("shulker_boxes"),
		Sheets.ARMOR_TRIMS_SHEET,
		ResourceLocation.withDefaultNamespace("armor_trims"),
		Sheets.DECORATED_POT_SHEET,
		ResourceLocation.withDefaultNamespace("decorated_pot"),
		TextureAtlas.LOCATION_BLOCKS,
		ResourceLocation.withDefaultNamespace("blocks")
	);
	private Map<ModelResourceLocation, BakedModel> bakedRegistry;
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
		return (BakedModel)this.bakedRegistry.getOrDefault(modelResourceLocation, this.missingModel);
	}

	public BakedModel getMissingModel() {
		return this.missingModel;
	}

	public BlockModelShaper getBlockModelShaper() {
		return this.blockModelShaper;
	}

	@Override
	public final CompletableFuture<Void> reload(
		PreparableReloadListener.PreparationBarrier preparationBarrier,
		ResourceManager resourceManager,
		ProfilerFiller profilerFiller,
		ProfilerFiller profilerFiller2,
		Executor executor,
		Executor executor2
	) {
		profilerFiller.startTick();
		UnbakedModel unbakedModel = MissingBlockModel.missingModel();
		BlockStateModelLoader blockStateModelLoader = new BlockStateModelLoader(unbakedModel);
		CompletableFuture<Map<ResourceLocation, UnbakedModel>> completableFuture = loadBlockModels(resourceManager, executor);
		CompletableFuture<BlockStateModelLoader.LoadedModels> completableFuture2 = loadBlockStates(blockStateModelLoader, resourceManager, executor);
		CompletableFuture<ModelDiscovery> completableFuture3 = completableFuture2.thenCombineAsync(
			completableFuture, (loadedModels, mapx) -> this.discoverModelDependencies(unbakedModel, mapx, loadedModels), executor
		);
		CompletableFuture<Object2IntMap<BlockState>> completableFuture4 = completableFuture2.thenApplyAsync(
			loadedModels -> buildModelGroups(this.blockColors, loadedModels), executor
		);
		Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>> map = this.atlases.scheduleLoad(resourceManager, this.maxMipmapLevels, executor);
		return CompletableFuture.allOf(
				(CompletableFuture[])Stream.concat(map.values().stream(), Stream.of(completableFuture3, completableFuture4)).toArray(CompletableFuture[]::new)
			)
			.thenApplyAsync(
				void_ -> {
					Map<ResourceLocation, AtlasSet.StitchResult> map2 = (Map<ResourceLocation, AtlasSet.StitchResult>)map.entrySet()
						.stream()
						.collect(Collectors.toMap(Entry::getKey, entry -> (AtlasSet.StitchResult)((CompletableFuture)entry.getValue()).join()));
					ModelDiscovery modelDiscovery = (ModelDiscovery)completableFuture3.join();
					Object2IntMap<BlockState> object2IntMap = (Object2IntMap<BlockState>)completableFuture4.join();
					return this.loadModels(
						profilerFiller, map2, new ModelBakery(modelDiscovery.getTopModels(), modelDiscovery.getReferencedModels(), unbakedModel), object2IntMap
					);
				},
				executor
			)
			.thenCompose(reloadState -> reloadState.readyForUpload.thenApply(void_ -> reloadState))
			.thenCompose(preparationBarrier::wait)
			.thenAcceptAsync(reloadState -> this.apply(reloadState, profilerFiller2), executor2);
	}

	private static CompletableFuture<Map<ResourceLocation, UnbakedModel>> loadBlockModels(ResourceManager resourceManager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> MODEL_LISTER.listMatchingResources(resourceManager), executor)
			.thenCompose(
				map -> {
					List<CompletableFuture<Pair<ResourceLocation, BlockModel>>> list = new ArrayList(map.size());

					for (Entry<ResourceLocation, Resource> entry : map.entrySet()) {
						list.add(CompletableFuture.supplyAsync(() -> {
							ResourceLocation resourceLocation = MODEL_LISTER.fileToId((ResourceLocation)entry.getKey());

							try {
								Reader reader = ((Resource)entry.getValue()).openAsReader();

								Pair var4x;
								try {
									BlockModel blockModel = BlockModel.fromStream(reader);
									blockModel.name = resourceLocation.toString();
									var4x = Pair.of(resourceLocation, blockModel);
								} catch (Throwable var6) {
									if (reader != null) {
										try {
											reader.close();
										} catch (Throwable var5) {
											var6.addSuppressed(var5);
										}
									}

									throw var6;
								}

								if (reader != null) {
									reader.close();
								}

								return var4x;
							} catch (Exception var7) {
								LOGGER.error("Failed to load model {}", entry.getKey(), var7);
								return null;
							}
						}, executor));
					}

					return Util.sequence(list)
						.thenApply(listx -> (Map)listx.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
				}
			);
	}

	private ModelDiscovery discoverModelDependencies(
		UnbakedModel unbakedModel, Map<ResourceLocation, UnbakedModel> map, BlockStateModelLoader.LoadedModels loadedModels
	) {
		ModelDiscovery modelDiscovery = new ModelDiscovery(map, unbakedModel);
		modelDiscovery.registerStandardModels(loadedModels);
		modelDiscovery.discoverDependencies();
		return modelDiscovery;
	}

	private static CompletableFuture<BlockStateModelLoader.LoadedModels> loadBlockStates(
		BlockStateModelLoader blockStateModelLoader, ResourceManager resourceManager, Executor executor
	) {
		Function<ResourceLocation, StateDefinition<Block, BlockState>> function = BlockStateModelLoader.definitionLocationToBlockMapper();
		return CompletableFuture.supplyAsync(() -> BLOCKSTATE_LISTER.listMatchingResourceStacks(resourceManager), executor).thenCompose(map -> {
			List<CompletableFuture<BlockStateModelLoader.LoadedModels>> list = new ArrayList(map.size());

			for (Entry<ResourceLocation, List<Resource>> entry : map.entrySet()) {
				list.add(CompletableFuture.supplyAsync(() -> {
					ResourceLocation resourceLocation = BLOCKSTATE_LISTER.fileToId((ResourceLocation)entry.getKey());
					StateDefinition<Block, BlockState> stateDefinition = (StateDefinition<Block, BlockState>)function.apply(resourceLocation);
					if (stateDefinition == null) {
						LOGGER.debug("Discovered unknown block state definition {}, ignoring", resourceLocation);
						return null;
					} else {
						List<Resource> listx = (List<Resource>)entry.getValue();
						List<BlockStateModelLoader.LoadedBlockModelDefinition> list2 = new ArrayList(listx.size());

						for (Resource resource : listx) {
							try {
								Reader reader = resource.openAsReader();

								try {
									JsonObject jsonObject = GsonHelper.parse(reader);
									BlockModelDefinition blockModelDefinition = BlockModelDefinition.fromJsonElement(jsonObject);
									list2.add(new BlockStateModelLoader.LoadedBlockModelDefinition(resource.sourcePackId(), blockModelDefinition));
								} catch (Throwable var14) {
									if (reader != null) {
										try {
											reader.close();
										} catch (Throwable var13) {
											var14.addSuppressed(var13);
										}
									}

									throw var14;
								}

								if (reader != null) {
									reader.close();
								}
							} catch (Exception var15) {
								LOGGER.error("Failed to load blockstate definition {} from pack {}", resourceLocation, resource.sourcePackId(), var15);
							}
						}

						try {
							return blockStateModelLoader.loadBlockStateDefinitionStack(resourceLocation, stateDefinition, list2);
						} catch (Exception var12) {
							LOGGER.error("Failed to load blockstate definition {}", resourceLocation, var12);
							return null;
						}
					}
				}, executor));
			}

			return Util.sequence(list).thenApply(listx -> {
				Map<ModelResourceLocation, BlockStateModelLoader.LoadedModel> mapx = new HashMap();

				for (BlockStateModelLoader.LoadedModels loadedModels : listx) {
					if (loadedModels != null) {
						mapx.putAll(loadedModels.models());
					}
				}

				return new BlockStateModelLoader.LoadedModels(mapx);
			});
		});
	}

	private ModelManager.ReloadState loadModels(
		ProfilerFiller profilerFiller, Map<ResourceLocation, AtlasSet.StitchResult> map, ModelBakery modelBakery, Object2IntMap<BlockState> object2IntMap
	) {
		profilerFiller.push("load");
		profilerFiller.popPush("baking");
		Multimap<ModelResourceLocation, Material> multimap = HashMultimap.create();
		modelBakery.bakeModels((modelResourceLocation, material) -> {
			AtlasSet.StitchResult stitchResult = (AtlasSet.StitchResult)map.get(material.atlasLocation());
			TextureAtlasSprite textureAtlasSprite = stitchResult.getSprite(material.texture());
			if (textureAtlasSprite != null) {
				return textureAtlasSprite;
			} else {
				multimap.put(modelResourceLocation, material);
				return stitchResult.missing();
			}
		});
		multimap.asMap()
			.forEach(
				(modelResourceLocation, collection) -> LOGGER.warn(
						"Missing textures in model {}:\n{}",
						modelResourceLocation,
						collection.stream()
							.sorted(Material.COMPARATOR)
							.map(material -> "    " + material.atlasLocation() + ":" + material.texture())
							.collect(Collectors.joining("\n"))
					)
			);
		profilerFiller.popPush("dispatch");
		Map<ModelResourceLocation, BakedModel> map2 = modelBakery.getBakedTopLevelModels();
		BakedModel bakedModel = (BakedModel)map2.get(MissingBlockModel.VARIANT);
		Map<BlockState, BakedModel> map3 = new IdentityHashMap();

		for (Block block : BuiltInRegistries.BLOCK) {
			block.getStateDefinition().getPossibleStates().forEach(blockState -> {
				ResourceLocation resourceLocation = blockState.getBlock().builtInRegistryHolder().key().location();
				BakedModel bakedModel2 = (BakedModel)map2.getOrDefault(BlockModelShaper.stateToModelLocation(resourceLocation, blockState), bakedModel);
				map3.put(blockState, bakedModel2);
			});
		}

		CompletableFuture<Void> completableFuture = CompletableFuture.allOf(
			(CompletableFuture[])map.values().stream().map(AtlasSet.StitchResult::readyForUpload).toArray(CompletableFuture[]::new)
		);
		profilerFiller.pop();
		profilerFiller.endTick();
		return new ModelManager.ReloadState(modelBakery, object2IntMap, bakedModel, map3, map, completableFuture);
	}

	private static Object2IntMap<BlockState> buildModelGroups(BlockColors blockColors, BlockStateModelLoader.LoadedModels loadedModels) {
		return ModelGroupCollector.build(blockColors, loadedModels);
	}

	private void apply(ModelManager.ReloadState reloadState, ProfilerFiller profilerFiller) {
		profilerFiller.startTick();
		profilerFiller.push("upload");
		reloadState.atlasPreparations.values().forEach(AtlasSet.StitchResult::upload);
		ModelBakery modelBakery = reloadState.modelBakery;
		this.bakedRegistry = modelBakery.getBakedTopLevelModels();
		this.modelGroups = reloadState.modelGroups;
		this.missingModel = reloadState.missingModel;
		profilerFiller.popPush("cache");
		this.blockModelShaper.replaceCache(reloadState.modelCache);
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
		this.maxMipmapLevels = i;
	}

	@Environment(EnvType.CLIENT)
	static record ReloadState(
		ModelBakery modelBakery,
		Object2IntMap<BlockState> modelGroups,
		BakedModel missingModel,
		Map<BlockState, BakedModel> modelCache,
		Map<ResourceLocation, AtlasSet.StitchResult> atlasPreparations,
		CompletableFuture<Void> readyForUpload
	) {
	}
}
