package net.minecraft.client.resources.model;

import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BlockStateModelLoader {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String FRAME_MAP_PROPERTY = "map";
	private static final String FRAME_MAP_PROPERTY_TRUE = "map=true";
	private static final String FRAME_MAP_PROPERTY_FALSE = "map=false";
	private static final StateDefinition<Block, BlockState> ITEM_FRAME_FAKE_DEFINITION = new StateDefinition.Builder<Block, BlockState>(Blocks.AIR)
		.add(BooleanProperty.create("map"))
		.create(Block::defaultBlockState, BlockState::new);
	private static final ResourceLocation GLOW_ITEM_FRAME_LOCATION = ResourceLocation.withDefaultNamespace("glow_item_frame");
	private static final ResourceLocation ITEM_FRAME_LOCATION = ResourceLocation.withDefaultNamespace("item_frame");
	private static final Map<ResourceLocation, StateDefinition<Block, BlockState>> STATIC_DEFINITIONS = Map.of(
		ITEM_FRAME_LOCATION, ITEM_FRAME_FAKE_DEFINITION, GLOW_ITEM_FRAME_LOCATION, ITEM_FRAME_FAKE_DEFINITION
	);
	public static final ModelResourceLocation GLOW_MAP_FRAME_LOCATION = new ModelResourceLocation(GLOW_ITEM_FRAME_LOCATION, "map=true");
	public static final ModelResourceLocation GLOW_FRAME_LOCATION = new ModelResourceLocation(GLOW_ITEM_FRAME_LOCATION, "map=false");
	public static final ModelResourceLocation MAP_FRAME_LOCATION = new ModelResourceLocation(ITEM_FRAME_LOCATION, "map=true");
	public static final ModelResourceLocation FRAME_LOCATION = new ModelResourceLocation(ITEM_FRAME_LOCATION, "map=false");
	private final UnbakedModel missingModel;

	public BlockStateModelLoader(UnbakedModel unbakedModel) {
		this.missingModel = unbakedModel;
	}

	public static Function<ResourceLocation, StateDefinition<Block, BlockState>> definitionLocationToBlockMapper() {
		Map<ResourceLocation, StateDefinition<Block, BlockState>> map = new HashMap(STATIC_DEFINITIONS);

		for (Block block : BuiltInRegistries.BLOCK) {
			map.put(block.builtInRegistryHolder().key().location(), block.getStateDefinition());
		}

		return map::get;
	}

	public BlockStateModelLoader.LoadedModels loadBlockStateDefinitionStack(
		ResourceLocation resourceLocation, StateDefinition<Block, BlockState> stateDefinition, List<BlockStateModelLoader.LoadedBlockModelDefinition> list
	) {
		List<BlockState> list2 = stateDefinition.getPossibleStates();
		Map<BlockState, BlockStateModelLoader.LoadedModel> map = new HashMap();
		Map<ModelResourceLocation, BlockStateModelLoader.LoadedModel> map2 = new HashMap();

		try {
			for (BlockStateModelLoader.LoadedBlockModelDefinition loadedBlockModelDefinition : list) {
				loadedBlockModelDefinition.contents
					.instantiate(stateDefinition, resourceLocation + "/" + loadedBlockModelDefinition.source)
					.forEach((blockStatex, unbakedBlockStateModel) -> map.put(blockStatex, new BlockStateModelLoader.LoadedModel(blockStatex, unbakedBlockStateModel)));
			}
		} finally {
			Iterator var12 = list2.iterator();

			while (true) {
				if (!var12.hasNext()) {
					;
				} else {
					BlockState blockState2 = (BlockState)var12.next();
					ModelResourceLocation modelResourceLocation2 = BlockModelShaper.stateToModelLocation(resourceLocation, blockState2);
					BlockStateModelLoader.LoadedModel loadedModel2 = (BlockStateModelLoader.LoadedModel)map.get(blockState2);
					if (loadedModel2 == null) {
						LOGGER.warn("Missing blockstate definition: '{}' missing model for variant: '{}'", resourceLocation, modelResourceLocation2);
						loadedModel2 = new BlockStateModelLoader.LoadedModel(blockState2, this.missingModel);
					}

					map2.put(modelResourceLocation2, loadedModel2);
				}
			}
		}

		return new BlockStateModelLoader.LoadedModels(map2);
	}

	@Environment(EnvType.CLIENT)
	public static record LoadedBlockModelDefinition(String source, BlockModelDefinition contents) {
	}

	@Environment(EnvType.CLIENT)
	public static record LoadedModel(BlockState state, UnbakedModel model) {
	}

	@Environment(EnvType.CLIENT)
	public static record LoadedModels(Map<ModelResourceLocation, BlockStateModelLoader.LoadedModel> models) {
	}
}
