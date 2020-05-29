/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class JukeboxBlock
extends BaseEntityBlock {
    public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

    protected JukeboxBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(HAS_RECORD, false));
    }

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (blockState.getValue(HAS_RECORD).booleanValue()) {
            this.dropRecording(level, blockPos);
            blockState = (BlockState)blockState.setValue(HAS_RECORD, false);
            level.setBlock(blockPos, blockState, 2);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public void setRecord(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, ItemStack itemStack) {
        BlockEntity blockEntity = levelAccessor.getBlockEntity(blockPos);
        if (!(blockEntity instanceof JukeboxBlockEntity)) {
            return;
        }
        ((JukeboxBlockEntity)blockEntity).setRecord(itemStack.copy());
        levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(HAS_RECORD, true), 2);
    }

    private void dropRecording(Level level, BlockPos blockPos) {
        if (level.isClientSide) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof JukeboxBlockEntity)) {
            return;
        }
        JukeboxBlockEntity jukeboxBlockEntity = (JukeboxBlockEntity)blockEntity;
        ItemStack itemStack = jukeboxBlockEntity.getRecord();
        if (itemStack.isEmpty()) {
            return;
        }
        level.levelEvent(1010, blockPos, 0);
        jukeboxBlockEntity.clearContent();
        float f = 0.7f;
        double d = (double)(level.random.nextFloat() * 0.7f) + (double)0.15f;
        double e = (double)(level.random.nextFloat() * 0.7f) + 0.06000000238418579 + 0.6;
        double g = (double)(level.random.nextFloat() * 0.7f) + (double)0.15f;
        ItemStack itemStack2 = itemStack.copy();
        ItemEntity itemEntity = new ItemEntity(level, (double)blockPos.getX() + d, (double)blockPos.getY() + e, (double)blockPos.getZ() + g, itemStack2);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.is(blockState2.getBlock())) {
            return;
        }
        this.dropRecording(level, blockPos);
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public BlockEntity newBlockEntity(BlockGetter blockGetter) {
        return new JukeboxBlockEntity();
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        Item item;
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof JukeboxBlockEntity && (item = ((JukeboxBlockEntity)blockEntity).getRecord().getItem()) instanceof RecordItem) {
            return ((RecordItem)item).getAnalogOutput();
        }
        return 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_RECORD);
    }
}

