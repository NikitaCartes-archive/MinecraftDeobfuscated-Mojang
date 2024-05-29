package net.minecraft.client.resources.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.Reader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.registries.BuiltInRegistries;
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

@Environment(EnvType.CLIENT)
public class ModelManager implements PreparableReloadListener, AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
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
		CompletableFuture<Map<ResourceLocation, BlockModel>> completableFuture = loadBlockModels(resourceManager, executor);
		CompletableFuture<Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>>> completableFuture2 = loadBlockStates(resourceManager, executor);
		CompletableFuture<ModelBakery> completableFuture3 = completableFuture.thenCombineAsync(
			completableFuture2, (mapx, map2) -> new ModelBakery(this.blockColors, profilerFiller, mapx, map2), executor
		);
		Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>> map = this.atlases.scheduleLoad(resourceManager, this.maxMipmapLevels, executor);
		return CompletableFuture.allOf((CompletableFuture[])Stream.concat(map.values().stream(), Stream.of(completableFuture3)).toArray(CompletableFuture[]::new))
			.thenApplyAsync(
				void_ -> this.loadModels(
						profilerFiller,
						(Map<ResourceLocation, AtlasSet.StitchResult>)map.entrySet()
							.stream()
							.collect(Collectors.toMap(Entry::getKey, entry -> (AtlasSet.StitchResult)((CompletableFuture)entry.getValue()).join())),
						(ModelBakery)completableFuture3.join()
					),
				executor
			)
			.thenCompose(reloadState -> reloadState.readyForUpload.thenApply(void_ -> reloadState))
			.thenCompose(preparationBarrier::wait)
			.thenAcceptAsync(reloadState -> this.apply(reloadState, profilerFiller2), executor2);
	}

	private static CompletableFuture<Map<ResourceLocation, BlockModel>> loadBlockModels(ResourceManager resourceManager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> ModelBakery.MODEL_LISTER.listMatchingResources(resourceManager), executor)
			.thenCompose(
				map -> {
					List<CompletableFuture<Pair<ResourceLocation, BlockModel>>> list = new ArrayList(map.size());

					for (Entry<ResourceLocation, Resource> entry : map.entrySet()) {
						list.add(CompletableFuture.supplyAsync(() -> {
							try {
								Reader reader = ((Resource)entry.getValue()).openAsReader();

								Pair var2x;
								try {
									var2x = Pair.of((ResourceLocation)entry.getKey(), BlockModel.fromStream(reader));
								} catch (Throwable var5) {
									if (reader != null) {
										try {
											reader.close();
										} catch (Throwable var4x) {
											var5.addSuppressed(var4x);
										}
									}

									throw var5;
								}

								if (reader != null) {
									reader.close();
								}

								return var2x;
							} catch (Exception var6) {
								LOGGER.error("Failed to load model {}", entry.getKey(), var6);
								return null;
							}
						}, executor));
					}

					return Util.sequence(list)
						.thenApply(listx -> (Map)listx.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
				}
			);
	}

	private static CompletableFuture<Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>>> loadBlockStates(
		ResourceManager resourceManager, Executor executor
	) {
		return CompletableFuture.supplyAsync(() -> BlockStateModelLoader.BLOCKSTATE_LISTER.listMatchingResourceStacks(resourceManager), executor)
			.thenCompose(
				map -> {
					List<CompletableFuture<Pair<ResourceLocation, List<BlockStateModelLoader.LoadedJson>>>> list = new ArrayList(map.size());

					for (Entry<ResourceLocation, List<Resource>> entry : map.entrySet()) {
						list.add(CompletableFuture.supplyAsync(() -> {
							List<Resource> listx = (List<Resource>)entry.getValue();
							List<BlockStateModelLoader.LoadedJson> list2 = new ArrayList(listx.size());

							for (Resource resource : listx) {
								try {
									Reader reader = resource.openAsReader();

									try {
										JsonObject jsonObject = GsonHelper.parse(reader);
										list2.add(new BlockStateModelLoader.LoadedJson(resource.sourcePackId(), jsonObject));
									} catch (Throwable var9) {
										if (reader != null) {
											try {
												reader.close();
											} catch (Throwable var8) {
												var9.addSuppressed(var8);
											}
										}

										throw var9;
									}

									if (reader != null) {
										reader.close();
									}
								} catch (Exception var10) {
									LOGGER.error("Failed to load blockstate {} from pack {}", entry.getKey(), resource.sourcePackId(), var10);
								}
							}

							return Pair.of((ResourceLocation)entry.getKey(), list2);
						}, executor));
					}

					return Util.sequence(list)
						.thenApply(listx -> (Map)listx.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
				}
			);
	}

	private ModelManager.ReloadState loadModels(ProfilerFiller profilerFiller, Map<ResourceLocation, AtlasSet.StitchResult> map, ModelBakery modelBakery) {
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
		BakedModel bakedModel = (BakedModel)map2.get(ModelBakery.MISSING_MODEL_VARIANT);
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
		return new ModelManager.ReloadState(modelBakery, bakedModel, map3, map, completableFuture);
	}

	private void apply(ModelManager.ReloadState reloadState, ProfilerFiller profilerFiller) {
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
		BakedModel missingModel,
		Map<BlockState, BakedModel> modelCache,
		Map<ResourceLocation, AtlasSet.StitchResult> atlasPreparations,
		CompletableFuture<Void> readyForUpload
	) {
	}
}
