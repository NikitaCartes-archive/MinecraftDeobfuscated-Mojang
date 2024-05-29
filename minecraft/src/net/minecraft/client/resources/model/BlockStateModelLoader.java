package net.minecraft.client.resources.model;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
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
public class BlockStateModelLoader {
	private static final Logger LOGGER = LogUtils.getLogger();
	static final int SINGLETON_MODEL_GROUP = -1;
	private static final int INVISIBLE_MODEL_GROUP = 0;
	public static final FileToIdConverter BLOCKSTATE_LISTER = FileToIdConverter.json("blockstates");
	private static final Splitter COMMA_SPLITTER = Splitter.on(',');
	private static final Splitter EQUAL_SPLITTER = Splitter.on('=').limit(2);
	private static final StateDefinition<Block, BlockState> ITEM_FRAME_FAKE_DEFINITION = new StateDefinition.Builder<Block, BlockState>(Blocks.AIR)
		.add(BooleanProperty.create("map"))
		.create(Block::defaultBlockState, BlockState::new);
	private static final Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS = Map.of(
		ResourceLocation.withDefaultNamespace("item_frame"),
		ITEM_FRAME_FAKE_DEFINITION,
		ResourceLocation.withDefaultNamespace("glow_item_frame"),
		ITEM_FRAME_FAKE_DEFINITION
	);
	private final Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> blockStateResources;
	private final ProfilerFiller profiler;
	private final BlockColors blockColors;
	private final BiConsumer<ModelResourceLocation, UnbakedModel> discoveredModelOutput;
	private int nextModelGroup = 1;
	private final Object2IntMap<BlockState> modelGroups = Util.make(
		new Object2IntOpenHashMap<>(), object2IntOpenHashMap -> object2IntOpenHashMap.defaultReturnValue(-1)
	);
	private final BlockStateModelLoader.LoadedModel missingModel;
	private final BlockModelDefinition.Context context = new BlockModelDefinition.Context();

	public BlockStateModelLoader(
		Map<ResourceLocation, List<BlockStateModelLoader.LoadedJson>> map,
		ProfilerFiller profilerFiller,
		UnbakedModel unbakedModel,
		BlockColors blockColors,
		BiConsumer<ModelResourceLocation, UnbakedModel> biConsumer
	) {
		this.blockStateResources = map;
		this.profiler = profilerFiller;
		this.blockColors = blockColors;
		this.discoveredModelOutput = biConsumer;
		BlockStateModelLoader.ModelGroupKey modelGroupKey = new BlockStateModelLoader.ModelGroupKey(List.of(unbakedModel), List.of());
		this.missingModel = new BlockStateModelLoader.LoadedModel(unbakedModel, () -> modelGroupKey);
	}

	public void loadAllBlockStates() {
		this.profiler.push("static_definitions");
		STATIC_DEFINITIONS.forEach(this::loadBlockStateDefinitions);
		this.profiler.popPush("blocks");

		for (Block block : BuiltInRegistries.BLOCK) {
			this.loadBlockStateDefinitions(block.builtInRegistryHolder().key().location(), block.getStateDefinition());
		}

		this.profiler.pop();
	}

