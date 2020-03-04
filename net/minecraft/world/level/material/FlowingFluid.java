/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.material;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FlowingFluid
extends Fluid {
    public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_FLOWING;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(200){

            @Override
            protected void rehash(int i) {
            }
        };
        object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
        return object2ByteLinkedOpenHashMap;
    });
    private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        builder.add(FALLING);
    }

    @Override
    public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos, FluidState fluidState) {
        double d = 0.0;
        double e = 0.0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            mutableBlockPos.setWithOffset(blockPos, direction);
            FluidState fluidState2 = blockGetter.getFluidState(mutableBlockPos);
            if (!this.affectsFlow(fluidState2)) continue;
            float f = fluidState2.getOwnHeight();
            float g = 0.0f;
            if (f == 0.0f) {
                Vec3i blockPos2;
                FluidState fluidState3;
                if (!blockGetter.getBlockState(mutableBlockPos).getMaterial().blocksMotion() && this.affectsFlow(fluidState3 = blockGetter.getFluidState((BlockPos)(blockPos2 = mutableBlockPos.below()))) && (f = fluidState3.getOwnHeight()) > 0.0f) {
                    g = fluidState.getOwnHeight() - (f - 0.8888889f);
                }
            } else if (f > 0.0f) {
                g = fluidState.getOwnHeight() - f;
            }
            if (g == 0.0f) continue;
            d += (double)((float)direction.getStepX() * g);
            e += (double)((float)direction.getStepZ() * g);
        }
        Vec3 vec3 = new Vec3(d, 0.0, e);
        if (fluidState.getValue(FALLING).booleanValue()) {
            for (Direction direction2 : Direction.Plane.HORIZONTAL) {
                mutableBlockPos.setWithOffset(blockPos, direction2);
                if (!this.isSolidFace(blockGetter, mutableBlockPos, direction2) && !this.isSolidFace(blockGetter, mutableBlockPos.above(), direction2)) continue;
                vec3 = vec3.normalize().add(0.0, -6.0, 0.0);
                break;
            }
        }
        return vec3.normalize();
    }

    private boolean affectsFlow(FluidState fluidState) {
        return fluidState.isEmpty() || fluidState.getType().isSame(this);
    }

    protected boolean isSolidFace(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        FluidState fluidState = blockGetter.getFluidState(blockPos);
        if (fluidState.getType().isSame(this)) {
            return false;
        }
        if (direction == Direction.UP) {
            return true;
        }
        if (blockState.getMaterial() == Material.ICE) {
            return false;
        }
        return blockState.isFaceSturdy(blockGetter, blockPos, direction);
    }

    protected void spread(LevelAccessor levelAccessor, BlockPos blockPos, FluidState fluidState) {
        if (fluidState.isEmpty()) {
            return;
        }
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState2 = levelAccessor.getBlockState(blockPos2);
        FluidState fluidState2 = this.getNewLiquid(levelAccessor, blockPos2, blockState2);
        if (this.canSpreadTo(levelAccessor, blockPos, blockState, Direction.DOWN, blockPos2, blockState2, levelAccessor.getFluidState(blockPos2), fluidState2.getType())) {
            this.spreadTo(levelAccessor, blockPos2, blockState2, Direction.DOWN, fluidState2);
            if (this.sourceNeighborCount(levelAccessor, blockPos) >= 3) {
                this.spreadToSides(levelAccessor, blockPos, fluidState, blockState);
            }
        } else if (fluidState.isSource() || !this.isWaterHole(levelAccessor, fluidState2.getType(), blockPos, blockState, blockPos2, blockState2)) {
            this.spreadToSides(levelAccessor, blockPos, fluidState, blockState);
        }
    }

    private void spreadToSides(LevelAccessor levelAccessor, BlockPos blockPos, FluidState fluidState, BlockState blockState) {
        int i = fluidState.getAmount() - this.getDropOff(levelAccessor);
        if (fluidState.getValue(FALLING).booleanValue()) {
            i = 7;
        }
        if (i <= 0) {
            return;
        }
        Map<Direction, FluidState> map = this.getSpread(levelAccessor, blockPos, blockState);
        for (Map.Entry<Direction, FluidState> entry : map.entrySet()) {
            BlockState blockState2;
            Direction direction = entry.getKey();
            FluidState fluidState2 = entry.getValue();
            BlockPos blockPos2 = blockPos.relative(direction);
            if (!this.canSpreadTo(levelAccessor, blockPos, blockState, direction, blockPos2, blockState2 = levelAccessor.getBlockState(blockPos2), levelAccessor.getFluidState(blockPos2), fluidState2.getType())) continue;
            this.spreadTo(levelAccessor, blockPos2, blockState2, direction, fluidState2);
        }
    }

    protected FluidState getNewLiquid(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        BlockPos blockPos3;
        BlockState blockState4;
        FluidState fluidState3;
        int i = 0;
        int j = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            BlockState blockState2 = levelReader.getBlockState(blockPos2);
            FluidState fluidState = blockState2.getFluidState();
            if (!fluidState.getType().isSame(this) || !this.canPassThroughWall(direction, levelReader, blockPos, blockState, blockPos2, blockState2)) continue;
            if (fluidState.isSource()) {
                ++j;
            }
            i = Math.max(i, fluidState.getAmount());
        }
        if (this.canConvertToSource() && j >= 2) {
            BlockState blockState3 = levelReader.getBlockState(blockPos.below());
            FluidState fluidState2 = blockState3.getFluidState();
            if (blockState3.getMaterial().isSolid() || this.isSourceBlockOfThisType(fluidState2)) {
                return this.getSource(false);
            }
        }
        if (!(fluidState3 = (blockState4 = levelReader.getBlockState(blockPos3 = blockPos.above())).getFluidState()).isEmpty() && fluidState3.getType().isSame(this) && this.canPassThroughWall(Direction.UP, levelReader, blockPos, blockState, blockPos3, blockState4)) {
            return this.getFlowing(8, true);
        }
        int k = i - this.getDropOff(levelReader);
        if (k <= 0) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return this.getFlowing(k, false);
    }

    private boolean canPassThroughWall(Direction direction, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
        VoxelShape voxelShape2;
        VoxelShape voxelShape;
        boolean bl;
        Block.BlockStatePairKey blockStatePairKey;
        Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2ByteLinkedOpenHashMap = blockState.getBlock().hasDynamicShape() || blockState2.getBlock().hasDynamicShape() ? null : OCCLUSION_CACHE.get();
        if (object2ByteLinkedOpenHashMap != null) {
            blockStatePairKey = new Block.BlockStatePairKey(blockState, blockState2, direction);
            byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst(blockStatePairKey);
            if (b != 127) {
                return b != 0;
            }
        } else {
            blockStatePairKey = null;
        }
        boolean bl2 = bl = !Shapes.mergedFaceOccludes(voxelShape = blockState.getCollisionShape(blockGetter, blockPos), voxelShape2 = blockState2.getCollisionShape(blockGetter, blockPos2), direction);
        if (object2ByteLinkedOpenHashMap != null) {
            if (object2ByteLinkedOpenHashMap.size() == 200) {
                object2ByteLinkedOpenHashMap.removeLastByte();
            }
            object2ByteLinkedOpenHashMap.putAndMoveToFirst(blockStatePairKey, (byte)(bl ? 1 : 0));
        }
        return bl;
    }

    public abstract Fluid getFlowing();

    public FluidState getFlowing(int i, boolean bl) {
        return (FluidState)((FluidState)this.getFlowing().defaultFluidState().setValue(LEVEL, i)).setValue(FALLING, bl);
    }

    public abstract Fluid getSource();

    public FluidState getSource(boolean bl) {
        return (FluidState)this.getSource().defaultFluidState().setValue(FALLING, bl);
    }

    protected abstract boolean canConvertToSource();

    protected void spreadTo(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Direction direction, FluidState fluidState) {
        if (blockState.getBlock() instanceof LiquidBlockContainer) {
            ((LiquidBlockContainer)((Object)blockState.getBlock())).placeLiquid(levelAccessor, blockPos, blockState, fluidState);
        } else {
            if (!blockState.isAir()) {
                this.beforeDestroyingBlock(levelAccessor, blockPos, blockState);
            }
            levelAccessor.setBlock(blockPos, fluidState.createLegacyBlock(), 3);
        }
    }

    protected abstract void beforeDestroyingBlock(LevelAccessor var1, BlockPos var2, BlockState var3);

    private static short getCacheKey(BlockPos blockPos, BlockPos blockPos2) {
        int i = blockPos2.getX() - blockPos.getX();
        int j = blockPos2.getZ() - blockPos.getZ();
        return (short)((i + 128 & 0xFF) << 8 | j + 128 & 0xFF);
    }

    protected int getSlopeDistance(LevelReader levelReader, BlockPos blockPos, int i2, Direction direction, BlockState blockState, BlockPos blockPos2, Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap, Short2BooleanMap short2BooleanMap) {
        int j = 1000;
        for (Direction direction2 : Direction.Plane.HORIZONTAL) {
            int k;
            if (direction2 == direction) continue;
            BlockPos blockPos3 = blockPos.relative(direction2);
            short s = FlowingFluid.getCacheKey(blockPos2, blockPos3);
            Pair pair = short2ObjectMap.computeIfAbsent(s, i -> {
                BlockState blockState = levelReader.getBlockState(blockPos3);
                return Pair.of(blockState, blockState.getFluidState());
            });
            BlockState blockState2 = (BlockState)pair.getFirst();
            FluidState fluidState = (FluidState)pair.getSecond();
            if (!this.canPassThrough(levelReader, this.getFlowing(), blockPos, blockState, direction2, blockPos3, blockState2, fluidState)) continue;
            boolean bl = short2BooleanMap.computeIfAbsent(s, i -> {
                BlockPos blockPos2 = blockPos3.below();
                BlockState blockState2 = levelReader.getBlockState(blockPos2);
                return this.isWaterHole(levelReader, this.getFlowing(), blockPos3, blockState2, blockPos2, blockState2);
            });
            if (bl) {
                return i2;
            }
            if (i2 >= this.getSlopeFindDistance(levelReader) || (k = this.getSlopeDistance(levelReader, blockPos3, i2 + 1, direction2.getOpposite(), blockState2, blockPos2, short2ObjectMap, short2BooleanMap)) >= j) continue;
            j = k;
        }
        return j;
    }

    private boolean isWaterHole(BlockGetter blockGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
        if (!this.canPassThroughWall(Direction.DOWN, blockGetter, blockPos, blockState, blockPos2, blockState2)) {
            return false;
        }
        if (blockState2.getFluidState().getType().isSame(this)) {
            return true;
        }
        return this.canHoldFluid(blockGetter, blockPos2, blockState2, fluid);
    }

    private boolean canPassThrough(BlockGetter blockGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState) {
        return !this.isSourceBlockOfThisType(fluidState) && this.canPassThroughWall(direction, blockGetter, blockPos, blockState, blockPos2, blockState2) && this.canHoldFluid(blockGetter, blockPos2, blockState2, fluid);
    }

    private boolean isSourceBlockOfThisType(FluidState fluidState) {
        return fluidState.getType().isSame(this) && fluidState.isSource();
    }

    protected abstract int getSlopeFindDistance(LevelReader var1);

    private int sourceNeighborCount(LevelReader levelReader, BlockPos blockPos) {
        int i = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            FluidState fluidState = levelReader.getFluidState(blockPos2);
            if (!this.isSourceBlockOfThisType(fluidState)) continue;
            ++i;
        }
        return i;
    }

    protected Map<Direction, FluidState> getSpread(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        int i2 = 1000;
        EnumMap<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
        Short2ObjectOpenHashMap<Pair<BlockState, FluidState>> short2ObjectMap = new Short2ObjectOpenHashMap<Pair<BlockState, FluidState>>();
        Short2BooleanOpenHashMap short2BooleanMap = new Short2BooleanOpenHashMap();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            short s = FlowingFluid.getCacheKey(blockPos, blockPos2);
            Pair pair = short2ObjectMap.computeIfAbsent(s, i -> {
                BlockState blockState = levelReader.getBlockState(blockPos2);
                return Pair.of(blockState, blockState.getFluidState());
            });
            BlockState blockState2 = (BlockState)pair.getFirst();
            FluidState fluidState = (FluidState)pair.getSecond();
            FluidState fluidState2 = this.getNewLiquid(levelReader, blockPos2, blockState2);
            if (!this.canPassThrough(levelReader, fluidState2.getType(), blockPos, blockState, direction, blockPos2, blockState2, fluidState)) continue;
            BlockPos blockPos3 = blockPos2.below();
            boolean bl = short2BooleanMap.computeIfAbsent(s, i -> {
                BlockState blockState2 = levelReader.getBlockState(blockPos3);
                return this.isWaterHole(levelReader, this.getFlowing(), blockPos2, blockState2, blockPos3, blockState2);
            });
            int j = bl ? 0 : this.getSlopeDistance(levelReader, blockPos2, 1, direction.getOpposite(), blockState2, blockPos, short2ObjectMap, short2BooleanMap);
            if (j < i2) {
                map.clear();
            }
            if (j > i2) continue;
            map.put(direction, fluidState2);
            i2 = j;
        }
        return map;
    }

    private boolean canHoldFluid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        Block block = blockState.getBlock();
        if (block instanceof LiquidBlockContainer) {
            return ((LiquidBlockContainer)((Object)block)).canPlaceLiquid(blockGetter, blockPos, blockState, fluid);
        }
        if (block instanceof DoorBlock || block.is(BlockTags.SIGNS) || block == Blocks.LADDER || block == Blocks.SUGAR_CANE || block == Blocks.BUBBLE_COLUMN) {
            return false;
        }
        Material material = blockState.getMaterial();
        if (material == Material.PORTAL || material == Material.STRUCTURAL_AIR || material == Material.WATER_PLANT || material == Material.REPLACEABLE_WATER_PLANT) {
            return false;
        }
        return !material.blocksMotion();
    }

    protected boolean canSpreadTo(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState, Fluid fluid) {
        return fluidState.canBeReplacedWith(blockGetter, blockPos2, fluid, direction) && this.canPassThroughWall(direction, blockGetter, blockPos, blockState, blockPos2, blockState2) && this.canHoldFluid(blockGetter, blockPos2, blockState2, fluid);
    }

    protected abstract int getDropOff(LevelReader var1);

    protected int getSpreadDelay(Level level, BlockPos blockPos, FluidState fluidState, FluidState fluidState2) {
        return this.getTickDelay(level);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, FluidState fluidState) {
        if (!fluidState.isSource()) {
            FluidState fluidState2 = this.getNewLiquid(level, blockPos, level.getBlockState(blockPos));
            int i = this.getSpreadDelay(level, blockPos, fluidState, fluidState2);
            if (fluidState2.isEmpty()) {
                fluidState = fluidState2;
                level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
            } else if (!fluidState2.equals(fluidState)) {
                fluidState = fluidState2;
                BlockState blockState = fluidState.createLegacyBlock();
                level.setBlock(blockPos, blockState, 2);
                level.getLiquidTicks().scheduleTick(blockPos, fluidState.getType(), i);
                level.updateNeighborsAt(blockPos, blockState.getBlock());
            }
        }
        this.spread(level, blockPos, fluidState);
    }

    protected static int getLegacyLevel(FluidState fluidState) {
        if (fluidState.isSource()) {
            return 0;
        }
        return 8 - Math.min(fluidState.getAmount(), 8) + (fluidState.getValue(FALLING) != false ? 8 : 0);
    }

    private static boolean hasSameAbove(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        return fluidState.getType().isSame(blockGetter.getFluidState(blockPos.above()).getType());
    }

    @Override
    public float getHeight(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        if (FlowingFluid.hasSameAbove(fluidState, blockGetter, blockPos)) {
            return 1.0f;
        }
        return fluidState.getOwnHeight();
    }

    @Override
    public float getOwnHeight(FluidState fluidState) {
        return (float)fluidState.getAmount() / 9.0f;
    }

    @Override
    public VoxelShape getShape(FluidState fluidState2, BlockGetter blockGetter, BlockPos blockPos) {
        if (fluidState2.getAmount() == 9 && FlowingFluid.hasSameAbove(fluidState2, blockGetter, blockPos)) {
            return Shapes.block();
        }
        return this.shapes.computeIfAbsent(fluidState2, fluidState -> Shapes.box(0.0, 0.0, 0.0, 1.0, fluidState.getHeight(blockGetter, blockPos), 1.0));
    }
}

