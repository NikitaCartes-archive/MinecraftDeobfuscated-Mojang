package net.minecraft.world.level.redstone;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;

public class ExperimentalRedstoneWireEvaluator extends RedstoneWireEvaluator {
	private final Deque<BlockPos> wiresToTurnOff = new ArrayDeque();
	private final Deque<BlockPos> wiresToTurnOn = new ArrayDeque();
	private final Object2IntMap<BlockPos> updatedWires = new Object2IntLinkedOpenHashMap<>();

	public ExperimentalRedstoneWireEvaluator(RedStoneWireBlock redStoneWireBlock) {
		super(redStoneWireBlock);
	}

	@Override
	public void updatePowerStrength(Level level, BlockPos blockPos, BlockState blockState, @Nullable Orientation orientation, boolean bl) {
		Orientation orientation2 = getInitialOrientation(level, orientation);
		this.calculateCurrentChanges(level, blockPos, orientation2);
		ObjectIterator<Entry<BlockPos>> objectIterator = this.updatedWires.object2IntEntrySet().iterator();

		for (boolean bl2 = true; objectIterator.hasNext(); bl2 = false) {
			Entry<BlockPos> entry = (Entry<BlockPos>)objectIterator.next();
			BlockPos blockPos2 = (BlockPos)entry.getKey();
			int i = entry.getIntValue();
			int j = unpackPower(i);
			BlockState blockState2 = level.getBlockState(blockPos2);
			if (blockState2.is(this.wireBlock) && !((Integer)blockState2.getValue(RedStoneWireBlock.POWER)).equals(j)) {
				int k = 2;
				if (!bl || !bl2) {
					k |= 128;
				}

				level.setBlock(blockPos2, blockState2.setValue(RedStoneWireBlock.POWER, Integer.valueOf(j)), k);
			} else {
				objectIterator.remove();
			}
		}

		this.causeNeighborUpdates(level);
	}

	private void causeNeighborUpdates(Level level) {
		this.updatedWires.forEach((blockPos, integer) -> {
			Orientation orientation = unpackOrientation(integer);
			BlockState blockState = level.getBlockState(blockPos);

			for (Direction direction : orientation.getDirections()) {
				if (isConnected(blockState, direction)) {
					BlockPos blockPos2 = blockPos.relative(direction);
					BlockState blockState2 = level.getBlockState(blockPos2);
					Orientation orientation2 = orientation.withFrontPreserveUp(direction);
					level.neighborChanged(blockState2, blockPos2, this.wireBlock, orientation2, false);
					if (blockState2.isRedstoneConductor(level, blockPos2)) {
						for (Direction direction2 : orientation2.getDirections()) {
							if (direction2 != direction.getOpposite()) {
								level.neighborChanged(blockPos2.relative(direction2), this.wireBlock, orientation2.withFrontPreserveUp(direction2));
							}
						}
					}
				}
			}
		});
	}

	private static boolean isConnected(BlockState blockState, Direction direction) {
		EnumProperty<RedstoneSide> enumProperty = (EnumProperty<RedstoneSide>)RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(direction);
		return enumProperty == null ? direction == Direction.DOWN : ((RedstoneSide)blockState.getValue(enumProperty)).isConnected();
	}

	private static Orientation getInitialOrientation(Level level, @Nullable Orientation orientation) {
		Orientation orientation2;
		if (orientation != null) {
			orientation2 = orientation;
		} else {
			orientation2 = Orientation.random(level.random);
		}

		return orientation2.withUp(Direction.UP).withSideBias(Orientation.SideBias.LEFT);
	}

	private void calculateCurrentChanges(Level level, BlockPos blockPos, Orientation orientation) {
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.is(this.wireBlock)) {
			this.setPower(blockPos, (Integer)blockState.getValue(RedStoneWireBlock.POWER), orientation);
			this.wiresToTurnOff.add(blockPos);
		} else {
			this.propagateChangeToNeighbors(level, blockPos, 0, orientation, true);
		}

