package net.minecraft.world.grid;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FloataterBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public record SubGridCapture(SubGridBlocks blocks, LongSet mask, BlockPos minPos, int engines) {
	private static final Direction[] DIRECTIONS = Direction.values();

	@Nullable
	public static SubGridCapture scan(Level level, BlockPos blockPos, Direction direction) {
		Long2ObjectMap<BlockState> long2ObjectMap = new Long2ObjectOpenHashMap<>();
		LongArrayFIFOQueue longArrayFIFOQueue = new LongArrayFIFOQueue();
		long2ObjectMap.put(blockPos.asLong(), level.getBlockState(blockPos));
		longArrayFIFOQueue.enqueue(blockPos.asLong());
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		int l = blockPos.getX();
		int m = blockPos.getY();
		int n = blockPos.getZ();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
		int o = level.getGameRules().getInt(GameRules.RULE_FLOATATER_SIZE_LIMIT);

		while (!longArrayFIFOQueue.isEmpty()) {
			long p = longArrayFIFOQueue.dequeueLastLong();
			mutableBlockPos.set(p);
			BlockState blockState = long2ObjectMap.get(p);
			i = Math.min(i, mutableBlockPos.getX());
			j = Math.min(j, mutableBlockPos.getY());
			k = Math.min(k, mutableBlockPos.getZ());
			l = Math.max(l, mutableBlockPos.getX());
			m = Math.max(m, mutableBlockPos.getY());
			n = Math.max(n, mutableBlockPos.getZ());
			if (l - i + 1 > o || m - j + 1 > o || n - k + 1 > o) {
				return null;
			}

			VoxelShape voxelShape = blockState.getShape(level, mutableBlockPos);

			for (Direction direction2 : DIRECTIONS) {
				mutableBlockPos2.setWithOffset(mutableBlockPos, direction2);
				long q = mutableBlockPos2.asLong();
				if (!long2ObjectMap.containsKey(q)) {
					BlockState blockState2 = level.getBlockState(mutableBlockPos2);
					VoxelShape voxelShape2 = blockState2.getShape(level, mutableBlockPos2);
					boolean bl = direction2 == direction && !blockState2.canBeReplaced();
					if (bl || isConnected(direction2, voxelShape, voxelShape2, blockState, blockState2)) {
						longArrayFIFOQueue.enqueue(q);
						long2ObjectMap.put(q, blockState2);
					}
				}
			}
		}

		int r = l - i + 1;
		int s = m - j + 1;
		int t = n - k + 1;
		SubGridBlocks subGridBlocks = new SubGridBlocks(r, s, t);
		int u = 0;

		for (Entry<BlockState> entry : Long2ObjectMaps.fastIterable(long2ObjectMap)) {
			mutableBlockPos.set(entry.getLongKey());
			BlockState blockState3 = ((BlockState)entry.getValue()).trySetValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false));
			if (blockState3.is(Blocks.FLOATATER) && blockState3.getValue(FloataterBlock.FACING) == direction && (Boolean)blockState3.getValue(FloataterBlock.TRIGGERED)) {
				u++;
			}

			if (blockState3.getBlock() instanceof FlyingTickable) {
				subGridBlocks.markTickable(new BlockPos(mutableBlockPos.getX() - i, mutableBlockPos.getY() - j, mutableBlockPos.getZ() - k));
			}

			if (!blockState3.hasBlockEntity()) {
				subGridBlocks.setBlockState(mutableBlockPos.getX() - i, mutableBlockPos.getY() - j, mutableBlockPos.getZ() - k, blockState3);
			}
		}

		return new SubGridCapture(subGridBlocks, new LongOpenHashSet(long2ObjectMap.keySet()), new BlockPos(i, j, k), u);
	}

	private static boolean isConnected(Direction direction, VoxelShape voxelShape, VoxelShape voxelShape2, BlockState blockState, BlockState blockState2) {
		if (isStickyInDirection(blockState, direction) || isStickyInDirection(blockState2, direction.getOpposite())) {
			return true;
		} else {
			return !areShapesConnected(direction, voxelShape, voxelShape2)
				? false
				: !isNonStickyInDirection(blockState, direction) && !isNonStickyInDirection(blockState2, direction.getOpposite());
		}
	}

	private static boolean areShapesConnected(Direction direction, VoxelShape voxelShape, VoxelShape voxelShape2) {
		if (voxelShape != Shapes.empty() && voxelShape2 != Shapes.empty()) {
			VoxelShape voxelShape3 = Shapes.getFaceShape(voxelShape, direction);
			VoxelShape voxelShape4 = Shapes.getFaceShape(voxelShape2, direction.getOpposite());
			return voxelShape3 == Shapes.block() && voxelShape4 == Shapes.block() ? true : Shapes.joinIsNotEmpty(voxelShape3, voxelShape4, BooleanOp.AND);
		} else {
			return false;
		}
	}

	private static boolean isNonStickyInDirection(BlockState blockState, Direction direction) {
		return blockState.is(Blocks.FLOATATER) && blockState.getValue(FloataterBlock.FACING) != direction;
	}

	private static boolean isStickyInDirection(BlockState blockState, Direction direction) {
		if (!blockState.is(Blocks.SLIME_BLOCK) && !blockState.is(Blocks.HONEY_BLOCK)) {
			return blockState.is(Blocks.STICKY_PISTON) && blockState.getValue(PistonBaseBlock.FACING) == direction
				? true
				: blockState.is(Blocks.FLOATATER) && blockState.getValue(FloataterBlock.FACING) == direction;
		} else {
			return true;
		}
	}

	public void remove(Level level) {
		this.forEachPos(blockPos -> {
			BlockState blockState = level.getBlockState(blockPos);
			if (blockState.hasBlockEntity()) {
				level.destroyBlock(blockPos, true);
			} else {
				FluidState fluidState = blockState.getFluidState();
				BlockState blockState2 = fluidState.createLegacyBlock();
				level.setBlock(blockPos, blockState2, 18);
			}
		});
		this.forEachPos(blockPos -> level.blockUpdated(blockPos, level.getBlockState(blockPos).getBlock()));
	}

	private void forEachPos(Consumer<BlockPos> consumer) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		LongIterator longIterator = this.mask.longIterator();

		while (longIterator.hasNext()) {
			mutableBlockPos.set(longIterator.nextLong());
			consumer.accept(mutableBlockPos);
		}
	}
}
