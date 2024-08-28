package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AttachedStemBlock extends BushBlock {
	public static final MapCodec<AttachedStemBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ResourceKey.codec(Registries.BLOCK).fieldOf("fruit").forGetter(attachedStemBlock -> attachedStemBlock.fruit),
					ResourceKey.codec(Registries.BLOCK).fieldOf("stem").forGetter(attachedStemBlock -> attachedStemBlock.stem),
					ResourceKey.codec(Registries.ITEM).fieldOf("seed").forGetter(attachedStemBlock -> attachedStemBlock.seed),
					propertiesCodec()
				)
				.apply(instance, AttachedStemBlock::new)
	);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	protected static final float AABB_OFFSET = 2.0F;
	private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(
		ImmutableMap.of(
			Direction.SOUTH,
			Block.box(6.0, 0.0, 6.0, 10.0, 10.0, 16.0),
			Direction.WEST,
			Block.box(0.0, 0.0, 6.0, 10.0, 10.0, 10.0),
			Direction.NORTH,
			Block.box(6.0, 0.0, 0.0, 10.0, 10.0, 10.0),
			Direction.EAST,
			Block.box(6.0, 0.0, 6.0, 16.0, 10.0, 10.0)
		)
	);
	private final ResourceKey<Block> fruit;
	private final ResourceKey<Block> stem;
	private final ResourceKey<Item> seed;

	@Override
	public MapCodec<AttachedStemBlock> codec() {
		return CODEC;
	}

	protected AttachedStemBlock(
		ResourceKey<Block> resourceKey, ResourceKey<Block> resourceKey2, ResourceKey<Item> resourceKey3, BlockBehaviour.Properties properties
	) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
		this.stem = resourceKey;
		this.fruit = resourceKey2;
		this.seed = resourceKey3;
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return (VoxelShape)AABBS.get(blockState.getValue(FACING));
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2
	) {
		if (!blockState2.is(this.fruit) && direction == blockState.getValue(FACING)) {
			Optional<Block> optional = levelAccessor.registryAccess().lookupOrThrow(Registries.BLOCK).getOptional(this.stem);
			if (optional.isPresent()) {
				return ((Block)optional.get()).defaultBlockState().trySetValue(StemBlock.AGE, Integer.valueOf(7));
			}
		}

		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(Blocks.FARMLAND);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
		return new ItemStack(DataFixUtils.orElse(levelReader.registryAccess().lookupOrThrow(Registries.ITEM).getOptional(this.seed), this));
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}
