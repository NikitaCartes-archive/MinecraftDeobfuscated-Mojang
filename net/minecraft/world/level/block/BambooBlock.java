/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BambooBlock
extends Block
implements BonemealableBlock {
    protected static final float SMALL_LEAVES_AABB_OFFSET = 3.0f;
    protected static final float LARGE_LEAVES_AABB_OFFSET = 5.0f;
    protected static final float COLLISION_AABB_OFFSET = 1.5f;
    protected static final VoxelShape SMALL_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
    protected static final VoxelShape LARGE_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);
    protected static final VoxelShape COLLISION_SHAPE = Block.box(6.5, 0.0, 6.5, 9.5, 16.0, 9.5);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_1;
    public static final EnumProperty<BambooLeaves> LEAVES = BlockStateProperties.BAMBOO_LEAVES;
    public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
    public static final int MAX_HEIGHT = 16;
    public static final int STAGE_GROWING = 0;
    public static final int STAGE_DONE_GROWING = 1;
    public static final int AGE_THIN_BAMBOO = 0;
    public static final int AGE_THICK_BAMBOO = 1;

    public BambooBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(AGE, 0)).setValue(LEAVES, BambooLeaves.NONE)).setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, LEAVES, STAGE);
    }

    @Override
    public BlockBehaviour.OffsetType getOffsetType() {
        return BlockBehaviour.OffsetType.XZ;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        VoxelShape voxelShape = blockState.getValue(LEAVES) == BambooLeaves.LARGE ? LARGE_SHAPE : SMALL_SHAPE;
        Vec3 vec3 = blockState.getOffset(blockGetter, blockPos);
        return voxelShape.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        Vec3 vec3 = blockState.getOffset(blockGetter, blockPos);
        return COLLISION_SHAPE.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        if (!fluidState.isEmpty()) {
            return null;
        }
        BlockState blockState = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().below());
        if (blockState.is(BlockTags.BAMBOO_PLANTABLE_ON)) {
            if (blockState.is(Blocks.BAMBOO_SAPLING)) {
                return (BlockState)this.defaultBlockState().setValue(AGE, 0);
            }
            if (blockState.is(Blocks.BAMBOO)) {
                int i = blockState.getValue(AGE) > 0 ? 1 : 0;
                return (BlockState)this.defaultBlockState().setValue(AGE, i);
            }
            BlockState blockState2 = blockPlaceContext.getLevel().getBlockState(blockPlaceContext.getClickedPos().above());
            if (blockState2.is(Blocks.BAMBOO)) {
                return (BlockState)this.defaultBlockState().setValue(AGE, blockState2.getValue(AGE));
            }
            return Blocks.BAMBOO_SAPLING.defaultBlockState();
        }
        return null;
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        if (!blockState.canSurvive(serverLevel, blockPos)) {
            serverLevel.destroyBlock(blockPos, true);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(STAGE) == 0;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        int i;
        if (blockState.getValue(STAGE) != 0) {
            return;
        }
        if (random.nextInt(3) == 0 && serverLevel.isEmptyBlock(blockPos.above()) && serverLevel.getRawBrightness(blockPos.above(), 0) >= 9 && (i = this.getHeightBelowUpToMax(serverLevel, blockPos) + 1) < 16) {
            this.growBamboo(blockState, serverLevel, blockPos, random, i);
        }
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        return levelReader.getBlockState(blockPos.below()).is(BlockTags.BAMBOO_PLANTABLE_ON);
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!blockState.canSurvive(levelAccessor, blockPos)) {
            levelAccessor.getBlockTicks().scheduleTick(blockPos, this, 1);
        }
        if (direction == Direction.UP && blockState2.is(Blocks.BAMBOO) && blockState2.getValue(AGE) > blockState.getValue(AGE)) {
            levelAccessor.setBlock(blockPos, (BlockState)blockState.cycle(AGE), 2);
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    @Override
    public boolean isValidBonemealTarget(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, boolean bl) {
        int j;
        int i = this.getHeightAboveUpToMax(blockGetter, blockPos);
        return i + (j = this.getHeightBelowUpToMax(blockGetter, blockPos)) + 1 < 16 && blockGetter.getBlockState(blockPos.above(i)).getValue(STAGE) != 1;
    }

    @Override
    public boolean isBonemealSuccess(Level level, Random random, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, Random random, BlockPos blockPos, BlockState blockState) {
        int i = this.getHeightAboveUpToMax(serverLevel, blockPos);
        int j = this.getHeightBelowUpToMax(serverLevel, blockPos);
        int k = i + j + 1;
        int l = 1 + random.nextInt(2);
        for (int m = 0; m < l; ++m) {
            BlockPos blockPos2 = blockPos.above(i);
            BlockState blockState2 = serverLevel.getBlockState(blockPos2);
            if (k >= 16 || blockState2.getValue(STAGE) == 1 || !serverLevel.isEmptyBlock(blockPos2.above())) {
                return;
            }
            this.growBamboo(blockState2, serverLevel, blockPos2, random, k);
            ++i;
            ++k;
        }
    }

    @Override
    public float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {
        if (player.getMainHandItem().getItem() instanceof SwordItem) {
            return 1.0f;
        }
        return super.getDestroyProgress(blockState, player, blockGetter, blockPos);
    }

    protected void growBamboo(BlockState blockState, Level level, BlockPos blockPos, Random random, int i) {
        BlockState blockState2 = level.getBlockState(blockPos.below());
        BlockPos blockPos2 = blockPos.below(2);
        BlockState blockState3 = level.getBlockState(blockPos2);
        BambooLeaves bambooLeaves = BambooLeaves.NONE;
        if (i >= 1) {
            if (!blockState2.is(Blocks.BAMBOO) || blockState2.getValue(LEAVES) == BambooLeaves.NONE) {
                bambooLeaves = BambooLeaves.SMALL;
            } else if (blockState2.is(Blocks.BAMBOO) && blockState2.getValue(LEAVES) != BambooLeaves.NONE) {
                bambooLeaves = BambooLeaves.LARGE;
                if (blockState3.is(Blocks.BAMBOO)) {
                    level.setBlock(blockPos.below(), (BlockState)blockState2.setValue(LEAVES, BambooLeaves.SMALL), 3);
                    level.setBlock(blockPos2, (BlockState)blockState3.setValue(LEAVES, BambooLeaves.NONE), 3);
                }
            }
        }
        int j = blockState.getValue(AGE) == 1 || blockState3.is(Blocks.BAMBOO) ? 1 : 0;
        int k = i >= 11 && random.nextFloat() < 0.25f || i == 15 ? 1 : 0;
        level.setBlock(blockPos.above(), (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(AGE, j)).setValue(LEAVES, bambooLeaves)).setValue(STAGE, k), 3);
    }

    protected int getHeightAboveUpToMax(BlockGetter blockGetter, BlockPos blockPos) {
        int i;
        for (i = 0; i < 16 && blockGetter.getBlockState(blockPos.above(i + 1)).is(Blocks.BAMBOO); ++i) {
        }
        return i;
    }

    protected int getHeightBelowUpToMax(BlockGetter blockGetter, BlockPos blockPos) {
        int i;
        for (i = 0; i < 16 && blockGetter.getBlockState(blockPos.below(i + 1)).is(Blocks.BAMBOO); ++i) {
        }
        return i;
    }
}

