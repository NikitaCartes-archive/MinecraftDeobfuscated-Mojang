/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import org.jetbrains.annotations.Nullable;

public class SculkCatalystBlock
extends BaseEntityBlock {
    public static final int PULSE_TICKS = 8;
    public static final BooleanProperty PULSE = BlockStateProperties.BLOOM;
    private final IntProvider xpRange = ConstantInt.of(20);

    public SculkCatalystBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(PULSE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PULSE);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (blockState.getValue(PULSE).booleanValue()) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(PULSE, false), 3);
        }
    }

    public static void bloom(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, Random random) {
        serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(PULSE, true), 3);
        serverLevel.scheduleTick(blockPos, blockState.getBlock(), 8);
        serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 1.15, (double)blockPos.getZ() + 0.5, 2, 0.2, 0.0, 0.2, 0.0);
        serverLevel.playSound(null, blockPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0f, 0.6f + random.nextFloat() * 0.4f);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SculkCatalystBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> GameEventListener getListener(Level level, T blockEntity) {
        if (blockEntity instanceof SculkCatalystBlockEntity) {
            SculkCatalystBlockEntity sculkCatalystBlockEntity = (SculkCatalystBlockEntity)blockEntity;
            return sculkCatalystBlockEntity;
        }
        return null;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return SculkCatalystBlock.createTickerHelper(blockEntityType, BlockEntityType.SCULK_CATALYST, SculkCatalystBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack);
        this.tryDropExperience(serverLevel, blockPos, itemStack, this.xpRange);
    }
}