	private void loadBlockStateDefinitions(ResourceLocation resourceLocation, StateDefinition<Block, BlockState> stateDefinition) {
		this.context.setDefinition(stateDefinition);
		List<Property<?>> list = List.copyOf(this.blockColors.getColoringProperties(stateDefinition.getOwner()));
		List<BlockState> list2 = stateDefinition.getPossibleStates();
		Map<ModelResourceLocation, BlockState> map = new HashMap();
		list2.forEach(blockState -> map.put(BlockModelShaper.stateToModelLocation(resourceLocation, blockState), blockState));
		Map<BlockState, BlockStateModelLoader.LoadedModel> map2 = new HashMap();
		ResourceLocation resourceLocation2 = BLOCKSTATE_LISTER.idToFile(resourceLocation);

		try {
			for (BlockStateModelLoader.LoadedJson loadedJson : (List)this.blockStateResources.getOrDefault(resourceLocation2, List.of())) {
				BlockModelDefinition blockModelDefinition = loadedJson.parse(resourceLocation, this.context);
				Map<BlockState, BlockStateModelLoader.LoadedModel> map3 = new IdentityHashMap();
				MultiPart multiPart;
				if (blockModelDefinition.isMultiPart()) {
					multiPart = blockModelDefinition.getMultiPart();
					list2.forEach(
						blockState -> map3.put(
								blockState, new BlockStateModelLoader.LoadedModel(multiPart, () -> BlockStateModelLoader.ModelGroupKey.create(blockState, multiPart, list))
							)
					);
				} else {
					multiPart = null;
				}

				blockModelDefinition.getVariants()
					.forEach(
						(string, multiVariant) -> {
							try {
								list2.stream()
									.filter(predicate(stateDefinition, string))
									.forEach(
										blockState -> {
											BlockStateModelLoader.LoadedModel loadedModel = (BlockStateModelLoader.LoadedModel)map3.put(
												blockState, new BlockStateModelLoader.LoadedModel(multiVariant, () -> BlockStateModelLoader.ModelGroupKey.create(blockState, multiVariant, list))
											);
											if (loadedModel != null && loadedModel.model != multiPart) {
												map3.put(blockState, this.missingModel);
												throw new RuntimeException(
													"Overlapping definition with: "
														+ (String)((Entry)blockModelDefinition.getVariants().entrySet().stream().filter(entry -> entry.getValue() == loadedModel.model).findFirst().get())
															.getKey()
												);
											}
										}
									);
							} catch (Exception var12x) {
								LOGGER.warn(
									"Exception loading blockstate definition: '{}' in resourcepack: '{}' for variant: '{}': {}",
									resourceLocation2,
									loadedJson.source,
									string,
									var12x.getMessage()
								);
							}
						}
					);
				map2.putAll(map3);
			}
		} catch (BlockStateModelLoader.BlockStateDefinitionException var18) {
			LOGGER.warn("{}", var18.getMessage());
		} catch (Exception var19) {
			LOGGER.warn("Exception loading blockstate definition: '{}'", resourceLocation2, var19);
		} finally {
			Map<BlockStateModelLoader.ModelGroupKey, Set<BlockState>> map5 = new HashMap();
			map.forEach((modelResourceLocation, blockState) -> {
				BlockStateModelLoader.LoadedModel loadedModel = (BlockStateModelLoader.LoadedModel)map2.get(blockState);
				if (loadedModel == null) {
					LOGGER.warn("Exception loading blockstate definition: '{}' missing model for variant: '{}'", resourceLocation2, modelResourceLocation);
					loadedModel = this.missingModel;
				}

				this.discoveredModelOutput.accept(modelResourceLocation, loadedModel.model);

				try {
					BlockStateModelLoader.ModelGroupKey modelGroupKey = (BlockStateModelLoader.ModelGroupKey)loadedModel.key().get();
					((Set)map5.computeIfAbsent(modelGroupKey, modelGroupKeyx -> Sets.newIdentityHashSet())).add(blockState);
				} catch (Exception var8) {
					LOGGER.warn("Exception evaluating model definition: '{}'", modelResourceLocation, var8);
				}
			});
			map5.forEach((modelGroupKey, set) -> {
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

	private static Predicate<BlockState> predicate(StateDefinition<Block, BlockState> stateDefinition, String string) {
		Map<Property<?>, Comparable<?>> map = new HashMap();

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

	private void registerModelGroup(Iterable<BlockState> iterable) {
		int i = this.nextModelGroup++;
		iterable.forEach(blockState -> this.modelGroups.put(blockState, i));
	}

	public Object2IntMap<BlockState> getModelGroups() {
		return this.modelGroups;
	}

	@Environment(EnvType.CLIENT)
	static class BlockStateDefinitionException extends RuntimeException {
		public BlockStateDefinitionException(String string) {
			super(string);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record LoadedJson(String source, JsonElement data) {

		BlockModelDefinition parse(ResourceLocation resourceLocation, BlockModelDefinition.Context context) {
			try {
				return BlockModelDefinition.fromJsonElement(context, this.data);
			} catch (Exception var4) {
				throw new BlockStateModelLoader.BlockStateDefinitionException(
					String.format(Locale.ROOT, "Exception loading blockstate definition: '%s' in resourcepack: '%s': %s", resourceLocation, this.source, var4.getMessage())
				);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static record LoadedModel(UnbakedModel model, Supplier<BlockStateModelLoader.ModelGroupKey> key) {
	}

	@Environment(EnvType.CLIENT)
	static record ModelGroupKey(List<UnbakedModel> models, List<Object> coloringValues) {
		public static BlockStateModelLoader.ModelGroupKey create(BlockState blockState, MultiPart multiPart, Collection<Property<?>> collection) {
			StateDefinition<Block, BlockState> stateDefinition = blockState.getBlock().getStateDefinition();
			List<UnbakedModel> list = (List<UnbakedModel>)multiPart.getSelectors()
				.stream()
				.filter(selector -> selector.getPredicate(stateDefinition).test(blockState))
				.map(Selector::getVariant)
				.collect(Collectors.toUnmodifiableList());
			List<Object> list2 = getColoringValues(blockState, collection);
			return new BlockStateModelLoader.ModelGroupKey(list, list2);
		}

		public static BlockStateModelLoader.ModelGroupKey create(BlockState blockState, UnbakedModel unbakedModel, Collection<Property<?>> collection) {
			List<Object> list = getColoringValues(blockState, collection);
			return new BlockStateModelLoader.ModelGroupKey(List.of(unbakedModel), list);
		}

		private static List<Object> getColoringValues(BlockState blockState, Collection<Property<?>> collection) {
			return (List<Object>)collection.stream().map(blockState::getValue).collect(Collectors.toUnmodifiableList());
		}
	}
}
