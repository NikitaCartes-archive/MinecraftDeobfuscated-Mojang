package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BannerBlock extends AbstractBannerBlock {
	public static final MapCodec<BannerBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(DyeColor.CODEC.fieldOf("color").forGetter(AbstractBannerBlock::getColor), propertiesCodec()).apply(instance, BannerBlock::new)
	);
	public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
	private static final Map<DyeColor, Block> BY_COLOR = Maps.<DyeColor, Block>newHashMap();
	private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

	@Override
	public MapCodec<BannerBlock> codec() {
		return CODEC;
	}

	public BannerBlock(DyeColor dyeColor, BlockBehaviour.Properties properties) {
		super(dyeColor, properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, Integer.valueOf(0)));
		BY_COLOR.put(dyeColor, this);
	}

	@Override
	protected boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return levelReader.getBlockState(blockPos.below()).isSolid();
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		return this.defaultBlockState().setValue(ROTATION, Integer.valueOf(RotationSegment.convertToSegment(blockPlaceContext.getRotation() + 180.0F)));
	}

	@Override
	protected BlockState updateShape(
		BlockState blockState,
		LevelReader levelReader,
		ScheduledTickAccess scheduledTickAccess,
		BlockPos blockPos,
		Direction direction,
		BlockPos blockPos2,
		BlockState blockState2,
		RandomSource randomSource
	) {
		return direction == Direction.DOWN && !blockState.canSurvive(levelReader, blockPos)
			? Blocks.AIR.defaultBlockState()
			: super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
	}

	@Override
	protected BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(ROTATION, Integer.valueOf(rotation.rotate((Integer)blockState.getValue(ROTATION), 16)));
	}

	@Override
	protected BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.setValue(ROTATION, Integer.valueOf(mirror.mirror((Integer)blockState.getValue(ROTATION), 16)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ROTATION);
	}

	public static Block byColor(DyeColor dyeColor) {
		return (Block)BY_COLOR.getOrDefault(dyeColor, Blocks.WHITE_BANNER);
	}
}
