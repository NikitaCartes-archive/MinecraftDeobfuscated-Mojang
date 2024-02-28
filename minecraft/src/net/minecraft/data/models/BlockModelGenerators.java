package net.minecraft.data.models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.Condition;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.DelegatedModel;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.SnifferEggBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.block.state.properties.WallSide;

public class BlockModelGenerators {
	final Consumer<BlockStateGenerator> blockStateOutput;
	final BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput;
	private final Consumer<Item> skippedAutoModelsOutput;
	final List<Block> nonOrientableTrapdoor = ImmutableList.of(Blocks.OAK_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.IRON_TRAPDOOR);
	final Map<Block, BlockModelGenerators.BlockStateGeneratorSupplier> fullBlockModelCustomGenerators = ImmutableMap.<Block, BlockModelGenerators.BlockStateGeneratorSupplier>builder()
		.put(Blocks.STONE, BlockModelGenerators::createMirroredCubeGenerator)
		.put(Blocks.DEEPSLATE, BlockModelGenerators::createMirroredColumnGenerator)
		.put(Blocks.MUD_BRICKS, BlockModelGenerators::createNorthWestMirroredCubeGenerator)
		.build();
	final Map<Block, TexturedModel> texturedModels = ImmutableMap.<Block, TexturedModel>builder()
		.put(Blocks.SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL.get(Blocks.SANDSTONE))
		.put(Blocks.RED_SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL.get(Blocks.RED_SANDSTONE))
		.put(Blocks.SMOOTH_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.SANDSTONE, "_top")))
		.put(Blocks.SMOOTH_RED_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.RED_SANDSTONE, "_top")))
		.put(
			Blocks.CUT_SANDSTONE,
			TexturedModel.COLUMN
				.get(Blocks.SANDSTONE)
				.updateTextures(textureMapping -> textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_SANDSTONE)))
		)
		.put(
			Blocks.CUT_RED_SANDSTONE,
			TexturedModel.COLUMN
				.get(Blocks.RED_SANDSTONE)
				.updateTextures(textureMapping -> textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_RED_SANDSTONE)))
		)
		.put(Blocks.QUARTZ_BLOCK, TexturedModel.COLUMN.get(Blocks.QUARTZ_BLOCK))
		.put(Blocks.SMOOTH_QUARTZ, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.QUARTZ_BLOCK, "_bottom")))
		.put(Blocks.BLACKSTONE, TexturedModel.COLUMN_WITH_WALL.get(Blocks.BLACKSTONE))
		.put(Blocks.DEEPSLATE, TexturedModel.COLUMN_WITH_WALL.get(Blocks.DEEPSLATE))
		.put(
			Blocks.CHISELED_QUARTZ_BLOCK,
			TexturedModel.COLUMN
				.get(Blocks.CHISELED_QUARTZ_BLOCK)
				.updateTextures(textureMapping -> textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_QUARTZ_BLOCK)))
		)
		.put(Blocks.CHISELED_SANDSTONE, TexturedModel.COLUMN.get(Blocks.CHISELED_SANDSTONE).updateTextures(textureMapping -> {
			textureMapping.put(TextureSlot.END, TextureMapping.getBlockTexture(Blocks.SANDSTONE, "_top"));
			textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_SANDSTONE));
		}))
		.put(Blocks.CHISELED_RED_SANDSTONE, TexturedModel.COLUMN.get(Blocks.CHISELED_RED_SANDSTONE).updateTextures(textureMapping -> {
			textureMapping.put(TextureSlot.END, TextureMapping.getBlockTexture(Blocks.RED_SANDSTONE, "_top"));
			textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_RED_SANDSTONE));
		}))
		.put(Blocks.CHISELED_TUFF_BRICKS, TexturedModel.COLUMN_WITH_WALL.get(Blocks.CHISELED_TUFF_BRICKS))
		.put(Blocks.CHISELED_TUFF, TexturedModel.COLUMN_WITH_WALL.get(Blocks.CHISELED_TUFF))
		.build();
	static final Map<BlockFamily.Variant, BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block>> SHAPE_CONSUMERS = ImmutableMap.<BlockFamily.Variant, BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block>>builder()
		.put(BlockFamily.Variant.BUTTON, BlockModelGenerators.BlockFamilyProvider::button)
		.put(BlockFamily.Variant.DOOR, BlockModelGenerators.BlockFamilyProvider::door)
		.put(BlockFamily.Variant.CHISELED, BlockModelGenerators.BlockFamilyProvider::fullBlockVariant)
		.put(BlockFamily.Variant.CRACKED, BlockModelGenerators.BlockFamilyProvider::fullBlockVariant)
		.put(BlockFamily.Variant.CUSTOM_FENCE, BlockModelGenerators.BlockFamilyProvider::customFence)
		.put(BlockFamily.Variant.FENCE, BlockModelGenerators.BlockFamilyProvider::fence)
		.put(BlockFamily.Variant.CUSTOM_FENCE_GATE, BlockModelGenerators.BlockFamilyProvider::customFenceGate)
		.put(BlockFamily.Variant.FENCE_GATE, BlockModelGenerators.BlockFamilyProvider::fenceGate)
		.put(BlockFamily.Variant.SIGN, BlockModelGenerators.BlockFamilyProvider::sign)
		.put(BlockFamily.Variant.SLAB, BlockModelGenerators.BlockFamilyProvider::slab)
		.put(BlockFamily.Variant.STAIRS, BlockModelGenerators.BlockFamilyProvider::stairs)
		.put(BlockFamily.Variant.PRESSURE_PLATE, BlockModelGenerators.BlockFamilyProvider::pressurePlate)
		.put(BlockFamily.Variant.TRAPDOOR, BlockModelGenerators.BlockFamilyProvider::trapdoor)
		.put(BlockFamily.Variant.WALL, BlockModelGenerators.BlockFamilyProvider::wall)
		.build();
	public static final List<Pair<BooleanProperty, Function<ResourceLocation, Variant>>> MULTIFACE_GENERATOR = List.of(
		Pair.of(BlockStateProperties.NORTH, resourceLocation -> Variant.variant().with(VariantProperties.MODEL, resourceLocation)),
		Pair.of(
			BlockStateProperties.EAST,
			resourceLocation -> Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation)
					.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					.with(VariantProperties.UV_LOCK, true)
		),
		Pair.of(
			BlockStateProperties.SOUTH,
			resourceLocation -> Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation)
					.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					.with(VariantProperties.UV_LOCK, true)
		),
		Pair.of(
			BlockStateProperties.WEST,
			resourceLocation -> Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation)
					.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					.with(VariantProperties.UV_LOCK, true)
		),
		Pair.of(
			BlockStateProperties.UP,
			resourceLocation -> Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation)
					.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
					.with(VariantProperties.UV_LOCK, true)
		),
		Pair.of(
			BlockStateProperties.DOWN,
			resourceLocation -> Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation)
					.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
					.with(VariantProperties.UV_LOCK, true)
		)
	);
	private static final Map<BlockModelGenerators.BookSlotModelCacheKey, ResourceLocation> CHISELED_BOOKSHELF_SLOT_MODEL_CACHE = new HashMap();

	private static BlockStateGenerator createMirroredCubeGenerator(
		Block block, ResourceLocation resourceLocation, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer
	) {
		ResourceLocation resourceLocation2 = ModelTemplates.CUBE_MIRRORED_ALL.create(block, textureMapping, biConsumer);
		return createRotatedVariant(block, resourceLocation, resourceLocation2);
	}

	private static BlockStateGenerator createNorthWestMirroredCubeGenerator(
		Block block, ResourceLocation resourceLocation, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer
	) {
		ResourceLocation resourceLocation2 = ModelTemplates.CUBE_NORTH_WEST_MIRRORED_ALL.create(block, textureMapping, biConsumer);
		return createSimpleBlock(block, resourceLocation2);
	}

	private static BlockStateGenerator createMirroredColumnGenerator(
		Block block, ResourceLocation resourceLocation, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer
	) {
		ResourceLocation resourceLocation2 = ModelTemplates.CUBE_COLUMN_MIRRORED.create(block, textureMapping, biConsumer);
		return createRotatedVariant(block, resourceLocation, resourceLocation2).with(createRotatedPillar());
	}

	public BlockModelGenerators(Consumer<BlockStateGenerator> consumer, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer, Consumer<Item> consumer2) {
		this.blockStateOutput = consumer;
		this.modelOutput = biConsumer;
		this.skippedAutoModelsOutput = consumer2;
	}

	void skipAutoItemBlock(Block block) {
		this.skippedAutoModelsOutput.accept(block.asItem());
	}

	void delegateItemModel(Block block, ResourceLocation resourceLocation) {
		this.modelOutput.accept(ModelLocationUtils.getModelLocation(block.asItem()), new DelegatedModel(resourceLocation));
	}

	private void delegateItemModel(Item item, ResourceLocation resourceLocation) {
		this.modelOutput.accept(ModelLocationUtils.getModelLocation(item), new DelegatedModel(resourceLocation));
	}

	void createSimpleFlatItemModel(Item item) {
		ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(item), this.modelOutput);
	}

	private void createSimpleFlatItemModel(Block block) {
		Item item = block.asItem();
		if (item != Items.AIR) {
			ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(block), this.modelOutput);
		}
	}

	private void createSimpleFlatItemModel(Block block, String string) {
		Item item = block.asItem();
		ModelTemplates.FLAT_ITEM
			.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(TextureMapping.getBlockTexture(block, string)), this.modelOutput);
	}

	private static PropertyDispatch createHorizontalFacingDispatch() {
		return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
			.select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
			.select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
			.select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
			.select(Direction.NORTH, Variant.variant());
	}

	private static PropertyDispatch createHorizontalFacingDispatchAlt() {
		return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
			.select(Direction.SOUTH, Variant.variant())
			.select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
			.select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
			.select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
	}

	private static PropertyDispatch createTorchHorizontalDispatch() {
		return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
			.select(Direction.EAST, Variant.variant())
			.select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
			.select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
			.select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
	}

	private static PropertyDispatch createFacingDispatch() {
		return PropertyDispatch.property(BlockStateProperties.FACING)
			.select(Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
			.select(Direction.UP, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
			.select(Direction.NORTH, Variant.variant())
			.select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
			.select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
			.select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
	}

	private static MultiVariantGenerator createRotatedVariant(Block block, ResourceLocation resourceLocation) {
		return MultiVariantGenerator.multiVariant(block, createRotatedVariants(resourceLocation));
	}

	private static Variant[] createRotatedVariants(ResourceLocation resourceLocation) {
		return new Variant[]{
			Variant.variant().with(VariantProperties.MODEL, resourceLocation),
			Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90),
			Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
			Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
		};
	}

	private static MultiVariantGenerator createRotatedVariant(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		return MultiVariantGenerator.multiVariant(
			block,
			Variant.variant().with(VariantProperties.MODEL, resourceLocation),
			Variant.variant().with(VariantProperties.MODEL, resourceLocation2),
			Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
			Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
		);
	}

	private static PropertyDispatch createBooleanModelDispatch(
		BooleanProperty booleanProperty, ResourceLocation resourceLocation, ResourceLocation resourceLocation2
	) {
		return PropertyDispatch.property(booleanProperty)
			.select(true, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
			.select(false, Variant.variant().with(VariantProperties.MODEL, resourceLocation2));
	}

	private void createRotatedMirroredVariantBlock(Block block) {
		ResourceLocation resourceLocation = TexturedModel.CUBE.create(block, this.modelOutput);
		ResourceLocation resourceLocation2 = TexturedModel.CUBE_MIRRORED.create(block, this.modelOutput);
		this.blockStateOutput.accept(createRotatedVariant(block, resourceLocation, resourceLocation2));
	}

	private void createRotatedVariantBlock(Block block) {
		ResourceLocation resourceLocation = TexturedModel.CUBE.create(block, this.modelOutput);
		this.blockStateOutput.accept(createRotatedVariant(block, resourceLocation));
	}

	private void createBrushableBlock(Block block) {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block)
					.with(
						PropertyDispatch.property(BlockStateProperties.DUSTED)
							.generate(
								integer -> {
									String string = "_" + integer;
									ResourceLocation resourceLocation = TextureMapping.getBlockTexture(block, string);
									return Variant.variant()
										.with(
											VariantProperties.MODEL,
											ModelTemplates.CUBE_ALL.createWithSuffix(block, string, new TextureMapping().put(TextureSlot.ALL, resourceLocation), this.modelOutput)
										);
								}
							)
					)
			);
		this.delegateItemModel(block, TextureMapping.getBlockTexture(block, "_0"));
	}

	static BlockStateGenerator createButton(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		return MultiVariantGenerator.multiVariant(block)
			.with(
				PropertyDispatch.property(BlockStateProperties.POWERED)
					.select(false, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(true, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
			)
			.with(
				PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
					.select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
					.select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
					.select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
					.select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
					.select(
						AttachFace.WALL,
						Direction.EAST,
						Variant.variant()
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						AttachFace.WALL,
						Direction.WEST,
						Variant.variant()
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						AttachFace.WALL,
						Direction.SOUTH,
						Variant.variant()
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						AttachFace.WALL, Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.UV_LOCK, true)
					)
					.select(
						AttachFace.CEILING,
						Direction.EAST,
						Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
					)
					.select(
						AttachFace.CEILING,
						Direction.WEST,
						Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
					)
					.select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
					.select(
						AttachFace.CEILING,
						Direction.NORTH,
						Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
					)
			);
	}

	private static PropertyDispatch.C4<Direction, DoubleBlockHalf, DoorHingeSide, Boolean> configureDoorHalf(
		PropertyDispatch.C4<Direction, DoubleBlockHalf, DoorHingeSide, Boolean> c4,
		DoubleBlockHalf doubleBlockHalf,
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
		ResourceLocation resourceLocation3,
		ResourceLocation resourceLocation4
	) {
		return c4.select(Direction.EAST, doubleBlockHalf, DoorHingeSide.LEFT, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
			.select(
				Direction.SOUTH,
				doubleBlockHalf,
				DoorHingeSide.LEFT,
				false,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
			)
			.select(
				Direction.WEST,
				doubleBlockHalf,
				DoorHingeSide.LEFT,
				false,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
			)
			.select(
				Direction.NORTH,
				doubleBlockHalf,
				DoorHingeSide.LEFT,
				false,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
			)
			.select(Direction.EAST, doubleBlockHalf, DoorHingeSide.RIGHT, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
			.select(
				Direction.SOUTH,
				doubleBlockHalf,
				DoorHingeSide.RIGHT,
				false,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
			)
			.select(
				Direction.WEST,
				doubleBlockHalf,
				DoorHingeSide.RIGHT,
				false,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
			)
			.select(
				Direction.NORTH,
				doubleBlockHalf,
				DoorHingeSide.RIGHT,
				false,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
			)
			.select(
				Direction.EAST,
				doubleBlockHalf,
				DoorHingeSide.LEFT,
				true,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
			)
			.select(
				Direction.SOUTH,
				doubleBlockHalf,
				DoorHingeSide.LEFT,
				true,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
			)
			.select(
				Direction.WEST,
				doubleBlockHalf,
				DoorHingeSide.LEFT,
				true,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
			)
			.select(Direction.NORTH, doubleBlockHalf, DoorHingeSide.LEFT, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
			.select(
				Direction.EAST,
				doubleBlockHalf,
				DoorHingeSide.RIGHT,
				true,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
			)
			.select(Direction.SOUTH, doubleBlockHalf, DoorHingeSide.RIGHT, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation4))
			.select(
				Direction.WEST,
				doubleBlockHalf,
				DoorHingeSide.RIGHT,
				true,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
			)
			.select(
				Direction.NORTH,
				doubleBlockHalf,
				DoorHingeSide.RIGHT,
				true,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
			);
	}

	private static BlockStateGenerator createDoor(
		Block block,
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
		ResourceLocation resourceLocation3,
		ResourceLocation resourceLocation4,
		ResourceLocation resourceLocation5,
		ResourceLocation resourceLocation6,
		ResourceLocation resourceLocation7,
		ResourceLocation resourceLocation8
	) {
		return MultiVariantGenerator.multiVariant(block)
			.with(
				configureDoorHalf(
					configureDoorHalf(
						PropertyDispatch.properties(
							BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.DOUBLE_BLOCK_HALF, BlockStateProperties.DOOR_HINGE, BlockStateProperties.OPEN
						),
						DoubleBlockHalf.LOWER,
						resourceLocation,
						resourceLocation2,
						resourceLocation3,
						resourceLocation4
					),
					DoubleBlockHalf.UPPER,
					resourceLocation5,
					resourceLocation6,
					resourceLocation7,
					resourceLocation8
				)
			);
	}

	static BlockStateGenerator createCustomFence(
		Block block,
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
		ResourceLocation resourceLocation3,
		ResourceLocation resourceLocation4,
		ResourceLocation resourceLocation5
	) {
		return MultiPartGenerator.multiPart(block)
			.with(Variant.variant().with(VariantProperties.MODEL, resourceLocation))
			.with(
				Condition.condition().term(BlockStateProperties.NORTH, true),
				Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.UV_LOCK, false)
			)
			.with(
				Condition.condition().term(BlockStateProperties.EAST, true),
				Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.UV_LOCK, false)
			)
			.with(
				Condition.condition().term(BlockStateProperties.SOUTH, true),
				Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.UV_LOCK, false)
			)
			.with(
				Condition.condition().term(BlockStateProperties.WEST, true),
				Variant.variant().with(VariantProperties.MODEL, resourceLocation5).with(VariantProperties.UV_LOCK, false)
			);
	}

	static BlockStateGenerator createFence(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		return MultiPartGenerator.multiPart(block)
			.with(Variant.variant().with(VariantProperties.MODEL, resourceLocation))
			.with(
				Condition.condition().term(BlockStateProperties.NORTH, true),
				Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.UV_LOCK, true)
			)
			.with(
				Condition.condition().term(BlockStateProperties.EAST, true),
				Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation2)
					.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					.with(VariantProperties.UV_LOCK, true)
			)
			.with(
				Condition.condition().term(BlockStateProperties.SOUTH, true),
				Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation2)
					.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					.with(VariantProperties.UV_LOCK, true)
			)
			.with(
				Condition.condition().term(BlockStateProperties.WEST, true),
				Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation2)
					.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					.with(VariantProperties.UV_LOCK, true)
			);
	}

	static BlockStateGenerator createWall(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3) {
		return MultiPartGenerator.multiPart(block)
			.with(Condition.condition().term(BlockStateProperties.UP, true), Variant.variant().with(VariantProperties.MODEL, resourceLocation))
			.with(
				Condition.condition().term(BlockStateProperties.NORTH_WALL, WallSide.LOW),
				Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.UV_LOCK, true)
			)
			.with(
				Condition.condition().term(BlockStateProperties.EAST_WALL, WallSide.LOW),
				Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation2)
					.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					.with(VariantProperties.UV_LOCK, true)
			)
			.with(
				Condition.condition().term(BlockStateProperties.SOUTH_WALL, WallSide.LOW),
				Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation2)
					.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					.with(VariantProperties.UV_LOCK, true)
			)
			.with(
				Condition.condition().term(BlockStateProperties.WEST_WALL, WallSide.LOW),
				Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation2)
					.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					.with(VariantProperties.UV_LOCK, true)
			)
			.with(
				Condition.condition().term(BlockStateProperties.NORTH_WALL, WallSide.TALL),
				Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.UV_LOCK, true)
			)
			.with(
				Condition.condition().term(BlockStateProperties.EAST_WALL, WallSide.TALL),
				Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation3)
					.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					.with(VariantProperties.UV_LOCK, true)
			)
			.with(
				Condition.condition().term(BlockStateProperties.SOUTH_WALL, WallSide.TALL),
				Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation3)
					.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					.with(VariantProperties.UV_LOCK, true)
			)
			.with(
				Condition.condition().term(BlockStateProperties.WEST_WALL, WallSide.TALL),
				Variant.variant()
					.with(VariantProperties.MODEL, resourceLocation3)
					.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					.with(VariantProperties.UV_LOCK, true)
			);
	}

	static BlockStateGenerator createFenceGate(
		Block block,
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
		ResourceLocation resourceLocation3,
		ResourceLocation resourceLocation4,
		boolean bl
	) {
		return MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.UV_LOCK, bl))
			.with(createHorizontalFacingDispatchAlt())
			.with(
				PropertyDispatch.properties(BlockStateProperties.IN_WALL, BlockStateProperties.OPEN)
					.select(false, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.select(true, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation4))
					.select(false, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(true, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
			);
	}

	static BlockStateGenerator createStairs(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3) {
		return MultiVariantGenerator.multiVariant(block)
			.with(
				PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE)
					.select(Direction.EAST, Half.BOTTOM, StairsShape.STRAIGHT, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.select(
						Direction.WEST,
						Half.BOTTOM,
						StairsShape.STRAIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.SOUTH,
						Half.BOTTOM,
						StairsShape.STRAIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.NORTH,
						Half.BOTTOM,
						StairsShape.STRAIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
					.select(
						Direction.WEST,
						Half.BOTTOM,
						StairsShape.OUTER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.SOUTH,
						Half.BOTTOM,
						StairsShape.OUTER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.NORTH,
						Half.BOTTOM,
						StairsShape.OUTER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.EAST,
						Half.BOTTOM,
						StairsShape.OUTER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.WEST,
						Half.BOTTOM,
						StairsShape.OUTER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
					.select(
						Direction.NORTH,
						Half.BOTTOM,
						StairsShape.OUTER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(
						Direction.WEST,
						Half.BOTTOM,
						StairsShape.INNER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.SOUTH,
						Half.BOTTOM,
						StairsShape.INNER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.NORTH,
						Half.BOTTOM,
						StairsShape.INNER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.EAST,
						Half.BOTTOM,
						StairsShape.INNER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.WEST,
						Half.BOTTOM,
						StairsShape.INNER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(
						Direction.NORTH,
						Half.BOTTOM,
						StairsShape.INNER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.EAST,
						Half.TOP,
						StairsShape.STRAIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.WEST,
						Half.TOP,
						StairsShape.STRAIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.SOUTH,
						Half.TOP,
						StairsShape.STRAIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.NORTH,
						Half.TOP,
						StairsShape.STRAIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.EAST,
						Half.TOP,
						StairsShape.OUTER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.WEST,
						Half.TOP,
						StairsShape.OUTER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.SOUTH,
						Half.TOP,
						StairsShape.OUTER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.NORTH,
						Half.TOP,
						StairsShape.OUTER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.EAST,
						Half.TOP,
						StairsShape.OUTER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.WEST,
						Half.TOP,
						StairsShape.OUTER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.SOUTH,
						Half.TOP,
						StairsShape.OUTER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.NORTH,
						Half.TOP,
						StairsShape.OUTER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.EAST,
						Half.TOP,
						StairsShape.INNER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.WEST,
						Half.TOP,
						StairsShape.INNER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.SOUTH,
						Half.TOP,
						StairsShape.INNER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.NORTH,
						Half.TOP,
						StairsShape.INNER_RIGHT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.EAST,
						Half.TOP,
						StairsShape.INNER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.WEST,
						Half.TOP,
						StairsShape.INNER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.SOUTH,
						Half.TOP,
						StairsShape.INNER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.select(
						Direction.NORTH,
						Half.TOP,
						StairsShape.INNER_LEFT,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
			);
	}

	private static BlockStateGenerator createOrientableTrapdoor(
		Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3
	) {
		return MultiVariantGenerator.multiVariant(block)
			.with(
				PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN)
					.select(Direction.NORTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.select(
						Direction.SOUTH,
						Half.BOTTOM,
						false,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					)
					.select(
						Direction.EAST,
						Half.BOTTOM,
						false,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.select(
						Direction.WEST,
						Half.BOTTOM,
						false,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
					.select(Direction.NORTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(
						Direction.SOUTH,
						Half.TOP,
						false,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					)
					.select(
						Direction.EAST,
						Half.TOP,
						false,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.select(
						Direction.WEST,
						Half.TOP,
						false,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
					.select(Direction.NORTH, Half.BOTTOM, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
					.select(
						Direction.SOUTH,
						Half.BOTTOM,
						true,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					)
					.select(
						Direction.EAST,
						Half.BOTTOM,
						true,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.select(
						Direction.WEST,
						Half.BOTTOM,
						true,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
					.select(
						Direction.NORTH,
						Half.TOP,
						true,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					)
					.select(
						Direction.SOUTH,
						Half.TOP,
						true,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0)
					)
					.select(
						Direction.EAST,
						Half.TOP,
						true,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
					.select(
						Direction.WEST,
						Half.TOP,
						true,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
			);
	}

	private static BlockStateGenerator createTrapdoor(
		Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3
	) {
		return MultiVariantGenerator.multiVariant(block)
			.with(
				PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN)
					.select(Direction.NORTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.select(Direction.SOUTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.select(Direction.EAST, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.select(Direction.WEST, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.select(Direction.NORTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(Direction.SOUTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(Direction.EAST, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(Direction.WEST, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(Direction.NORTH, Half.BOTTOM, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
					.select(
						Direction.SOUTH,
						Half.BOTTOM,
						true,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					)
					.select(
						Direction.EAST,
						Half.BOTTOM,
						true,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.select(
						Direction.WEST,
						Half.BOTTOM,
						true,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
					.select(Direction.NORTH, Half.TOP, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
					.select(
						Direction.SOUTH,
						Half.TOP,
						true,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					)
					.select(
						Direction.EAST,
						Half.TOP,
						true,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.select(
						Direction.WEST,
						Half.TOP,
						true,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
			);
	}

	static MultiVariantGenerator createSimpleBlock(Block block, ResourceLocation resourceLocation) {
		return MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, resourceLocation));
	}

	private static PropertyDispatch createRotatedPillar() {
		return PropertyDispatch.property(BlockStateProperties.AXIS)
			.select(Direction.Axis.Y, Variant.variant())
			.select(Direction.Axis.Z, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
			.select(
				Direction.Axis.X,
				Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
			);
	}

	static BlockStateGenerator createPillarBlockUVLocked(
		Block block, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer
	) {
		ResourceLocation resourceLocation = ModelTemplates.CUBE_COLUMN_UV_LOCKED_X.create(block, textureMapping, biConsumer);
		ResourceLocation resourceLocation2 = ModelTemplates.CUBE_COLUMN_UV_LOCKED_Y.create(block, textureMapping, biConsumer);
		ResourceLocation resourceLocation3 = ModelTemplates.CUBE_COLUMN_UV_LOCKED_Z.create(block, textureMapping, biConsumer);
		ResourceLocation resourceLocation4 = ModelTemplates.CUBE_COLUMN.create(block, textureMapping, biConsumer);
		return MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, resourceLocation4))
			.with(
				PropertyDispatch.property(BlockStateProperties.AXIS)
					.select(Direction.Axis.X, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(Direction.Axis.Y, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.select(Direction.Axis.Z, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
			);
	}

	static BlockStateGenerator createAxisAlignedPillarBlock(Block block, ResourceLocation resourceLocation) {
		return MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, resourceLocation)).with(createRotatedPillar());
	}

	private void createAxisAlignedPillarBlockCustomModel(Block block, ResourceLocation resourceLocation) {
		this.blockStateOutput.accept(createAxisAlignedPillarBlock(block, resourceLocation));
	}

	public void createAxisAlignedPillarBlock(Block block, TexturedModel.Provider provider) {
		ResourceLocation resourceLocation = provider.create(block, this.modelOutput);
		this.blockStateOutput.accept(createAxisAlignedPillarBlock(block, resourceLocation));
	}

	private void createHorizontallyRotatedBlock(Block block, TexturedModel.Provider provider) {
		ResourceLocation resourceLocation = provider.create(block, this.modelOutput);
		this.blockStateOutput
			.accept(MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, resourceLocation)).with(createHorizontalFacingDispatch()));
	}

	static BlockStateGenerator createRotatedPillarWithHorizontalVariant(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		return MultiVariantGenerator.multiVariant(block)
			.with(
				PropertyDispatch.property(BlockStateProperties.AXIS)
					.select(Direction.Axis.Y, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(Direction.Axis.Z, Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
					.select(
						Direction.Axis.X,
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
			);
	}

	private void createRotatedPillarWithHorizontalVariant(Block block, TexturedModel.Provider provider, TexturedModel.Provider provider2) {
		ResourceLocation resourceLocation = provider.create(block, this.modelOutput);
		ResourceLocation resourceLocation2 = provider2.create(block, this.modelOutput);
		this.blockStateOutput.accept(createRotatedPillarWithHorizontalVariant(block, resourceLocation, resourceLocation2));
	}

	private ResourceLocation createSuffixedVariant(Block block, String string, ModelTemplate modelTemplate, Function<ResourceLocation, TextureMapping> function) {
		return modelTemplate.createWithSuffix(block, string, (TextureMapping)function.apply(TextureMapping.getBlockTexture(block, string)), this.modelOutput);
	}

	static BlockStateGenerator createPressurePlate(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		return MultiVariantGenerator.multiVariant(block).with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourceLocation2, resourceLocation));
	}

	static BlockStateGenerator createSlab(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3) {
		return MultiVariantGenerator.multiVariant(block)
			.with(
				PropertyDispatch.property(BlockStateProperties.SLAB_TYPE)
					.select(SlabType.BOTTOM, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(SlabType.TOP, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.select(SlabType.DOUBLE, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
			);
	}

	public void createTrivialCube(Block block) {
		this.createTrivialBlock(block, TexturedModel.CUBE);
	}

	public void createTrivialBlock(Block block, TexturedModel.Provider provider) {
		this.blockStateOutput.accept(createSimpleBlock(block, provider.create(block, this.modelOutput)));
	}

	private void createTrivialBlock(Block block, TextureMapping textureMapping, ModelTemplate modelTemplate) {
		ResourceLocation resourceLocation = modelTemplate.create(block, textureMapping, this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block, resourceLocation));
	}

	private BlockModelGenerators.BlockFamilyProvider family(Block block) {
		TexturedModel texturedModel = (TexturedModel)this.texturedModels.getOrDefault(block, TexturedModel.CUBE.get(block));
		return new BlockModelGenerators.BlockFamilyProvider(texturedModel.getMapping()).fullBlock(block, texturedModel.getTemplate());
	}

	public void createHangingSign(Block block, Block block2, Block block3) {
		TextureMapping textureMapping = TextureMapping.particle(block);
		ResourceLocation resourceLocation = ModelTemplates.PARTICLE_ONLY.create(block2, textureMapping, this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block2, resourceLocation));
		this.blockStateOutput.accept(createSimpleBlock(block3, resourceLocation));
		this.createSimpleFlatItemModel(block2.asItem());
		this.skipAutoItemBlock(block3);
	}

	void createDoor(Block block) {
		TextureMapping textureMapping = TextureMapping.door(block);
		ResourceLocation resourceLocation = ModelTemplates.DOOR_BOTTOM_LEFT.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.DOOR_BOTTOM_RIGHT.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation4 = ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation5 = ModelTemplates.DOOR_TOP_LEFT.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation6 = ModelTemplates.DOOR_TOP_LEFT_OPEN.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation7 = ModelTemplates.DOOR_TOP_RIGHT.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation8 = ModelTemplates.DOOR_TOP_RIGHT_OPEN.create(block, textureMapping, this.modelOutput);
		this.createSimpleFlatItemModel(block.asItem());
		this.blockStateOutput
			.accept(
				createDoor(
					block,
					resourceLocation,
					resourceLocation2,
					resourceLocation3,
					resourceLocation4,
					resourceLocation5,
					resourceLocation6,
					resourceLocation7,
					resourceLocation8
				)
			);
	}

	private void copyDoorModel(Block block, Block block2) {
		ResourceLocation resourceLocation = ModelTemplates.DOOR_BOTTOM_LEFT.getDefaultModelLocation(block);
		ResourceLocation resourceLocation2 = ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.getDefaultModelLocation(block);
		ResourceLocation resourceLocation3 = ModelTemplates.DOOR_BOTTOM_RIGHT.getDefaultModelLocation(block);
		ResourceLocation resourceLocation4 = ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.getDefaultModelLocation(block);
		ResourceLocation resourceLocation5 = ModelTemplates.DOOR_TOP_LEFT.getDefaultModelLocation(block);
		ResourceLocation resourceLocation6 = ModelTemplates.DOOR_TOP_LEFT_OPEN.getDefaultModelLocation(block);
		ResourceLocation resourceLocation7 = ModelTemplates.DOOR_TOP_RIGHT.getDefaultModelLocation(block);
		ResourceLocation resourceLocation8 = ModelTemplates.DOOR_TOP_RIGHT_OPEN.getDefaultModelLocation(block);
		this.delegateItemModel(block2, ModelLocationUtils.getModelLocation(block.asItem()));
		this.blockStateOutput
			.accept(
				createDoor(
					block2,
					resourceLocation,
					resourceLocation2,
					resourceLocation3,
					resourceLocation4,
					resourceLocation5,
					resourceLocation6,
					resourceLocation7,
					resourceLocation8
				)
			);
	}

	void createOrientableTrapdoor(Block block) {
		TextureMapping textureMapping = TextureMapping.defaultTexture(block);
		ResourceLocation resourceLocation = ModelTemplates.ORIENTABLE_TRAPDOOR_TOP.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.ORIENTABLE_TRAPDOOR_BOTTOM.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.ORIENTABLE_TRAPDOOR_OPEN.create(block, textureMapping, this.modelOutput);
		this.blockStateOutput.accept(createOrientableTrapdoor(block, resourceLocation, resourceLocation2, resourceLocation3));
		this.delegateItemModel(block, resourceLocation2);
	}

	void createTrapdoor(Block block) {
		TextureMapping textureMapping = TextureMapping.defaultTexture(block);
		ResourceLocation resourceLocation = ModelTemplates.TRAPDOOR_TOP.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.TRAPDOOR_BOTTOM.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.TRAPDOOR_OPEN.create(block, textureMapping, this.modelOutput);
		this.blockStateOutput.accept(createTrapdoor(block, resourceLocation, resourceLocation2, resourceLocation3));
		this.delegateItemModel(block, resourceLocation2);
	}

	private void copyTrapdoorModel(Block block, Block block2) {
		ResourceLocation resourceLocation = ModelTemplates.TRAPDOOR_TOP.getDefaultModelLocation(block);
		ResourceLocation resourceLocation2 = ModelTemplates.TRAPDOOR_BOTTOM.getDefaultModelLocation(block);
		ResourceLocation resourceLocation3 = ModelTemplates.TRAPDOOR_OPEN.getDefaultModelLocation(block);
		this.delegateItemModel(block2, ModelLocationUtils.getModelLocation(block.asItem()));
		this.blockStateOutput.accept(createTrapdoor(block2, resourceLocation, resourceLocation2, resourceLocation3));
	}

	private void createBigDripLeafBlock() {
		this.skipAutoItemBlock(Blocks.BIG_DRIPLEAF);
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF);
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF, "_partial_tilt");
		ResourceLocation resourceLocation3 = ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF, "_full_tilt");
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.BIG_DRIPLEAF)
					.with(createHorizontalFacingDispatch())
					.with(
						PropertyDispatch.property(BlockStateProperties.TILT)
							.select(Tilt.NONE, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
							.select(Tilt.UNSTABLE, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
							.select(Tilt.PARTIAL, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
							.select(Tilt.FULL, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
					)
			);
	}

	private BlockModelGenerators.WoodProvider woodProvider(Block block) {
		return new BlockModelGenerators.WoodProvider(TextureMapping.logColumn(block));
	}

	private void createNonTemplateModelBlock(Block block) {
		this.createNonTemplateModelBlock(block, block);
	}

	private void createNonTemplateModelBlock(Block block, Block block2) {
		this.blockStateOutput.accept(createSimpleBlock(block, ModelLocationUtils.getModelLocation(block2)));
	}

	private void createCrossBlockWithDefaultItem(Block block, BlockModelGenerators.TintState tintState) {
		this.createSimpleFlatItemModel(block);
		this.createCrossBlock(block, tintState);
	}

	private void createCrossBlockWithDefaultItem(Block block, BlockModelGenerators.TintState tintState, TextureMapping textureMapping) {
		this.createSimpleFlatItemModel(block);
		this.createCrossBlock(block, tintState, textureMapping);
	}

	private void createCrossBlock(Block block, BlockModelGenerators.TintState tintState) {
		TextureMapping textureMapping = TextureMapping.cross(block);
		this.createCrossBlock(block, tintState, textureMapping);
	}

	private void createCrossBlock(Block block, BlockModelGenerators.TintState tintState, TextureMapping textureMapping) {
		ResourceLocation resourceLocation = tintState.getCross().create(block, textureMapping, this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block, resourceLocation));
	}

	private void createCrossBlock(Block block, BlockModelGenerators.TintState tintState, Property<Integer> property, int... is) {
		if (property.getPossibleValues().size() != is.length) {
			throw new IllegalArgumentException("missing values for property: " + property);
		} else {
			PropertyDispatch propertyDispatch = PropertyDispatch.property(property).generate(integer -> {
				String string = "_stage" + is[integer];
				TextureMapping textureMapping = TextureMapping.cross(TextureMapping.getBlockTexture(block, string));
				ResourceLocation resourceLocation = tintState.getCross().createWithSuffix(block, string, textureMapping, this.modelOutput);
				return Variant.variant().with(VariantProperties.MODEL, resourceLocation);
			});
			this.createSimpleFlatItemModel(block.asItem());
			this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(propertyDispatch));
		}
	}

	private void createPlant(Block block, Block block2, BlockModelGenerators.TintState tintState) {
		this.createCrossBlockWithDefaultItem(block, tintState);
		TextureMapping textureMapping = TextureMapping.plant(block);
		ResourceLocation resourceLocation = tintState.getCrossPot().create(block2, textureMapping, this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block2, resourceLocation));
	}

	private void createCoralFans(Block block, Block block2) {
		TexturedModel texturedModel = TexturedModel.CORAL_FAN.get(block);
		ResourceLocation resourceLocation = texturedModel.create(block, this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block, resourceLocation));
		ResourceLocation resourceLocation2 = ModelTemplates.CORAL_WALL_FAN.create(block2, texturedModel.getMapping(), this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block2, Variant.variant().with(VariantProperties.MODEL, resourceLocation2)).with(createHorizontalFacingDispatch())
			);
		this.createSimpleFlatItemModel(block);
	}

	private void createStems(Block block, Block block2) {
		this.createSimpleFlatItemModel(block.asItem());
		TextureMapping textureMapping = TextureMapping.stem(block);
		TextureMapping textureMapping2 = TextureMapping.attachedStem(block, block2);
		ResourceLocation resourceLocation = ModelTemplates.ATTACHED_STEM.create(block2, textureMapping2, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block2, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.with(
						PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
							.select(Direction.WEST, Variant.variant())
							.select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
							.select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
							.select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
					)
			);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block)
					.with(
						PropertyDispatch.property(BlockStateProperties.AGE_7)
							.generate(integer -> Variant.variant().with(VariantProperties.MODEL, ModelTemplates.STEMS[integer].create(block, textureMapping, this.modelOutput)))
					)
			);
	}

	private void createPitcherPlant() {
		Block block = Blocks.PITCHER_PLANT;
		this.createSimpleFlatItemModel(block.asItem());
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(block, "_top");
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(block, "_bottom");
		this.createDoubleBlock(block, resourceLocation, resourceLocation2);
	}

	private void createPitcherCrop() {
		Block block = Blocks.PITCHER_CROP;
		this.createSimpleFlatItemModel(block.asItem());
		PropertyDispatch propertyDispatch = PropertyDispatch.properties(PitcherCropBlock.AGE, BlockStateProperties.DOUBLE_BLOCK_HALF)
			.generate((integer, doubleBlockHalf) -> {
				return switch (doubleBlockHalf) {
					case UPPER -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_top_stage_" + integer));
					case LOWER -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_bottom_stage_" + integer));
				};
			});
		this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(propertyDispatch));
	}

	private void createCoral(Block block, Block block2, Block block3, Block block4, Block block5, Block block6, Block block7, Block block8) {
		this.createCrossBlockWithDefaultItem(block, BlockModelGenerators.TintState.NOT_TINTED);
		this.createCrossBlockWithDefaultItem(block2, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialCube(block3);
		this.createTrivialCube(block4);
		this.createCoralFans(block5, block7);
		this.createCoralFans(block6, block8);
	}

	private void createDoublePlant(Block block, BlockModelGenerators.TintState tintState) {
		this.createSimpleFlatItemModel(block, "_top");
		ResourceLocation resourceLocation = this.createSuffixedVariant(block, "_top", tintState.getCross(), TextureMapping::cross);
		ResourceLocation resourceLocation2 = this.createSuffixedVariant(block, "_bottom", tintState.getCross(), TextureMapping::cross);
		this.createDoubleBlock(block, resourceLocation, resourceLocation2);
	}

	private void createSunflower() {
		this.createSimpleFlatItemModel(Blocks.SUNFLOWER, "_front");
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.SUNFLOWER, "_top");
		ResourceLocation resourceLocation2 = this.createSuffixedVariant(
			Blocks.SUNFLOWER, "_bottom", BlockModelGenerators.TintState.NOT_TINTED.getCross(), TextureMapping::cross
		);
		this.createDoubleBlock(Blocks.SUNFLOWER, resourceLocation, resourceLocation2);
	}

	private void createTallSeagrass() {
		ResourceLocation resourceLocation = this.createSuffixedVariant(Blocks.TALL_SEAGRASS, "_top", ModelTemplates.SEAGRASS, TextureMapping::defaultTexture);
		ResourceLocation resourceLocation2 = this.createSuffixedVariant(Blocks.TALL_SEAGRASS, "_bottom", ModelTemplates.SEAGRASS, TextureMapping::defaultTexture);
		this.createDoubleBlock(Blocks.TALL_SEAGRASS, resourceLocation, resourceLocation2);
	}

	private void createSmallDripleaf() {
		this.skipAutoItemBlock(Blocks.SMALL_DRIPLEAF);
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.SMALL_DRIPLEAF, "_top");
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.SMALL_DRIPLEAF, "_bottom");
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.SMALL_DRIPLEAF)
					.with(createHorizontalFacingDispatch())
					.with(
						PropertyDispatch.property(BlockStateProperties.DOUBLE_BLOCK_HALF)
							.select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
							.select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					)
			);
	}

	private void createDoubleBlock(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block)
					.with(
						PropertyDispatch.property(BlockStateProperties.DOUBLE_BLOCK_HALF)
							.select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
							.select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					)
			);
	}

	private void createPassiveRail(Block block) {
		TextureMapping textureMapping = TextureMapping.rail(block);
		TextureMapping textureMapping2 = TextureMapping.rail(TextureMapping.getBlockTexture(block, "_corner"));
		ResourceLocation resourceLocation = ModelTemplates.RAIL_FLAT.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.RAIL_CURVED.create(block, textureMapping2, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.RAIL_RAISED_NE.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation4 = ModelTemplates.RAIL_RAISED_SW.create(block, textureMapping, this.modelOutput);
		this.createSimpleFlatItemModel(block);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block)
					.with(
						PropertyDispatch.property(BlockStateProperties.RAIL_SHAPE)
							.select(RailShape.NORTH_SOUTH, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
							.select(
								RailShape.EAST_WEST, Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								RailShape.ASCENDING_EAST,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								RailShape.ASCENDING_WEST,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(RailShape.ASCENDING_NORTH, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
							.select(RailShape.ASCENDING_SOUTH, Variant.variant().with(VariantProperties.MODEL, resourceLocation4))
							.select(RailShape.SOUTH_EAST, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
							.select(
								RailShape.SOUTH_WEST, Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								RailShape.NORTH_WEST, Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								RailShape.NORTH_EAST, Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
					)
			);
	}

	private void createActiveRail(Block block) {
		ResourceLocation resourceLocation = this.createSuffixedVariant(block, "", ModelTemplates.RAIL_FLAT, TextureMapping::rail);
		ResourceLocation resourceLocation2 = this.createSuffixedVariant(block, "", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail);
		ResourceLocation resourceLocation3 = this.createSuffixedVariant(block, "", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail);
		ResourceLocation resourceLocation4 = this.createSuffixedVariant(block, "_on", ModelTemplates.RAIL_FLAT, TextureMapping::rail);
		ResourceLocation resourceLocation5 = this.createSuffixedVariant(block, "_on", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail);
		ResourceLocation resourceLocation6 = this.createSuffixedVariant(block, "_on", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail);
		PropertyDispatch propertyDispatch = PropertyDispatch.properties(BlockStateProperties.POWERED, BlockStateProperties.RAIL_SHAPE_STRAIGHT)
			.generate(
				(boolean_, railShape) -> {
					switch (railShape) {
						case NORTH_SOUTH:
							return Variant.variant().with(VariantProperties.MODEL, boolean_ ? resourceLocation4 : resourceLocation);
						case EAST_WEST:
							return Variant.variant()
								.with(VariantProperties.MODEL, boolean_ ? resourceLocation4 : resourceLocation)
								.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
						case ASCENDING_EAST:
							return Variant.variant()
								.with(VariantProperties.MODEL, boolean_ ? resourceLocation5 : resourceLocation2)
								.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
						case ASCENDING_WEST:
							return Variant.variant()
								.with(VariantProperties.MODEL, boolean_ ? resourceLocation6 : resourceLocation3)
								.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
						case ASCENDING_NORTH:
							return Variant.variant().with(VariantProperties.MODEL, boolean_ ? resourceLocation5 : resourceLocation2);
						case ASCENDING_SOUTH:
							return Variant.variant().with(VariantProperties.MODEL, boolean_ ? resourceLocation6 : resourceLocation3);
						default:
							throw new UnsupportedOperationException("Fix you generator!");
					}
				}
			);
		this.createSimpleFlatItemModel(block);
		this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(propertyDispatch));
	}

	private BlockModelGenerators.BlockEntityModelGenerator blockEntityModels(ResourceLocation resourceLocation, Block block) {
		return new BlockModelGenerators.BlockEntityModelGenerator(resourceLocation, block);
	}

	private BlockModelGenerators.BlockEntityModelGenerator blockEntityModels(Block block, Block block2) {
		return new BlockModelGenerators.BlockEntityModelGenerator(ModelLocationUtils.getModelLocation(block), block2);
	}

	private void createAirLikeBlock(Block block, Item item) {
		ResourceLocation resourceLocation = ModelTemplates.PARTICLE_ONLY.create(block, TextureMapping.particleFromItem(item), this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block, resourceLocation));
	}

	private void createAirLikeBlock(Block block, ResourceLocation resourceLocation) {
		ResourceLocation resourceLocation2 = ModelTemplates.PARTICLE_ONLY.create(block, TextureMapping.particle(resourceLocation), this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block, resourceLocation2));
	}

	private void createFullAndCarpetBlocks(Block block, Block block2) {
		this.createTrivialCube(block);
		ResourceLocation resourceLocation = TexturedModel.CARPET.get(block).create(block2, this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block2, resourceLocation));
	}

	private void createFlowerBed(Block block) {
		this.createSimpleFlatItemModel(block.asItem());
		ResourceLocation resourceLocation = TexturedModel.FLOWERBED_1.create(block, this.modelOutput);
		ResourceLocation resourceLocation2 = TexturedModel.FLOWERBED_2.create(block, this.modelOutput);
		ResourceLocation resourceLocation3 = TexturedModel.FLOWERBED_3.create(block, this.modelOutput);
		ResourceLocation resourceLocation4 = TexturedModel.FLOWERBED_4.create(block, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(block)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation2)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation4)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					)
					.with(
						Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
			);
	}

	private void createColoredBlockWithRandomRotations(TexturedModel.Provider provider, Block... blocks) {
		for (Block block : blocks) {
			ResourceLocation resourceLocation = provider.create(block, this.modelOutput);
			this.blockStateOutput.accept(createRotatedVariant(block, resourceLocation));
		}
	}

	private void createColoredBlockWithStateRotations(TexturedModel.Provider provider, Block... blocks) {
		for (Block block : blocks) {
			ResourceLocation resourceLocation = provider.create(block, this.modelOutput);
			this.blockStateOutput
				.accept(
					MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, resourceLocation)).with(createHorizontalFacingDispatchAlt())
				);
		}
	}

	private void createGlassBlocks(Block block, Block block2) {
		this.createTrivialCube(block);
		TextureMapping textureMapping = TextureMapping.pane(block, block2);
		ResourceLocation resourceLocation = ModelTemplates.STAINED_GLASS_PANE_POST.create(block2, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.STAINED_GLASS_PANE_SIDE.create(block2, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.STAINED_GLASS_PANE_SIDE_ALT.create(block2, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation4 = ModelTemplates.STAINED_GLASS_PANE_NOSIDE.create(block2, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation5 = ModelTemplates.STAINED_GLASS_PANE_NOSIDE_ALT.create(block2, textureMapping, this.modelOutput);
		Item item = block2.asItem();
		ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(item), TextureMapping.layer0(block), this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(block2)
					.with(Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.with(
						Condition.condition().term(BlockStateProperties.EAST, true),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.with(Condition.condition().term(BlockStateProperties.SOUTH, true), Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
					.with(
						Condition.condition().term(BlockStateProperties.WEST, true),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.with(Condition.condition().term(BlockStateProperties.NORTH, false), Variant.variant().with(VariantProperties.MODEL, resourceLocation4))
					.with(Condition.condition().term(BlockStateProperties.EAST, false), Variant.variant().with(VariantProperties.MODEL, resourceLocation5))
					.with(
						Condition.condition().term(BlockStateProperties.SOUTH, false),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation5).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.with(
						Condition.condition().term(BlockStateProperties.WEST, false),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
			);
	}

	private void createCommandBlock(Block block) {
		TextureMapping textureMapping = TextureMapping.commandBlock(block);
		ResourceLocation resourceLocation = ModelTemplates.COMMAND_BLOCK.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = this.createSuffixedVariant(
			block, "_conditional", ModelTemplates.COMMAND_BLOCK, resourceLocationx -> textureMapping.copyAndUpdate(TextureSlot.SIDE, resourceLocationx)
		);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block)
					.with(createBooleanModelDispatch(BlockStateProperties.CONDITIONAL, resourceLocation2, resourceLocation))
					.with(createFacingDispatch())
			);
	}

	private void createAnvil(Block block) {
		ResourceLocation resourceLocation = TexturedModel.ANVIL.create(block, this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block, resourceLocation).with(createHorizontalFacingDispatchAlt()));
	}

	private List<Variant> createBambooModels(int i) {
		String string = "_age" + i;
		return (List<Variant>)IntStream.range(1, 5)
			.mapToObj(ix -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, ix + string)))
			.collect(Collectors.toList());
	}

	private void createBamboo() {
		this.skipAutoItemBlock(Blocks.BAMBOO);
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.BAMBOO)
					.with(Condition.condition().term(BlockStateProperties.AGE_1, 0), this.createBambooModels(0))
					.with(Condition.condition().term(BlockStateProperties.AGE_1, 1), this.createBambooModels(1))
					.with(
						Condition.condition().term(BlockStateProperties.BAMBOO_LEAVES, BambooLeaves.SMALL),
						Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, "_small_leaves"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.BAMBOO_LEAVES, BambooLeaves.LARGE),
						Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, "_large_leaves"))
					)
			);
	}

	private PropertyDispatch createColumnWithFacing() {
		return PropertyDispatch.property(BlockStateProperties.FACING)
			.select(Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
			.select(Direction.UP, Variant.variant())
			.select(Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
			.select(
				Direction.SOUTH,
				Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
			)
			.select(
				Direction.WEST,
				Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
			)
			.select(
				Direction.EAST,
				Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
			);
	}

	private void createBarrel() {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.BARREL, "_top_open");
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.BARREL)
					.with(this.createColumnWithFacing())
					.with(
						PropertyDispatch.property(BlockStateProperties.OPEN)
							.select(false, Variant.variant().with(VariantProperties.MODEL, TexturedModel.CUBE_TOP_BOTTOM.create(Blocks.BARREL, this.modelOutput)))
							.select(
								true,
								Variant.variant()
									.with(
										VariantProperties.MODEL,
										TexturedModel.CUBE_TOP_BOTTOM
											.get(Blocks.BARREL)
											.updateTextures(textureMapping -> textureMapping.put(TextureSlot.TOP, resourceLocation))
											.createWithSuffix(Blocks.BARREL, "_open", this.modelOutput)
									)
							)
					)
			);
	}

	private static <T extends Comparable<T>> PropertyDispatch createEmptyOrFullDispatch(
		Property<T> property, T comparable, ResourceLocation resourceLocation, ResourceLocation resourceLocation2
	) {
		Variant variant = Variant.variant().with(VariantProperties.MODEL, resourceLocation);
		Variant variant2 = Variant.variant().with(VariantProperties.MODEL, resourceLocation2);
		return PropertyDispatch.property(property).generate(comparable2 -> {
			boolean bl = comparable2.compareTo(comparable) >= 0;
			return bl ? variant : variant2;
		});
	}

	private void createBeeNest(Block block, Function<Block, TextureMapping> function) {
		TextureMapping textureMapping = ((TextureMapping)function.apply(block)).copyForced(TextureSlot.SIDE, TextureSlot.PARTICLE);
		TextureMapping textureMapping2 = textureMapping.copyAndUpdate(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front_honey"));
		ResourceLocation resourceLocation = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(block, "_honey", textureMapping2, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block)
					.with(createHorizontalFacingDispatch())
					.with(createEmptyOrFullDispatch(BlockStateProperties.LEVEL_HONEY, 5, resourceLocation2, resourceLocation))
			);
	}

	private void createCropBlock(Block block, Property<Integer> property, int... is) {
		if (property.getPossibleValues().size() != is.length) {
			throw new IllegalArgumentException();
		} else {
			Int2ObjectMap<ResourceLocation> int2ObjectMap = new Int2ObjectOpenHashMap<>();
			PropertyDispatch propertyDispatch = PropertyDispatch.property(property)
				.generate(
					integer -> {
						int i = is[integer];
						ResourceLocation resourceLocation = int2ObjectMap.computeIfAbsent(
							i, (Int2ObjectFunction<? extends ResourceLocation>)(j -> this.createSuffixedVariant(block, "_stage" + i, ModelTemplates.CROP, TextureMapping::crop))
						);
						return Variant.variant().with(VariantProperties.MODEL, resourceLocation);
					}
				);
			this.createSimpleFlatItemModel(block.asItem());
			this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(propertyDispatch));
		}
	}

	private void createBell() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.BELL, "_floor");
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_ceiling");
		ResourceLocation resourceLocation3 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_wall");
		ResourceLocation resourceLocation4 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_between_walls");
		this.createSimpleFlatItemModel(Items.BELL);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.BELL)
					.with(
						PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.BELL_ATTACHMENT)
							.select(Direction.NORTH, BellAttachType.FLOOR, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
							.select(
								Direction.SOUTH,
								BellAttachType.FLOOR,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								Direction.EAST,
								BellAttachType.FLOOR,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								Direction.WEST,
								BellAttachType.FLOOR,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(Direction.NORTH, BellAttachType.CEILING, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
							.select(
								Direction.SOUTH,
								BellAttachType.CEILING,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								Direction.EAST,
								BellAttachType.CEILING,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								Direction.WEST,
								BellAttachType.CEILING,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(
								Direction.NORTH,
								BellAttachType.SINGLE_WALL,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(
								Direction.SOUTH,
								BellAttachType.SINGLE_WALL,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(Direction.EAST, BellAttachType.SINGLE_WALL, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
							.select(
								Direction.WEST,
								BellAttachType.SINGLE_WALL,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								Direction.SOUTH,
								BellAttachType.DOUBLE_WALL,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								Direction.NORTH,
								BellAttachType.DOUBLE_WALL,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(Direction.EAST, BellAttachType.DOUBLE_WALL, Variant.variant().with(VariantProperties.MODEL, resourceLocation4))
							.select(
								Direction.WEST,
								BellAttachType.DOUBLE_WALL,
								Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
					)
			);
	}

	private void createGrindstone() {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(
						Blocks.GRINDSTONE, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.GRINDSTONE))
					)
					.with(
						PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
							.select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
							.select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
							.select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
							.select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
							.select(AttachFace.WALL, Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
							.select(
								AttachFace.WALL,
								Direction.EAST,
								Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								AttachFace.WALL,
								Direction.SOUTH,
								Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								AttachFace.WALL,
								Direction.WEST,
								Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
							.select(
								AttachFace.CEILING,
								Direction.WEST,
								Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								AttachFace.CEILING,
								Direction.NORTH,
								Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								AttachFace.CEILING,
								Direction.EAST,
								Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
					)
			);
	}

	private void createFurnace(Block block, TexturedModel.Provider provider) {
		ResourceLocation resourceLocation = provider.create(block, this.modelOutput);
		ResourceLocation resourceLocation2 = TextureMapping.getBlockTexture(block, "_front_on");
		ResourceLocation resourceLocation3 = provider.get(block)
			.updateTextures(textureMapping -> textureMapping.put(TextureSlot.FRONT, resourceLocation2))
			.createWithSuffix(block, "_on", this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block)
					.with(createBooleanModelDispatch(BlockStateProperties.LIT, resourceLocation3, resourceLocation))
					.with(createHorizontalFacingDispatch())
			);
	}

	private void createCampfires(Block... blocks) {
		ResourceLocation resourceLocation = ModelLocationUtils.decorateBlockModelLocation("campfire_off");

		for (Block block : blocks) {
			ResourceLocation resourceLocation2 = ModelTemplates.CAMPFIRE.create(block, TextureMapping.campfire(block), this.modelOutput);
			this.createSimpleFlatItemModel(block.asItem());
			this.blockStateOutput
				.accept(
					MultiVariantGenerator.multiVariant(block)
						.with(createBooleanModelDispatch(BlockStateProperties.LIT, resourceLocation2, resourceLocation))
						.with(createHorizontalFacingDispatchAlt())
				);
		}
	}

	private void createAzalea(Block block) {
		ResourceLocation resourceLocation = ModelTemplates.AZALEA.create(block, TextureMapping.cubeTop(block), this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block, resourceLocation));
	}

	private void createPottedAzalea(Block block) {
		ResourceLocation resourceLocation;
		if (block == Blocks.POTTED_FLOWERING_AZALEA) {
			resourceLocation = ModelTemplates.POTTED_FLOWERING_AZALEA.create(block, TextureMapping.pottedAzalea(block), this.modelOutput);
		} else {
			resourceLocation = ModelTemplates.POTTED_AZALEA.create(block, TextureMapping.pottedAzalea(block), this.modelOutput);
		}

		this.blockStateOutput.accept(createSimpleBlock(block, resourceLocation));
	}

	private void createBookshelf() {
		TextureMapping textureMapping = TextureMapping.column(TextureMapping.getBlockTexture(Blocks.BOOKSHELF), TextureMapping.getBlockTexture(Blocks.OAK_PLANKS));
		ResourceLocation resourceLocation = ModelTemplates.CUBE_COLUMN.create(Blocks.BOOKSHELF, textureMapping, this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(Blocks.BOOKSHELF, resourceLocation));
	}

	private void createRedstoneWire() {
		this.createSimpleFlatItemModel(Items.REDSTONE);
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.REDSTONE_WIRE)
					.with(
						Condition.or(
							Condition.condition()
								.term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.NONE)
								.term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.NONE)
								.term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.NONE)
								.term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.NONE),
							Condition.condition()
								.term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
								.term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
							Condition.condition()
								.term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
								.term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
							Condition.condition()
								.term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
								.term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
							Condition.condition()
								.term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
								.term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
						),
						Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_dot"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
						Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side0"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
						Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side_alt0"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
						Variant.variant()
							.with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side_alt1"))
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
					.with(
						Condition.condition().term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
						Variant.variant()
							.with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side1"))
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
					.with(
						Condition.condition().term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.UP),
						Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.UP),
						Variant.variant()
							.with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.with(
						Condition.condition().term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.UP),
						Variant.variant()
							.with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
					)
					.with(
						Condition.condition().term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.UP),
						Variant.variant()
							.with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
					)
			);
	}

	private void createComparator() {
		this.createSimpleFlatItemModel(Items.COMPARATOR);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.COMPARATOR)
					.with(createHorizontalFacingDispatchAlt())
					.with(
						PropertyDispatch.properties(BlockStateProperties.MODE_COMPARATOR, BlockStateProperties.POWERED)
							.select(ComparatorMode.COMPARE, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR)))
							.select(ComparatorMode.COMPARE, true, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_on")))
							.select(
								ComparatorMode.SUBTRACT, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_subtract"))
							)
							.select(
								ComparatorMode.SUBTRACT, true, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_on_subtract"))
							)
					)
			);
	}

	private void createSmoothStoneSlab() {
		TextureMapping textureMapping = TextureMapping.cube(Blocks.SMOOTH_STONE);
		TextureMapping textureMapping2 = TextureMapping.column(TextureMapping.getBlockTexture(Blocks.SMOOTH_STONE_SLAB, "_side"), textureMapping.get(TextureSlot.TOP));
		ResourceLocation resourceLocation = ModelTemplates.SLAB_BOTTOM.create(Blocks.SMOOTH_STONE_SLAB, textureMapping2, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.SLAB_TOP.create(Blocks.SMOOTH_STONE_SLAB, textureMapping2, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.CUBE_COLUMN.createWithOverride(Blocks.SMOOTH_STONE_SLAB, "_double", textureMapping2, this.modelOutput);
		this.blockStateOutput.accept(createSlab(Blocks.SMOOTH_STONE_SLAB, resourceLocation, resourceLocation2, resourceLocation3));
		this.blockStateOutput.accept(createSimpleBlock(Blocks.SMOOTH_STONE, ModelTemplates.CUBE_ALL.create(Blocks.SMOOTH_STONE, textureMapping, this.modelOutput)));
	}

	private void createBrewingStand() {
		this.createSimpleFlatItemModel(Items.BREWING_STAND);
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.BREWING_STAND)
					.with(Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND)))
					.with(
						Condition.condition().term(BlockStateProperties.HAS_BOTTLE_0, true),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle0"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.HAS_BOTTLE_1, true),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle1"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.HAS_BOTTLE_2, true),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle2"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.HAS_BOTTLE_0, false),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty0"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.HAS_BOTTLE_1, false),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty1"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.HAS_BOTTLE_2, false),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty2"))
					)
			);
	}

	private void createMushroomBlock(Block block) {
		ResourceLocation resourceLocation = ModelTemplates.SINGLE_FACE.create(block, TextureMapping.defaultTexture(block), this.modelOutput);
		ResourceLocation resourceLocation2 = ModelLocationUtils.decorateBlockModelLocation("mushroom_block_inside");
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(block)
					.with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.with(
						Condition.condition().term(BlockStateProperties.EAST, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.SOUTH, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.WEST, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.UP, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.DOWN, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(Condition.condition().term(BlockStateProperties.NORTH, false), Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.with(
						Condition.condition().term(BlockStateProperties.EAST, false),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, false)
					)
					.with(
						Condition.condition().term(BlockStateProperties.SOUTH, false),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, false)
					)
					.with(
						Condition.condition().term(BlockStateProperties.WEST, false),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, false)
					)
					.with(
						Condition.condition().term(BlockStateProperties.UP, false),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, false)
					)
					.with(
						Condition.condition().term(BlockStateProperties.DOWN, false),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, false)
					)
			);
		this.delegateItemModel(block, TexturedModel.CUBE.createWithSuffix(block, "_inventory", this.modelOutput));
	}

	private void createCakeBlock() {
		this.createSimpleFlatItemModel(Items.CAKE);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.CAKE)
					.with(
						PropertyDispatch.property(BlockStateProperties.BITES)
							.select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE)))
							.select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice1")))
							.select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice2")))
							.select(3, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice3")))
							.select(4, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice4")))
							.select(5, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice5")))
							.select(6, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice6")))
					)
			);
	}

	private void createCartographyTable() {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
			.put(TextureSlot.DOWN, TextureMapping.getBlockTexture(Blocks.DARK_OAK_PLANKS))
			.put(TextureSlot.UP, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_top"))
			.put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
			.put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
			.put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side1"))
			.put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side2"));
		this.blockStateOutput
			.accept(createSimpleBlock(Blocks.CARTOGRAPHY_TABLE, ModelTemplates.CUBE.create(Blocks.CARTOGRAPHY_TABLE, textureMapping, this.modelOutput)));
	}

	private void createSmithingTable() {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
			.put(TextureSlot.DOWN, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_bottom"))
			.put(TextureSlot.UP, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_top"))
			.put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
			.put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
			.put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_side"))
			.put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_side"));
		this.blockStateOutput.accept(createSimpleBlock(Blocks.SMITHING_TABLE, ModelTemplates.CUBE.create(Blocks.SMITHING_TABLE, textureMapping, this.modelOutput)));
	}

	private void createCraftingTableLike(Block block, Block block2, BiFunction<Block, Block, TextureMapping> biFunction) {
		TextureMapping textureMapping = (TextureMapping)biFunction.apply(block, block2);
		this.blockStateOutput.accept(createSimpleBlock(block, ModelTemplates.CUBE.create(block, textureMapping, this.modelOutput)));
	}

	public void createGenericCube(Block block) {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_particle"))
			.put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block, "_down"))
			.put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_up"))
			.put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_north"))
			.put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_south"))
			.put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_east"))
			.put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_west"));
		this.blockStateOutput.accept(createSimpleBlock(block, ModelTemplates.CUBE.create(block, textureMapping, this.modelOutput)));
	}

	private void createPumpkins() {
		TextureMapping textureMapping = TextureMapping.column(Blocks.PUMPKIN);
		this.blockStateOutput.accept(createSimpleBlock(Blocks.PUMPKIN, ModelLocationUtils.getModelLocation(Blocks.PUMPKIN)));
		this.createPumpkinVariant(Blocks.CARVED_PUMPKIN, textureMapping);
		this.createPumpkinVariant(Blocks.JACK_O_LANTERN, textureMapping);
	}

	private void createPumpkinVariant(Block block, TextureMapping textureMapping) {
		ResourceLocation resourceLocation = ModelTemplates.CUBE_ORIENTABLE
			.create(block, textureMapping.copyAndUpdate(TextureSlot.FRONT, TextureMapping.getBlockTexture(block)), this.modelOutput);
		this.blockStateOutput
			.accept(MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, resourceLocation)).with(createHorizontalFacingDispatch()));
	}

	private void createCauldrons() {
		this.createSimpleFlatItemModel(Items.CAULDRON);
		this.createNonTemplateModelBlock(Blocks.CAULDRON);
		this.blockStateOutput
			.accept(
				createSimpleBlock(
					Blocks.LAVA_CAULDRON,
					ModelTemplates.CAULDRON_FULL
						.create(Blocks.LAVA_CAULDRON, TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.LAVA, "_still")), this.modelOutput)
				)
			);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.WATER_CAULDRON)
					.with(
						PropertyDispatch.property(LayeredCauldronBlock.LEVEL)
							.select(
								1,
								Variant.variant()
									.with(
										VariantProperties.MODEL,
										ModelTemplates.CAULDRON_LEVEL1
											.createWithSuffix(
												Blocks.WATER_CAULDRON, "_level1", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")), this.modelOutput
											)
									)
							)
							.select(
								2,
								Variant.variant()
									.with(
										VariantProperties.MODEL,
										ModelTemplates.CAULDRON_LEVEL2
											.createWithSuffix(
												Blocks.WATER_CAULDRON, "_level2", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")), this.modelOutput
											)
									)
							)
							.select(
								3,
								Variant.variant()
									.with(
										VariantProperties.MODEL,
										ModelTemplates.CAULDRON_FULL
											.createWithSuffix(Blocks.WATER_CAULDRON, "_full", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")), this.modelOutput)
									)
							)
					)
			);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.POWDER_SNOW_CAULDRON)
					.with(
						PropertyDispatch.property(LayeredCauldronBlock.LEVEL)
							.select(
								1,
								Variant.variant()
									.with(
										VariantProperties.MODEL,
										ModelTemplates.CAULDRON_LEVEL1
											.createWithSuffix(
												Blocks.POWDER_SNOW_CAULDRON, "_level1", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)), this.modelOutput
											)
									)
							)
							.select(
								2,
								Variant.variant()
									.with(
										VariantProperties.MODEL,
										ModelTemplates.CAULDRON_LEVEL2
											.createWithSuffix(
												Blocks.POWDER_SNOW_CAULDRON, "_level2", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)), this.modelOutput
											)
									)
							)
							.select(
								3,
								Variant.variant()
									.with(
										VariantProperties.MODEL,
										ModelTemplates.CAULDRON_FULL
											.createWithSuffix(
												Blocks.POWDER_SNOW_CAULDRON, "_full", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)), this.modelOutput
											)
									)
							)
					)
			);
	}

	private void createChorusFlower() {
		TextureMapping textureMapping = TextureMapping.defaultTexture(Blocks.CHORUS_FLOWER);
		ResourceLocation resourceLocation = ModelTemplates.CHORUS_FLOWER.create(Blocks.CHORUS_FLOWER, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = this.createSuffixedVariant(
			Blocks.CHORUS_FLOWER, "_dead", ModelTemplates.CHORUS_FLOWER, resourceLocationx -> textureMapping.copyAndUpdate(TextureSlot.TEXTURE, resourceLocationx)
		);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.CHORUS_FLOWER)
					.with(createEmptyOrFullDispatch(BlockStateProperties.AGE_5, 5, resourceLocation2, resourceLocation))
			);
	}

	private void createCrafterBlock() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.CRAFTER);
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_triggered");
		ResourceLocation resourceLocation3 = ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_crafting");
		ResourceLocation resourceLocation4 = ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_crafting_triggered");
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.CRAFTER)
					.with(PropertyDispatch.property(BlockStateProperties.ORIENTATION).generate(frontAndTop -> this.applyRotation(frontAndTop, Variant.variant())))
					.with(
						PropertyDispatch.properties(BlockStateProperties.TRIGGERED, CrafterBlock.CRAFTING)
							.select(false, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
							.select(true, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation4))
							.select(true, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
							.select(false, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
					)
			);
	}

	private void createDispenserBlock(Block block) {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.FURNACE, "_side"))
			.put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front"));
		TextureMapping textureMapping2 = new TextureMapping()
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
			.put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front_vertical"));
		ResourceLocation resourceLocation = ModelTemplates.CUBE_ORIENTABLE.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.CUBE_ORIENTABLE_VERTICAL.create(block, textureMapping2, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block)
					.with(
						PropertyDispatch.property(BlockStateProperties.FACING)
							.select(
								Direction.DOWN, Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
							)
							.select(Direction.UP, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
							.select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
							.select(Direction.EAST, Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
							.select(
								Direction.SOUTH, Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(Direction.WEST, Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
					)
			);
	}

	private void createEndPortalFrame() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.END_PORTAL_FRAME);
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.END_PORTAL_FRAME, "_filled");
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.END_PORTAL_FRAME)
					.with(
						PropertyDispatch.property(BlockStateProperties.EYE)
							.select(false, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
							.select(true, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					)
					.with(createHorizontalFacingDispatchAlt())
			);
	}

	private void createChorusPlant() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_side");
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside");
		ResourceLocation resourceLocation3 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside1");
		ResourceLocation resourceLocation4 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside2");
		ResourceLocation resourceLocation5 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside3");
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.CHORUS_PLANT)
					.with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.with(
						Condition.condition().term(BlockStateProperties.EAST, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.SOUTH, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.WEST, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.UP, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.DOWN, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.NORTH, false),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.WEIGHT, 2),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation4),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation5)
					)
					.with(
						Condition.condition().term(BlockStateProperties.EAST, false),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation4)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation5)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.WEIGHT, 2)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.SOUTH, false),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation4)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation5)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.WEIGHT, 2)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.WEST, false),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation5)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.WEIGHT, 2)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation4)
							.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.UP, false),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.WEIGHT, 2)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation5)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation4)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
							.with(VariantProperties.UV_LOCK, true)
					)
					.with(
						Condition.condition().term(BlockStateProperties.DOWN, false),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation5)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation4)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation3)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true),
						Variant.variant()
							.with(VariantProperties.MODEL, resourceLocation2)
							.with(VariantProperties.WEIGHT, 2)
							.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
							.with(VariantProperties.UV_LOCK, true)
					)
			);
	}

	private void createComposter() {
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.COMPOSTER)
					.with(Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER)))
					.with(
						Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 1),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents1"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 2),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents2"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 3),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents3"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 4),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents4"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 5),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents5"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 6),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents6"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 7),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents7"))
					)
					.with(
						Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 8),
						Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents_ready"))
					)
			);
	}

	private void createCopperBulb(Block block) {
		ResourceLocation resourceLocation = ModelTemplates.CUBE_ALL.create(block, TextureMapping.cube(block), this.modelOutput);
		ResourceLocation resourceLocation2 = this.createSuffixedVariant(block, "_powered", ModelTemplates.CUBE_ALL, TextureMapping::cube);
		ResourceLocation resourceLocation3 = this.createSuffixedVariant(block, "_lit", ModelTemplates.CUBE_ALL, TextureMapping::cube);
		ResourceLocation resourceLocation4 = this.createSuffixedVariant(block, "_lit_powered", ModelTemplates.CUBE_ALL, TextureMapping::cube);
		this.blockStateOutput.accept(this.createCopperBulb(block, resourceLocation, resourceLocation3, resourceLocation2, resourceLocation4));
	}

	private BlockStateGenerator createCopperBulb(
		Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3, ResourceLocation resourceLocation4
	) {
		return MultiVariantGenerator.multiVariant(block)
			.with(
				PropertyDispatch.properties(BlockStateProperties.LIT, BlockStateProperties.POWERED)
					.generate(
						(boolean_, boolean2) -> boolean_
								? Variant.variant().with(VariantProperties.MODEL, boolean2 ? resourceLocation4 : resourceLocation2)
								: Variant.variant().with(VariantProperties.MODEL, boolean2 ? resourceLocation3 : resourceLocation)
					)
			);
	}

	private void copyCopperBulbModel(Block block, Block block2) {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(block);
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(block, "_powered");
		ResourceLocation resourceLocation3 = ModelLocationUtils.getModelLocation(block, "_lit");
		ResourceLocation resourceLocation4 = ModelLocationUtils.getModelLocation(block, "_lit_powered");
		this.delegateItemModel(block2, ModelLocationUtils.getModelLocation(block.asItem()));
		this.blockStateOutput.accept(this.createCopperBulb(block2, resourceLocation, resourceLocation3, resourceLocation2, resourceLocation4));
	}

	private void createAmethystCluster(Block block) {
		this.skipAutoItemBlock(block);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(
						block, Variant.variant().with(VariantProperties.MODEL, ModelTemplates.CROSS.create(block, TextureMapping.cross(block), this.modelOutput))
					)
					.with(this.createColumnWithFacing())
			);
	}

	private void createAmethystClusters() {
		this.createAmethystCluster(Blocks.SMALL_AMETHYST_BUD);
		this.createAmethystCluster(Blocks.MEDIUM_AMETHYST_BUD);
		this.createAmethystCluster(Blocks.LARGE_AMETHYST_BUD);
		this.createAmethystCluster(Blocks.AMETHYST_CLUSTER);
	}

	private void createPointedDripstone() {
		this.skipAutoItemBlock(Blocks.POINTED_DRIPSTONE);
		PropertyDispatch.C2<Direction, DripstoneThickness> c2 = PropertyDispatch.properties(
			BlockStateProperties.VERTICAL_DIRECTION, BlockStateProperties.DRIPSTONE_THICKNESS
		);

		for (DripstoneThickness dripstoneThickness : DripstoneThickness.values()) {
			c2.select(Direction.UP, dripstoneThickness, this.createPointedDripstoneVariant(Direction.UP, dripstoneThickness));
		}

		for (DripstoneThickness dripstoneThickness : DripstoneThickness.values()) {
			c2.select(Direction.DOWN, dripstoneThickness, this.createPointedDripstoneVariant(Direction.DOWN, dripstoneThickness));
		}

		this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(Blocks.POINTED_DRIPSTONE).with(c2));
	}

	private Variant createPointedDripstoneVariant(Direction direction, DripstoneThickness dripstoneThickness) {
		String string = "_" + direction.getSerializedName() + "_" + dripstoneThickness.getSerializedName();
		TextureMapping textureMapping = TextureMapping.cross(TextureMapping.getBlockTexture(Blocks.POINTED_DRIPSTONE, string));
		return Variant.variant()
			.with(VariantProperties.MODEL, ModelTemplates.POINTED_DRIPSTONE.createWithSuffix(Blocks.POINTED_DRIPSTONE, string, textureMapping, this.modelOutput));
	}

	private void createNyliumBlock(Block block) {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.NETHERRACK))
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(block))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side"));
		this.blockStateOutput.accept(createSimpleBlock(block, ModelTemplates.CUBE_BOTTOM_TOP.create(block, textureMapping, this.modelOutput)));
	}

	private void createDaylightDetector() {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_side");
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_top"))
			.put(TextureSlot.SIDE, resourceLocation);
		TextureMapping textureMapping2 = new TextureMapping()
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_inverted_top"))
			.put(TextureSlot.SIDE, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.DAYLIGHT_DETECTOR)
					.with(
						PropertyDispatch.property(BlockStateProperties.INVERTED)
							.select(
								false,
								Variant.variant().with(VariantProperties.MODEL, ModelTemplates.DAYLIGHT_DETECTOR.create(Blocks.DAYLIGHT_DETECTOR, textureMapping, this.modelOutput))
							)
							.select(
								true,
								Variant.variant()
									.with(
										VariantProperties.MODEL,
										ModelTemplates.DAYLIGHT_DETECTOR
											.create(ModelLocationUtils.getModelLocation(Blocks.DAYLIGHT_DETECTOR, "_inverted"), textureMapping2, this.modelOutput)
									)
							)
					)
			);
	}

	private void createRotatableColumn(Block block) {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block)))
					.with(this.createColumnWithFacing())
			);
	}

	private void createLightningRod() {
		Block block = Blocks.LIGHTNING_ROD;
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(block, "_on");
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(block);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block)))
					.with(this.createColumnWithFacing())
					.with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourceLocation, resourceLocation2))
			);
	}

	private void createFarmland() {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FARMLAND));
		TextureMapping textureMapping2 = new TextureMapping()
			.put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FARMLAND, "_moist"));
		ResourceLocation resourceLocation = ModelTemplates.FARMLAND.create(Blocks.FARMLAND, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.FARMLAND
			.create(TextureMapping.getBlockTexture(Blocks.FARMLAND, "_moist"), textureMapping2, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.FARMLAND).with(createEmptyOrFullDispatch(BlockStateProperties.MOISTURE, 7, resourceLocation2, resourceLocation))
			);
	}

	private List<ResourceLocation> createFloorFireModels(Block block) {
		ResourceLocation resourceLocation = ModelTemplates.FIRE_FLOOR
			.create(ModelLocationUtils.getModelLocation(block, "_floor0"), TextureMapping.fire0(block), this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.FIRE_FLOOR
			.create(ModelLocationUtils.getModelLocation(block, "_floor1"), TextureMapping.fire1(block), this.modelOutput);
		return ImmutableList.of(resourceLocation, resourceLocation2);
	}

	private List<ResourceLocation> createSideFireModels(Block block) {
		ResourceLocation resourceLocation = ModelTemplates.FIRE_SIDE
			.create(ModelLocationUtils.getModelLocation(block, "_side0"), TextureMapping.fire0(block), this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.FIRE_SIDE
			.create(ModelLocationUtils.getModelLocation(block, "_side1"), TextureMapping.fire1(block), this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.FIRE_SIDE_ALT
			.create(ModelLocationUtils.getModelLocation(block, "_side_alt0"), TextureMapping.fire0(block), this.modelOutput);
		ResourceLocation resourceLocation4 = ModelTemplates.FIRE_SIDE_ALT
			.create(ModelLocationUtils.getModelLocation(block, "_side_alt1"), TextureMapping.fire1(block), this.modelOutput);
		return ImmutableList.of(resourceLocation, resourceLocation2, resourceLocation3, resourceLocation4);
	}

	private List<ResourceLocation> createTopFireModels(Block block) {
		ResourceLocation resourceLocation = ModelTemplates.FIRE_UP
			.create(ModelLocationUtils.getModelLocation(block, "_up0"), TextureMapping.fire0(block), this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.FIRE_UP
			.create(ModelLocationUtils.getModelLocation(block, "_up1"), TextureMapping.fire1(block), this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.FIRE_UP_ALT
			.create(ModelLocationUtils.getModelLocation(block, "_up_alt0"), TextureMapping.fire0(block), this.modelOutput);
		ResourceLocation resourceLocation4 = ModelTemplates.FIRE_UP_ALT
			.create(ModelLocationUtils.getModelLocation(block, "_up_alt1"), TextureMapping.fire1(block), this.modelOutput);
		return ImmutableList.of(resourceLocation, resourceLocation2, resourceLocation3, resourceLocation4);
	}

	private static List<Variant> wrapModels(List<ResourceLocation> list, UnaryOperator<Variant> unaryOperator) {
		return (List<Variant>)list.stream()
			.map(resourceLocation -> Variant.variant().with(VariantProperties.MODEL, resourceLocation))
			.map(unaryOperator)
			.collect(Collectors.toList());
	}

	private void createFire() {
		Condition condition = Condition.condition()
			.term(BlockStateProperties.NORTH, false)
			.term(BlockStateProperties.EAST, false)
			.term(BlockStateProperties.SOUTH, false)
			.term(BlockStateProperties.WEST, false)
			.term(BlockStateProperties.UP, false);
		List<ResourceLocation> list = this.createFloorFireModels(Blocks.FIRE);
		List<ResourceLocation> list2 = this.createSideFireModels(Blocks.FIRE);
		List<ResourceLocation> list3 = this.createTopFireModels(Blocks.FIRE);
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.FIRE)
					.with(condition, wrapModels(list, variant -> variant))
					.with(Condition.or(Condition.condition().term(BlockStateProperties.NORTH, true), condition), wrapModels(list2, variant -> variant))
					.with(
						Condition.or(Condition.condition().term(BlockStateProperties.EAST, true), condition),
						wrapModels(list2, variant -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
					)
					.with(
						Condition.or(Condition.condition().term(BlockStateProperties.SOUTH, true), condition),
						wrapModels(list2, variant -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
					)
					.with(
						Condition.or(Condition.condition().term(BlockStateProperties.WEST, true), condition),
						wrapModels(list2, variant -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
					)
					.with(Condition.condition().term(BlockStateProperties.UP, true), wrapModels(list3, variant -> variant))
			);
	}

	private void createSoulFire() {
		List<ResourceLocation> list = this.createFloorFireModels(Blocks.SOUL_FIRE);
		List<ResourceLocation> list2 = this.createSideFireModels(Blocks.SOUL_FIRE);
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.SOUL_FIRE)
					.with(wrapModels(list, variant -> variant))
					.with(wrapModels(list2, variant -> variant))
					.with(wrapModels(list2, variant -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)))
					.with(wrapModels(list2, variant -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)))
					.with(wrapModels(list2, variant -> variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)))
			);
	}

	private void createLantern(Block block) {
		ResourceLocation resourceLocation = TexturedModel.LANTERN.create(block, this.modelOutput);
		ResourceLocation resourceLocation2 = TexturedModel.HANGING_LANTERN.create(block, this.modelOutput);
		this.createSimpleFlatItemModel(block.asItem());
		this.blockStateOutput
			.accept(MultiVariantGenerator.multiVariant(block).with(createBooleanModelDispatch(BlockStateProperties.HANGING, resourceLocation2, resourceLocation)));
	}

	private void createMuddyMangroveRoots() {
		TextureMapping textureMapping = TextureMapping.column(
			TextureMapping.getBlockTexture(Blocks.MUDDY_MANGROVE_ROOTS, "_side"), TextureMapping.getBlockTexture(Blocks.MUDDY_MANGROVE_ROOTS, "_top")
		);
		ResourceLocation resourceLocation = ModelTemplates.CUBE_COLUMN.create(Blocks.MUDDY_MANGROVE_ROOTS, textureMapping, this.modelOutput);
		this.blockStateOutput.accept(createAxisAlignedPillarBlock(Blocks.MUDDY_MANGROVE_ROOTS, resourceLocation));
	}

	private void createMangrovePropagule() {
		this.createSimpleFlatItemModel(Items.MANGROVE_PROPAGULE);
		Block block = Blocks.MANGROVE_PROPAGULE;
		PropertyDispatch.C2<Boolean, Integer> c2 = PropertyDispatch.properties(MangrovePropaguleBlock.HANGING, MangrovePropaguleBlock.AGE);
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(block);

		for (int i = 0; i <= 4; i++) {
			ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(block, "_hanging_" + i);
			c2.select(true, i, Variant.variant().with(VariantProperties.MODEL, resourceLocation2));
			c2.select(false, i, Variant.variant().with(VariantProperties.MODEL, resourceLocation));
		}

		this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(Blocks.MANGROVE_PROPAGULE).with(c2));
	}

	private void createFrostedIce() {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.FROSTED_ICE)
					.with(
						PropertyDispatch.property(BlockStateProperties.AGE_3)
							.select(
								0, Variant.variant().with(VariantProperties.MODEL, this.createSuffixedVariant(Blocks.FROSTED_ICE, "_0", ModelTemplates.CUBE_ALL, TextureMapping::cube))
							)
							.select(
								1, Variant.variant().with(VariantProperties.MODEL, this.createSuffixedVariant(Blocks.FROSTED_ICE, "_1", ModelTemplates.CUBE_ALL, TextureMapping::cube))
							)
							.select(
								2, Variant.variant().with(VariantProperties.MODEL, this.createSuffixedVariant(Blocks.FROSTED_ICE, "_2", ModelTemplates.CUBE_ALL, TextureMapping::cube))
							)
							.select(
								3, Variant.variant().with(VariantProperties.MODEL, this.createSuffixedVariant(Blocks.FROSTED_ICE, "_3", ModelTemplates.CUBE_ALL, TextureMapping::cube))
							)
					)
			);
	}

	private void createGrassBlocks() {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.DIRT);
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.BOTTOM, resourceLocation)
			.copyForced(TextureSlot.BOTTOM, TextureSlot.PARTICLE)
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_top"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_snow"));
		Variant variant = Variant.variant()
			.with(VariantProperties.MODEL, ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.GRASS_BLOCK, "_snow", textureMapping, this.modelOutput));
		this.createGrassLikeBlock(Blocks.GRASS_BLOCK, ModelLocationUtils.getModelLocation(Blocks.GRASS_BLOCK), variant);
		ResourceLocation resourceLocation2 = TexturedModel.CUBE_TOP_BOTTOM
			.get(Blocks.MYCELIUM)
			.updateTextures(textureMappingx -> textureMappingx.put(TextureSlot.BOTTOM, resourceLocation))
			.create(Blocks.MYCELIUM, this.modelOutput);
		this.createGrassLikeBlock(Blocks.MYCELIUM, resourceLocation2, variant);
		ResourceLocation resourceLocation3 = TexturedModel.CUBE_TOP_BOTTOM
			.get(Blocks.PODZOL)
			.updateTextures(textureMappingx -> textureMappingx.put(TextureSlot.BOTTOM, resourceLocation))
			.create(Blocks.PODZOL, this.modelOutput);
		this.createGrassLikeBlock(Blocks.PODZOL, resourceLocation3, variant);
	}

	private void createGrassLikeBlock(Block block, ResourceLocation resourceLocation, Variant variant) {
		List<Variant> list = Arrays.asList(createRotatedVariants(resourceLocation));
		this.blockStateOutput
			.accept(MultiVariantGenerator.multiVariant(block).with(PropertyDispatch.property(BlockStateProperties.SNOWY).select(true, variant).select(false, list)));
	}

	private void createCocoa() {
		this.createSimpleFlatItemModel(Items.COCOA_BEANS);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.COCOA)
					.with(
						PropertyDispatch.property(BlockStateProperties.AGE_2)
							.select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage0")))
							.select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage1")))
							.select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage2")))
					)
					.with(createHorizontalFacingDispatchAlt())
			);
	}

	private void createDirtPath() {
		this.blockStateOutput.accept(createRotatedVariant(Blocks.DIRT_PATH, ModelLocationUtils.getModelLocation(Blocks.DIRT_PATH)));
	}

	private void createWeightedPressurePlate(Block block, Block block2) {
		TextureMapping textureMapping = TextureMapping.defaultTexture(block2);
		ResourceLocation resourceLocation = ModelTemplates.PRESSURE_PLATE_UP.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.PRESSURE_PLATE_DOWN.create(block, textureMapping, this.modelOutput);
		this.blockStateOutput
			.accept(MultiVariantGenerator.multiVariant(block).with(createEmptyOrFullDispatch(BlockStateProperties.POWER, 1, resourceLocation2, resourceLocation)));
	}

	private void createHopper() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.HOPPER);
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.HOPPER, "_side");
		this.createSimpleFlatItemModel(Items.HOPPER);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.HOPPER)
					.with(
						PropertyDispatch.property(BlockStateProperties.FACING_HOPPER)
							.select(Direction.DOWN, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
							.select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
							.select(Direction.EAST, Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
							.select(
								Direction.SOUTH, Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								Direction.WEST, Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
					)
			);
	}

	private void copyModel(Block block, Block block2) {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(block);
		this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block2, Variant.variant().with(VariantProperties.MODEL, resourceLocation)));
		this.delegateItemModel(block2, resourceLocation);
	}

	private void createIronBars() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_post_ends");
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_post");
		ResourceLocation resourceLocation3 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_cap");
		ResourceLocation resourceLocation4 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_cap_alt");
		ResourceLocation resourceLocation5 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_side");
		ResourceLocation resourceLocation6 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_side_alt");
		this.blockStateOutput
			.accept(
				MultiPartGenerator.multiPart(Blocks.IRON_BARS)
					.with(Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.with(
						Condition.condition()
							.term(BlockStateProperties.NORTH, false)
							.term(BlockStateProperties.EAST, false)
							.term(BlockStateProperties.SOUTH, false)
							.term(BlockStateProperties.WEST, false),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation2)
					)
					.with(
						Condition.condition()
							.term(BlockStateProperties.NORTH, true)
							.term(BlockStateProperties.EAST, false)
							.term(BlockStateProperties.SOUTH, false)
							.term(BlockStateProperties.WEST, false),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3)
					)
					.with(
						Condition.condition()
							.term(BlockStateProperties.NORTH, false)
							.term(BlockStateProperties.EAST, true)
							.term(BlockStateProperties.SOUTH, false)
							.term(BlockStateProperties.WEST, false),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation3).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.with(
						Condition.condition()
							.term(BlockStateProperties.NORTH, false)
							.term(BlockStateProperties.EAST, false)
							.term(BlockStateProperties.SOUTH, true)
							.term(BlockStateProperties.WEST, false),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation4)
					)
					.with(
						Condition.condition()
							.term(BlockStateProperties.NORTH, false)
							.term(BlockStateProperties.EAST, false)
							.term(BlockStateProperties.SOUTH, false)
							.term(BlockStateProperties.WEST, true),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation4).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.with(Condition.condition().term(BlockStateProperties.NORTH, true), Variant.variant().with(VariantProperties.MODEL, resourceLocation5))
					.with(
						Condition.condition().term(BlockStateProperties.EAST, true),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation5).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
					.with(Condition.condition().term(BlockStateProperties.SOUTH, true), Variant.variant().with(VariantProperties.MODEL, resourceLocation6))
					.with(
						Condition.condition().term(BlockStateProperties.WEST, true),
						Variant.variant().with(VariantProperties.MODEL, resourceLocation6).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
					)
			);
		this.createSimpleFlatItemModel(Blocks.IRON_BARS);
	}

	private void createNonTemplateHorizontalBlock(Block block) {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block)))
					.with(createHorizontalFacingDispatch())
			);
	}

	private void createLever() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.LEVER);
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.LEVER, "_on");
		this.createSimpleFlatItemModel(Blocks.LEVER);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.LEVER)
					.with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourceLocation, resourceLocation2))
					.with(
						PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
							.select(
								AttachFace.CEILING,
								Direction.NORTH,
								Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								AttachFace.CEILING,
								Direction.EAST,
								Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
							.select(
								AttachFace.CEILING,
								Direction.WEST,
								Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
							.select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
							.select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
							.select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
							.select(AttachFace.WALL, Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
							.select(
								AttachFace.WALL,
								Direction.EAST,
								Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								AttachFace.WALL,
								Direction.SOUTH,
								Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								AttachFace.WALL,
								Direction.WEST,
								Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
					)
			);
	}

	private void createLilyPad() {
		this.createSimpleFlatItemModel(Blocks.LILY_PAD);
		this.blockStateOutput.accept(createRotatedVariant(Blocks.LILY_PAD, ModelLocationUtils.getModelLocation(Blocks.LILY_PAD)));
	}

	private void createFrogspawnBlock() {
		this.createSimpleFlatItemModel(Blocks.FROGSPAWN);
		this.blockStateOutput.accept(createSimpleBlock(Blocks.FROGSPAWN, ModelLocationUtils.getModelLocation(Blocks.FROGSPAWN)));
	}

	private void createNetherPortalBlock() {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.NETHER_PORTAL)
					.with(
						PropertyDispatch.property(BlockStateProperties.HORIZONTAL_AXIS)
							.select(Direction.Axis.X, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ns")))
							.select(Direction.Axis.Z, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ew")))
					)
			);
	}

	private void createNetherrack() {
		ResourceLocation resourceLocation = TexturedModel.CUBE.create(Blocks.NETHERRACK, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(
					Blocks.NETHERRACK,
					Variant.variant().with(VariantProperties.MODEL, resourceLocation),
					Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
					Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
					Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
					Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90),
					Variant.variant()
						.with(VariantProperties.MODEL, resourceLocation)
						.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
						.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
					Variant.variant()
						.with(VariantProperties.MODEL, resourceLocation)
						.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
						.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
					Variant.variant()
						.with(VariantProperties.MODEL, resourceLocation)
						.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
						.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
					Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
					Variant.variant()
						.with(VariantProperties.MODEL, resourceLocation)
						.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
						.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
					Variant.variant()
						.with(VariantProperties.MODEL, resourceLocation)
						.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
						.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
					Variant.variant()
						.with(VariantProperties.MODEL, resourceLocation)
						.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
						.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
					Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270),
					Variant.variant()
						.with(VariantProperties.MODEL, resourceLocation)
						.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
						.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
					Variant.variant()
						.with(VariantProperties.MODEL, resourceLocation)
						.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
						.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
					Variant.variant()
						.with(VariantProperties.MODEL, resourceLocation)
						.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
						.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
				)
			);
	}

	private void createObserver() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.OBSERVER);
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.OBSERVER, "_on");
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.OBSERVER)
					.with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourceLocation2, resourceLocation))
					.with(createFacingDispatch())
			);
	}

	private void createPistons() {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.PISTON, "_bottom"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.PISTON, "_top_sticky");
		ResourceLocation resourceLocation2 = TextureMapping.getBlockTexture(Blocks.PISTON, "_top");
		TextureMapping textureMapping2 = textureMapping.copyAndUpdate(TextureSlot.PLATFORM, resourceLocation);
		TextureMapping textureMapping3 = textureMapping.copyAndUpdate(TextureSlot.PLATFORM, resourceLocation2);
		ResourceLocation resourceLocation3 = ModelLocationUtils.getModelLocation(Blocks.PISTON, "_base");
		this.createPistonVariant(Blocks.PISTON, resourceLocation3, textureMapping3);
		this.createPistonVariant(Blocks.STICKY_PISTON, resourceLocation3, textureMapping2);
		ResourceLocation resourceLocation4 = ModelTemplates.CUBE_BOTTOM_TOP
			.createWithSuffix(Blocks.PISTON, "_inventory", textureMapping.copyAndUpdate(TextureSlot.TOP, resourceLocation2), this.modelOutput);
		ResourceLocation resourceLocation5 = ModelTemplates.CUBE_BOTTOM_TOP
			.createWithSuffix(Blocks.STICKY_PISTON, "_inventory", textureMapping.copyAndUpdate(TextureSlot.TOP, resourceLocation), this.modelOutput);
		this.delegateItemModel(Blocks.PISTON, resourceLocation4);
		this.delegateItemModel(Blocks.STICKY_PISTON, resourceLocation5);
	}

	private void createPistonVariant(Block block, ResourceLocation resourceLocation, TextureMapping textureMapping) {
		ResourceLocation resourceLocation2 = ModelTemplates.PISTON.create(block, textureMapping, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block)
					.with(createBooleanModelDispatch(BlockStateProperties.EXTENDED, resourceLocation, resourceLocation2))
					.with(createFacingDispatch())
			);
	}

	private void createPistonHeads() {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.UNSTICKY, TextureMapping.getBlockTexture(Blocks.PISTON, "_top"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
		TextureMapping textureMapping2 = textureMapping.copyAndUpdate(TextureSlot.PLATFORM, TextureMapping.getBlockTexture(Blocks.PISTON, "_top_sticky"));
		TextureMapping textureMapping3 = textureMapping.copyAndUpdate(TextureSlot.PLATFORM, TextureMapping.getBlockTexture(Blocks.PISTON, "_top"));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.PISTON_HEAD)
					.with(
						PropertyDispatch.properties(BlockStateProperties.SHORT, BlockStateProperties.PISTON_TYPE)
							.select(
								false,
								PistonType.DEFAULT,
								Variant.variant().with(VariantProperties.MODEL, ModelTemplates.PISTON_HEAD.createWithSuffix(Blocks.PISTON, "_head", textureMapping3, this.modelOutput))
							)
							.select(
								false,
								PistonType.STICKY,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelTemplates.PISTON_HEAD.createWithSuffix(Blocks.PISTON, "_head_sticky", textureMapping2, this.modelOutput))
							)
							.select(
								true,
								PistonType.DEFAULT,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelTemplates.PISTON_HEAD_SHORT.createWithSuffix(Blocks.PISTON, "_head_short", textureMapping3, this.modelOutput))
							)
							.select(
								true,
								PistonType.STICKY,
								Variant.variant()
									.with(
										VariantProperties.MODEL, ModelTemplates.PISTON_HEAD_SHORT.createWithSuffix(Blocks.PISTON, "_head_short_sticky", textureMapping2, this.modelOutput)
									)
							)
					)
					.with(createFacingDispatch())
			);
	}

	private void createTrialSpawner() {
		Block block = Blocks.TRIAL_SPAWNER;
		TextureMapping textureMapping = TextureMapping.trialSpawner(block, "_side_inactive", "_top_inactive");
		TextureMapping textureMapping2 = TextureMapping.trialSpawner(block, "_side_active", "_top_active");
		TextureMapping textureMapping3 = TextureMapping.trialSpawner(block, "_side_active", "_top_ejecting_reward");
		ResourceLocation resourceLocation = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_active", textureMapping2, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES
			.createWithSuffix(block, "_ejecting_reward", textureMapping3, this.modelOutput);
		this.delegateItemModel(block, resourceLocation);
		this.blockStateOutput
			.accept(MultiVariantGenerator.multiVariant(block).with(PropertyDispatch.property(BlockStateProperties.TRIAL_SPAWNER_STATE).generate(trialSpawnerState -> {
				return switch (trialSpawnerState) {
					case INACTIVE, COOLDOWN -> Variant.variant().with(VariantProperties.MODEL, resourceLocation);
					case WAITING_FOR_PLAYERS, ACTIVE, WAITING_FOR_REWARD_EJECTION -> Variant.variant().with(VariantProperties.MODEL, resourceLocation2);
					case EJECTING_REWARD -> Variant.variant().with(VariantProperties.MODEL, resourceLocation3);
				};
			})));
	}

	private void createVault() {
		Block block = Blocks.VAULT;
		TextureMapping textureMapping = TextureMapping.vault(block, "_front_off", "_side_off", "_top", "_bottom");
		TextureMapping textureMapping2 = TextureMapping.vault(block, "_front_on", "_side_on", "_top", "_bottom");
		TextureMapping textureMapping3 = TextureMapping.vault(block, "_front_ejecting", "_side_on", "_top", "_bottom");
		TextureMapping textureMapping4 = TextureMapping.vault(block, "_front_ejecting", "_side_on", "_top_ejecting", "_bottom");
		ResourceLocation resourceLocation = ModelTemplates.VAULT.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.VAULT.createWithSuffix(block, "_active", textureMapping2, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.VAULT.createWithSuffix(block, "_unlocking", textureMapping3, this.modelOutput);
		ResourceLocation resourceLocation4 = ModelTemplates.VAULT.createWithSuffix(block, "_ejecting_reward", textureMapping4, this.modelOutput);
		this.delegateItemModel(block, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block).with(createHorizontalFacingDispatch()).with(PropertyDispatch.property(VaultBlock.STATE).generate(vaultState -> {
					return switch (vaultState) {
						case INACTIVE -> Variant.variant().with(VariantProperties.MODEL, resourceLocation);
						case ACTIVE -> Variant.variant().with(VariantProperties.MODEL, resourceLocation2);
						case UNLOCKING -> Variant.variant().with(VariantProperties.MODEL, resourceLocation3);
						case EJECTING -> Variant.variant().with(VariantProperties.MODEL, resourceLocation4);
					};
				}))
			);
	}

	private void createSculkSensor() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.SCULK_SENSOR, "_inactive");
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.SCULK_SENSOR, "_active");
		this.delegateItemModel(Blocks.SCULK_SENSOR, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.SCULK_SENSOR)
					.with(
						PropertyDispatch.property(BlockStateProperties.SCULK_SENSOR_PHASE)
							.generate(
								sculkSensorPhase -> Variant.variant()
										.with(
											VariantProperties.MODEL,
											sculkSensorPhase != SculkSensorPhase.ACTIVE && sculkSensorPhase != SculkSensorPhase.COOLDOWN ? resourceLocation : resourceLocation2
										)
							)
					)
			);
	}

	private void createCalibratedSculkSensor() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.CALIBRATED_SCULK_SENSOR, "_inactive");
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.CALIBRATED_SCULK_SENSOR, "_active");
		this.delegateItemModel(Blocks.CALIBRATED_SCULK_SENSOR, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.CALIBRATED_SCULK_SENSOR)
					.with(
						PropertyDispatch.property(BlockStateProperties.SCULK_SENSOR_PHASE)
							.generate(
								sculkSensorPhase -> Variant.variant()
										.with(
											VariantProperties.MODEL,
											sculkSensorPhase != SculkSensorPhase.ACTIVE && sculkSensorPhase != SculkSensorPhase.COOLDOWN ? resourceLocation : resourceLocation2
										)
							)
					)
					.with(createHorizontalFacingDispatch())
			);
	}

	private void createSculkShrieker() {
		ResourceLocation resourceLocation = ModelTemplates.SCULK_SHRIEKER.create(Blocks.SCULK_SHRIEKER, TextureMapping.sculkShrieker(false), this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.SCULK_SHRIEKER
			.createWithSuffix(Blocks.SCULK_SHRIEKER, "_can_summon", TextureMapping.sculkShrieker(true), this.modelOutput);
		this.delegateItemModel(Blocks.SCULK_SHRIEKER, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.SCULK_SHRIEKER)
					.with(createBooleanModelDispatch(BlockStateProperties.CAN_SUMMON, resourceLocation2, resourceLocation))
			);
	}

	private void createScaffolding() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_stable");
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_unstable");
		this.delegateItemModel(Blocks.SCAFFOLDING, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.SCAFFOLDING).with(createBooleanModelDispatch(BlockStateProperties.BOTTOM, resourceLocation2, resourceLocation))
			);
	}

	private void createCaveVines() {
		ResourceLocation resourceLocation = this.createSuffixedVariant(Blocks.CAVE_VINES, "", ModelTemplates.CROSS, TextureMapping::cross);
		ResourceLocation resourceLocation2 = this.createSuffixedVariant(Blocks.CAVE_VINES, "_lit", ModelTemplates.CROSS, TextureMapping::cross);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.CAVE_VINES).with(createBooleanModelDispatch(BlockStateProperties.BERRIES, resourceLocation2, resourceLocation))
			);
		ResourceLocation resourceLocation3 = this.createSuffixedVariant(Blocks.CAVE_VINES_PLANT, "", ModelTemplates.CROSS, TextureMapping::cross);
		ResourceLocation resourceLocation4 = this.createSuffixedVariant(Blocks.CAVE_VINES_PLANT, "_lit", ModelTemplates.CROSS, TextureMapping::cross);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.CAVE_VINES_PLANT)
					.with(createBooleanModelDispatch(BlockStateProperties.BERRIES, resourceLocation4, resourceLocation3))
			);
	}

	private void createRedstoneLamp() {
		ResourceLocation resourceLocation = TexturedModel.CUBE.create(Blocks.REDSTONE_LAMP, this.modelOutput);
		ResourceLocation resourceLocation2 = this.createSuffixedVariant(Blocks.REDSTONE_LAMP, "_on", ModelTemplates.CUBE_ALL, TextureMapping::cube);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.REDSTONE_LAMP).with(createBooleanModelDispatch(BlockStateProperties.LIT, resourceLocation2, resourceLocation))
			);
	}

	private void createNormalTorch(Block block, Block block2) {
		TextureMapping textureMapping = TextureMapping.torch(block);
		this.blockStateOutput.accept(createSimpleBlock(block, ModelTemplates.TORCH.create(block, textureMapping, this.modelOutput)));
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(
						block2, Variant.variant().with(VariantProperties.MODEL, ModelTemplates.WALL_TORCH.create(block2, textureMapping, this.modelOutput))
					)
					.with(createTorchHorizontalDispatch())
			);
		this.createSimpleFlatItemModel(block);
		this.skipAutoItemBlock(block2);
	}

	private void createRedstoneTorch() {
		TextureMapping textureMapping = TextureMapping.torch(Blocks.REDSTONE_TORCH);
		TextureMapping textureMapping2 = TextureMapping.torch(TextureMapping.getBlockTexture(Blocks.REDSTONE_TORCH, "_off"));
		ResourceLocation resourceLocation = ModelTemplates.TORCH.create(Blocks.REDSTONE_TORCH, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.TORCH.createWithSuffix(Blocks.REDSTONE_TORCH, "_off", textureMapping2, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.REDSTONE_TORCH).with(createBooleanModelDispatch(BlockStateProperties.LIT, resourceLocation, resourceLocation2))
			);
		ResourceLocation resourceLocation3 = ModelTemplates.WALL_TORCH.create(Blocks.REDSTONE_WALL_TORCH, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation4 = ModelTemplates.WALL_TORCH.createWithSuffix(Blocks.REDSTONE_WALL_TORCH, "_off", textureMapping2, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.REDSTONE_WALL_TORCH)
					.with(createBooleanModelDispatch(BlockStateProperties.LIT, resourceLocation3, resourceLocation4))
					.with(createTorchHorizontalDispatch())
			);
		this.createSimpleFlatItemModel(Blocks.REDSTONE_TORCH);
		this.skipAutoItemBlock(Blocks.REDSTONE_WALL_TORCH);
	}

	private void createRepeater() {
		this.createSimpleFlatItemModel(Items.REPEATER);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.REPEATER)
					.with(
						PropertyDispatch.properties(BlockStateProperties.DELAY, BlockStateProperties.LOCKED, BlockStateProperties.POWERED)
							.generate((integer, boolean_, boolean2) -> {
								StringBuilder stringBuilder = new StringBuilder();
								stringBuilder.append('_').append(integer).append("tick");
								if (boolean2) {
									stringBuilder.append("_on");
								}

								if (boolean_) {
									stringBuilder.append("_locked");
								}

								return Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.REPEATER, stringBuilder.toString()));
							})
					)
					.with(createHorizontalFacingDispatchAlt())
			);
	}

	private void createSeaPickle() {
		this.createSimpleFlatItemModel(Items.SEA_PICKLE);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.SEA_PICKLE)
					.with(
						PropertyDispatch.properties(BlockStateProperties.PICKLES, BlockStateProperties.WATERLOGGED)
							.select(1, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("dead_sea_pickle"))))
							.select(2, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("two_dead_sea_pickles"))))
							.select(3, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("three_dead_sea_pickles"))))
							.select(4, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("four_dead_sea_pickles"))))
							.select(1, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("sea_pickle"))))
							.select(2, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("two_sea_pickles"))))
							.select(3, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("three_sea_pickles"))))
							.select(4, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("four_sea_pickles"))))
					)
			);
	}

	private void createSnowBlocks() {
		TextureMapping textureMapping = TextureMapping.cube(Blocks.SNOW);
		ResourceLocation resourceLocation = ModelTemplates.CUBE_ALL.create(Blocks.SNOW_BLOCK, textureMapping, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.SNOW)
					.with(
						PropertyDispatch.property(BlockStateProperties.LAYERS)
							.generate(
								integer -> Variant.variant()
										.with(VariantProperties.MODEL, integer < 8 ? ModelLocationUtils.getModelLocation(Blocks.SNOW, "_height" + integer * 2) : resourceLocation)
							)
					)
			);
		this.delegateItemModel(Blocks.SNOW, ModelLocationUtils.getModelLocation(Blocks.SNOW, "_height2"));
		this.blockStateOutput.accept(createSimpleBlock(Blocks.SNOW_BLOCK, resourceLocation));
	}

	private void createStonecutter() {
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(
						Blocks.STONECUTTER, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.STONECUTTER))
					)
					.with(createHorizontalFacingDispatch())
			);
	}

	private void createStructureBlock() {
		ResourceLocation resourceLocation = TexturedModel.CUBE.create(Blocks.STRUCTURE_BLOCK, this.modelOutput);
		this.delegateItemModel(Blocks.STRUCTURE_BLOCK, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.STRUCTURE_BLOCK)
					.with(
						PropertyDispatch.property(BlockStateProperties.STRUCTUREBLOCK_MODE)
							.generate(
								structureMode -> Variant.variant()
										.with(
											VariantProperties.MODEL,
											this.createSuffixedVariant(Blocks.STRUCTURE_BLOCK, "_" + structureMode.getSerializedName(), ModelTemplates.CUBE_ALL, TextureMapping::cube)
										)
							)
					)
			);
	}

	private void createSweetBerryBush() {
		this.createSimpleFlatItemModel(Items.SWEET_BERRIES);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.SWEET_BERRY_BUSH)
					.with(
						PropertyDispatch.property(BlockStateProperties.AGE_3)
							.generate(
								integer -> Variant.variant()
										.with(VariantProperties.MODEL, this.createSuffixedVariant(Blocks.SWEET_BERRY_BUSH, "_stage" + integer, ModelTemplates.CROSS, TextureMapping::cross))
							)
					)
			);
	}

	private void createTripwire() {
		this.createSimpleFlatItemModel(Items.STRING);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.TRIPWIRE)
					.with(
						PropertyDispatch.properties(
								BlockStateProperties.ATTACHED, BlockStateProperties.EAST, BlockStateProperties.NORTH, BlockStateProperties.SOUTH, BlockStateProperties.WEST
							)
							.select(false, false, false, false, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns")))
							.select(
								false,
								true,
								false,
								false,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(false, false, true, false, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n")))
							.select(
								false,
								false,
								false,
								true,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								false,
								false,
								false,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(false, true, true, false, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne")))
							.select(
								false,
								true,
								false,
								true,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								false,
								false,
								false,
								true,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								false,
								false,
								true,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(false, false, true, true, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns")))
							.select(
								false,
								true,
								false,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(false, true, true, true, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse")))
							.select(
								false,
								true,
								false,
								true,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								false,
								false,
								true,
								true,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								false,
								true,
								true,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(false, true, true, true, true, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nsew")))
							.select(
								true, false, false, false, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
							)
							.select(
								true, false, true, false, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
							)
							.select(
								true,
								false,
								false,
								true,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								true,
								true,
								false,
								false,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								true,
								false,
								false,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(
								true, true, true, false, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
							)
							.select(
								true,
								true,
								false,
								true,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								true,
								false,
								false,
								true,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								true,
								false,
								true,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(
								true, false, true, true, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
							)
							.select(
								true,
								true,
								false,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								true, true, true, true, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
							)
							.select(
								true,
								true,
								false,
								true,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								true,
								false,
								true,
								true,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								true,
								true,
								true,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(
								true, true, true, true, true, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nsew"))
							)
					)
			);
	}

	private void createTripwireHook() {
		this.createSimpleFlatItemModel(Blocks.TRIPWIRE_HOOK);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.TRIPWIRE_HOOK)
					.with(
						PropertyDispatch.properties(BlockStateProperties.ATTACHED, BlockStateProperties.POWERED)
							.generate(
								(boolean_, boolean2) -> Variant.variant()
										.with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.TRIPWIRE_HOOK, (boolean_ ? "_attached" : "") + (boolean2 ? "_on" : "")))
							)
					)
					.with(createHorizontalFacingDispatch())
			);
	}

	private ResourceLocation createTurtleEggModel(int i, String string, TextureMapping textureMapping) {
		switch (i) {
			case 1:
				return ModelTemplates.TURTLE_EGG.create(ModelLocationUtils.decorateBlockModelLocation(string + "turtle_egg"), textureMapping, this.modelOutput);
			case 2:
				return ModelTemplates.TWO_TURTLE_EGGS
					.create(ModelLocationUtils.decorateBlockModelLocation("two_" + string + "turtle_eggs"), textureMapping, this.modelOutput);
			case 3:
				return ModelTemplates.THREE_TURTLE_EGGS
					.create(ModelLocationUtils.decorateBlockModelLocation("three_" + string + "turtle_eggs"), textureMapping, this.modelOutput);
			case 4:
				return ModelTemplates.FOUR_TURTLE_EGGS
					.create(ModelLocationUtils.decorateBlockModelLocation("four_" + string + "turtle_eggs"), textureMapping, this.modelOutput);
			default:
				throw new UnsupportedOperationException();
		}
	}

	private ResourceLocation createTurtleEggModel(Integer integer, Integer integer2) {
		switch (integer2) {
			case 0:
				return this.createTurtleEggModel(integer, "", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG)));
			case 1:
				return this.createTurtleEggModel(integer, "slightly_cracked_", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG, "_slightly_cracked")));
			case 2:
				return this.createTurtleEggModel(integer, "very_cracked_", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG, "_very_cracked")));
			default:
				throw new UnsupportedOperationException();
		}
	}

	private void createTurtleEgg() {
		this.createSimpleFlatItemModel(Items.TURTLE_EGG);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.TURTLE_EGG)
					.with(
						PropertyDispatch.properties(BlockStateProperties.EGGS, BlockStateProperties.HATCH)
							.generateList((integer, integer2) -> Arrays.asList(createRotatedVariants(this.createTurtleEggModel(integer, integer2))))
					)
			);
	}

	private void createSnifferEgg() {
		this.createSimpleFlatItemModel(Items.SNIFFER_EGG);
		Function<Integer, ResourceLocation> function = integer -> {
			String string = switch (integer) {
				case 1 -> "_slightly_cracked";
				case 2 -> "_very_cracked";
				default -> "_not_cracked";
			};
			TextureMapping textureMapping = TextureMapping.snifferEgg(string);
			return ModelTemplates.SNIFFER_EGG.createWithSuffix(Blocks.SNIFFER_EGG, string, textureMapping, this.modelOutput);
		};
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.SNIFFER_EGG)
					.with(
						PropertyDispatch.property(SnifferEggBlock.HATCH)
							.generate(integer -> Variant.variant().with(VariantProperties.MODEL, (ResourceLocation)function.apply(integer)))
					)
			);
	}

	private void createMultiface(Block block) {
		this.createSimpleFlatItemModel(block);
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(block);
		MultiPartGenerator multiPartGenerator = MultiPartGenerator.multiPart(block);
		Condition.TerminalCondition terminalCondition = Util.make(
			Condition.condition(), terminalConditionx -> MULTIFACE_GENERATOR.stream().map(Pair::getFirst).forEach(booleanPropertyx -> {
					if (block.defaultBlockState().hasProperty(booleanPropertyx)) {
						terminalConditionx.term(booleanPropertyx, false);
					}
				})
		);

		for (Pair<BooleanProperty, Function<ResourceLocation, Variant>> pair : MULTIFACE_GENERATOR) {
			BooleanProperty booleanProperty = pair.getFirst();
			Function<ResourceLocation, Variant> function = pair.getSecond();
			if (block.defaultBlockState().hasProperty(booleanProperty)) {
				multiPartGenerator.with(Condition.condition().term(booleanProperty, true), (Variant)function.apply(resourceLocation));
				multiPartGenerator.with(terminalCondition, (Variant)function.apply(resourceLocation));
			}
		}

		this.blockStateOutput.accept(multiPartGenerator);
	}

	private void createSculkCatalyst() {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_bottom");
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.BOTTOM, resourceLocation)
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_top"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_side"));
		TextureMapping textureMapping2 = new TextureMapping()
			.put(TextureSlot.BOTTOM, resourceLocation)
			.put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_top_bloom"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_side_bloom"));
		ResourceLocation resourceLocation2 = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.SCULK_CATALYST, "", textureMapping, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.SCULK_CATALYST, "_bloom", textureMapping2, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.SCULK_CATALYST)
					.with(
						PropertyDispatch.property(BlockStateProperties.BLOOM)
							.generate(boolean_ -> Variant.variant().with(VariantProperties.MODEL, boolean_ ? resourceLocation3 : resourceLocation2))
					)
			);
		this.delegateItemModel(Items.SCULK_CATALYST, resourceLocation2);
	}

	private void createChiseledBookshelf() {
		Block block = Blocks.CHISELED_BOOKSHELF;
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(block);
		MultiPartGenerator multiPartGenerator = MultiPartGenerator.multiPart(block);
		List.of(
				Pair.of(Direction.NORTH, VariantProperties.Rotation.R0),
				Pair.of(Direction.EAST, VariantProperties.Rotation.R90),
				Pair.of(Direction.SOUTH, VariantProperties.Rotation.R180),
				Pair.of(Direction.WEST, VariantProperties.Rotation.R270)
			)
			.forEach(
				pair -> {
					Direction direction = (Direction)pair.getFirst();
					VariantProperties.Rotation rotation = (VariantProperties.Rotation)pair.getSecond();
					Condition.TerminalCondition terminalCondition = Condition.condition().term(BlockStateProperties.HORIZONTAL_FACING, direction);
					multiPartGenerator.with(
						terminalCondition,
						Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, rotation).with(VariantProperties.UV_LOCK, true)
					);
					this.addSlotStateAndRotationVariants(multiPartGenerator, terminalCondition, rotation);
				}
			);
		this.blockStateOutput.accept(multiPartGenerator);
		this.delegateItemModel(block, ModelLocationUtils.getModelLocation(block, "_inventory"));
		CHISELED_BOOKSHELF_SLOT_MODEL_CACHE.clear();
	}

	private void addSlotStateAndRotationVariants(
		MultiPartGenerator multiPartGenerator, Condition.TerminalCondition terminalCondition, VariantProperties.Rotation rotation
	) {
		List.of(
				Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_LEFT),
				Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_MID),
				Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_RIGHT),
				Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_LEFT),
				Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_MID),
				Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_RIGHT)
			)
			.forEach(pair -> {
				BooleanProperty booleanProperty = (BooleanProperty)pair.getFirst();
				ModelTemplate modelTemplate = (ModelTemplate)pair.getSecond();
				this.addBookSlotModel(multiPartGenerator, terminalCondition, rotation, booleanProperty, modelTemplate, true);
				this.addBookSlotModel(multiPartGenerator, terminalCondition, rotation, booleanProperty, modelTemplate, false);
			});
	}

	private void addBookSlotModel(
		MultiPartGenerator multiPartGenerator,
		Condition.TerminalCondition terminalCondition,
		VariantProperties.Rotation rotation,
		BooleanProperty booleanProperty,
		ModelTemplate modelTemplate,
		boolean bl
	) {
		String string = bl ? "_occupied" : "_empty";
		TextureMapping textureMapping = new TextureMapping().put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(Blocks.CHISELED_BOOKSHELF, string));
		BlockModelGenerators.BookSlotModelCacheKey bookSlotModelCacheKey = new BlockModelGenerators.BookSlotModelCacheKey(modelTemplate, string);
		ResourceLocation resourceLocation = (ResourceLocation)CHISELED_BOOKSHELF_SLOT_MODEL_CACHE.computeIfAbsent(
			bookSlotModelCacheKey, bookSlotModelCacheKeyx -> modelTemplate.createWithSuffix(Blocks.CHISELED_BOOKSHELF, string, textureMapping, this.modelOutput)
		);
		multiPartGenerator.with(
			Condition.and(terminalCondition, Condition.condition().term(booleanProperty, bl)),
			Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, rotation)
		);
	}

	private void createMagmaBlock() {
		this.blockStateOutput
			.accept(
				createSimpleBlock(
					Blocks.MAGMA_BLOCK,
					ModelTemplates.CUBE_ALL.create(Blocks.MAGMA_BLOCK, TextureMapping.cube(ModelLocationUtils.decorateBlockModelLocation("magma")), this.modelOutput)
				)
			);
	}

	private void createShulkerBox(Block block) {
		this.createTrivialBlock(block, TexturedModel.PARTICLE_ONLY);
		ModelTemplates.SHULKER_BOX_INVENTORY.create(ModelLocationUtils.getModelLocation(block.asItem()), TextureMapping.particle(block), this.modelOutput);
	}

	private void createGrowingPlant(Block block, Block block2, BlockModelGenerators.TintState tintState) {
		this.createCrossBlock(block, tintState);
		this.createCrossBlock(block2, tintState);
	}

	private void createBedItem(Block block, Block block2) {
		ModelTemplates.BED_INVENTORY.create(ModelLocationUtils.getModelLocation(block.asItem()), TextureMapping.particle(block2), this.modelOutput);
	}

	private void createInfestedStone() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.STONE);
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.STONE, "_mirrored");
		this.blockStateOutput.accept(createRotatedVariant(Blocks.INFESTED_STONE, resourceLocation, resourceLocation2));
		this.delegateItemModel(Blocks.INFESTED_STONE, resourceLocation);
	}

	private void createInfestedDeepslate() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.DEEPSLATE);
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.DEEPSLATE, "_mirrored");
		this.blockStateOutput.accept(createRotatedVariant(Blocks.INFESTED_DEEPSLATE, resourceLocation, resourceLocation2).with(createRotatedPillar()));
		this.delegateItemModel(Blocks.INFESTED_DEEPSLATE, resourceLocation);
	}

	private void createNetherRoots(Block block, Block block2) {
		this.createCrossBlockWithDefaultItem(block, BlockModelGenerators.TintState.NOT_TINTED);
		TextureMapping textureMapping = TextureMapping.plant(TextureMapping.getBlockTexture(block, "_pot"));
		ResourceLocation resourceLocation = BlockModelGenerators.TintState.NOT_TINTED.getCrossPot().create(block2, textureMapping, this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block2, resourceLocation));
	}

	private void createRespawnAnchor() {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_bottom");
		ResourceLocation resourceLocation2 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_top_off");
		ResourceLocation resourceLocation3 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_top");
		ResourceLocation[] resourceLocations = new ResourceLocation[5];

		for (int i = 0; i < 5; i++) {
			TextureMapping textureMapping = new TextureMapping()
				.put(TextureSlot.BOTTOM, resourceLocation)
				.put(TextureSlot.TOP, i == 0 ? resourceLocation2 : resourceLocation3)
				.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_side" + i));
			resourceLocations[i] = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.RESPAWN_ANCHOR, "_" + i, textureMapping, this.modelOutput);
		}

		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.RESPAWN_ANCHOR)
					.with(
						PropertyDispatch.property(BlockStateProperties.RESPAWN_ANCHOR_CHARGES)
							.generate(integer -> Variant.variant().with(VariantProperties.MODEL, resourceLocations[integer]))
					)
			);
		this.delegateItemModel(Items.RESPAWN_ANCHOR, resourceLocations[0]);
	}

	private Variant applyRotation(FrontAndTop frontAndTop, Variant variant) {
		switch (frontAndTop) {
			case DOWN_NORTH:
				return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
			case DOWN_SOUTH:
				return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
			case DOWN_WEST:
				return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
			case DOWN_EAST:
				return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
			case UP_NORTH:
				return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
			case UP_SOUTH:
				return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270);
			case UP_WEST:
				return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
			case UP_EAST:
				return variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
			case NORTH_UP:
				return variant;
			case SOUTH_UP:
				return variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
			case WEST_UP:
				return variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
			case EAST_UP:
				return variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
			default:
				throw new UnsupportedOperationException("Rotation " + frontAndTop + " can't be expressed with existing x and y values");
		}
	}

	private void createJigsaw() {
		ResourceLocation resourceLocation = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_top");
		ResourceLocation resourceLocation2 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_bottom");
		ResourceLocation resourceLocation3 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_side");
		ResourceLocation resourceLocation4 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_lock");
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.DOWN, resourceLocation3)
			.put(TextureSlot.WEST, resourceLocation3)
			.put(TextureSlot.EAST, resourceLocation3)
			.put(TextureSlot.PARTICLE, resourceLocation)
			.put(TextureSlot.NORTH, resourceLocation)
			.put(TextureSlot.SOUTH, resourceLocation2)
			.put(TextureSlot.UP, resourceLocation4);
		ResourceLocation resourceLocation5 = ModelTemplates.CUBE_DIRECTIONAL.create(Blocks.JIGSAW, textureMapping, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.JIGSAW, Variant.variant().with(VariantProperties.MODEL, resourceLocation5))
					.with(PropertyDispatch.property(BlockStateProperties.ORIENTATION).generate(frontAndTop -> this.applyRotation(frontAndTop, Variant.variant())))
			);
	}

	private void createPetrifiedOakSlab() {
		Block block = Blocks.OAK_PLANKS;
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(block);
		TexturedModel texturedModel = TexturedModel.CUBE.get(block);
		Block block2 = Blocks.PETRIFIED_OAK_SLAB;
		ResourceLocation resourceLocation2 = ModelTemplates.SLAB_BOTTOM.create(block2, texturedModel.getMapping(), this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.SLAB_TOP.create(block2, texturedModel.getMapping(), this.modelOutput);
		this.blockStateOutput.accept(createSlab(block2, resourceLocation2, resourceLocation3, resourceLocation));
	}

	public void run() {
		BlockFamilies.getAllFamilies()
			.filter(BlockFamily::shouldGenerateModel)
			.forEach(blockFamily -> this.family(blockFamily.getBaseBlock()).generateFor(blockFamily));
		this.family(Blocks.CUT_COPPER)
			.generateFor(BlockFamilies.CUT_COPPER)
			.donateModelTo(Blocks.CUT_COPPER, Blocks.WAXED_CUT_COPPER)
			.donateModelTo(Blocks.CHISELED_COPPER, Blocks.WAXED_CHISELED_COPPER)
			.generateFor(BlockFamilies.WAXED_CUT_COPPER);
		this.family(Blocks.EXPOSED_CUT_COPPER)
			.generateFor(BlockFamilies.EXPOSED_CUT_COPPER)
			.donateModelTo(Blocks.EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER)
			.donateModelTo(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_CHISELED_COPPER)
			.generateFor(BlockFamilies.WAXED_EXPOSED_CUT_COPPER);
		this.family(Blocks.WEATHERED_CUT_COPPER)
			.generateFor(BlockFamilies.WEATHERED_CUT_COPPER)
			.donateModelTo(Blocks.WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER)
			.donateModelTo(Blocks.WEATHERED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_CHISELED_COPPER)
			.generateFor(BlockFamilies.WAXED_WEATHERED_CUT_COPPER);
		this.family(Blocks.OXIDIZED_CUT_COPPER)
			.generateFor(BlockFamilies.OXIDIZED_CUT_COPPER)
			.donateModelTo(Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER)
			.donateModelTo(Blocks.OXIDIZED_CHISELED_COPPER, Blocks.WAXED_OXIDIZED_CHISELED_COPPER)
			.generateFor(BlockFamilies.WAXED_OXIDIZED_CUT_COPPER);
		this.createCopperBulb(Blocks.COPPER_BULB);
		this.createCopperBulb(Blocks.EXPOSED_COPPER_BULB);
		this.createCopperBulb(Blocks.WEATHERED_COPPER_BULB);
		this.createCopperBulb(Blocks.OXIDIZED_COPPER_BULB);
		this.copyCopperBulbModel(Blocks.COPPER_BULB, Blocks.WAXED_COPPER_BULB);
		this.copyCopperBulbModel(Blocks.EXPOSED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_BULB);
		this.copyCopperBulbModel(Blocks.WEATHERED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_BULB);
		this.copyCopperBulbModel(Blocks.OXIDIZED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_BULB);
		this.createNonTemplateModelBlock(Blocks.AIR);
		this.createNonTemplateModelBlock(Blocks.CAVE_AIR, Blocks.AIR);
		this.createNonTemplateModelBlock(Blocks.VOID_AIR, Blocks.AIR);
		this.createNonTemplateModelBlock(Blocks.BEACON);
		this.createNonTemplateModelBlock(Blocks.CACTUS);
		this.createNonTemplateModelBlock(Blocks.BUBBLE_COLUMN, Blocks.WATER);
		this.createNonTemplateModelBlock(Blocks.DRAGON_EGG);
		this.createNonTemplateModelBlock(Blocks.DRIED_KELP_BLOCK);
		this.createNonTemplateModelBlock(Blocks.ENCHANTING_TABLE);
		this.createNonTemplateModelBlock(Blocks.FLOWER_POT);
		this.createSimpleFlatItemModel(Items.FLOWER_POT);
		this.createNonTemplateModelBlock(Blocks.HONEY_BLOCK);
		this.createNonTemplateModelBlock(Blocks.WATER);
		this.createNonTemplateModelBlock(Blocks.LAVA);
		this.createNonTemplateModelBlock(Blocks.SLIME_BLOCK);
		this.createSimpleFlatItemModel(Items.CHAIN);
		this.createCandleAndCandleCake(Blocks.WHITE_CANDLE, Blocks.WHITE_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.ORANGE_CANDLE, Blocks.ORANGE_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.MAGENTA_CANDLE, Blocks.MAGENTA_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.LIGHT_BLUE_CANDLE, Blocks.LIGHT_BLUE_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.YELLOW_CANDLE, Blocks.YELLOW_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.LIME_CANDLE, Blocks.LIME_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.PINK_CANDLE, Blocks.PINK_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.GRAY_CANDLE, Blocks.GRAY_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.LIGHT_GRAY_CANDLE, Blocks.LIGHT_GRAY_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.CYAN_CANDLE, Blocks.CYAN_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.PURPLE_CANDLE, Blocks.PURPLE_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.BLUE_CANDLE, Blocks.BLUE_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.BROWN_CANDLE, Blocks.BROWN_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.GREEN_CANDLE, Blocks.GREEN_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.RED_CANDLE, Blocks.RED_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.BLACK_CANDLE, Blocks.BLACK_CANDLE_CAKE);
		this.createCandleAndCandleCake(Blocks.CANDLE, Blocks.CANDLE_CAKE);
		this.createNonTemplateModelBlock(Blocks.POTTED_BAMBOO);
		this.createNonTemplateModelBlock(Blocks.POTTED_CACTUS);
		this.createNonTemplateModelBlock(Blocks.POWDER_SNOW);
		this.createNonTemplateModelBlock(Blocks.SPORE_BLOSSOM);
		this.createAzalea(Blocks.AZALEA);
		this.createAzalea(Blocks.FLOWERING_AZALEA);
		this.createPottedAzalea(Blocks.POTTED_AZALEA);
		this.createPottedAzalea(Blocks.POTTED_FLOWERING_AZALEA);
		this.createCaveVines();
		this.createFullAndCarpetBlocks(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET);
		this.createFlowerBed(Blocks.PINK_PETALS);
		this.createAirLikeBlock(Blocks.BARRIER, Items.BARRIER);
		this.createSimpleFlatItemModel(Items.BARRIER);
		this.createLightBlock();
		this.createAirLikeBlock(Blocks.STRUCTURE_VOID, Items.STRUCTURE_VOID);
		this.createSimpleFlatItemModel(Items.STRUCTURE_VOID);
		this.createAirLikeBlock(Blocks.MOVING_PISTON, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
		this.createTrivialCube(Blocks.COAL_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_COAL_ORE);
		this.createTrivialCube(Blocks.COAL_BLOCK);
		this.createTrivialCube(Blocks.DIAMOND_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_DIAMOND_ORE);
		this.createTrivialCube(Blocks.DIAMOND_BLOCK);
		this.createTrivialCube(Blocks.EMERALD_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_EMERALD_ORE);
		this.createTrivialCube(Blocks.EMERALD_BLOCK);
		this.createTrivialCube(Blocks.GOLD_ORE);
		this.createTrivialCube(Blocks.NETHER_GOLD_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_GOLD_ORE);
		this.createTrivialCube(Blocks.GOLD_BLOCK);
		this.createTrivialCube(Blocks.IRON_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_IRON_ORE);
		this.createTrivialCube(Blocks.IRON_BLOCK);
		this.createTrivialBlock(Blocks.ANCIENT_DEBRIS, TexturedModel.COLUMN);
		this.createTrivialCube(Blocks.NETHERITE_BLOCK);
		this.createTrivialCube(Blocks.LAPIS_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_LAPIS_ORE);
		this.createTrivialCube(Blocks.LAPIS_BLOCK);
		this.createTrivialCube(Blocks.NETHER_QUARTZ_ORE);
		this.createTrivialCube(Blocks.REDSTONE_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_REDSTONE_ORE);
		this.createTrivialCube(Blocks.REDSTONE_BLOCK);
		this.createTrivialCube(Blocks.GILDED_BLACKSTONE);
		this.createTrivialCube(Blocks.BLUE_ICE);
		this.createTrivialCube(Blocks.CLAY);
		this.createTrivialCube(Blocks.COARSE_DIRT);
		this.createTrivialCube(Blocks.CRYING_OBSIDIAN);
		this.createTrivialCube(Blocks.END_STONE);
		this.createTrivialCube(Blocks.GLOWSTONE);
		this.createTrivialCube(Blocks.GRAVEL);
		this.createTrivialCube(Blocks.HONEYCOMB_BLOCK);
		this.createTrivialCube(Blocks.ICE);
		this.createTrivialBlock(Blocks.JUKEBOX, TexturedModel.CUBE_TOP);
		this.createTrivialBlock(Blocks.LODESTONE, TexturedModel.COLUMN);
		this.createTrivialBlock(Blocks.MELON, TexturedModel.COLUMN);
		this.createNonTemplateModelBlock(Blocks.MANGROVE_ROOTS);
		this.createNonTemplateModelBlock(Blocks.POTTED_MANGROVE_PROPAGULE);
		this.createTrivialCube(Blocks.NETHER_WART_BLOCK);
		this.createTrivialCube(Blocks.NOTE_BLOCK);
		this.createTrivialCube(Blocks.PACKED_ICE);
		this.createTrivialCube(Blocks.OBSIDIAN);
		this.createTrivialCube(Blocks.QUARTZ_BRICKS);
		this.createTrivialCube(Blocks.SEA_LANTERN);
		this.createTrivialCube(Blocks.SHROOMLIGHT);
		this.createTrivialCube(Blocks.SOUL_SAND);
		this.createTrivialCube(Blocks.SOUL_SOIL);
		this.createTrivialBlock(Blocks.SPAWNER, TexturedModel.CUBE_INNER_FACES);
		this.createTrivialCube(Blocks.SPONGE);
		this.createTrivialBlock(Blocks.SEAGRASS, TexturedModel.SEAGRASS);
		this.createSimpleFlatItemModel(Items.SEAGRASS);
		this.createTrivialBlock(Blocks.TNT, TexturedModel.CUBE_TOP_BOTTOM);
		this.createTrivialBlock(Blocks.TARGET, TexturedModel.COLUMN);
		this.createTrivialCube(Blocks.WARPED_WART_BLOCK);
		this.createTrivialCube(Blocks.WET_SPONGE);
		this.createTrivialCube(Blocks.AMETHYST_BLOCK);
		this.createTrivialCube(Blocks.BUDDING_AMETHYST);
		this.createTrivialCube(Blocks.CALCITE);
		this.createTrivialCube(Blocks.DRIPSTONE_BLOCK);
		this.createTrivialCube(Blocks.RAW_IRON_BLOCK);
		this.createTrivialCube(Blocks.RAW_COPPER_BLOCK);
		this.createTrivialCube(Blocks.RAW_GOLD_BLOCK);
		this.createRotatedMirroredVariantBlock(Blocks.SCULK);
		this.createPetrifiedOakSlab();
		this.createTrivialCube(Blocks.COPPER_ORE);
		this.createTrivialCube(Blocks.DEEPSLATE_COPPER_ORE);
		this.createTrivialCube(Blocks.COPPER_BLOCK);
		this.createTrivialCube(Blocks.EXPOSED_COPPER);
		this.createTrivialCube(Blocks.WEATHERED_COPPER);
		this.createTrivialCube(Blocks.OXIDIZED_COPPER);
		this.copyModel(Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK);
		this.copyModel(Blocks.EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER);
		this.copyModel(Blocks.WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER);
		this.copyModel(Blocks.OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER);
		this.createDoor(Blocks.COPPER_DOOR);
		this.createDoor(Blocks.EXPOSED_COPPER_DOOR);
		this.createDoor(Blocks.WEATHERED_COPPER_DOOR);
		this.createDoor(Blocks.OXIDIZED_COPPER_DOOR);
		this.copyDoorModel(Blocks.COPPER_DOOR, Blocks.WAXED_COPPER_DOOR);
		this.copyDoorModel(Blocks.EXPOSED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_DOOR);
		this.copyDoorModel(Blocks.WEATHERED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR);
		this.copyDoorModel(Blocks.OXIDIZED_COPPER_DOOR, Blocks.WAXED_OXIDIZED_COPPER_DOOR);
		this.createTrapdoor(Blocks.COPPER_TRAPDOOR);
		this.createTrapdoor(Blocks.EXPOSED_COPPER_TRAPDOOR);
		this.createTrapdoor(Blocks.WEATHERED_COPPER_TRAPDOOR);
		this.createTrapdoor(Blocks.OXIDIZED_COPPER_TRAPDOOR);
		this.copyTrapdoorModel(Blocks.COPPER_TRAPDOOR, Blocks.WAXED_COPPER_TRAPDOOR);
		this.copyTrapdoorModel(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR);
		this.copyTrapdoorModel(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR);
		this.copyTrapdoorModel(Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR);
		this.createTrivialCube(Blocks.COPPER_GRATE);
		this.createTrivialCube(Blocks.EXPOSED_COPPER_GRATE);
		this.createTrivialCube(Blocks.WEATHERED_COPPER_GRATE);
		this.createTrivialCube(Blocks.OXIDIZED_COPPER_GRATE);
		this.copyModel(Blocks.COPPER_GRATE, Blocks.WAXED_COPPER_GRATE);
		this.copyModel(Blocks.EXPOSED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER_GRATE);
		this.copyModel(Blocks.WEATHERED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER_GRATE);
		this.copyModel(Blocks.OXIDIZED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER_GRATE);
		this.createWeightedPressurePlate(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.GOLD_BLOCK);
		this.createWeightedPressurePlate(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.IRON_BLOCK);
		this.createAmethystClusters();
		this.createBookshelf();
		this.createChiseledBookshelf();
		this.createBrewingStand();
		this.createCakeBlock();
		this.createCampfires(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
		this.createCartographyTable();
		this.createCauldrons();
		this.createChorusFlower();
		this.createChorusPlant();
		this.createComposter();
		this.createDaylightDetector();
		this.createEndPortalFrame();
		this.createRotatableColumn(Blocks.END_ROD);
		this.createLightningRod();
		this.createFarmland();
		this.createFire();
		this.createSoulFire();
		this.createFrostedIce();
		this.createGrassBlocks();
		this.createCocoa();
		this.createDirtPath();
		this.createGrindstone();
		this.createHopper();
		this.createIronBars();
		this.createLever();
		this.createLilyPad();
		this.createNetherPortalBlock();
		this.createNetherrack();
		this.createObserver();
		this.createPistons();
		this.createPistonHeads();
		this.createScaffolding();
		this.createRedstoneTorch();
		this.createRedstoneLamp();
		this.createRepeater();
		this.createSeaPickle();
		this.createSmithingTable();
		this.createSnowBlocks();
		this.createStonecutter();
		this.createStructureBlock();
		this.createSweetBerryBush();
		this.createTripwire();
		this.createTripwireHook();
		this.createTurtleEgg();
		this.createSnifferEgg();
		this.createMultiface(Blocks.VINE);
		this.createMultiface(Blocks.GLOW_LICHEN);
		this.createMultiface(Blocks.SCULK_VEIN);
		this.createMagmaBlock();
		this.createJigsaw();
		this.createSculkSensor();
		this.createCalibratedSculkSensor();
		this.createSculkShrieker();
		this.createFrogspawnBlock();
		this.createMangrovePropagule();
		this.createMuddyMangroveRoots();
		this.createTrialSpawner();
		this.createVault();
		this.createNonTemplateHorizontalBlock(Blocks.LADDER);
		this.createSimpleFlatItemModel(Blocks.LADDER);
		this.createNonTemplateHorizontalBlock(Blocks.LECTERN);
		this.createBigDripLeafBlock();
		this.createNonTemplateHorizontalBlock(Blocks.BIG_DRIPLEAF_STEM);
		this.createNormalTorch(Blocks.TORCH, Blocks.WALL_TORCH);
		this.createNormalTorch(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH);
		this.createCraftingTableLike(Blocks.CRAFTING_TABLE, Blocks.OAK_PLANKS, TextureMapping::craftingTable);
		this.createCraftingTableLike(Blocks.FLETCHING_TABLE, Blocks.BIRCH_PLANKS, TextureMapping::fletchingTable);
		this.createNyliumBlock(Blocks.CRIMSON_NYLIUM);
		this.createNyliumBlock(Blocks.WARPED_NYLIUM);
		this.createDispenserBlock(Blocks.DISPENSER);
		this.createDispenserBlock(Blocks.DROPPER);
		this.createCrafterBlock();
		this.createLantern(Blocks.LANTERN);
		this.createLantern(Blocks.SOUL_LANTERN);
		this.createAxisAlignedPillarBlockCustomModel(Blocks.CHAIN, ModelLocationUtils.getModelLocation(Blocks.CHAIN));
		this.createAxisAlignedPillarBlock(Blocks.BASALT, TexturedModel.COLUMN);
		this.createAxisAlignedPillarBlock(Blocks.POLISHED_BASALT, TexturedModel.COLUMN);
		this.createTrivialCube(Blocks.SMOOTH_BASALT);
		this.createAxisAlignedPillarBlock(Blocks.BONE_BLOCK, TexturedModel.COLUMN);
		this.createRotatedVariantBlock(Blocks.DIRT);
		this.createRotatedVariantBlock(Blocks.ROOTED_DIRT);
		this.createRotatedVariantBlock(Blocks.SAND);
		this.createBrushableBlock(Blocks.SUSPICIOUS_SAND);
		this.createBrushableBlock(Blocks.SUSPICIOUS_GRAVEL);
		this.createRotatedVariantBlock(Blocks.RED_SAND);
		this.createRotatedMirroredVariantBlock(Blocks.BEDROCK);
		this.createTrivialBlock(Blocks.REINFORCED_DEEPSLATE, TexturedModel.CUBE_TOP_BOTTOM);
		this.createRotatedPillarWithHorizontalVariant(Blocks.HAY_BLOCK, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
		this.createRotatedPillarWithHorizontalVariant(Blocks.PURPUR_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
		this.createRotatedPillarWithHorizontalVariant(Blocks.QUARTZ_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
		this.createRotatedPillarWithHorizontalVariant(Blocks.OCHRE_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
		this.createRotatedPillarWithHorizontalVariant(Blocks.VERDANT_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
		this.createRotatedPillarWithHorizontalVariant(Blocks.PEARLESCENT_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
		this.createHorizontallyRotatedBlock(Blocks.LOOM, TexturedModel.ORIENTABLE);
		this.createPumpkins();
		this.createBeeNest(Blocks.BEE_NEST, TextureMapping::orientableCube);
		this.createBeeNest(Blocks.BEEHIVE, TextureMapping::orientableCubeSameEnds);
		this.createCropBlock(Blocks.BEETROOTS, BlockStateProperties.AGE_3, 0, 1, 2, 3);
		this.createCropBlock(Blocks.CARROTS, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
		this.createCropBlock(Blocks.NETHER_WART, BlockStateProperties.AGE_3, 0, 1, 1, 2);
		this.createCropBlock(Blocks.POTATOES, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
		this.createCropBlock(Blocks.WHEAT, BlockStateProperties.AGE_7, 0, 1, 2, 3, 4, 5, 6, 7);
		this.createCrossBlock(Blocks.TORCHFLOWER_CROP, BlockModelGenerators.TintState.NOT_TINTED, BlockStateProperties.AGE_1, 0, 1);
		this.createPitcherCrop();
		this.createPitcherPlant();
		this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("decorated_pot"), Blocks.TERRACOTTA).createWithoutBlockItem(Blocks.DECORATED_POT);
		this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("banner"), Blocks.OAK_PLANKS)
			.createWithCustomBlockItemModel(
				ModelTemplates.BANNER_INVENTORY,
				Blocks.WHITE_BANNER,
				Blocks.ORANGE_BANNER,
				Blocks.MAGENTA_BANNER,
				Blocks.LIGHT_BLUE_BANNER,
				Blocks.YELLOW_BANNER,
				Blocks.LIME_BANNER,
				Blocks.PINK_BANNER,
				Blocks.GRAY_BANNER,
				Blocks.LIGHT_GRAY_BANNER,
				Blocks.CYAN_BANNER,
				Blocks.PURPLE_BANNER,
				Blocks.BLUE_BANNER,
				Blocks.BROWN_BANNER,
				Blocks.GREEN_BANNER,
				Blocks.RED_BANNER,
				Blocks.BLACK_BANNER
			)
			.createWithoutBlockItem(
				Blocks.WHITE_WALL_BANNER,
				Blocks.ORANGE_WALL_BANNER,
				Blocks.MAGENTA_WALL_BANNER,
				Blocks.LIGHT_BLUE_WALL_BANNER,
				Blocks.YELLOW_WALL_BANNER,
				Blocks.LIME_WALL_BANNER,
				Blocks.PINK_WALL_BANNER,
				Blocks.GRAY_WALL_BANNER,
				Blocks.LIGHT_GRAY_WALL_BANNER,
				Blocks.CYAN_WALL_BANNER,
				Blocks.PURPLE_WALL_BANNER,
				Blocks.BLUE_WALL_BANNER,
				Blocks.BROWN_WALL_BANNER,
				Blocks.GREEN_WALL_BANNER,
				Blocks.RED_WALL_BANNER,
				Blocks.BLACK_WALL_BANNER
			);
		this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("bed"), Blocks.OAK_PLANKS)
			.createWithoutBlockItem(
				Blocks.WHITE_BED,
				Blocks.ORANGE_BED,
				Blocks.MAGENTA_BED,
				Blocks.LIGHT_BLUE_BED,
				Blocks.YELLOW_BED,
				Blocks.LIME_BED,
				Blocks.PINK_BED,
				Blocks.GRAY_BED,
				Blocks.LIGHT_GRAY_BED,
				Blocks.CYAN_BED,
				Blocks.PURPLE_BED,
				Blocks.BLUE_BED,
				Blocks.BROWN_BED,
				Blocks.GREEN_BED,
				Blocks.RED_BED,
				Blocks.BLACK_BED
			);
		this.createBedItem(Blocks.WHITE_BED, Blocks.WHITE_WOOL);
		this.createBedItem(Blocks.ORANGE_BED, Blocks.ORANGE_WOOL);
		this.createBedItem(Blocks.MAGENTA_BED, Blocks.MAGENTA_WOOL);
		this.createBedItem(Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL);
		this.createBedItem(Blocks.YELLOW_BED, Blocks.YELLOW_WOOL);
		this.createBedItem(Blocks.LIME_BED, Blocks.LIME_WOOL);
		this.createBedItem(Blocks.PINK_BED, Blocks.PINK_WOOL);
		this.createBedItem(Blocks.GRAY_BED, Blocks.GRAY_WOOL);
		this.createBedItem(Blocks.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL);
		this.createBedItem(Blocks.CYAN_BED, Blocks.CYAN_WOOL);
		this.createBedItem(Blocks.PURPLE_BED, Blocks.PURPLE_WOOL);
		this.createBedItem(Blocks.BLUE_BED, Blocks.BLUE_WOOL);
		this.createBedItem(Blocks.BROWN_BED, Blocks.BROWN_WOOL);
		this.createBedItem(Blocks.GREEN_BED, Blocks.GREEN_WOOL);
		this.createBedItem(Blocks.RED_BED, Blocks.RED_WOOL);
		this.createBedItem(Blocks.BLACK_BED, Blocks.BLACK_WOOL);
		this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("skull"), Blocks.SOUL_SAND)
			.createWithCustomBlockItemModel(
				ModelTemplates.SKULL_INVENTORY,
				Blocks.CREEPER_HEAD,
				Blocks.PLAYER_HEAD,
				Blocks.ZOMBIE_HEAD,
				Blocks.SKELETON_SKULL,
				Blocks.WITHER_SKELETON_SKULL,
				Blocks.PIGLIN_HEAD
			)
			.create(Blocks.DRAGON_HEAD)
			.createWithoutBlockItem(
				Blocks.CREEPER_WALL_HEAD,
				Blocks.DRAGON_WALL_HEAD,
				Blocks.PLAYER_WALL_HEAD,
				Blocks.ZOMBIE_WALL_HEAD,
				Blocks.SKELETON_WALL_SKULL,
				Blocks.WITHER_SKELETON_WALL_SKULL,
				Blocks.PIGLIN_WALL_HEAD
			);
		this.createShulkerBox(Blocks.SHULKER_BOX);
		this.createShulkerBox(Blocks.WHITE_SHULKER_BOX);
		this.createShulkerBox(Blocks.ORANGE_SHULKER_BOX);
		this.createShulkerBox(Blocks.MAGENTA_SHULKER_BOX);
		this.createShulkerBox(Blocks.LIGHT_BLUE_SHULKER_BOX);
		this.createShulkerBox(Blocks.YELLOW_SHULKER_BOX);
		this.createShulkerBox(Blocks.LIME_SHULKER_BOX);
		this.createShulkerBox(Blocks.PINK_SHULKER_BOX);
		this.createShulkerBox(Blocks.GRAY_SHULKER_BOX);
		this.createShulkerBox(Blocks.LIGHT_GRAY_SHULKER_BOX);
		this.createShulkerBox(Blocks.CYAN_SHULKER_BOX);
		this.createShulkerBox(Blocks.PURPLE_SHULKER_BOX);
		this.createShulkerBox(Blocks.BLUE_SHULKER_BOX);
		this.createShulkerBox(Blocks.BROWN_SHULKER_BOX);
		this.createShulkerBox(Blocks.GREEN_SHULKER_BOX);
		this.createShulkerBox(Blocks.RED_SHULKER_BOX);
		this.createShulkerBox(Blocks.BLACK_SHULKER_BOX);
		this.createTrivialBlock(Blocks.CONDUIT, TexturedModel.PARTICLE_ONLY);
		this.skipAutoItemBlock(Blocks.CONDUIT);
		this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("chest"), Blocks.OAK_PLANKS).createWithoutBlockItem(Blocks.CHEST, Blocks.TRAPPED_CHEST);
		this.blockEntityModels(ModelLocationUtils.decorateBlockModelLocation("ender_chest"), Blocks.OBSIDIAN).createWithoutBlockItem(Blocks.ENDER_CHEST);
		this.blockEntityModels(Blocks.END_PORTAL, Blocks.OBSIDIAN).create(Blocks.END_PORTAL, Blocks.END_GATEWAY);
		this.createTrivialCube(Blocks.AZALEA_LEAVES);
		this.createTrivialCube(Blocks.FLOWERING_AZALEA_LEAVES);
		this.createTrivialCube(Blocks.WHITE_CONCRETE);
		this.createTrivialCube(Blocks.ORANGE_CONCRETE);
		this.createTrivialCube(Blocks.MAGENTA_CONCRETE);
		this.createTrivialCube(Blocks.LIGHT_BLUE_CONCRETE);
		this.createTrivialCube(Blocks.YELLOW_CONCRETE);
		this.createTrivialCube(Blocks.LIME_CONCRETE);
		this.createTrivialCube(Blocks.PINK_CONCRETE);
		this.createTrivialCube(Blocks.GRAY_CONCRETE);
		this.createTrivialCube(Blocks.LIGHT_GRAY_CONCRETE);
		this.createTrivialCube(Blocks.CYAN_CONCRETE);
		this.createTrivialCube(Blocks.PURPLE_CONCRETE);
		this.createTrivialCube(Blocks.BLUE_CONCRETE);
		this.createTrivialCube(Blocks.BROWN_CONCRETE);
		this.createTrivialCube(Blocks.GREEN_CONCRETE);
		this.createTrivialCube(Blocks.RED_CONCRETE);
		this.createTrivialCube(Blocks.BLACK_CONCRETE);
		this.createColoredBlockWithRandomRotations(
			TexturedModel.CUBE,
			Blocks.WHITE_CONCRETE_POWDER,
			Blocks.ORANGE_CONCRETE_POWDER,
			Blocks.MAGENTA_CONCRETE_POWDER,
			Blocks.LIGHT_BLUE_CONCRETE_POWDER,
			Blocks.YELLOW_CONCRETE_POWDER,
			Blocks.LIME_CONCRETE_POWDER,
			Blocks.PINK_CONCRETE_POWDER,
			Blocks.GRAY_CONCRETE_POWDER,
			Blocks.LIGHT_GRAY_CONCRETE_POWDER,
			Blocks.CYAN_CONCRETE_POWDER,
			Blocks.PURPLE_CONCRETE_POWDER,
			Blocks.BLUE_CONCRETE_POWDER,
			Blocks.BROWN_CONCRETE_POWDER,
			Blocks.GREEN_CONCRETE_POWDER,
			Blocks.RED_CONCRETE_POWDER,
			Blocks.BLACK_CONCRETE_POWDER
		);
		this.createTrivialCube(Blocks.TERRACOTTA);
		this.createTrivialCube(Blocks.WHITE_TERRACOTTA);
		this.createTrivialCube(Blocks.ORANGE_TERRACOTTA);
		this.createTrivialCube(Blocks.MAGENTA_TERRACOTTA);
		this.createTrivialCube(Blocks.LIGHT_BLUE_TERRACOTTA);
		this.createTrivialCube(Blocks.YELLOW_TERRACOTTA);
		this.createTrivialCube(Blocks.LIME_TERRACOTTA);
		this.createTrivialCube(Blocks.PINK_TERRACOTTA);
		this.createTrivialCube(Blocks.GRAY_TERRACOTTA);
		this.createTrivialCube(Blocks.LIGHT_GRAY_TERRACOTTA);
		this.createTrivialCube(Blocks.CYAN_TERRACOTTA);
		this.createTrivialCube(Blocks.PURPLE_TERRACOTTA);
		this.createTrivialCube(Blocks.BLUE_TERRACOTTA);
		this.createTrivialCube(Blocks.BROWN_TERRACOTTA);
		this.createTrivialCube(Blocks.GREEN_TERRACOTTA);
		this.createTrivialCube(Blocks.RED_TERRACOTTA);
		this.createTrivialCube(Blocks.BLACK_TERRACOTTA);
		this.createTrivialCube(Blocks.TINTED_GLASS);
		this.createGlassBlocks(Blocks.GLASS, Blocks.GLASS_PANE);
		this.createGlassBlocks(Blocks.WHITE_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.ORANGE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.MAGENTA_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.YELLOW_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.LIME_STAINED_GLASS, Blocks.LIME_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.PINK_STAINED_GLASS, Blocks.PINK_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.GRAY_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.CYAN_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.PURPLE_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.BLUE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.BROWN_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.GREEN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.RED_STAINED_GLASS, Blocks.RED_STAINED_GLASS_PANE);
		this.createGlassBlocks(Blocks.BLACK_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS_PANE);
		this.createColoredBlockWithStateRotations(
			TexturedModel.GLAZED_TERRACOTTA,
			Blocks.WHITE_GLAZED_TERRACOTTA,
			Blocks.ORANGE_GLAZED_TERRACOTTA,
			Blocks.MAGENTA_GLAZED_TERRACOTTA,
			Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA,
			Blocks.YELLOW_GLAZED_TERRACOTTA,
			Blocks.LIME_GLAZED_TERRACOTTA,
			Blocks.PINK_GLAZED_TERRACOTTA,
			Blocks.GRAY_GLAZED_TERRACOTTA,
			Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA,
			Blocks.CYAN_GLAZED_TERRACOTTA,
			Blocks.PURPLE_GLAZED_TERRACOTTA,
			Blocks.BLUE_GLAZED_TERRACOTTA,
			Blocks.BROWN_GLAZED_TERRACOTTA,
			Blocks.GREEN_GLAZED_TERRACOTTA,
			Blocks.RED_GLAZED_TERRACOTTA,
			Blocks.BLACK_GLAZED_TERRACOTTA
		);
		this.createFullAndCarpetBlocks(Blocks.WHITE_WOOL, Blocks.WHITE_CARPET);
		this.createFullAndCarpetBlocks(Blocks.ORANGE_WOOL, Blocks.ORANGE_CARPET);
		this.createFullAndCarpetBlocks(Blocks.MAGENTA_WOOL, Blocks.MAGENTA_CARPET);
		this.createFullAndCarpetBlocks(Blocks.LIGHT_BLUE_WOOL, Blocks.LIGHT_BLUE_CARPET);
		this.createFullAndCarpetBlocks(Blocks.YELLOW_WOOL, Blocks.YELLOW_CARPET);
		this.createFullAndCarpetBlocks(Blocks.LIME_WOOL, Blocks.LIME_CARPET);
		this.createFullAndCarpetBlocks(Blocks.PINK_WOOL, Blocks.PINK_CARPET);
		this.createFullAndCarpetBlocks(Blocks.GRAY_WOOL, Blocks.GRAY_CARPET);
		this.createFullAndCarpetBlocks(Blocks.LIGHT_GRAY_WOOL, Blocks.LIGHT_GRAY_CARPET);
		this.createFullAndCarpetBlocks(Blocks.CYAN_WOOL, Blocks.CYAN_CARPET);
		this.createFullAndCarpetBlocks(Blocks.PURPLE_WOOL, Blocks.PURPLE_CARPET);
		this.createFullAndCarpetBlocks(Blocks.BLUE_WOOL, Blocks.BLUE_CARPET);
		this.createFullAndCarpetBlocks(Blocks.BROWN_WOOL, Blocks.BROWN_CARPET);
		this.createFullAndCarpetBlocks(Blocks.GREEN_WOOL, Blocks.GREEN_CARPET);
		this.createFullAndCarpetBlocks(Blocks.RED_WOOL, Blocks.RED_CARPET);
		this.createFullAndCarpetBlocks(Blocks.BLACK_WOOL, Blocks.BLACK_CARPET);
		this.createTrivialCube(Blocks.MUD);
		this.createTrivialCube(Blocks.PACKED_MUD);
		this.createPlant(Blocks.FERN, Blocks.POTTED_FERN, BlockModelGenerators.TintState.TINTED);
		this.createPlant(Blocks.DANDELION, Blocks.POTTED_DANDELION, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.POPPY, Blocks.POTTED_POPPY, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.BLUE_ORCHID, Blocks.POTTED_BLUE_ORCHID, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.ALLIUM, Blocks.POTTED_ALLIUM, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.AZURE_BLUET, Blocks.POTTED_AZURE_BLUET, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.RED_TULIP, Blocks.POTTED_RED_TULIP, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.ORANGE_TULIP, Blocks.POTTED_ORANGE_TULIP, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.WHITE_TULIP, Blocks.POTTED_WHITE_TULIP, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.PINK_TULIP, Blocks.POTTED_PINK_TULIP, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.OXEYE_DAISY, Blocks.POTTED_OXEYE_DAISY, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.CORNFLOWER, Blocks.POTTED_CORNFLOWER, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.LILY_OF_THE_VALLEY, Blocks.POTTED_LILY_OF_THE_VALLEY, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.WITHER_ROSE, Blocks.POTTED_WITHER_ROSE, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.RED_MUSHROOM, Blocks.POTTED_RED_MUSHROOM, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.BROWN_MUSHROOM, Blocks.POTTED_BROWN_MUSHROOM, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.DEAD_BUSH, Blocks.POTTED_DEAD_BUSH, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPlant(Blocks.TORCHFLOWER, Blocks.POTTED_TORCHFLOWER, BlockModelGenerators.TintState.NOT_TINTED);
		this.createPointedDripstone();
		this.createMushroomBlock(Blocks.BROWN_MUSHROOM_BLOCK);
		this.createMushroomBlock(Blocks.RED_MUSHROOM_BLOCK);
		this.createMushroomBlock(Blocks.MUSHROOM_STEM);
		this.createCrossBlockWithDefaultItem(Blocks.SHORT_GRASS, BlockModelGenerators.TintState.TINTED);
		this.createCrossBlock(Blocks.SUGAR_CANE, BlockModelGenerators.TintState.TINTED);
		this.createSimpleFlatItemModel(Items.SUGAR_CANE);
		this.createGrowingPlant(Blocks.KELP, Blocks.KELP_PLANT, BlockModelGenerators.TintState.NOT_TINTED);
		this.createSimpleFlatItemModel(Items.KELP);
		this.skipAutoItemBlock(Blocks.KELP_PLANT);
		this.createCrossBlock(Blocks.HANGING_ROOTS, BlockModelGenerators.TintState.NOT_TINTED);
		this.skipAutoItemBlock(Blocks.HANGING_ROOTS);
		this.skipAutoItemBlock(Blocks.CAVE_VINES_PLANT);
		this.createGrowingPlant(Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT, BlockModelGenerators.TintState.NOT_TINTED);
		this.createGrowingPlant(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, BlockModelGenerators.TintState.NOT_TINTED);
		this.createSimpleFlatItemModel(Blocks.WEEPING_VINES, "_plant");
		this.skipAutoItemBlock(Blocks.WEEPING_VINES_PLANT);
		this.createSimpleFlatItemModel(Blocks.TWISTING_VINES, "_plant");
		this.skipAutoItemBlock(Blocks.TWISTING_VINES_PLANT);
		this.createCrossBlockWithDefaultItem(
			Blocks.BAMBOO_SAPLING, BlockModelGenerators.TintState.TINTED, TextureMapping.cross(TextureMapping.getBlockTexture(Blocks.BAMBOO, "_stage0"))
		);
		this.createBamboo();
		this.createCrossBlockWithDefaultItem(Blocks.COBWEB, BlockModelGenerators.TintState.NOT_TINTED);
		this.createDoublePlant(Blocks.LILAC, BlockModelGenerators.TintState.NOT_TINTED);
		this.createDoublePlant(Blocks.ROSE_BUSH, BlockModelGenerators.TintState.NOT_TINTED);
		this.createDoublePlant(Blocks.PEONY, BlockModelGenerators.TintState.NOT_TINTED);
		this.createDoublePlant(Blocks.TALL_GRASS, BlockModelGenerators.TintState.TINTED);
		this.createDoublePlant(Blocks.LARGE_FERN, BlockModelGenerators.TintState.TINTED);
		this.createSunflower();
		this.createTallSeagrass();
		this.createSmallDripleaf();
		this.createCoral(
			Blocks.TUBE_CORAL,
			Blocks.DEAD_TUBE_CORAL,
			Blocks.TUBE_CORAL_BLOCK,
			Blocks.DEAD_TUBE_CORAL_BLOCK,
			Blocks.TUBE_CORAL_FAN,
			Blocks.DEAD_TUBE_CORAL_FAN,
			Blocks.TUBE_CORAL_WALL_FAN,
			Blocks.DEAD_TUBE_CORAL_WALL_FAN
		);
		this.createCoral(
			Blocks.BRAIN_CORAL,
			Blocks.DEAD_BRAIN_CORAL,
			Blocks.BRAIN_CORAL_BLOCK,
			Blocks.DEAD_BRAIN_CORAL_BLOCK,
			Blocks.BRAIN_CORAL_FAN,
			Blocks.DEAD_BRAIN_CORAL_FAN,
			Blocks.BRAIN_CORAL_WALL_FAN,
			Blocks.DEAD_BRAIN_CORAL_WALL_FAN
		);
		this.createCoral(
			Blocks.BUBBLE_CORAL,
			Blocks.DEAD_BUBBLE_CORAL,
			Blocks.BUBBLE_CORAL_BLOCK,
			Blocks.DEAD_BUBBLE_CORAL_BLOCK,
			Blocks.BUBBLE_CORAL_FAN,
			Blocks.DEAD_BUBBLE_CORAL_FAN,
			Blocks.BUBBLE_CORAL_WALL_FAN,
			Blocks.DEAD_BUBBLE_CORAL_WALL_FAN
		);
		this.createCoral(
			Blocks.FIRE_CORAL,
			Blocks.DEAD_FIRE_CORAL,
			Blocks.FIRE_CORAL_BLOCK,
			Blocks.DEAD_FIRE_CORAL_BLOCK,
			Blocks.FIRE_CORAL_FAN,
			Blocks.DEAD_FIRE_CORAL_FAN,
			Blocks.FIRE_CORAL_WALL_FAN,
			Blocks.DEAD_FIRE_CORAL_WALL_FAN
		);
		this.createCoral(
			Blocks.HORN_CORAL,
			Blocks.DEAD_HORN_CORAL,
			Blocks.HORN_CORAL_BLOCK,
			Blocks.DEAD_HORN_CORAL_BLOCK,
			Blocks.HORN_CORAL_FAN,
			Blocks.DEAD_HORN_CORAL_FAN,
			Blocks.HORN_CORAL_WALL_FAN,
			Blocks.DEAD_HORN_CORAL_WALL_FAN
		);
		this.createStems(Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM);
		this.createStems(Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
		this.woodProvider(Blocks.MANGROVE_LOG).logWithHorizontal(Blocks.MANGROVE_LOG).wood(Blocks.MANGROVE_WOOD);
		this.woodProvider(Blocks.STRIPPED_MANGROVE_LOG).logWithHorizontal(Blocks.STRIPPED_MANGROVE_LOG).wood(Blocks.STRIPPED_MANGROVE_WOOD);
		this.createHangingSign(Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN);
		this.createTrivialBlock(Blocks.MANGROVE_LEAVES, TexturedModel.LEAVES);
		this.woodProvider(Blocks.ACACIA_LOG).logWithHorizontal(Blocks.ACACIA_LOG).wood(Blocks.ACACIA_WOOD);
		this.woodProvider(Blocks.STRIPPED_ACACIA_LOG).logWithHorizontal(Blocks.STRIPPED_ACACIA_LOG).wood(Blocks.STRIPPED_ACACIA_WOOD);
		this.createHangingSign(Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN);
		this.createPlant(Blocks.ACACIA_SAPLING, Blocks.POTTED_ACACIA_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.ACACIA_LEAVES, TexturedModel.LEAVES);
		this.woodProvider(Blocks.CHERRY_LOG).logUVLocked(Blocks.CHERRY_LOG).wood(Blocks.CHERRY_WOOD);
		this.woodProvider(Blocks.STRIPPED_CHERRY_LOG).logUVLocked(Blocks.STRIPPED_CHERRY_LOG).wood(Blocks.STRIPPED_CHERRY_WOOD);
		this.createHangingSign(Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN);
		this.createPlant(Blocks.CHERRY_SAPLING, Blocks.POTTED_CHERRY_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.CHERRY_LEAVES, TexturedModel.LEAVES);
		this.woodProvider(Blocks.BIRCH_LOG).logWithHorizontal(Blocks.BIRCH_LOG).wood(Blocks.BIRCH_WOOD);
		this.woodProvider(Blocks.STRIPPED_BIRCH_LOG).logWithHorizontal(Blocks.STRIPPED_BIRCH_LOG).wood(Blocks.STRIPPED_BIRCH_WOOD);
		this.createHangingSign(Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN);
		this.createPlant(Blocks.BIRCH_SAPLING, Blocks.POTTED_BIRCH_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.BIRCH_LEAVES, TexturedModel.LEAVES);
		this.woodProvider(Blocks.OAK_LOG).logWithHorizontal(Blocks.OAK_LOG).wood(Blocks.OAK_WOOD);
		this.woodProvider(Blocks.STRIPPED_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_OAK_LOG).wood(Blocks.STRIPPED_OAK_WOOD);
		this.createHangingSign(Blocks.STRIPPED_OAK_LOG, Blocks.OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN);
		this.createPlant(Blocks.OAK_SAPLING, Blocks.POTTED_OAK_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.OAK_LEAVES, TexturedModel.LEAVES);
		this.woodProvider(Blocks.SPRUCE_LOG).logWithHorizontal(Blocks.SPRUCE_LOG).wood(Blocks.SPRUCE_WOOD);
		this.woodProvider(Blocks.STRIPPED_SPRUCE_LOG).logWithHorizontal(Blocks.STRIPPED_SPRUCE_LOG).wood(Blocks.STRIPPED_SPRUCE_WOOD);
		this.createHangingSign(Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN);
		this.createPlant(Blocks.SPRUCE_SAPLING, Blocks.POTTED_SPRUCE_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.SPRUCE_LEAVES, TexturedModel.LEAVES);
		this.woodProvider(Blocks.DARK_OAK_LOG).logWithHorizontal(Blocks.DARK_OAK_LOG).wood(Blocks.DARK_OAK_WOOD);
		this.woodProvider(Blocks.STRIPPED_DARK_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_DARK_OAK_LOG).wood(Blocks.STRIPPED_DARK_OAK_WOOD);
		this.createHangingSign(Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN);
		this.createPlant(Blocks.DARK_OAK_SAPLING, Blocks.POTTED_DARK_OAK_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.DARK_OAK_LEAVES, TexturedModel.LEAVES);
		this.woodProvider(Blocks.JUNGLE_LOG).logWithHorizontal(Blocks.JUNGLE_LOG).wood(Blocks.JUNGLE_WOOD);
		this.woodProvider(Blocks.STRIPPED_JUNGLE_LOG).logWithHorizontal(Blocks.STRIPPED_JUNGLE_LOG).wood(Blocks.STRIPPED_JUNGLE_WOOD);
		this.createHangingSign(Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN);
		this.createPlant(Blocks.JUNGLE_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.JUNGLE_LEAVES, TexturedModel.LEAVES);
		this.woodProvider(Blocks.CRIMSON_STEM).log(Blocks.CRIMSON_STEM).wood(Blocks.CRIMSON_HYPHAE);
		this.woodProvider(Blocks.STRIPPED_CRIMSON_STEM).log(Blocks.STRIPPED_CRIMSON_STEM).wood(Blocks.STRIPPED_CRIMSON_HYPHAE);
		this.createHangingSign(Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN);
		this.createPlant(Blocks.CRIMSON_FUNGUS, Blocks.POTTED_CRIMSON_FUNGUS, BlockModelGenerators.TintState.NOT_TINTED);
		this.createNetherRoots(Blocks.CRIMSON_ROOTS, Blocks.POTTED_CRIMSON_ROOTS);
		this.woodProvider(Blocks.WARPED_STEM).log(Blocks.WARPED_STEM).wood(Blocks.WARPED_HYPHAE);
		this.woodProvider(Blocks.STRIPPED_WARPED_STEM).log(Blocks.STRIPPED_WARPED_STEM).wood(Blocks.STRIPPED_WARPED_HYPHAE);
		this.createHangingSign(Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN);
		this.createPlant(Blocks.WARPED_FUNGUS, Blocks.POTTED_WARPED_FUNGUS, BlockModelGenerators.TintState.NOT_TINTED);
		this.createNetherRoots(Blocks.WARPED_ROOTS, Blocks.POTTED_WARPED_ROOTS);
		this.woodProvider(Blocks.BAMBOO_BLOCK).logUVLocked(Blocks.BAMBOO_BLOCK);
		this.woodProvider(Blocks.STRIPPED_BAMBOO_BLOCK).logUVLocked(Blocks.STRIPPED_BAMBOO_BLOCK);
		this.createHangingSign(Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN);
		this.createCrossBlock(Blocks.NETHER_SPROUTS, BlockModelGenerators.TintState.NOT_TINTED);
		this.createSimpleFlatItemModel(Items.NETHER_SPROUTS);
		this.createDoor(Blocks.IRON_DOOR);
		this.createTrapdoor(Blocks.IRON_TRAPDOOR);
		this.createSmoothStoneSlab();
		this.createPassiveRail(Blocks.RAIL);
		this.createActiveRail(Blocks.POWERED_RAIL);
		this.createActiveRail(Blocks.DETECTOR_RAIL);
		this.createActiveRail(Blocks.ACTIVATOR_RAIL);
		this.createComparator();
		this.createCommandBlock(Blocks.COMMAND_BLOCK);
		this.createCommandBlock(Blocks.REPEATING_COMMAND_BLOCK);
		this.createCommandBlock(Blocks.CHAIN_COMMAND_BLOCK);
		this.createAnvil(Blocks.ANVIL);
		this.createAnvil(Blocks.CHIPPED_ANVIL);
		this.createAnvil(Blocks.DAMAGED_ANVIL);
		this.createBarrel();
		this.createBell();
		this.createFurnace(Blocks.FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
		this.createFurnace(Blocks.BLAST_FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
		this.createFurnace(Blocks.SMOKER, TexturedModel.ORIENTABLE);
		this.createRedstoneWire();
		this.createRespawnAnchor();
		this.createSculkCatalyst();
		this.copyModel(Blocks.CHISELED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS);
		this.copyModel(Blocks.COBBLESTONE, Blocks.INFESTED_COBBLESTONE);
		this.copyModel(Blocks.CRACKED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS);
		this.copyModel(Blocks.MOSSY_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS);
		this.createInfestedStone();
		this.copyModel(Blocks.STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
		this.createInfestedDeepslate();
		SpawnEggItem.eggs().forEach(spawnEggItem -> this.delegateItemModel(spawnEggItem, ModelLocationUtils.decorateItemModelLocation("template_spawn_egg")));
	}

	private void createLightBlock() {
		this.skipAutoItemBlock(Blocks.LIGHT);
		PropertyDispatch.C1<Integer> c1 = PropertyDispatch.property(BlockStateProperties.LEVEL);

		for (int i = 0; i < 16; i++) {
			String string = String.format(Locale.ROOT, "_%02d", i);
			ResourceLocation resourceLocation = TextureMapping.getItemTexture(Items.LIGHT, string);
			c1.select(
				i,
				Variant.variant()
					.with(
						VariantProperties.MODEL, ModelTemplates.PARTICLE_ONLY.createWithSuffix(Blocks.LIGHT, string, TextureMapping.particle(resourceLocation), this.modelOutput)
					)
			);
			ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(Items.LIGHT, string), TextureMapping.layer0(resourceLocation), this.modelOutput);
		}

		this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(Blocks.LIGHT).with(c1));
	}

	private void createCandleAndCandleCake(Block block, Block block2) {
		this.createSimpleFlatItemModel(block.asItem());
		TextureMapping textureMapping = TextureMapping.cube(TextureMapping.getBlockTexture(block));
		TextureMapping textureMapping2 = TextureMapping.cube(TextureMapping.getBlockTexture(block, "_lit"));
		ResourceLocation resourceLocation = ModelTemplates.CANDLE.createWithSuffix(block, "_one_candle", textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.TWO_CANDLES.createWithSuffix(block, "_two_candles", textureMapping, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.THREE_CANDLES.createWithSuffix(block, "_three_candles", textureMapping, this.modelOutput);
		ResourceLocation resourceLocation4 = ModelTemplates.FOUR_CANDLES.createWithSuffix(block, "_four_candles", textureMapping, this.modelOutput);
		ResourceLocation resourceLocation5 = ModelTemplates.CANDLE.createWithSuffix(block, "_one_candle_lit", textureMapping2, this.modelOutput);
		ResourceLocation resourceLocation6 = ModelTemplates.TWO_CANDLES.createWithSuffix(block, "_two_candles_lit", textureMapping2, this.modelOutput);
		ResourceLocation resourceLocation7 = ModelTemplates.THREE_CANDLES.createWithSuffix(block, "_three_candles_lit", textureMapping2, this.modelOutput);
		ResourceLocation resourceLocation8 = ModelTemplates.FOUR_CANDLES.createWithSuffix(block, "_four_candles_lit", textureMapping2, this.modelOutput);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(block)
					.with(
						PropertyDispatch.properties(BlockStateProperties.CANDLES, BlockStateProperties.LIT)
							.select(1, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
							.select(2, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
							.select(3, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
							.select(4, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation4))
							.select(1, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation5))
							.select(2, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation6))
							.select(3, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation7))
							.select(4, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation8))
					)
			);
		ResourceLocation resourceLocation9 = ModelTemplates.CANDLE_CAKE.create(block2, TextureMapping.candleCake(block, false), this.modelOutput);
		ResourceLocation resourceLocation10 = ModelTemplates.CANDLE_CAKE.createWithSuffix(block2, "_lit", TextureMapping.candleCake(block, true), this.modelOutput);
		this.blockStateOutput
			.accept(MultiVariantGenerator.multiVariant(block2).with(createBooleanModelDispatch(BlockStateProperties.LIT, resourceLocation10, resourceLocation9)));
	}

	class BlockEntityModelGenerator {
		private final ResourceLocation baseModel;

		public BlockEntityModelGenerator(ResourceLocation resourceLocation, Block block) {
			this.baseModel = ModelTemplates.PARTICLE_ONLY.create(resourceLocation, TextureMapping.particle(block), BlockModelGenerators.this.modelOutput);
		}

		public BlockModelGenerators.BlockEntityModelGenerator create(Block... blocks) {
			for (Block block : blocks) {
				BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, this.baseModel));
			}

			return this;
		}

		public BlockModelGenerators.BlockEntityModelGenerator createWithoutBlockItem(Block... blocks) {
			for (Block block : blocks) {
				BlockModelGenerators.this.skipAutoItemBlock(block);
			}

			return this.create(blocks);
		}

		public BlockModelGenerators.BlockEntityModelGenerator createWithCustomBlockItemModel(ModelTemplate modelTemplate, Block... blocks) {
			for (Block block : blocks) {
				modelTemplate.create(ModelLocationUtils.getModelLocation(block.asItem()), TextureMapping.particle(block), BlockModelGenerators.this.modelOutput);
			}

			return this.create(blocks);
		}
	}

	class BlockFamilyProvider {
		private final TextureMapping mapping;
		private final Map<ModelTemplate, ResourceLocation> models = Maps.<ModelTemplate, ResourceLocation>newHashMap();
		@Nullable
		private BlockFamily family;
		@Nullable
		private ResourceLocation fullBlock;
		private final Set<Block> skipGeneratingModelsFor = new HashSet();

		public BlockFamilyProvider(TextureMapping textureMapping) {
			this.mapping = textureMapping;
		}

		public BlockModelGenerators.BlockFamilyProvider fullBlock(Block block, ModelTemplate modelTemplate) {
			this.fullBlock = modelTemplate.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			if (BlockModelGenerators.this.fullBlockModelCustomGenerators.containsKey(block)) {
				BlockModelGenerators.this.blockStateOutput
					.accept(
						((BlockModelGenerators.BlockStateGeneratorSupplier)BlockModelGenerators.this.fullBlockModelCustomGenerators.get(block))
							.create(block, this.fullBlock, this.mapping, BlockModelGenerators.this.modelOutput)
					);
			} else {
				BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, this.fullBlock));
			}

			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider donateModelTo(Block block, Block block2) {
			ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(block);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block2, resourceLocation));
			BlockModelGenerators.this.delegateItemModel(block2, resourceLocation);
			this.skipGeneratingModelsFor.add(block2);
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider button(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.BUTTON.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.BUTTON_PRESSED.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createButton(block, resourceLocation, resourceLocation2));
			ResourceLocation resourceLocation3 = ModelTemplates.BUTTON_INVENTORY.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.delegateItemModel(block, resourceLocation3);
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider wall(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.WALL_POST.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.WALL_LOW_SIDE.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation3 = ModelTemplates.WALL_TALL_SIDE.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createWall(block, resourceLocation, resourceLocation2, resourceLocation3));
			ResourceLocation resourceLocation4 = ModelTemplates.WALL_INVENTORY.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.delegateItemModel(block, resourceLocation4);
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider customFence(Block block) {
			TextureMapping textureMapping = TextureMapping.customParticle(block);
			ResourceLocation resourceLocation = ModelTemplates.CUSTOM_FENCE_POST.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.CUSTOM_FENCE_SIDE_NORTH.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation3 = ModelTemplates.CUSTOM_FENCE_SIDE_EAST.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation4 = ModelTemplates.CUSTOM_FENCE_SIDE_SOUTH.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation5 = ModelTemplates.CUSTOM_FENCE_SIDE_WEST.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createCustomFence(block, resourceLocation, resourceLocation2, resourceLocation3, resourceLocation4, resourceLocation5));
			ResourceLocation resourceLocation6 = ModelTemplates.CUSTOM_FENCE_INVENTORY.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.delegateItemModel(block, resourceLocation6);
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider fence(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.FENCE_POST.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.FENCE_SIDE.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createFence(block, resourceLocation, resourceLocation2));
			ResourceLocation resourceLocation3 = ModelTemplates.FENCE_INVENTORY.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.delegateItemModel(block, resourceLocation3);
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider customFenceGate(Block block) {
			TextureMapping textureMapping = TextureMapping.customParticle(block);
			ResourceLocation resourceLocation = ModelTemplates.CUSTOM_FENCE_GATE_OPEN.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.CUSTOM_FENCE_GATE_CLOSED.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation3 = ModelTemplates.CUSTOM_FENCE_GATE_WALL_OPEN.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation4 = ModelTemplates.CUSTOM_FENCE_GATE_WALL_CLOSED.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createFenceGate(block, resourceLocation, resourceLocation2, resourceLocation3, resourceLocation4, false));
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider fenceGate(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.FENCE_GATE_OPEN.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.FENCE_GATE_CLOSED.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation3 = ModelTemplates.FENCE_GATE_WALL_OPEN.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation4 = ModelTemplates.FENCE_GATE_WALL_CLOSED.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createFenceGate(block, resourceLocation, resourceLocation2, resourceLocation3, resourceLocation4, true));
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider pressurePlate(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.PRESSURE_PLATE_UP.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.PRESSURE_PLATE_DOWN.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createPressurePlate(block, resourceLocation, resourceLocation2));
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider sign(Block block) {
			if (this.family == null) {
				throw new IllegalStateException("Family not defined");
			} else {
				Block block2 = (Block)this.family.getVariants().get(BlockFamily.Variant.WALL_SIGN);
				ResourceLocation resourceLocation = ModelTemplates.PARTICLE_ONLY.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
				BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, resourceLocation));
				BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block2, resourceLocation));
				BlockModelGenerators.this.createSimpleFlatItemModel(block.asItem());
				BlockModelGenerators.this.skipAutoItemBlock(block2);
				return this;
			}
		}

		public BlockModelGenerators.BlockFamilyProvider slab(Block block) {
			if (this.fullBlock == null) {
				throw new IllegalStateException("Full block not generated yet");
			} else {
				ResourceLocation resourceLocation = this.getOrCreateModel(ModelTemplates.SLAB_BOTTOM, block);
				ResourceLocation resourceLocation2 = this.getOrCreateModel(ModelTemplates.SLAB_TOP, block);
				BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSlab(block, resourceLocation, resourceLocation2, this.fullBlock));
				BlockModelGenerators.this.delegateItemModel(block, resourceLocation);
				return this;
			}
		}

		public BlockModelGenerators.BlockFamilyProvider stairs(Block block) {
			ResourceLocation resourceLocation = this.getOrCreateModel(ModelTemplates.STAIRS_INNER, block);
			ResourceLocation resourceLocation2 = this.getOrCreateModel(ModelTemplates.STAIRS_STRAIGHT, block);
			ResourceLocation resourceLocation3 = this.getOrCreateModel(ModelTemplates.STAIRS_OUTER, block);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createStairs(block, resourceLocation, resourceLocation2, resourceLocation3));
			BlockModelGenerators.this.delegateItemModel(block, resourceLocation2);
			return this;
		}

		private BlockModelGenerators.BlockFamilyProvider fullBlockVariant(Block block) {
			TexturedModel texturedModel = (TexturedModel)BlockModelGenerators.this.texturedModels.getOrDefault(block, TexturedModel.CUBE.get(block));
			ResourceLocation resourceLocation = texturedModel.create(block, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, resourceLocation));
			return this;
		}

		private BlockModelGenerators.BlockFamilyProvider door(Block block) {
			BlockModelGenerators.this.createDoor(block);
			return this;
		}

		private void trapdoor(Block block) {
			if (BlockModelGenerators.this.nonOrientableTrapdoor.contains(block)) {
				BlockModelGenerators.this.createTrapdoor(block);
			} else {
				BlockModelGenerators.this.createOrientableTrapdoor(block);
			}
		}

		private ResourceLocation getOrCreateModel(ModelTemplate modelTemplate, Block block) {
			return (ResourceLocation)this.models
				.computeIfAbsent(modelTemplate, modelTemplatex -> modelTemplatex.create(block, this.mapping, BlockModelGenerators.this.modelOutput));
		}

		public BlockModelGenerators.BlockFamilyProvider generateFor(BlockFamily blockFamily) {
			this.family = blockFamily;
			blockFamily.getVariants()
				.forEach(
					(variant, block) -> {
						if (!this.skipGeneratingModelsFor.contains(block)) {
							BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block> biConsumer = (BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block>)BlockModelGenerators.SHAPE_CONSUMERS
								.get(variant);
							if (biConsumer != null) {
								biConsumer.accept(this, block);
							}
						}
					}
				);
			return this;
		}
	}

	@FunctionalInterface
	interface BlockStateGeneratorSupplier {
		BlockStateGenerator create(
			Block block, ResourceLocation resourceLocation, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer
		);
	}

	static record BookSlotModelCacheKey(ModelTemplate template, String modelSuffix) {
	}

	static enum TintState {
		TINTED,
		NOT_TINTED;

		public ModelTemplate getCross() {
			return this == TINTED ? ModelTemplates.TINTED_CROSS : ModelTemplates.CROSS;
		}

		public ModelTemplate getCrossPot() {
			return this == TINTED ? ModelTemplates.TINTED_FLOWER_POT_CROSS : ModelTemplates.FLOWER_POT_CROSS;
		}
	}

	class WoodProvider {
		private final TextureMapping logMapping;

		public WoodProvider(TextureMapping textureMapping) {
			this.logMapping = textureMapping;
		}

		public BlockModelGenerators.WoodProvider wood(Block block) {
			TextureMapping textureMapping = this.logMapping.copyAndUpdate(TextureSlot.END, this.logMapping.get(TextureSlot.SIDE));
			ResourceLocation resourceLocation = ModelTemplates.CUBE_COLUMN.create(block, textureMapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(block, resourceLocation));
			return this;
		}

		public BlockModelGenerators.WoodProvider log(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.CUBE_COLUMN.create(block, this.logMapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(block, resourceLocation));
			return this;
		}

		public BlockModelGenerators.WoodProvider logWithHorizontal(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.CUBE_COLUMN.create(block, this.logMapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(block, this.logMapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(block, resourceLocation, resourceLocation2));
			return this;
		}

		public BlockModelGenerators.WoodProvider logUVLocked(Block block) {
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createPillarBlockUVLocked(block, this.logMapping, BlockModelGenerators.this.modelOutput));
			return this;
		}
	}
}
