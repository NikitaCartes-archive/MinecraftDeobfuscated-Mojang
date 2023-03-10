/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class LayeredCauldronBlock
extends AbstractCauldronBlock {
    public static final int MIN_FILL_LEVEL = 1;
    public static final int MAX_FILL_LEVEL = 3;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
    private static final int BASE_CONTENT_HEIGHT = 6;
    private static final double HEIGHT_PER_LEVEL = 3.0;
    public static final Predicate<Biome.Precipitation> RAIN = precipitation -> precipitation == Biome.Precipitation.RAIN;
    public static final Predicate<Biome.Precipitation> SNOW = precipitation -> precipitation == Biome.Precipitation.SNOW;
    private final Predicate<Biome.Precipitation> fillPredicate;

    public LayeredCauldronBlock(BlockBehaviour.Properties properties, Predicate<Biome.Precipitation> predicate, Map<Item, CauldronInteraction> map) {
        super(properties, map);
        this.fillPredicate = predicate;
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(LEVEL, 1));
    }

    @Override
    public boolean isFull(BlockState blockState) {
        return blockState.getValue(LEVEL) == 3;
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return fluid == Fluids.WATER && this.fillPredicate == RAIN;
    }

    @Override
    protected double getContentHeight(BlockState blockState) {
        return (6.0 + (double)blockState.getValue(LEVEL).intValue() * 3.0) / 16.0;
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (!level.isClientSide && entity.isOnFire() && this.isEntityInsideContent(blockState, blockPos, entity)) {
            entity.clearFire();
            if (entity.mayInteract(level, blockPos)) {
                this.handleEntityOnFireInside(blockState, level, blockPos);
            }
        }
    }

    protected void handleEntityOnFireInside(BlockState blockState, Level level, BlockPos blockPos) {
        LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
    }

    public static void lowerFillLevel(BlockState blockState, Level level, BlockPos blockPos) {
        int i = blockState.getValue(LEVEL) - 1;
        BlockState blockState2 = i == 0 ? Blocks.CAULDRON.defaultBlockState() : (BlockState)blockState.setValue(LEVEL, i);
        level.setBlockAndUpdate(blockPos, blockState2);
        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState2));
    }

    @Override
    public void handlePrecipitation(BlockState blockState, Level level, BlockPos blockPos, Biome.Precipitation precipitation) {
        if (!CauldronBlock.shouldHandlePrecipitation(level, precipitation) || blockState.getValue(LEVEL) == 3 || !this.fillPredicate.test(precipitation)) {
            return;
        }
        BlockState blockState2 = (BlockState)blockState.cycle(LEVEL);
        level.setBlockAndUpdate(blockPos, blockState2);
        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState2));
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return blockState.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected void receiveStalactiteDrip(BlockState blockState, Level level, BlockPos blockPos, Fluid fluid) {
        if (this.isFull(blockState)) {
            return;
        }
        BlockState blockState2 = (BlockState)blockState.setValue(LEVEL, blockState.getValue(LEVEL) + 1);
        level.setBlockAndUpdate(blockPos, blockState2);
        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState2));
        level.levelEvent(1047, blockPos, 0);
    }
}

