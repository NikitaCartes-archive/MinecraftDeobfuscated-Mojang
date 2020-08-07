package net.minecraft.data.models;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
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
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.WallSide;

public class BlockModelGenerators {
	private final Consumer<BlockStateGenerator> blockStateOutput;
	private final BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput;
	private final Consumer<Item> skippedAutoModelsOutput;

	public BlockModelGenerators(Consumer<BlockStateGenerator> consumer, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer, Consumer<Item> consumer2) {
		this.blockStateOutput = consumer;
		this.modelOutput = biConsumer;
		this.skippedAutoModelsOutput = consumer2;
	}

	private void skipAutoItemBlock(Block block) {
		this.skippedAutoModelsOutput.accept(block.asItem());
	}

	private void delegateItemModel(Block block, ResourceLocation resourceLocation) {
		this.modelOutput.accept(ModelLocationUtils.getModelLocation(block.asItem()), new DelegatedModel(resourceLocation));
	}

	private void delegateItemModel(Item item, ResourceLocation resourceLocation) {
		this.modelOutput.accept(ModelLocationUtils.getModelLocation(item), new DelegatedModel(resourceLocation));
	}

	private void createSimpleFlatItemModel(Item item) {
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

	private static BlockStateGenerator createButton(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
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
		ResourceLocation resourceLocation2
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
			.select(Direction.EAST, doubleBlockHalf, DoorHingeSide.RIGHT, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
			.select(
				Direction.SOUTH,
				doubleBlockHalf,
				DoorHingeSide.RIGHT,
				false,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
			)
			.select(
				Direction.WEST,
				doubleBlockHalf,
				DoorHingeSide.RIGHT,
				false,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
			)
			.select(
				Direction.NORTH,
				doubleBlockHalf,
				DoorHingeSide.RIGHT,
				false,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
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
				Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
			)
			.select(Direction.SOUTH, doubleBlockHalf, DoorHingeSide.RIGHT, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
			.select(
				Direction.WEST,
				doubleBlockHalf,
				DoorHingeSide.RIGHT,
				true,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
			)
			.select(
				Direction.NORTH,
				doubleBlockHalf,
				DoorHingeSide.RIGHT,
				true,
				Variant.variant().with(VariantProperties.MODEL, resourceLocation).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
			);
	}

	private static BlockStateGenerator createDoor(
		Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3, ResourceLocation resourceLocation4
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
						resourceLocation2
					),
					DoubleBlockHalf.UPPER,
					resourceLocation3,
					resourceLocation4
				)
			);
	}

	private static BlockStateGenerator createFence(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
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

	private static BlockStateGenerator createWall(
		Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3
	) {
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

	private static BlockStateGenerator createFenceGate(
		Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3, ResourceLocation resourceLocation4
	) {
		return MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.UV_LOCK, true))
			.with(createHorizontalFacingDispatchAlt())
			.with(
				PropertyDispatch.properties(BlockStateProperties.IN_WALL, BlockStateProperties.OPEN)
					.select(false, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.select(true, false, Variant.variant().with(VariantProperties.MODEL, resourceLocation4))
					.select(false, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(true, true, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
			);
	}

	private static BlockStateGenerator createStairs(
		Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3
	) {
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

	private static MultiVariantGenerator createSimpleBlock(Block block, ResourceLocation resourceLocation) {
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

	private static BlockStateGenerator createAxisAlignedPillarBlock(Block block, ResourceLocation resourceLocation) {
		return MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, resourceLocation)).with(createRotatedPillar());
	}

	private void createAxisAlignedPillarBlockCustomModel(Block block, ResourceLocation resourceLocation) {
		this.blockStateOutput.accept(createAxisAlignedPillarBlock(block, resourceLocation));
	}

	private void createAxisAlignedPillarBlock(Block block, TexturedModel.Provider provider) {
		ResourceLocation resourceLocation = provider.create(block, this.modelOutput);
		this.blockStateOutput.accept(createAxisAlignedPillarBlock(block, resourceLocation));
	}

	private void createHorizontallyRotatedBlock(Block block, TexturedModel.Provider provider) {
		ResourceLocation resourceLocation = provider.create(block, this.modelOutput);
		this.blockStateOutput
			.accept(MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, resourceLocation)).with(createHorizontalFacingDispatch()));
	}

	private static BlockStateGenerator createRotatedPillarWithHorizontalVariant(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
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

	private static BlockStateGenerator createPressurePlate(Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2) {
		return MultiVariantGenerator.multiVariant(block).with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourceLocation2, resourceLocation));
	}

	private static BlockStateGenerator createSlab(
		Block block, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3
	) {
		return MultiVariantGenerator.multiVariant(block)
			.with(
				PropertyDispatch.property(BlockStateProperties.SLAB_TYPE)
					.select(SlabType.BOTTOM, Variant.variant().with(VariantProperties.MODEL, resourceLocation))
					.select(SlabType.TOP, Variant.variant().with(VariantProperties.MODEL, resourceLocation2))
					.select(SlabType.DOUBLE, Variant.variant().with(VariantProperties.MODEL, resourceLocation3))
			);
	}

	private void createTrivialCube(Block block) {
		this.createTrivialBlock(block, TexturedModel.CUBE);
	}

	private void createTrivialBlock(Block block, TexturedModel.Provider provider) {
		this.blockStateOutput.accept(createSimpleBlock(block, provider.create(block, this.modelOutput)));
	}

