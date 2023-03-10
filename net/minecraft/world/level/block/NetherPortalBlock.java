/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherPortalBlock
extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    protected static final int AABB_OFFSET = 2;
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public NetherPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(AXIS, Direction.Axis.X));
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        switch (blockState.getValue(AXIS)) {
            case Z: {
                return Z_AXIS_AABB;
            }
        }
        return X_AXIS_AABB;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (serverLevel.dimensionType().natural() && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && randomSource.nextInt(2000) < serverLevel.getDifficulty().getId()) {
            ZombifiedPiglin entity;
            while (serverLevel.getBlockState(blockPos).is(this)) {
                blockPos = blockPos.below();
            }
            if (serverLevel.getBlockState(blockPos).isValidSpawn(serverLevel, blockPos, EntityType.ZOMBIFIED_PIGLIN) && (entity = EntityType.ZOMBIFIED_PIGLIN.spawn(serverLevel, blockPos.above(), MobSpawnType.STRUCTURE)) != null) {
                entity.setPortalCooldown();
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        boolean bl;
        Direction.Axis axis = direction.getAxis();
        Direction.Axis axis2 = blockState.getValue(AXIS);
        boolean bl2 = bl = axis2 != axis && axis.isHorizontal();
        if (bl || blockState2.is(this) || new PortalShape(levelAccessor, blockPos, axis2).isComplete()) {
            return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
        if (entity.canChangeDimensions()) {
            entity.handleInsidePortal(blockPos);
        }
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (randomSource.nextInt(100) == 0) {
            level.playLocalSound((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5f, randomSource.nextFloat() * 0.4f + 0.8f, false);
        }
        for (int i = 0; i < 4; ++i) {
            double d = (double)blockPos.getX() + randomSource.nextDouble();
            double e = (double)blockPos.getY() + randomSource.nextDouble();
            double f = (double)blockPos.getZ() + randomSource.nextDouble();
            double g = ((double)randomSource.nextFloat() - 0.5) * 0.5;
            double h = ((double)randomSource.nextFloat() - 0.5) * 0.5;
            double j = ((double)randomSource.nextFloat() - 0.5) * 0.5;
            int k = randomSource.nextInt(2) * 2 - 1;
            if (level.getBlockState(blockPos.west()).is(this) || level.getBlockState(blockPos.east()).is(this)) {
                f = (double)blockPos.getZ() + 0.5 + 0.25 * (double)k;
                j = randomSource.nextFloat() * 2.0f * (float)k;
            } else {
                d = (double)blockPos.getX() + 0.5 + 0.25 * (double)k;
                g = randomSource.nextFloat() * 2.0f * (float)k;
            }
            level.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, j);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                switch (blockState.getValue(AXIS)) {
                    case X: {
                        return (BlockState)blockState.setValue(AXIS, Direction.Axis.Z);
                    }
                    case Z: {
                        return (BlockState)blockState.setValue(AXIS, Direction.Axis.X);
                    }
                }
                return blockState;
            }
        }
        return blockState;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }
}

