/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class MultifaceBlock
extends Block {
    private static final float AABB_OFFSET = 1.0f;
    private static final VoxelShape UP_AABB = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape DOWN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape EAST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
    private static final Map<Direction, VoxelShape> SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), enumMap -> {
        enumMap.put(Direction.NORTH, NORTH_AABB);
        enumMap.put(Direction.EAST, EAST_AABB);
        enumMap.put(Direction.SOUTH, SOUTH_AABB);
        enumMap.put(Direction.WEST, WEST_AABB);
        enumMap.put(Direction.UP, UP_AABB);
        enumMap.put(Direction.DOWN, DOWN_AABB);
    });
    protected static final Direction[] DIRECTIONS = Direction.values();
    private final ImmutableMap<BlockState, VoxelShape> shapesCache;
    private final boolean canRotate;
    private final boolean canMirrorX;
    private final boolean canMirrorZ;

    public MultifaceBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(MultifaceBlock.getDefaultMultifaceState(this.stateDefinition));
        this.shapesCache = this.getShapeForEachState(MultifaceBlock::calculateMultifaceShape);
        this.canRotate = Direction.Plane.HORIZONTAL.stream().allMatch(this::isFaceSupported);
        this.canMirrorX = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::isFaceSupported).count() % 2L == 0L;
        this.canMirrorZ = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::isFaceSupported).count() % 2L == 0L;
    }

    public static Set<Direction> availableFaces(BlockState blockState) {
        if (!(blockState.getBlock() instanceof MultifaceBlock)) {
            return Set.of();
        }
        EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if (!MultifaceBlock.hasFace(blockState, direction)) continue;
            set.add(direction);
        }
        return set;
    }

    public static Set<Direction> unpack(byte b) {
        EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if ((b & (byte)(1 << direction.ordinal())) <= 0) continue;
            set.add(direction);
        }
        return set;
    }

    public static byte pack(Collection<Direction> collection) {
        byte b = 0;
        for (Direction direction : collection) {
            b = (byte)(b | 1 << direction.ordinal());
        }
        return b;
    }

    protected boolean isFaceSupported(Direction direction) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        for (Direction direction : DIRECTIONS) {
            if (!this.isFaceSupported(direction)) continue;
            builder.add(MultifaceBlock.getFaceProperty(direction));
        }
    }

    @Override
    public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (!MultifaceBlock.hasAnyFace(blockState)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (!MultifaceBlock.hasFace(blockState, direction) || MultifaceBlock.canAttachTo(levelAccessor, direction, blockPos2, blockState2)) {
            return blockState;
        }
        return MultifaceBlock.removeFace(blockState, MultifaceBlock.getFaceProperty(direction));
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shapesCache.get(blockState);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        boolean bl = false;
        for (Direction direction : DIRECTIONS) {
            if (!MultifaceBlock.hasFace(blockState, direction)) continue;
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!MultifaceBlock.canAttachTo(levelReader, direction, blockPos2, levelReader.getBlockState(blockPos2))) {
                return false;
            }
            bl = true;
        }
        return bl;
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return MultifaceBlock.hasAnyVacantFace(blockState);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Level level = blockPlaceContext.getLevel();
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        return Arrays.stream(blockPlaceContext.getNearestLookingDirections()).map(direction -> this.getStateForPlacement(blockState, level, blockPos, (Direction)direction)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public boolean isValidStateForPlacement(BlockGetter blockGetter, BlockState blockState, BlockPos blockPos, Direction direction) {
        if (!this.isFaceSupported(direction) || blockState.is(this) && MultifaceBlock.hasFace(blockState, direction)) {
            return false;
        }
        BlockPos blockPos2 = blockPos.relative(direction);
        return MultifaceBlock.canAttachTo(blockGetter, direction, blockPos2, blockGetter.getBlockState(blockPos2));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!this.isValidStateForPlacement(blockGetter, blockState, blockPos, direction)) {
            return null;
        }
        BlockState blockState2 = blockState.is(this) ? blockState : (this.isWaterloggable() && blockState.getFluidState().isSourceOfType(Fluids.WATER) ? (BlockState)this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true) : this.defaultBlockState());
        return (BlockState)blockState2.setValue(MultifaceBlock.getFaceProperty(direction), true);
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        if (!this.canRotate) {
            return blockState;
        }
        return this.mapDirections(blockState, rotation::rotate);
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        if (mirror == Mirror.FRONT_BACK && !this.canMirrorX) {
            return blockState;
        }
        if (mirror == Mirror.LEFT_RIGHT && !this.canMirrorZ) {
            return blockState;
        }
        return this.mapDirections(blockState, mirror::mirror);
    }

    private BlockState mapDirections(BlockState blockState, Function<Direction, Direction> function) {
        BlockState blockState2 = blockState;
        for (Direction direction : DIRECTIONS) {
            if (!this.isFaceSupported(direction)) continue;
            blockState2 = (BlockState)blockState2.setValue(MultifaceBlock.getFaceProperty(function.apply(direction)), blockState.getValue(MultifaceBlock.getFaceProperty(direction)));
        }
        return blockState2;
    }

    public static boolean hasFace(BlockState blockState, Direction direction) {
        BooleanProperty booleanProperty = MultifaceBlock.getFaceProperty(direction);
        return blockState.hasProperty(booleanProperty) && blockState.getValue(booleanProperty) != false;
    }

    public static boolean canAttachTo(BlockGetter blockGetter, Direction direction, BlockPos blockPos, BlockState blockState) {
        return Block.isFaceFull(blockState.getBlockSupportShape(blockGetter, blockPos), direction.getOpposite()) || Block.isFaceFull(blockState.getCollisionShape(blockGetter, blockPos), direction.getOpposite());
    }

    private boolean isWaterloggable() {
        return this.stateDefinition.getProperties().contains(BlockStateProperties.WATERLOGGED);
    }

    private static BlockState removeFace(BlockState blockState, BooleanProperty booleanProperty) {
        BlockState blockState2 = (BlockState)blockState.setValue(booleanProperty, false);
        if (MultifaceBlock.hasAnyFace(blockState2)) {
            return blockState2;
        }
        return Blocks.AIR.defaultBlockState();
    }

    public static BooleanProperty getFaceProperty(Direction direction) {
        return PROPERTY_BY_DIRECTION.get(direction);
    }

    private static BlockState getDefaultMultifaceState(StateDefinition<Block, BlockState> stateDefinition) {
        BlockState blockState = stateDefinition.any();
        for (BooleanProperty booleanProperty : PROPERTY_BY_DIRECTION.values()) {
            if (!blockState.hasProperty(booleanProperty)) continue;
            blockState = (BlockState)blockState.setValue(booleanProperty, false);
        }
        return blockState;
    }

    private static VoxelShape calculateMultifaceShape(BlockState blockState) {
        VoxelShape voxelShape = Shapes.empty();
        for (Direction direction : DIRECTIONS) {
            if (!MultifaceBlock.hasFace(blockState, direction)) continue;
            voxelShape = Shapes.or(voxelShape, SHAPE_BY_DIRECTION.get(direction));
        }
        return voxelShape.isEmpty() ? Shapes.block() : voxelShape;
    }

    protected static boolean hasAnyFace(BlockState blockState) {
        return Arrays.stream(DIRECTIONS).anyMatch(direction -> MultifaceBlock.hasFace(blockState, direction));
    }

    private static boolean hasAnyVacantFace(BlockState blockState) {
        return Arrays.stream(DIRECTIONS).anyMatch(direction -> !MultifaceBlock.hasFace(blockState, direction));
    }

    public abstract MultifaceSpreader getSpreader();
}

