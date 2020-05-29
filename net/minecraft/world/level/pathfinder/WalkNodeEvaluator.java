/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WalkNodeEvaluator
extends NodeEvaluator {
    protected float oldWaterCost;
    private final Long2ObjectMap<BlockPathTypes> pathTypesByPosCache = new Long2ObjectOpenHashMap<BlockPathTypes>();
    private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap<AABB>();

    @Override
    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        super.prepare(pathNavigationRegion, mob);
        this.oldWaterCost = mob.getPathfindingMalus(BlockPathTypes.WATER);
    }

    @Override
    public void done() {
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        this.pathTypesByPosCache.clear();
        this.collisionCache.clear();
        super.done();
    }

    @Override
    public Node getStart() {
        BlockPos blockPos;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int i = Mth.floor(this.mob.getY());
        BlockState blockState = this.level.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)i, this.mob.getZ()));
        if (this.mob.canStandOnFluid(blockState.getFluidState().getType())) {
            while (this.mob.canStandOnFluid(blockState.getFluidState().getType())) {
                blockState = this.level.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)(++i), this.mob.getZ()));
            }
            --i;
        } else if (this.canFloat() && this.mob.isInWater()) {
            while (blockState.getBlock() == Blocks.WATER || blockState.getFluidState() == Fluids.WATER.getSource(false)) {
                blockState = this.level.getBlockState(mutableBlockPos.set(this.mob.getX(), (double)(++i), this.mob.getZ()));
            }
            --i;
        } else if (this.mob.isOnGround()) {
            i = Mth.floor(this.mob.getY() + 0.5);
        } else {
            blockPos = this.mob.blockPosition();
            while ((this.level.getBlockState(blockPos).isAir() || this.level.getBlockState(blockPos).isPathfindable(this.level, blockPos, PathComputationType.LAND)) && blockPos.getY() > 0) {
                blockPos = blockPos.below();
            }
            i = blockPos.above().getY();
        }
        blockPos = this.mob.blockPosition();
        BlockPathTypes blockPathTypes = this.getCachedBlockType(this.mob, blockPos.getX(), i, blockPos.getZ());
        if (this.mob.getPathfindingMalus(blockPathTypes) < 0.0f) {
            AABB aABB = this.mob.getBoundingBox();
            if (this.hasPositiveMalus(mutableBlockPos.set(aABB.minX, (double)i, aABB.minZ)) || this.hasPositiveMalus(mutableBlockPos.set(aABB.minX, (double)i, aABB.maxZ)) || this.hasPositiveMalus(mutableBlockPos.set(aABB.maxX, (double)i, aABB.minZ)) || this.hasPositiveMalus(mutableBlockPos.set(aABB.maxX, (double)i, aABB.maxZ))) {
                Node node = this.getNode(mutableBlockPos);
                node.type = this.getBlockPathType(this.mob, node.asBlockPos());
                node.costMalus = this.mob.getPathfindingMalus(node.type);
                return node;
            }
        }
        Node node2 = this.getNode(blockPos.getX(), i, blockPos.getZ());
        node2.type = this.getBlockPathType(this.mob, node2.asBlockPos());
        node2.costMalus = this.mob.getPathfindingMalus(node2.type);
        return node2;
    }

    private boolean hasPositiveMalus(BlockPos blockPos) {
        BlockPathTypes blockPathTypes = this.getBlockPathType(this.mob, blockPos);
        return this.mob.getPathfindingMalus(blockPathTypes) >= 0.0f;
    }

    @Override
    public Target getGoal(double d, double e, double f) {
        return new Target(this.getNode(Mth.floor(d), Mth.floor(e), Mth.floor(f)));
    }

    @Override
    public int getNeighbors(Node[] nodes, Node node) {
        Node node9;
        Node node8;
        Node node7;
        Node node6;
        Node node5;
        Node node4;
        Node node3;
        double d;
        Node node2;
        int i = 0;
        int j = 0;
        BlockPathTypes blockPathTypes = this.getCachedBlockType(this.mob, node.x, node.y + 1, node.z);
        if (this.mob.getPathfindingMalus(blockPathTypes) >= 0.0f) {
            BlockPathTypes blockPathTypes2 = this.getCachedBlockType(this.mob, node.x, node.y, node.z);
            j = blockPathTypes2 == BlockPathTypes.STICKY_HONEY ? 0 : Mth.floor(Math.max(1.0f, this.mob.maxUpStep));
        }
        if ((node2 = this.getLandNode(node.x, node.y, node.z + 1, j, d = WalkNodeEvaluator.getFloorLevel(this.level, new BlockPos(node.x, node.y, node.z)), Direction.SOUTH)) != null && !node2.closed && (node2.costMalus >= 0.0f || node.costMalus < 0.0f)) {
            nodes[i++] = node2;
        }
        if ((node3 = this.getLandNode(node.x - 1, node.y, node.z, j, d, Direction.WEST)) != null && !node3.closed && (node3.costMalus >= 0.0f || node.costMalus < 0.0f)) {
            nodes[i++] = node3;
        }
        if ((node4 = this.getLandNode(node.x + 1, node.y, node.z, j, d, Direction.EAST)) != null && !node4.closed && (node4.costMalus >= 0.0f || node.costMalus < 0.0f)) {
            nodes[i++] = node4;
        }
        if ((node5 = this.getLandNode(node.x, node.y, node.z - 1, j, d, Direction.NORTH)) != null && !node5.closed && (node5.costMalus >= 0.0f || node.costMalus < 0.0f)) {
            nodes[i++] = node5;
        }
        if (this.isDiagonalValid(node, node3, node5, node6 = this.getLandNode(node.x - 1, node.y, node.z - 1, j, d, Direction.NORTH))) {
            nodes[i++] = node6;
        }
        if (this.isDiagonalValid(node, node4, node5, node7 = this.getLandNode(node.x + 1, node.y, node.z - 1, j, d, Direction.NORTH))) {
            nodes[i++] = node7;
        }
        if (this.isDiagonalValid(node, node3, node2, node8 = this.getLandNode(node.x - 1, node.y, node.z + 1, j, d, Direction.SOUTH))) {
            nodes[i++] = node8;
        }
        if (this.isDiagonalValid(node, node4, node2, node9 = this.getLandNode(node.x + 1, node.y, node.z + 1, j, d, Direction.SOUTH))) {
            nodes[i++] = node9;
        }
        return i;
    }

    private boolean isDiagonalValid(Node node, @Nullable Node node2, @Nullable Node node3, @Nullable Node node4) {
        if (node4 == null || node3 == null || node2 == null) {
            return false;
        }
        if (node4.closed) {
            return false;
        }
        if (node3.y > node.y || node2.y > node.y) {
            return false;
        }
        return node4.costMalus >= 0.0f && (node3.y < node.y || node3.costMalus >= 0.0f) && (node2.y < node.y || node2.costMalus >= 0.0f);
    }

    public static double getFloorLevel(BlockGetter blockGetter, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        VoxelShape voxelShape = blockGetter.getBlockState(blockPos2).getCollisionShape(blockGetter, blockPos2);
        return (double)blockPos2.getY() + (voxelShape.isEmpty() ? 0.0 : voxelShape.max(Direction.Axis.Y));
    }

    @Nullable
    private Node getLandNode(int i, int j, int k, int l, double d, Direction direction) {
        double m;
        double h;
        AABB aABB;
        Node node = null;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        double e = WalkNodeEvaluator.getFloorLevel(this.level, mutableBlockPos.set(i, j, k));
        if (e - d > 1.125) {
            return null;
        }
        BlockPathTypes blockPathTypes = this.getCachedBlockType(this.mob, i, j, k);
        float f = this.mob.getPathfindingMalus(blockPathTypes);
        double g = (double)this.mob.getBbWidth() / 2.0;
        if (f >= 0.0f) {
            node = this.getNode(i, j, k);
            node.type = blockPathTypes;
            node.costMalus = Math.max(node.costMalus, f);
        }
        if (blockPathTypes == BlockPathTypes.WALKABLE) {
            return node;
        }
        if ((node == null || node.costMalus < 0.0f) && l > 0 && blockPathTypes != BlockPathTypes.FENCE && blockPathTypes != BlockPathTypes.TRAPDOOR && (node = this.getLandNode(i, j + 1, k, l - 1, d, direction)) != null && (node.type == BlockPathTypes.OPEN || node.type == BlockPathTypes.WALKABLE) && this.mob.getBbWidth() < 1.0f && this.hasCollisions(aABB = new AABB((h = (double)(i - direction.getStepX()) + 0.5) - g, WalkNodeEvaluator.getFloorLevel(this.level, mutableBlockPos.set(h, (double)(j + 1), m = (double)(k - direction.getStepZ()) + 0.5)) + 0.001, m - g, h + g, (double)this.mob.getBbHeight() + WalkNodeEvaluator.getFloorLevel(this.level, mutableBlockPos.set((double)node.x, (double)node.y, (double)node.z)) - 0.002, m + g))) {
            node = null;
        }
        if (blockPathTypes == BlockPathTypes.WATER && !this.canFloat()) {
            if (this.getCachedBlockType(this.mob, i, j - 1, k) != BlockPathTypes.WATER) {
                return node;
            }
            while (j > 0) {
                if ((blockPathTypes = this.getCachedBlockType(this.mob, i, --j, k)) == BlockPathTypes.WATER) {
                    node = this.getNode(i, j, k);
                    node.type = blockPathTypes;
                    node.costMalus = Math.max(node.costMalus, this.mob.getPathfindingMalus(blockPathTypes));
                    continue;
                }
                return node;
            }
        }
        if (blockPathTypes == BlockPathTypes.OPEN) {
            BlockPathTypes blockPathTypes2;
            AABB aABB2 = new AABB((double)i - g + 0.5, (double)j + 0.001, (double)k - g + 0.5, (double)i + g + 0.5, (float)j + this.mob.getBbHeight(), (double)k + g + 0.5);
            if (this.hasCollisions(aABB2)) {
                return null;
            }
            if (this.mob.getBbWidth() >= 1.0f && (blockPathTypes2 = this.getCachedBlockType(this.mob, i, j - 1, k)) == BlockPathTypes.BLOCKED) {
                node = this.getNode(i, j, k);
                node.type = BlockPathTypes.WALKABLE;
                node.costMalus = Math.max(node.costMalus, f);
                return node;
            }
            int n = 0;
            int o = j;
            while (blockPathTypes == BlockPathTypes.OPEN) {
                Node node2;
                if (--j < 0) {
                    node2 = this.getNode(i, o, k);
                    node2.type = BlockPathTypes.BLOCKED;
                    node2.costMalus = -1.0f;
                    return node2;
                }
                node2 = this.getNode(i, j, k);
                if (n++ >= this.mob.getMaxFallDistance()) {
                    node2.type = BlockPathTypes.BLOCKED;
                    node2.costMalus = -1.0f;
                    return node2;
                }
                blockPathTypes = this.getCachedBlockType(this.mob, i, j, k);
                f = this.mob.getPathfindingMalus(blockPathTypes);
                if (blockPathTypes != BlockPathTypes.OPEN && f >= 0.0f) {
                    node = node2;
                    node.type = blockPathTypes;
                    node.costMalus = Math.max(node.costMalus, f);
                    break;
                }
                if (!(f < 0.0f)) continue;
                node2.type = BlockPathTypes.BLOCKED;
                node2.costMalus = -1.0f;
                return node2;
            }
        }
        return node;
    }

    private boolean hasCollisions(AABB aABB) {
        return this.collisionCache.computeIfAbsent(aABB, aABB2 -> !this.level.noCollision(this.mob, aABB));
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k, Mob mob, int l, int m, int n, boolean bl, boolean bl2) {
        EnumSet<BlockPathTypes> enumSet = EnumSet.noneOf(BlockPathTypes.class);
        BlockPathTypes blockPathTypes = BlockPathTypes.BLOCKED;
        double d = (double)mob.getBbWidth() / 2.0;
        BlockPos blockPos = mob.blockPosition();
        blockPathTypes = this.getBlockPathTypes(blockGetter, i, j, k, l, m, n, bl, bl2, enumSet, blockPathTypes, blockPos);
        if (enumSet.contains((Object)BlockPathTypes.FENCE)) {
            return BlockPathTypes.FENCE;
        }
        BlockPathTypes blockPathTypes2 = BlockPathTypes.BLOCKED;
        for (BlockPathTypes blockPathTypes3 : enumSet) {
            if (mob.getPathfindingMalus(blockPathTypes3) < 0.0f) {
                return blockPathTypes3;
            }
            if (!(mob.getPathfindingMalus(blockPathTypes3) >= mob.getPathfindingMalus(blockPathTypes2))) continue;
            blockPathTypes2 = blockPathTypes3;
        }
        if (blockPathTypes == BlockPathTypes.OPEN && mob.getPathfindingMalus(blockPathTypes2) == 0.0f) {
            return BlockPathTypes.OPEN;
        }
        return blockPathTypes2;
    }

    public BlockPathTypes getBlockPathTypes(BlockGetter blockGetter, int i, int j, int k, int l, int m, int n, boolean bl, boolean bl2, EnumSet<BlockPathTypes> enumSet, BlockPathTypes blockPathTypes, BlockPos blockPos) {
        for (int o = 0; o < l; ++o) {
            for (int p = 0; p < m; ++p) {
                for (int q = 0; q < n; ++q) {
                    int r = o + i;
                    int s = p + j;
                    int t = q + k;
                    BlockPathTypes blockPathTypes2 = this.getBlockPathType(blockGetter, r, s, t);
                    blockPathTypes2 = this.evaluateBlockPathType(blockGetter, bl, bl2, blockPos, blockPathTypes2);
                    if (o == 0 && p == 0 && q == 0) {
                        blockPathTypes = blockPathTypes2;
                    }
                    enumSet.add(blockPathTypes2);
                }
            }
        }
        return blockPathTypes;
    }

    protected BlockPathTypes evaluateBlockPathType(BlockGetter blockGetter, boolean bl, boolean bl2, BlockPos blockPos, BlockPathTypes blockPathTypes) {
        if (blockPathTypes == BlockPathTypes.DOOR_WOOD_CLOSED && bl && bl2) {
            blockPathTypes = BlockPathTypes.WALKABLE;
        }
        if (blockPathTypes == BlockPathTypes.DOOR_OPEN && !bl2) {
            blockPathTypes = BlockPathTypes.BLOCKED;
        }
        if (blockPathTypes == BlockPathTypes.RAIL && !(blockGetter.getBlockState(blockPos).getBlock() instanceof BaseRailBlock) && !(blockGetter.getBlockState(blockPos.below()).getBlock() instanceof BaseRailBlock)) {
            blockPathTypes = BlockPathTypes.FENCE;
        }
        if (blockPathTypes == BlockPathTypes.LEAVES) {
            blockPathTypes = BlockPathTypes.BLOCKED;
        }
        return blockPathTypes;
    }

    private BlockPathTypes getBlockPathType(Mob mob, BlockPos blockPos) {
        return this.getCachedBlockType(mob, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    private BlockPathTypes getCachedBlockType(Mob mob, int i, int j, int k) {
        return this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong(i, j, k), l -> this.getBlockPathType(this.level, i, j, k, mob, this.entityWidth, this.entityHeight, this.entityDepth, this.canOpenDoors(), this.canPassDoors()));
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k) {
        return WalkNodeEvaluator.getBlockPathTypeStatic(blockGetter, new BlockPos.MutableBlockPos(i, j, k));
    }

    public static BlockPathTypes getBlockPathTypeStatic(BlockGetter blockGetter, BlockPos.MutableBlockPos mutableBlockPos) {
        int i = mutableBlockPos.getX();
        int j = mutableBlockPos.getY();
        int k = mutableBlockPos.getZ();
        BlockPathTypes blockPathTypes = WalkNodeEvaluator.getBlockPathTypeRaw(blockGetter, mutableBlockPos);
        if (blockPathTypes == BlockPathTypes.OPEN && j >= 1) {
            BlockPathTypes blockPathTypes2 = WalkNodeEvaluator.getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(i, j - 1, k));
            BlockPathTypes blockPathTypes3 = blockPathTypes = blockPathTypes2 == BlockPathTypes.WALKABLE || blockPathTypes2 == BlockPathTypes.OPEN || blockPathTypes2 == BlockPathTypes.WATER || blockPathTypes2 == BlockPathTypes.LAVA ? BlockPathTypes.OPEN : BlockPathTypes.WALKABLE;
            if (blockPathTypes2 == BlockPathTypes.DAMAGE_FIRE) {
                blockPathTypes = BlockPathTypes.DAMAGE_FIRE;
            }
            if (blockPathTypes2 == BlockPathTypes.DAMAGE_CACTUS) {
                blockPathTypes = BlockPathTypes.DAMAGE_CACTUS;
            }
            if (blockPathTypes2 == BlockPathTypes.DAMAGE_OTHER) {
                blockPathTypes = BlockPathTypes.DAMAGE_OTHER;
            }
            if (blockPathTypes2 == BlockPathTypes.STICKY_HONEY) {
                blockPathTypes = BlockPathTypes.STICKY_HONEY;
            }
        }
        if (blockPathTypes == BlockPathTypes.WALKABLE) {
            blockPathTypes = WalkNodeEvaluator.checkNeighbourBlocks(blockGetter, mutableBlockPos.set(i, j, k), blockPathTypes);
        }
        return blockPathTypes;
    }

    public static BlockPathTypes checkNeighbourBlocks(BlockGetter blockGetter, BlockPos.MutableBlockPos mutableBlockPos, BlockPathTypes blockPathTypes) {
        int i = mutableBlockPos.getX();
        int j = mutableBlockPos.getY();
        int k = mutableBlockPos.getZ();
        for (int l = -1; l <= 1; ++l) {
            for (int m = -1; m <= 1; ++m) {
                for (int n = -1; n <= 1; ++n) {
                    if (l == 0 && n == 0) continue;
                    mutableBlockPos.set(i + l, j + m, k + n);
                    BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
                    if (blockState.is(Blocks.CACTUS)) {
                        return BlockPathTypes.DANGER_CACTUS;
                    }
                    if (blockState.is(Blocks.SWEET_BERRY_BUSH)) {
                        return BlockPathTypes.DANGER_OTHER;
                    }
                    if (!WalkNodeEvaluator.isBurningBlock(blockState)) continue;
                    return BlockPathTypes.DANGER_FIRE;
                }
            }
        }
        return blockPathTypes;
    }

    protected static BlockPathTypes getBlockPathTypeRaw(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        Block block = blockState.getBlock();
        Material material = blockState.getMaterial();
        if (blockState.isAir()) {
            return BlockPathTypes.OPEN;
        }
        if (blockState.is(BlockTags.TRAPDOORS) || blockState.is(Blocks.LILY_PAD)) {
            return BlockPathTypes.TRAPDOOR;
        }
        if (blockState.is(Blocks.CACTUS)) {
            return BlockPathTypes.DAMAGE_CACTUS;
        }
        if (blockState.is(Blocks.SWEET_BERRY_BUSH)) {
            return BlockPathTypes.DAMAGE_OTHER;
        }
        if (blockState.is(Blocks.HONEY_BLOCK)) {
            return BlockPathTypes.STICKY_HONEY;
        }
        if (blockState.is(Blocks.COCOA)) {
            return BlockPathTypes.COCOA;
        }
        if (WalkNodeEvaluator.isBurningBlock(blockState)) {
            return BlockPathTypes.DAMAGE_FIRE;
        }
        if (DoorBlock.isWoodenDoor(blockState) && !blockState.getValue(DoorBlock.OPEN).booleanValue()) {
            return BlockPathTypes.DOOR_WOOD_CLOSED;
        }
        if (block instanceof DoorBlock && material == Material.METAL && !blockState.getValue(DoorBlock.OPEN).booleanValue()) {
            return BlockPathTypes.DOOR_IRON_CLOSED;
        }
        if (block instanceof DoorBlock && blockState.getValue(DoorBlock.OPEN).booleanValue()) {
            return BlockPathTypes.DOOR_OPEN;
        }
        if (block instanceof BaseRailBlock) {
            return BlockPathTypes.RAIL;
        }
        if (block instanceof LeavesBlock) {
            return BlockPathTypes.LEAVES;
        }
        if (block.is(BlockTags.FENCES) || block.is(BlockTags.WALLS) || block instanceof FenceGateBlock && !blockState.getValue(FenceGateBlock.OPEN).booleanValue()) {
            return BlockPathTypes.FENCE;
        }
        if (!blockState.isPathfindable(blockGetter, blockPos, PathComputationType.LAND)) {
            return BlockPathTypes.BLOCKED;
        }
        FluidState fluidState = blockGetter.getFluidState(blockPos);
        if (fluidState.is(FluidTags.WATER)) {
            return BlockPathTypes.WATER;
        }
        if (fluidState.is(FluidTags.LAVA)) {
            return BlockPathTypes.LAVA;
        }
        return BlockPathTypes.OPEN;
    }

    private static boolean isBurningBlock(BlockState blockState) {
        return blockState.is(BlockTags.FIRE) || blockState.is(Blocks.LAVA) || blockState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState);
    }
}

