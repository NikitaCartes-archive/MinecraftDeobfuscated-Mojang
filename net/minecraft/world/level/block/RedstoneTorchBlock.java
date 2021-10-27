/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class RedstoneTorchBlock
extends TorchBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final Map<BlockGetter, List<Toggle>> RECENT_TOGGLES = new WeakHashMap<BlockGetter, List<Toggle>>();
    public static final int RECENT_TOGGLE_TIMER = 60;
    public static final int MAX_RECENT_TOGGLES = 8;
    public static final int RESTART_DELAY = 160;
    private static final int TOGGLE_DELAY = 2;

    protected RedstoneTorchBlock(BlockBehaviour.Properties properties) {
        super(properties, DustParticleOptions.REDSTONE);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(LIT, true));
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (bl) {
            return;
        }
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(blockPos.relative(direction), this);
        }
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (blockState.getValue(LIT).booleanValue() && Direction.UP != direction) {
            return 15;
        }
        return 0;
    }

    protected boolean hasNeighborSignal(Level level, BlockPos blockPos, BlockState blockState) {
        return level.hasSignal(blockPos.below(), Direction.DOWN);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        boolean bl = this.hasNeighborSignal(serverLevel, blockPos, blockState);
        List<Toggle> list = RECENT_TOGGLES.get(serverLevel);
        while (list != null && !list.isEmpty() && serverLevel.getGameTime() - list.get((int)0).when > 60L) {
            list.remove(0);
        }
        if (blockState.getValue(LIT).booleanValue()) {
            if (bl) {
                serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(LIT, false), 3);
                if (RedstoneTorchBlock.isToggledTooFrequently(serverLevel, blockPos, true)) {
                    serverLevel.levelEvent(1502, blockPos, 0);
                    serverLevel.scheduleTick(blockPos, serverLevel.getBlockState(blockPos).getBlock(), 160);
                }
            }
        } else if (!bl && !RedstoneTorchBlock.isToggledTooFrequently(serverLevel, blockPos, false)) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(LIT, true), 3);
        }
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
        if (blockState.getValue(LIT).booleanValue() == this.hasNeighborSignal(level, blockPos, blockState) && !level.getBlockTicks().willTickThisTick(blockPos, this)) {
            level.scheduleTick(blockPos, this, 2);
        }
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (direction == Direction.DOWN) {
            return blockState.getSignal(blockGetter, blockPos, direction);
        }
        return 0;
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (!blockState.getValue(LIT).booleanValue()) {
            return;
        }
        double d = (double)blockPos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
        double e = (double)blockPos.getY() + 0.7 + (random.nextDouble() - 0.5) * 0.2;
        double f = (double)blockPos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
        level.addParticle(this.flameParticle, d, e, f, 0.0, 0.0, 0.0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    private static boolean isToggledTooFrequently(Level level, BlockPos blockPos, boolean bl) {
        List list = RECENT_TOGGLES.computeIfAbsent(level, blockGetter -> Lists.newArrayList());
        if (bl) {
            list.add(new Toggle(blockPos.immutable(), level.getGameTime()));
        }
        int i = 0;
        for (int j = 0; j < list.size(); ++j) {
            Toggle toggle = (Toggle)list.get(j);
            if (!toggle.pos.equals(blockPos) || ++i < 8) continue;
            return true;
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

