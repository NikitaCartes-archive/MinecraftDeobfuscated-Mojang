package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ConcretePowderBlock extends FallingBlock {
	public static final MapCodec<ConcretePowderBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					BuiltInRegistries.BLOCK.byNameCodec().fieldOf("concrete").forGetter(concretePowderBlock -> concretePowderBlock.concrete), propertiesCodec()
				)
				.apply(instance, ConcretePowderBlock::new)
	);
	private final Block concrete;

	@Override
	public MapCodec<ConcretePowderBlock> codec() {
		return CODEC;
	}

	public ConcretePowderBlock(Block block, BlockBehaviour.Properties properties) {
		super(properties);
		this.concrete = block;
	}

	@Override
	public void onLand(Level level, BlockPos blockPos, BlockState blockState, BlockState blockState2, FallingBlockEntity fallingBlockEntity) {
		if (shouldSolidify(level, blockPos, blockState2)) {
			level.setBlock(blockPos, this.concrete.defaultBlockState(), 3);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockGetter blockGetter = blockPlaceContext.getLevel();
		BlockPos blockPos = blockPlaceContext.getClickedPos();
		BlockState blockState = blockGetter.getBlockState(blockPos);
		return shouldSolidify(blockGetter, blockPos, blockState) ? this.concrete.defaultBlockState() : super.getStateForPlacement(blockPlaceContext);
	}

	private static boolean shouldSolidify(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
		return canSolidify(blockState) || touchesLiquid(blockGetter, blockPos);
	}

	private static boolean touchesLiquid(BlockGetter blockGetter, BlockPos blockPos) {
		boolean bl = false;
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (Direction direction : Direction.values()) {
			BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
			if (direction != Direction.DOWN || canSolidify(blockState)) {
				mutableBlockPos.setWithOffset(blockPos, direction);
				blockState = blockGetter.getBlockState(mutableBlockPos);
				if (canSolidify(blockState) && !blockState.isFaceSturdy(blockGetter, blockPos, direction.getOpposite())) {
					bl = true;
					break;
				}
			}
		}

		return bl;
	}

	private static boolean canSolidify(BlockState blockState) {
		return blockState.getFluidState().is(FluidTags.WATER);
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
		return touchesLiquid(levelReader, blockPos)
			? this.concrete.defaultBlockState()
			: super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
	}

	@Override
	public int getDustColor(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.getMapColor(blockGetter, blockPos).col;
	}
}
