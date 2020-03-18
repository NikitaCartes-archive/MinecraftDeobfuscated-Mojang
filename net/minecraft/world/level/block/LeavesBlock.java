/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeavesBlock
extends Block {
    public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;

    public LeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(DISTANCE, 7)).setValue(PERSISTENT, false));
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.empty();
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(DISTANCE) == 7 && blockState.getValue(PERSISTENT) == false;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!blockState.getValue(PERSISTENT).booleanValue() && blockState.getValue(DISTANCE) == 7) {
            LeavesBlock.dropResources(blockState, serverLevel, blockPos);
            serverLevel.removeBlock(blockPos, false);
        }
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        serverLevel.setBlock(blockPos, LeavesBlock.updateDistance(blockState, serverLevel, blockPos), 3);
    }

    @Override
    public int getLightBlock(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return 1;
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        int i = LeavesBlock.getDistanceAt(blockState2) + 1;
        if (i != 1 || blockState.getValue(DISTANCE) != i) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
        return blockState;
    }

    private static BlockState updateDistance(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        int i = 7;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            mutableBlockPos.setWithOffset(blockPos, direction);
            i = Math.min(i, LeavesBlock.getDistanceAt(levelAccessor.getBlockState(mutableBlockPos)) + 1);
            if (i == 1) break;
        }
        return (BlockState)blockState.setValue(DISTANCE, i);
    }

    private static int getDistanceAt(BlockState blockState) {
        if (BlockTags.LOGS.contains(blockState.getBlock())) {
            return 0;
        }
        if (blockState.getBlock() instanceof LeavesBlock) {
            return blockState.getValue(DISTANCE);
        }
        return 7;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, Random random) {
        if (!level.isRainingAt(blockPos.above())) {
            return;
        }
        if (random.nextInt(15) != 1) {
            return;
        }
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState2 = level.getBlockState(blockPos2);
        if (blockState2.canOcclude() && blockState2.isFaceSturdy(level, blockPos2, Direction.UP)) {
            return;
        }
        double d = (float)blockPos.getX() + random.nextFloat();
        double e = (double)blockPos.getY() - 0.05;
        double f = (float)blockPos.getZ() + random.nextFloat();
        level.addParticle(ParticleTypes.DRIPPING_WATER, d, e, f, 0.0, 0.0, 0.0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, PERSISTENT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return LeavesBlock.updateDistance((BlockState)this.defaultBlockState().setValue(PERSISTENT, true), blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos());
    }
}

