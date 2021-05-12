package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class RedstoneTorchBlock extends TorchBlock {
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	private static final Map<BlockGetter, List<RedstoneTorchBlock.Toggle>> RECENT_TOGGLES = new WeakHashMap();
	public static final int RECENT_TOGGLE_TIMER = 60;
	public static final int MAX_RECENT_TOGGLES = 8;
	public static final int RESTART_DELAY = 160;
	private static final int TOGGLE_DELAY = 2;

	protected RedstoneTorchBlock(BlockBehaviour.Properties properties) {
		super(properties, DustParticleOptions.REDSTONE);
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.valueOf(true)));
	}

	@Override
	public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		for (Direction direction : Direction.values()) {
			level.updateNeighborsAt(blockPos.relative(direction), this);
		}
	}

	@Override
	public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
		if (!bl) {
			for (Direction direction : Direction.values()) {
				level.updateNeighborsAt(blockPos.relative(direction), this);
			}
		}
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(LIT) && Direction.UP != direction ? 15 : 0;
	}

	protected boolean hasNeighborSignal(Level level, BlockPos blockPos, BlockState blockState) {
		return level.hasSignal(blockPos.below(), Direction.DOWN);
	}

	@Override
	public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
		boolean bl = this.hasNeighborSignal(serverLevel, blockPos, blockState);
		List<RedstoneTorchBlock.Toggle> list = (List<RedstoneTorchBlock.Toggle>)RECENT_TOGGLES.get(serverLevel);

		while (list != null && !list.isEmpty() && serverLevel.getGameTime() - ((RedstoneTorchBlock.Toggle)list.get(0)).when > 60L) {
			list.remove(0);
		}

		if ((Boolean)blockState.getValue(LIT)) {
			if (bl) {
				serverLevel.setBlock(blockPos, blockState.setValue(LIT, Boolean.valueOf(false)), 3);
				if (isToggledTooFrequently(serverLevel, blockPos, true)) {
					serverLevel.levelEvent(1502, blockPos, 0);
					serverLevel.getBlockTicks().scheduleTick(blockPos, serverLevel.getBlockState(blockPos).getBlock(), 160);
				}
			}
		} else if (!bl && !isToggledTooFrequently(serverLevel, blockPos, false)) {
			serverLevel.setBlock(blockPos, blockState.setValue(LIT, Boolean.valueOf(true)), 3);
		}
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		if ((Boolean)blockState.getValue(LIT) == this.hasNeighborSignal(level, blockPos, blockState) && !level.getBlockTicks().willTickThisTick(blockPos, this)) {
			level.getBlockTicks().scheduleTick(blockPos, this, 2);
		}
	}

	@Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return direction == Direction.DOWN ? blockState.getSignal(blockGetter, blockPos, direction) : 0;
	}

	@Override
	public boolean isSignalSource(BlockState blockState) {
		return true;
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
		if ((Boolean)blockState.getValue(LIT)) {
			double d = (double)blockPos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
			double e = (double)blockPos.getY() + 0.7 + (random.nextDouble() - 0.5) * 0.2;
			double f = (double)blockPos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
			level.addParticle(this.flameParticle, d, e, f, 0.0, 0.0, 0.0);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	private static boolean isToggledTooFrequently(Level level, BlockPos blockPos, boolean bl) {
		List<RedstoneTorchBlock.Toggle> list = (List<RedstoneTorchBlock.Toggle>)RECENT_TOGGLES.computeIfAbsent(level, blockGetter -> Lists.newArrayList());
		if (bl) {
			list.add(new RedstoneTorchBlock.Toggle(blockPos.immutable(), level.getGameTime()));
		}

		int i = 0;

		for (int j = 0; j < list.size(); j++) {
			RedstoneTorchBlock.Toggle toggle = (RedstoneTorchBlock.Toggle)list.get(j);
			if (toggle.pos.equals(blockPos)) {
				if (++i >= 8) {
					return true;
				}
			}
		}

		return false;
	}

	public static class Toggle {
		final BlockPos pos;
		final long when;

		public Toggle(BlockPos blockPos, long l) {
			this.pos = blockPos;
			this.when = l;
		}
	}
}
