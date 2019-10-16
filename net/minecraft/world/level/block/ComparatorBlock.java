/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ComparatorBlock
extends DiodeBlock
implements EntityBlock {
    public static final EnumProperty<ComparatorMode> MODE = BlockStateProperties.MODE_COMPARATOR;

    public ComparatorBlock(Block.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(POWERED, false)).setValue(MODE, ComparatorMode.COMPARE));
    }

    @Override
    protected int getDelay(BlockState blockState) {
        return 2;
    }

    @Override
    protected int getOutputSignal(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof ComparatorBlockEntity) {
            return ((ComparatorBlockEntity)blockEntity).getOutputSignal();
        }
        return 0;
    }

    private int calculateOutputSignal(Level level, BlockPos blockPos, BlockState blockState) {
        if (blockState.getValue(MODE) == ComparatorMode.SUBTRACT) {
            return Math.max(this.getInputSignal(level, blockPos, blockState) - this.getAlternateSignal(level, blockPos, blockState), 0);
        }
        return this.getInputSignal(level, blockPos, blockState);
    }

    @Override
    protected boolean shouldTurnOn(Level level, BlockPos blockPos, BlockState blockState) {
        int i = this.getInputSignal(level, blockPos, blockState);
        if (i >= 15) {
            return true;
        }
        if (i == 0) {
            return false;
        }
        return i >= this.getAlternateSignal(level, blockPos, blockState);
    }

    @Override
    protected int getInputSignal(Level level, BlockPos blockPos, BlockState blockState) {
        int i = super.getInputSignal(level, blockPos, blockState);
        Direction direction = blockState.getValue(FACING);
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockState blockState2 = level.getBlockState(blockPos2);
        if (blockState2.hasAnalogOutputSignal()) {
            i = blockState2.getAnalogOutputSignal(level, blockPos2);
        } else if (i < 15 && blockState2.isRedstoneConductor(level, blockPos2)) {
            ItemFrame itemFrame;
            blockState2 = level.getBlockState(blockPos2 = blockPos2.relative(direction));
            if (blockState2.hasAnalogOutputSignal()) {
                i = blockState2.getAnalogOutputSignal(level, blockPos2);
            } else if (blockState2.isAir() && (itemFrame = this.getItemFrame(level, direction, blockPos2)) != null) {
                i = itemFrame.getAnalogOutput();
            }
        }
        return i;
    }

    @Nullable
    private ItemFrame getItemFrame(Level level, Direction direction, BlockPos blockPos) {
        List<ItemFrame> list = level.getEntitiesOfClass(ItemFrame.class, new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1), itemFrame -> itemFrame != null && itemFrame.getDirection() == direction);
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!player.abilities.mayBuild) {
            return InteractionResult.PASS;
        }
        float f = (blockState = (BlockState)blockState.cycle(MODE)).getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55f : 0.5f;
        level.playSound(player, blockPos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3f, f);
        level.setBlock(blockPos, blockState, 2);
        this.refreshOutputState(level, blockPos, blockState);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void checkTickOnNeighbor(Level level, BlockPos blockPos, BlockState blockState) {
        int j;
        if (level.getBlockTicks().willTickThisTick(blockPos, this)) {
            return;
        }
        int i = this.calculateOutputSignal(level, blockPos, blockState);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        int n = j = blockEntity instanceof ComparatorBlockEntity ? ((ComparatorBlockEntity)blockEntity).getOutputSignal() : 0;
        if (i != j || blockState.getValue(POWERED).booleanValue() != this.shouldTurnOn(level, blockPos, blockState)) {
            TickPriority tickPriority = this.shouldPrioritize(level, blockPos, blockState) ? TickPriority.HIGH : TickPriority.NORMAL;
            level.getBlockTicks().scheduleTick(blockPos, this, 2, tickPriority);
        }
    }

    private void refreshOutputState(Level level, BlockPos blockPos, BlockState blockState) {
        int i = this.calculateOutputSignal(level, blockPos, blockState);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        int j = 0;
        if (blockEntity instanceof ComparatorBlockEntity) {
            ComparatorBlockEntity comparatorBlockEntity = (ComparatorBlockEntity)blockEntity;
            j = comparatorBlockEntity.getOutputSignal();
            comparatorBlockEntity.setOutputSignal(i);
        }
        if (j != i || blockState.getValue(MODE) == ComparatorMode.COMPARE) {
            boolean bl = this.shouldTurnOn(level, blockPos, blockState);
            boolean bl2 = blockState.getValue(POWERED);
            if (bl2 && !bl) {
                level.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, false), 2);
            } else if (!bl2 && bl) {
                level.setBlock(blockPos, (BlockState)blockState.setValue(POWERED, true), 2);
            }
            this.updateNeighborsInFront(level, blockPos, blockState);
        }
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        this.refreshOutputState(serverLevel, blockPos, blockState);
    }

    @Override
    public boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int i, int j) {
        super.triggerEvent(blockState, level, blockPos, i, j);
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        return blockEntity != null && blockEntity.triggerEvent(i, j);
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new ComparatorBlockEntity();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODE, POWERED);
    }
}