		while (!this.wiresToTurnOff.isEmpty()) {
			BlockPos blockPos2 = (BlockPos)this.wiresToTurnOff.removeFirst();
			int i = this.updatedWires.getInt(blockPos2);
			Orientation orientation2 = unpackOrientation(i);
			int j = unpackPower(i);
			int k = this.getBlockSignal(level, blockPos2);
			int l = this.getIncomingWireSignal(level, blockPos2);
			int m = Math.max(k, l);
			int n;
			if (m < j) {
				if (k > 0 && !this.wiresToTurnOn.contains(blockPos2)) {
					this.wiresToTurnOn.add(blockPos2);
				}

				n = 0;
			} else {
				n = m;
			}

			if (n != j) {
				this.setPower(blockPos2, n, orientation2);
			}

			this.propagateChangeToNeighbors(level, blockPos2, n, orientation2, j > m);
		}

		while (!this.wiresToTurnOn.isEmpty()) {
			BlockPos blockPos2x = (BlockPos)this.wiresToTurnOn.removeFirst();
			int ix = this.updatedWires.getInt(blockPos2x);
			int o = unpackPower(ix);
			int jx = this.getBlockSignal(level, blockPos2x);
			int kx = this.getIncomingWireSignal(level, blockPos2x);
			int lx = Math.max(jx, kx);
			Orientation orientation3 = unpackOrientation(ix);
			if (lx > o) {
				this.setPower(blockPos2x, lx, orientation3);
			} else if (lx < o) {
				throw new IllegalStateException("Turning off wire while trying to turn it on. Should not happen.");
			}

			this.propagateChangeToNeighbors(level, blockPos2x, lx, orientation3, false);
		}
	}

	private static int packOrientationAndPower(Orientation orientation, int i) {
		return orientation.getIndex() << 4 | i;
	}

	private static Orientation unpackOrientation(int i) {
		return Orientation.fromIndex(i >> 4);
	}

	private static int unpackPower(int i) {
		return i & 15;
	}

	private void setPower(BlockPos blockPos, int i, Orientation orientation) {
		this.updatedWires
			.compute(
				blockPos, (blockPosx, integer) -> integer == null ? packOrientationAndPower(orientation, i) : packOrientationAndPower(unpackOrientation(integer), i)
			);
	}

	private void propagateChangeToNeighbors(Level level, BlockPos blockPos, int i, Orientation orientation, boolean bl) {
		for (Direction direction : orientation.getHorizontalDirections()) {
			BlockPos blockPos2 = blockPos.relative(direction);
			this.enqueueNeighborWire(level, blockPos2, i, orientation.withFront(direction), bl);
		}

		for (Direction direction : orientation.getVerticalDirections()) {
			BlockPos blockPos2 = blockPos.relative(direction);
			boolean bl2 = level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2);

			for (Direction direction2 : orientation.getHorizontalDirections()) {
				BlockPos blockPos3 = blockPos.relative(direction2);
				if (direction == Direction.UP && !bl2) {
					BlockPos blockPos4 = blockPos2.relative(direction2);
					this.enqueueNeighborWire(level, blockPos4, i, orientation.withFront(direction2), bl);
				} else if (direction == Direction.DOWN && !level.getBlockState(blockPos3).isRedstoneConductor(level, blockPos3)) {
					BlockPos blockPos4 = blockPos2.relative(direction2);
					this.enqueueNeighborWire(level, blockPos4, i, orientation.withFront(direction2), bl);
				}
			}
		}
	}

	private void enqueueNeighborWire(Level level, BlockPos blockPos, int i, Orientation orientation, boolean bl) {
		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.is(this.wireBlock)) {
			int j = this.getWireSignal(blockPos, blockState);
			if (j < i - 1 && !this.wiresToTurnOn.contains(blockPos)) {
				this.wiresToTurnOn.add(blockPos);
				this.setPower(blockPos, j, orientation);
			}

			if (bl && j > i && !this.wiresToTurnOff.contains(blockPos)) {
				this.wiresToTurnOff.add(blockPos);
				this.setPower(blockPos, j, orientation);
			}
		}
	}

	@Override
	protected int getWireSignal(BlockPos blockPos, BlockState blockState) {
		int i = this.updatedWires.getOrDefault(blockPos, -1);
		return i != -1 ? unpackPower(i) : super.getWireSignal(blockPos, blockState);
	}
}
