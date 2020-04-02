/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TargetBlock
extends Block {
    private static final IntegerProperty OUTPUT_POWER = BlockStateProperties.POWER;

    public TargetBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(OUTPUT_POWER, 0));
    }

    @Override
    public void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        int i = TargetBlock.updateRedstoneOutput(level, blockState, blockHitResult, projectile);
        Entity entity = projectile.getOwner();
        if (entity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)entity;
            serverPlayer.awardStat(Stats.TARGET_HIT);
            CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverPlayer, projectile, blockHitResult.getLocation(), i);
        }
    }

    private static int updateRedstoneOutput(LevelAccessor levelAccessor, BlockState blockState, BlockHitResult blockHitResult, Entity entity) {
        int j;
        int i = TargetBlock.getRedstoneStrength(blockHitResult, blockHitResult.getLocation());
        int n = j = entity instanceof AbstractArrow ? 20 : 8;
        if (!levelAccessor.getBlockTicks().hasScheduledTick(blockHitResult.getBlockPos(), blockState.getBlock())) {
            TargetBlock.setOutputPower(levelAccessor, blockState, i, blockHitResult.getBlockPos(), j);
        }
        return i;
    }

    private static int getRedstoneStrength(BlockHitResult blockHitResult, Vec3 vec3) {
        Direction direction = blockHitResult.getDirection();
        double d = Math.abs(Mth.frac(vec3.x) - 0.5);
        double e = Math.abs(Mth.frac(vec3.y) - 0.5);
        double f = Math.abs(Mth.frac(vec3.z) - 0.5);
        Direction.Axis axis = direction.getAxis();
        double g = axis == Direction.Axis.Y ? Math.max(d, f) : (axis == Direction.Axis.Z ? Math.max(d, e) : Math.max(e, f));
        return Math.max(1, Mth.ceil(15.0 * Mth.clamp((0.5 - g) / 0.5, 0.0, 1.0)));
    }

    private static void setOutputPower(LevelAccessor levelAccessor, BlockState blockState, int i, BlockPos blockPos, int j) {
        levelAccessor.setBlock(blockPos, (BlockState)blockState.setValue(OUTPUT_POWER, i), 3);
        levelAccessor.getBlockTicks().scheduleTick(blockPos, blockState.getBlock(), j);
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (blockState.getValue(OUTPUT_POWER) != 0) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(OUTPUT_POWER, 0), 3);
        }
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(OUTPUT_POWER);
    }

    @Override
    public boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OUTPUT_POWER);
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (level.isClientSide() || blockState.getBlock() == blockState2.getBlock()) {
            return;
        }
        if (blockState.getValue(OUTPUT_POWER) > 0 && !level.getBlockTicks().hasScheduledTick(blockPos, this)) {
            level.setBlock(blockPos, (BlockState)blockState.setValue(OUTPUT_POWER, 0), 18);
        }
    }
}