	private void createTrivialBlock(Block block, TextureMapping textureMapping, ModelTemplate modelTemplate) {
		ResourceLocation resourceLocation = modelTemplate.create(block, textureMapping, this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block, resourceLocation));
	}

	private BlockModelGenerators.BlockFamilyProvider family(Block block, TexturedModel texturedModel) {
		return new BlockModelGenerators.BlockFamilyProvider(texturedModel.getMapping()).fullBlock(block, texturedModel.getTemplate());
	}

	private BlockModelGenerators.BlockFamilyProvider family(Block block, TexturedModel.Provider provider) {
		TexturedModel texturedModel = provider.get(block);
		return new BlockModelGenerators.BlockFamilyProvider(texturedModel.getMapping()).fullBlock(block, texturedModel.getTemplate());
	}

	private BlockModelGenerators.BlockFamilyProvider family(Block block) {
		return this.family(block, TexturedModel.CUBE);
	}

	private BlockModelGenerators.BlockFamilyProvider family(TextureMapping textureMapping) {
		return new BlockModelGenerators.BlockFamilyProvider(textureMapping);
	}

	private void createDoor(Block block) {
		TextureMapping textureMapping = TextureMapping.door(block);
		ResourceLocation resourceLocation = ModelTemplates.DOOR_BOTTOM.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.DOOR_BOTTOM_HINGE.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.DOOR_TOP.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation4 = ModelTemplates.DOOR_TOP_HINGE.create(block, textureMapping, this.modelOutput);
		this.createSimpleFlatItemModel(block.asItem());
		this.blockStateOutput.accept(createDoor(block, resourceLocation, resourceLocation2, resourceLocation3, resourceLocation4));
	}

	private void createOrientableTrapdoor(Block block) {
		TextureMapping textureMapping = TextureMapping.defaultTexture(block);
		ResourceLocation resourceLocation = ModelTemplates.ORIENTABLE_TRAPDOOR_TOP.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.ORIENTABLE_TRAPDOOR_BOTTOM.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.ORIENTABLE_TRAPDOOR_OPEN.create(block, textureMapping, this.modelOutput);
		this.blockStateOutput.accept(createOrientableTrapdoor(block, resourceLocation, resourceLocation2, resourceLocation3));
		this.delegateItemModel(block, resourceLocation2);
	}

	private void createTrapdoor(Block block) {
		TextureMapping textureMapping = TextureMapping.defaultTexture(block);
		ResourceLocation resourceLocation = ModelTemplates.TRAPDOOR_TOP.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation2 = ModelTemplates.TRAPDOOR_BOTTOM.create(block, textureMapping, this.modelOutput);
		ResourceLocation resourceLocation3 = ModelTemplates.TRAPDOOR_OPEN.create(block, textureMapping, this.modelOutput);
		this.blockStateOutput.accept(createTrapdoor(block, resourceLocation, resourceLocation2, resourceLocation3));
		this.delegateItemModel(block, resourceLocation2);
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

	private void createWoolBlocks(Block block, Block block2) {
		this.createTrivialBlock(block, TexturedModel.CUBE);
		ResourceLocation resourceLocation = TexturedModel.CARPET.get(block).create(block2, this.modelOutput);
		this.blockStateOutput.accept(createSimpleBlock(block2, resourceLocation));
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
							i, j -> this.createSuffixedVariant(block, "_stage" + i, ModelTemplates.CROP, TextureMapping::crop)
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

	private void createCauldron() {
		this.createSimpleFlatItemModel(Items.CAULDRON);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.CAULDRON)
					.with(
						PropertyDispatch.property(BlockStateProperties.LEVEL_CAULDRON)
							.select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAULDRON)))
							.select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAULDRON, "_level1")))
							.select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAULDRON, "_level2")))
							.select(3, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAULDRON, "_level3")))
					)
			);
	}

	private void createChiseledSandsone(Block block, Block block2) {
		TextureMapping textureMapping = new TextureMapping()
			.put(TextureSlot.END, TextureMapping.getBlockTexture(block2, "_top"))
			.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block));
		this.createTrivialBlock(block, textureMapping, ModelTemplates.CUBE_COLUMN);
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

	private void createGrassPath() {
		this.blockStateOutput.accept(createRotatedVariant(Blocks.GRASS_PATH, ModelLocationUtils.getModelLocation(Blocks.GRASS_PATH)));
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

	private void createScaffolding() {
		ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_stable");
		ResourceLocation resourceLocation2 = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_unstable");
		this.delegateItemModel(Blocks.SCAFFOLDING, resourceLocation);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.SCAFFOLDING).with(createBooleanModelDispatch(BlockStateProperties.BOTTOM, resourceLocation2, resourceLocation))
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

	private void createVine() {
		this.createSimpleFlatItemModel(Blocks.VINE);
		this.blockStateOutput
			.accept(
				MultiVariantGenerator.multiVariant(Blocks.VINE)
					.with(
						PropertyDispatch.properties(
								BlockStateProperties.EAST, BlockStateProperties.NORTH, BlockStateProperties.SOUTH, BlockStateProperties.UP, BlockStateProperties.WEST
							)
							.select(false, false, false, false, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1")))
							.select(false, false, true, false, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1")))
							.select(
								false,
								false,
								false,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								false,
								true,
								false,
								false,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								true,
								false,
								false,
								false,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(true, true, false, false, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2")))
							.select(
								true,
								false,
								true,
								false,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								false,
								false,
								true,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								false,
								true,
								false,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(
								true, false, false, false, true, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2_opposite"))
							)
							.select(
								false,
								true,
								true,
								false,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2_opposite"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(true, true, true, false, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3")))
							.select(
								true,
								false,
								true,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								false,
								true,
								true,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								true,
								true,
								false,
								false,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(true, true, true, false, true, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_4")))
							.select(false, false, false, true, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_u")))
							.select(false, false, true, true, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1u")))
							.select(
								false,
								false,
								false,
								true,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1u"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								false,
								true,
								false,
								true,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1u"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								true,
								false,
								false,
								true,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_1u"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(true, true, false, true, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2u")))
							.select(
								true,
								false,
								true,
								true,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2u"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								false,
								false,
								true,
								true,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2u"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								false,
								true,
								false,
								true,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2u"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(
								true, false, false, true, true, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2u_opposite"))
							)
							.select(
								false,
								true,
								true,
								true,
								false,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_2u_opposite"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(true, true, true, true, false, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3u")))
							.select(
								true,
								false,
								true,
								true,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3u"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
							)
							.select(
								false,
								true,
								true,
								true,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3u"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
							)
							.select(
								true,
								true,
								false,
								true,
								true,
								Variant.variant()
									.with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_3u"))
									.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
							)
							.select(true, true, true, true, true, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.VINE, "_4u")))
					)
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

	public void run() {
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
		this.createNonTemplateModelBlock(Blocks.POTTED_BAMBOO);
		this.createNonTemplateModelBlock(Blocks.POTTED_CACTUS);
		this.createAirLikeBlock(Blocks.BARRIER, Items.BARRIER);
		this.createSimpleFlatItemModel(Items.BARRIER);
		this.createAirLikeBlock(Blocks.STRUCTURE_VOID, Items.STRUCTURE_VOID);
		this.createSimpleFlatItemModel(Items.STRUCTURE_VOID);
		this.createAirLikeBlock(Blocks.MOVING_PISTON, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
		this.createTrivialBlock(Blocks.COAL_ORE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.COAL_BLOCK, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.DIAMOND_ORE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.DIAMOND_BLOCK, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.EMERALD_ORE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.EMERALD_BLOCK, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.GOLD_ORE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.NETHER_GOLD_ORE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.GOLD_BLOCK, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.IRON_ORE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.IRON_BLOCK, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.ANCIENT_DEBRIS, TexturedModel.COLUMN);
		this.createTrivialBlock(Blocks.NETHERITE_BLOCK, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.LAPIS_ORE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.LAPIS_BLOCK, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.NETHER_QUARTZ_ORE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.REDSTONE_ORE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.REDSTONE_BLOCK, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.GILDED_BLACKSTONE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.BLUE_ICE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.CHISELED_NETHER_BRICKS, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.CLAY, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.COARSE_DIRT, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.CRACKED_NETHER_BRICKS, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.CRACKED_STONE_BRICKS, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.CRYING_OBSIDIAN, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.END_STONE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.GLOWSTONE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.GRAVEL, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.HONEYCOMB_BLOCK, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.ICE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.JUKEBOX, TexturedModel.CUBE_TOP);
		this.createTrivialBlock(Blocks.LODESTONE, TexturedModel.COLUMN);
		this.createTrivialBlock(Blocks.MELON, TexturedModel.COLUMN);
		this.createTrivialBlock(Blocks.NETHER_WART_BLOCK, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.NOTE_BLOCK, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.PACKED_ICE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.OBSIDIAN, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.QUARTZ_BRICKS, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.SEA_LANTERN, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.SHROOMLIGHT, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.SOUL_SAND, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.SOUL_SOIL, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.SPAWNER, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.SPONGE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.SEAGRASS, TexturedModel.SEAGRASS);
		this.createSimpleFlatItemModel(Items.SEAGRASS);
		this.createTrivialBlock(Blocks.TNT, TexturedModel.CUBE_TOP_BOTTOM);
		this.createTrivialBlock(Blocks.TARGET, TexturedModel.COLUMN);
		this.createTrivialBlock(Blocks.WARPED_WART_BLOCK, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.WET_SPONGE, TexturedModel.CUBE);
		this.createTrivialBlock(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, TexturedModel.CUBE);
		this.createTrivialBlock(
			Blocks.CHISELED_QUARTZ_BLOCK,
			TexturedModel.COLUMN.updateTexture(textureMapping -> textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_QUARTZ_BLOCK)))
		);
		this.createTrivialBlock(Blocks.CHISELED_STONE_BRICKS, TexturedModel.CUBE);
		this.createChiseledSandsone(Blocks.CHISELED_SANDSTONE, Blocks.SANDSTONE);
		this.createChiseledSandsone(Blocks.CHISELED_RED_SANDSTONE, Blocks.RED_SANDSTONE);
		this.createTrivialBlock(Blocks.CHISELED_POLISHED_BLACKSTONE, TexturedModel.CUBE);
		this.createWeightedPressurePlate(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.GOLD_BLOCK);
		this.createWeightedPressurePlate(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.IRON_BLOCK);
		this.createBookshelf();
		this.createBrewingStand();
		this.createCakeBlock();
		this.createCampfires(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
		this.createCartographyTable();
		this.createCauldron();
		this.createChorusFlower();
		this.createChorusPlant();
		this.createComposter();
		this.createDaylightDetector();
		this.createEndPortalFrame();
		this.createRotatableColumn(Blocks.END_ROD);
		this.createFarmland();
		this.createFire();
		this.createSoulFire();
		this.createFrostedIce();
		this.createGrassBlocks();
		this.createCocoa();
		this.createGrassPath();
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
		this.createVine();
		this.createMagmaBlock();
		this.createJigsaw();
		this.createNonTemplateHorizontalBlock(Blocks.LADDER);
		this.createSimpleFlatItemModel(Blocks.LADDER);
		this.createNonTemplateHorizontalBlock(Blocks.LECTERN);
		this.createNormalTorch(Blocks.TORCH, Blocks.WALL_TORCH);
		this.createNormalTorch(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH);
		this.createCraftingTableLike(Blocks.CRAFTING_TABLE, Blocks.OAK_PLANKS, TextureMapping::craftingTable);
		this.createCraftingTableLike(Blocks.FLETCHING_TABLE, Blocks.BIRCH_PLANKS, TextureMapping::fletchingTable);
		this.createNyliumBlock(Blocks.CRIMSON_NYLIUM);
		this.createNyliumBlock(Blocks.WARPED_NYLIUM);
		this.createDispenserBlock(Blocks.DISPENSER);
		this.createDispenserBlock(Blocks.DROPPER);
		this.createLantern(Blocks.LANTERN);
		this.createLantern(Blocks.SOUL_LANTERN);
		this.createAxisAlignedPillarBlockCustomModel(Blocks.CHAIN, ModelLocationUtils.getModelLocation(Blocks.CHAIN));
		this.createAxisAlignedPillarBlock(Blocks.BASALT, TexturedModel.COLUMN);
		this.createAxisAlignedPillarBlock(Blocks.POLISHED_BASALT, TexturedModel.COLUMN);
		this.createAxisAlignedPillarBlock(Blocks.BONE_BLOCK, TexturedModel.COLUMN);
		this.createRotatedVariantBlock(Blocks.DIRT);
		this.createRotatedVariantBlock(Blocks.SAND);
		this.createRotatedVariantBlock(Blocks.RED_SAND);
		this.createRotatedMirroredVariantBlock(Blocks.BEDROCK);
		this.createRotatedPillarWithHorizontalVariant(Blocks.HAY_BLOCK, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
		this.createRotatedPillarWithHorizontalVariant(Blocks.PURPUR_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
		this.createRotatedPillarWithHorizontalVariant(Blocks.QUARTZ_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
		this.createHorizontallyRotatedBlock(Blocks.LOOM, TexturedModel.ORIENTABLE);
		this.createPumpkins();
		this.createBeeNest(Blocks.BEE_NEST, TextureMapping::orientableCube);
		this.createBeeNest(Blocks.BEEHIVE, TextureMapping::orientableCubeSameEnds);
		this.createCropBlock(Blocks.BEETROOTS, BlockStateProperties.AGE_3, 0, 1, 2, 3);
		this.createCropBlock(Blocks.CARROTS, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
		this.createCropBlock(Blocks.NETHER_WART, BlockStateProperties.AGE_3, 0, 1, 1, 2);
		this.createCropBlock(Blocks.POTATOES, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
		this.createCropBlock(Blocks.WHEAT, BlockStateProperties.AGE_7, 0, 1, 2, 3, 4, 5, 6, 7);
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
				ModelTemplates.SKULL_INVENTORY, Blocks.CREEPER_HEAD, Blocks.PLAYER_HEAD, Blocks.ZOMBIE_HEAD, Blocks.SKELETON_SKULL, Blocks.WITHER_SKELETON_SKULL
			)
			.create(Blocks.DRAGON_HEAD)
			.createWithoutBlockItem(
				Blocks.CREEPER_WALL_HEAD,
				Blocks.DRAGON_WALL_HEAD,
				Blocks.PLAYER_WALL_HEAD,
				Blocks.ZOMBIE_WALL_HEAD,
				Blocks.SKELETON_WALL_SKULL,
				Blocks.WITHER_SKELETON_WALL_SKULL
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
		this.createWoolBlocks(Blocks.WHITE_WOOL, Blocks.WHITE_CARPET);
		this.createWoolBlocks(Blocks.ORANGE_WOOL, Blocks.ORANGE_CARPET);
		this.createWoolBlocks(Blocks.MAGENTA_WOOL, Blocks.MAGENTA_CARPET);
		this.createWoolBlocks(Blocks.LIGHT_BLUE_WOOL, Blocks.LIGHT_BLUE_CARPET);
		this.createWoolBlocks(Blocks.YELLOW_WOOL, Blocks.YELLOW_CARPET);
		this.createWoolBlocks(Blocks.LIME_WOOL, Blocks.LIME_CARPET);
		this.createWoolBlocks(Blocks.PINK_WOOL, Blocks.PINK_CARPET);
		this.createWoolBlocks(Blocks.GRAY_WOOL, Blocks.GRAY_CARPET);
		this.createWoolBlocks(Blocks.LIGHT_GRAY_WOOL, Blocks.LIGHT_GRAY_CARPET);
		this.createWoolBlocks(Blocks.CYAN_WOOL, Blocks.CYAN_CARPET);
		this.createWoolBlocks(Blocks.PURPLE_WOOL, Blocks.PURPLE_CARPET);
		this.createWoolBlocks(Blocks.BLUE_WOOL, Blocks.BLUE_CARPET);
		this.createWoolBlocks(Blocks.BROWN_WOOL, Blocks.BROWN_CARPET);
		this.createWoolBlocks(Blocks.GREEN_WOOL, Blocks.GREEN_CARPET);
		this.createWoolBlocks(Blocks.RED_WOOL, Blocks.RED_CARPET);
		this.createWoolBlocks(Blocks.BLACK_WOOL, Blocks.BLACK_CARPET);
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
		this.createMushroomBlock(Blocks.BROWN_MUSHROOM_BLOCK);
		this.createMushroomBlock(Blocks.RED_MUSHROOM_BLOCK);
		this.createMushroomBlock(Blocks.MUSHROOM_STEM);
		this.createCrossBlockWithDefaultItem(Blocks.GRASS, BlockModelGenerators.TintState.TINTED);
		this.createCrossBlock(Blocks.SUGAR_CANE, BlockModelGenerators.TintState.TINTED);
		this.createSimpleFlatItemModel(Items.SUGAR_CANE);
		this.createGrowingPlant(Blocks.KELP, Blocks.KELP_PLANT, BlockModelGenerators.TintState.TINTED);
		this.createSimpleFlatItemModel(Items.KELP);
		this.skipAutoItemBlock(Blocks.KELP_PLANT);
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
		this.family(Blocks.ACACIA_PLANKS)
			.button(Blocks.ACACIA_BUTTON)
			.fence(Blocks.ACACIA_FENCE)
			.fenceGate(Blocks.ACACIA_FENCE_GATE)
			.pressurePlate(Blocks.ACACIA_PRESSURE_PLATE)
			.sign(Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN)
			.slab(Blocks.ACACIA_SLAB)
			.stairs(Blocks.ACACIA_STAIRS);
		this.createDoor(Blocks.ACACIA_DOOR);
		this.createOrientableTrapdoor(Blocks.ACACIA_TRAPDOOR);
		this.woodProvider(Blocks.ACACIA_LOG).logWithHorizontal(Blocks.ACACIA_LOG).wood(Blocks.ACACIA_WOOD);
		this.woodProvider(Blocks.STRIPPED_ACACIA_LOG).logWithHorizontal(Blocks.STRIPPED_ACACIA_LOG).wood(Blocks.STRIPPED_ACACIA_WOOD);
		this.createPlant(Blocks.ACACIA_SAPLING, Blocks.POTTED_ACACIA_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.ACACIA_LEAVES, TexturedModel.LEAVES);
		this.family(Blocks.BIRCH_PLANKS)
			.button(Blocks.BIRCH_BUTTON)
			.fence(Blocks.BIRCH_FENCE)
			.fenceGate(Blocks.BIRCH_FENCE_GATE)
			.pressurePlate(Blocks.BIRCH_PRESSURE_PLATE)
			.sign(Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN)
			.slab(Blocks.BIRCH_SLAB)
			.stairs(Blocks.BIRCH_STAIRS);
		this.createDoor(Blocks.BIRCH_DOOR);
		this.createOrientableTrapdoor(Blocks.BIRCH_TRAPDOOR);
		this.woodProvider(Blocks.BIRCH_LOG).logWithHorizontal(Blocks.BIRCH_LOG).wood(Blocks.BIRCH_WOOD);
		this.woodProvider(Blocks.STRIPPED_BIRCH_LOG).logWithHorizontal(Blocks.STRIPPED_BIRCH_LOG).wood(Blocks.STRIPPED_BIRCH_WOOD);
		this.createPlant(Blocks.BIRCH_SAPLING, Blocks.POTTED_BIRCH_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.BIRCH_LEAVES, TexturedModel.LEAVES);
		this.family(Blocks.OAK_PLANKS)
			.button(Blocks.OAK_BUTTON)
			.fence(Blocks.OAK_FENCE)
			.fenceGate(Blocks.OAK_FENCE_GATE)
			.pressurePlate(Blocks.OAK_PRESSURE_PLATE)
			.sign(Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN)
			.slab(Blocks.OAK_SLAB)
			.slab(Blocks.PETRIFIED_OAK_SLAB)
			.stairs(Blocks.OAK_STAIRS);
		this.createDoor(Blocks.OAK_DOOR);
		this.createTrapdoor(Blocks.OAK_TRAPDOOR);
		this.woodProvider(Blocks.OAK_LOG).logWithHorizontal(Blocks.OAK_LOG).wood(Blocks.OAK_WOOD);
		this.woodProvider(Blocks.STRIPPED_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_OAK_LOG).wood(Blocks.STRIPPED_OAK_WOOD);
		this.createPlant(Blocks.OAK_SAPLING, Blocks.POTTED_OAK_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.OAK_LEAVES, TexturedModel.LEAVES);
		this.family(Blocks.SPRUCE_PLANKS)
			.button(Blocks.SPRUCE_BUTTON)
			.fence(Blocks.SPRUCE_FENCE)
			.fenceGate(Blocks.SPRUCE_FENCE_GATE)
			.pressurePlate(Blocks.SPRUCE_PRESSURE_PLATE)
			.sign(Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN)
			.slab(Blocks.SPRUCE_SLAB)
			.stairs(Blocks.SPRUCE_STAIRS);
		this.createDoor(Blocks.SPRUCE_DOOR);
		this.createOrientableTrapdoor(Blocks.SPRUCE_TRAPDOOR);
		this.woodProvider(Blocks.SPRUCE_LOG).logWithHorizontal(Blocks.SPRUCE_LOG).wood(Blocks.SPRUCE_WOOD);
		this.woodProvider(Blocks.STRIPPED_SPRUCE_LOG).logWithHorizontal(Blocks.STRIPPED_SPRUCE_LOG).wood(Blocks.STRIPPED_SPRUCE_WOOD);
		this.createPlant(Blocks.SPRUCE_SAPLING, Blocks.POTTED_SPRUCE_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.SPRUCE_LEAVES, TexturedModel.LEAVES);
		this.family(Blocks.DARK_OAK_PLANKS)
			.button(Blocks.DARK_OAK_BUTTON)
			.fence(Blocks.DARK_OAK_FENCE)
			.fenceGate(Blocks.DARK_OAK_FENCE_GATE)
			.pressurePlate(Blocks.DARK_OAK_PRESSURE_PLATE)
			.sign(Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN)
			.slab(Blocks.DARK_OAK_SLAB)
			.stairs(Blocks.DARK_OAK_STAIRS);
		this.createDoor(Blocks.DARK_OAK_DOOR);
		this.createTrapdoor(Blocks.DARK_OAK_TRAPDOOR);
		this.woodProvider(Blocks.DARK_OAK_LOG).logWithHorizontal(Blocks.DARK_OAK_LOG).wood(Blocks.DARK_OAK_WOOD);
		this.woodProvider(Blocks.STRIPPED_DARK_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_DARK_OAK_LOG).wood(Blocks.STRIPPED_DARK_OAK_WOOD);
		this.createPlant(Blocks.DARK_OAK_SAPLING, Blocks.POTTED_DARK_OAK_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.DARK_OAK_LEAVES, TexturedModel.LEAVES);
		this.family(Blocks.JUNGLE_PLANKS)
			.button(Blocks.JUNGLE_BUTTON)
			.fence(Blocks.JUNGLE_FENCE)
			.fenceGate(Blocks.JUNGLE_FENCE_GATE)
			.pressurePlate(Blocks.JUNGLE_PRESSURE_PLATE)
			.sign(Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN)
			.slab(Blocks.JUNGLE_SLAB)
			.stairs(Blocks.JUNGLE_STAIRS);
		this.createDoor(Blocks.JUNGLE_DOOR);
		this.createOrientableTrapdoor(Blocks.JUNGLE_TRAPDOOR);
		this.woodProvider(Blocks.JUNGLE_LOG).logWithHorizontal(Blocks.JUNGLE_LOG).wood(Blocks.JUNGLE_WOOD);
		this.woodProvider(Blocks.STRIPPED_JUNGLE_LOG).logWithHorizontal(Blocks.STRIPPED_JUNGLE_LOG).wood(Blocks.STRIPPED_JUNGLE_WOOD);
		this.createPlant(Blocks.JUNGLE_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, BlockModelGenerators.TintState.NOT_TINTED);
		this.createTrivialBlock(Blocks.JUNGLE_LEAVES, TexturedModel.LEAVES);
		this.family(Blocks.CRIMSON_PLANKS)
			.button(Blocks.CRIMSON_BUTTON)
			.fence(Blocks.CRIMSON_FENCE)
			.fenceGate(Blocks.CRIMSON_FENCE_GATE)
			.pressurePlate(Blocks.CRIMSON_PRESSURE_PLATE)
			.sign(Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN)
			.slab(Blocks.CRIMSON_SLAB)
			.stairs(Blocks.CRIMSON_STAIRS);
		this.createDoor(Blocks.CRIMSON_DOOR);
		this.createOrientableTrapdoor(Blocks.CRIMSON_TRAPDOOR);
		this.woodProvider(Blocks.CRIMSON_STEM).log(Blocks.CRIMSON_STEM).wood(Blocks.CRIMSON_HYPHAE);
		this.woodProvider(Blocks.STRIPPED_CRIMSON_STEM).log(Blocks.STRIPPED_CRIMSON_STEM).wood(Blocks.STRIPPED_CRIMSON_HYPHAE);
		this.createPlant(Blocks.CRIMSON_FUNGUS, Blocks.POTTED_CRIMSON_FUNGUS, BlockModelGenerators.TintState.NOT_TINTED);
		this.createNetherRoots(Blocks.CRIMSON_ROOTS, Blocks.POTTED_CRIMSON_ROOTS);
		this.family(Blocks.WARPED_PLANKS)
			.button(Blocks.WARPED_BUTTON)
			.fence(Blocks.WARPED_FENCE)
			.fenceGate(Blocks.WARPED_FENCE_GATE)
			.pressurePlate(Blocks.WARPED_PRESSURE_PLATE)
			.sign(Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN)
			.slab(Blocks.WARPED_SLAB)
			.stairs(Blocks.WARPED_STAIRS);
		this.createDoor(Blocks.WARPED_DOOR);
		this.createOrientableTrapdoor(Blocks.WARPED_TRAPDOOR);
		this.woodProvider(Blocks.WARPED_STEM).log(Blocks.WARPED_STEM).wood(Blocks.WARPED_HYPHAE);
		this.woodProvider(Blocks.STRIPPED_WARPED_STEM).log(Blocks.STRIPPED_WARPED_STEM).wood(Blocks.STRIPPED_WARPED_HYPHAE);
		this.createPlant(Blocks.WARPED_FUNGUS, Blocks.POTTED_WARPED_FUNGUS, BlockModelGenerators.TintState.NOT_TINTED);
		this.createNetherRoots(Blocks.WARPED_ROOTS, Blocks.POTTED_WARPED_ROOTS);
		this.createCrossBlock(Blocks.NETHER_SPROUTS, BlockModelGenerators.TintState.NOT_TINTED);
		this.createSimpleFlatItemModel(Items.NETHER_SPROUTS);
		this.family(TextureMapping.cube(Blocks.STONE)).fullBlock(textureMapping -> {
			ResourceLocation resourceLocation = ModelTemplates.CUBE_ALL.create(Blocks.STONE, textureMapping, this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.CUBE_MIRRORED_ALL.create(Blocks.STONE, textureMapping, this.modelOutput);
			this.blockStateOutput.accept(createRotatedVariant(Blocks.STONE, resourceLocation, resourceLocation2));
			return resourceLocation;
		}).slab(Blocks.STONE_SLAB).pressurePlate(Blocks.STONE_PRESSURE_PLATE).button(Blocks.STONE_BUTTON).stairs(Blocks.STONE_STAIRS);
		this.createDoor(Blocks.IRON_DOOR);
		this.createTrapdoor(Blocks.IRON_TRAPDOOR);
		this.family(Blocks.STONE_BRICKS).wall(Blocks.STONE_BRICK_WALL).stairs(Blocks.STONE_BRICK_STAIRS).slab(Blocks.STONE_BRICK_SLAB);
		this.family(Blocks.MOSSY_STONE_BRICKS).wall(Blocks.MOSSY_STONE_BRICK_WALL).stairs(Blocks.MOSSY_STONE_BRICK_STAIRS).slab(Blocks.MOSSY_STONE_BRICK_SLAB);
		this.family(Blocks.COBBLESTONE).wall(Blocks.COBBLESTONE_WALL).stairs(Blocks.COBBLESTONE_STAIRS).slab(Blocks.COBBLESTONE_SLAB);
		this.family(Blocks.MOSSY_COBBLESTONE).wall(Blocks.MOSSY_COBBLESTONE_WALL).stairs(Blocks.MOSSY_COBBLESTONE_STAIRS).slab(Blocks.MOSSY_COBBLESTONE_SLAB);
		this.family(Blocks.PRISMARINE).wall(Blocks.PRISMARINE_WALL).stairs(Blocks.PRISMARINE_STAIRS).slab(Blocks.PRISMARINE_SLAB);
		this.family(Blocks.PRISMARINE_BRICKS).stairs(Blocks.PRISMARINE_BRICK_STAIRS).slab(Blocks.PRISMARINE_BRICK_SLAB);
		this.family(Blocks.DARK_PRISMARINE).stairs(Blocks.DARK_PRISMARINE_STAIRS).slab(Blocks.DARK_PRISMARINE_SLAB);
		this.family(Blocks.SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL).wall(Blocks.SANDSTONE_WALL).stairs(Blocks.SANDSTONE_STAIRS).slab(Blocks.SANDSTONE_SLAB);
		this.family(Blocks.SMOOTH_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.SANDSTONE, "_top")))
			.slab(Blocks.SMOOTH_SANDSTONE_SLAB)
			.stairs(Blocks.SMOOTH_SANDSTONE_STAIRS);
		this.family(
				Blocks.CUT_SANDSTONE,
				TexturedModel.COLUMN
					.get(Blocks.SANDSTONE)
					.updateTextures(textureMapping -> textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_SANDSTONE)))
			)
			.slab(Blocks.CUT_SANDSTONE_SLAB);
		this.family(Blocks.RED_SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL)
			.wall(Blocks.RED_SANDSTONE_WALL)
			.stairs(Blocks.RED_SANDSTONE_STAIRS)
			.slab(Blocks.RED_SANDSTONE_SLAB);
		this.family(Blocks.SMOOTH_RED_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.RED_SANDSTONE, "_top")))
			.slab(Blocks.SMOOTH_RED_SANDSTONE_SLAB)
			.stairs(Blocks.SMOOTH_RED_SANDSTONE_STAIRS);
		this.family(
				Blocks.CUT_RED_SANDSTONE,
				TexturedModel.COLUMN
					.get(Blocks.RED_SANDSTONE)
					.updateTextures(textureMapping -> textureMapping.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_RED_SANDSTONE)))
			)
			.slab(Blocks.CUT_RED_SANDSTONE_SLAB);
		this.family(Blocks.BRICKS).wall(Blocks.BRICK_WALL).stairs(Blocks.BRICK_STAIRS).slab(Blocks.BRICK_SLAB);
		this.family(Blocks.NETHER_BRICKS)
			.fence(Blocks.NETHER_BRICK_FENCE)
			.wall(Blocks.NETHER_BRICK_WALL)
			.stairs(Blocks.NETHER_BRICK_STAIRS)
			.slab(Blocks.NETHER_BRICK_SLAB);
		this.family(Blocks.PURPUR_BLOCK).stairs(Blocks.PURPUR_STAIRS).slab(Blocks.PURPUR_SLAB);
		this.family(Blocks.DIORITE).wall(Blocks.DIORITE_WALL).stairs(Blocks.DIORITE_STAIRS).slab(Blocks.DIORITE_SLAB);
		this.family(Blocks.POLISHED_DIORITE).stairs(Blocks.POLISHED_DIORITE_STAIRS).slab(Blocks.POLISHED_DIORITE_SLAB);
		this.family(Blocks.GRANITE).wall(Blocks.GRANITE_WALL).stairs(Blocks.GRANITE_STAIRS).slab(Blocks.GRANITE_SLAB);
		this.family(Blocks.POLISHED_GRANITE).stairs(Blocks.POLISHED_GRANITE_STAIRS).slab(Blocks.POLISHED_GRANITE_SLAB);
		this.family(Blocks.ANDESITE).wall(Blocks.ANDESITE_WALL).stairs(Blocks.ANDESITE_STAIRS).slab(Blocks.ANDESITE_SLAB);
		this.family(Blocks.POLISHED_ANDESITE).stairs(Blocks.POLISHED_ANDESITE_STAIRS).slab(Blocks.POLISHED_ANDESITE_SLAB);
		this.family(Blocks.END_STONE_BRICKS).wall(Blocks.END_STONE_BRICK_WALL).stairs(Blocks.END_STONE_BRICK_STAIRS).slab(Blocks.END_STONE_BRICK_SLAB);
		this.family(Blocks.QUARTZ_BLOCK, TexturedModel.COLUMN).stairs(Blocks.QUARTZ_STAIRS).slab(Blocks.QUARTZ_SLAB);
		this.family(Blocks.SMOOTH_QUARTZ, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.QUARTZ_BLOCK, "_bottom")))
			.stairs(Blocks.SMOOTH_QUARTZ_STAIRS)
			.slab(Blocks.SMOOTH_QUARTZ_SLAB);
		this.family(Blocks.RED_NETHER_BRICKS).slab(Blocks.RED_NETHER_BRICK_SLAB).stairs(Blocks.RED_NETHER_BRICK_STAIRS).wall(Blocks.RED_NETHER_BRICK_WALL);
		this.family(Blocks.BLACKSTONE, TexturedModel.COLUMN_WITH_WALL).wall(Blocks.BLACKSTONE_WALL).stairs(Blocks.BLACKSTONE_STAIRS).slab(Blocks.BLACKSTONE_SLAB);
		this.family(Blocks.POLISHED_BLACKSTONE_BRICKS)
			.wall(Blocks.POLISHED_BLACKSTONE_BRICK_WALL)
			.stairs(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)
			.slab(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
		this.family(Blocks.POLISHED_BLACKSTONE)
			.wall(Blocks.POLISHED_BLACKSTONE_WALL)
			.pressurePlate(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE)
			.button(Blocks.POLISHED_BLACKSTONE_BUTTON)
			.stairs(Blocks.POLISHED_BLACKSTONE_STAIRS)
			.slab(Blocks.POLISHED_BLACKSTONE_SLAB);
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
		this.copyModel(Blocks.CHISELED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS);
		this.copyModel(Blocks.COBBLESTONE, Blocks.INFESTED_COBBLESTONE);
		this.copyModel(Blocks.CRACKED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS);
		this.copyModel(Blocks.MOSSY_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS);
		this.createInfestedStone();
		this.copyModel(Blocks.STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
		SpawnEggItem.eggs().forEach(spawnEggItem -> this.delegateItemModel(spawnEggItem, ModelLocationUtils.decorateItemModelLocation("template_spawn_egg")));
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
		@Nullable
		private ResourceLocation fullBlock;

		public BlockFamilyProvider(TextureMapping textureMapping) {
			this.mapping = textureMapping;
		}

		public BlockModelGenerators.BlockFamilyProvider fullBlock(Block block, ModelTemplate modelTemplate) {
			this.fullBlock = modelTemplate.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, this.fullBlock));
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider fullBlock(Function<TextureMapping, ResourceLocation> function) {
			this.fullBlock = (ResourceLocation)function.apply(this.mapping);
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

		public BlockModelGenerators.BlockFamilyProvider fence(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.FENCE_POST.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.FENCE_SIDE.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createFence(block, resourceLocation, resourceLocation2));
			ResourceLocation resourceLocation3 = ModelTemplates.FENCE_INVENTORY.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.delegateItemModel(block, resourceLocation3);
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider fenceGate(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.FENCE_GATE_OPEN.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.FENCE_GATE_CLOSED.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation3 = ModelTemplates.FENCE_GATE_WALL_OPEN.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation4 = ModelTemplates.FENCE_GATE_WALL_CLOSED.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput
				.accept(BlockModelGenerators.createFenceGate(block, resourceLocation, resourceLocation2, resourceLocation3, resourceLocation4));
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider pressurePlate(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.PRESSURE_PLATE_UP.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.PRESSURE_PLATE_DOWN.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createPressurePlate(block, resourceLocation, resourceLocation2));
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider sign(Block block, Block block2) {
			ResourceLocation resourceLocation = ModelTemplates.PARTICLE_ONLY.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, resourceLocation));
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block2, resourceLocation));
			BlockModelGenerators.this.createSimpleFlatItemModel(block.asItem());
			BlockModelGenerators.this.skipAutoItemBlock(block2);
			return this;
		}

		public BlockModelGenerators.BlockFamilyProvider slab(Block block) {
			if (this.fullBlock == null) {
				throw new IllegalStateException("Full block not generated yet");
			} else {
				ResourceLocation resourceLocation = ModelTemplates.SLAB_BOTTOM.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
				ResourceLocation resourceLocation2 = ModelTemplates.SLAB_TOP.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
				BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSlab(block, resourceLocation, resourceLocation2, this.fullBlock));
				return this;
			}
		}

		public BlockModelGenerators.BlockFamilyProvider stairs(Block block) {
			ResourceLocation resourceLocation = ModelTemplates.STAIRS_INNER.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation2 = ModelTemplates.STAIRS_STRAIGHT.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			ResourceLocation resourceLocation3 = ModelTemplates.STAIRS_OUTER.create(block, this.mapping, BlockModelGenerators.this.modelOutput);
			BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createStairs(block, resourceLocation, resourceLocation2, resourceLocation3));
			return this;
		}
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
	}
}
