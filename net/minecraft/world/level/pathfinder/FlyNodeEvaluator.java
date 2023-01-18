/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class FlyNodeEvaluator
extends WalkNodeEvaluator {
    private final Long2ObjectMap<BlockPathTypes> pathTypeByPosCache = new Long2ObjectOpenHashMap<BlockPathTypes>();
    private static final float SMALL_MOB_INFLATED_START_NODE_BOUNDING_BOX = 1.5f;
    private static final int MAX_START_NODE_CANDIDATES = 10;

    @Override
    public void prepare(PathNavigationRegion pathNavigationRegion, Mob mob) {
        super.prepare(pathNavigationRegion, mob);
        this.pathTypeByPosCache.clear();
        this.oldWaterCost = mob.getPathfindingMalus(BlockPathTypes.WATER);
    }

    @Override
    public void done() {
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        this.pathTypeByPosCache.clear();
        super.done();
    }

    @Override
    public Node getStart() {
        BlockPos blockPos;
        int i;
        if (this.canFloat() && this.mob.isInWater()) {
            i = this.mob.getBlockY();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(this.mob.getX(), (double)i, this.mob.getZ());
            BlockState blockState = this.level.getBlockState(mutableBlockPos);
            while (blockState.is(Blocks.WATER)) {
                mutableBlockPos.set(this.mob.getX(), (double)(++i), this.mob.getZ());
                blockState = this.level.getBlockState(mutableBlockPos);
            }
        } else {
            i = Mth.floor(this.mob.getY() + 0.5);
        }
        if (!this.canStartAt(blockPos = new BlockPos(this.mob.getX(), (double)i, this.mob.getZ()))) {
            for (BlockPos blockPos2 : this.iteratePathfindingStartNodeCandidatePositions(this.mob)) {
                if (!this.canStartAt(blockPos2)) continue;
                return super.getStartNode(blockPos2);
            }
        }
        return super.getStartNode(blockPos);
    }

    @Override
    protected boolean canStartAt(BlockPos blockPos) {
        BlockPathTypes blockPathTypes = this.getBlockPathType(this.mob, blockPos);
        return this.mob.getPathfindingMalus(blockPathTypes) >= 0.0f;
    }

    @Override
    public Target getGoal(double d, double e, double f) {
        return this.getTargetFromNode(this.getNode(Mth.floor(d), Mth.floor(e), Mth.floor(f)));
    }

    @Override
    public int getNeighbors(Node[] nodes, Node node) {
        Node node27;
        Node node26;
        Node node25;
        Node node24;
        Node node23;
        Node node22;
        Node node21;
        Node node20;
        Node node19;
        Node node18;
        Node node17;
        Node node16;
        Node node15;
        Node node14;
        Node node13;
        Node node12;
        Node node11;
        Node node10;
        Node node9;
        Node node8;
        Node node7;
        Node node6;
        Node node5;
        Node node4;
        Node node3;
        int i = 0;
        Node node2 = this.findAcceptedNode(node.x, node.y, node.z + 1);
        if (this.isOpen(node2)) {
            nodes[i++] = node2;
        }
        if (this.isOpen(node3 = this.findAcceptedNode(node.x - 1, node.y, node.z))) {
            nodes[i++] = node3;
        }
        if (this.isOpen(node4 = this.findAcceptedNode(node.x + 1, node.y, node.z))) {
            nodes[i++] = node4;
        }
        if (this.isOpen(node5 = this.findAcceptedNode(node.x, node.y, node.z - 1))) {
            nodes[i++] = node5;
        }
        if (this.isOpen(node6 = this.findAcceptedNode(node.x, node.y + 1, node.z))) {
            nodes[i++] = node6;
        }
        if (this.isOpen(node7 = this.findAcceptedNode(node.x, node.y - 1, node.z))) {
            nodes[i++] = node7;
        }
        if (this.isOpen(node8 = this.findAcceptedNode(node.x, node.y + 1, node.z + 1)) && this.hasMalus(node2) && this.hasMalus(node6)) {
            nodes[i++] = node8;
        }
        if (this.isOpen(node9 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z)) && this.hasMalus(node3) && this.hasMalus(node6)) {
            nodes[i++] = node9;
        }
        if (this.isOpen(node10 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z)) && this.hasMalus(node4) && this.hasMalus(node6)) {
            nodes[i++] = node10;
        }
        if (this.isOpen(node11 = this.findAcceptedNode(node.x, node.y + 1, node.z - 1)) && this.hasMalus(node5) && this.hasMalus(node6)) {
            nodes[i++] = node11;
        }
        if (this.isOpen(node12 = this.findAcceptedNode(node.x, node.y - 1, node.z + 1)) && this.hasMalus(node2) && this.hasMalus(node7)) {
            nodes[i++] = node12;
        }
        if (this.isOpen(node13 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z)) && this.hasMalus(node3) && this.hasMalus(node7)) {
            nodes[i++] = node13;
        }
        if (this.isOpen(node14 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z)) && this.hasMalus(node4) && this.hasMalus(node7)) {
            nodes[i++] = node14;
        }
        if (this.isOpen(node15 = this.findAcceptedNode(node.x, node.y - 1, node.z - 1)) && this.hasMalus(node5) && this.hasMalus(node7)) {
            nodes[i++] = node15;
        }
        if (this.isOpen(node16 = this.findAcceptedNode(node.x + 1, node.y, node.z - 1)) && this.hasMalus(node5) && this.hasMalus(node4)) {
            nodes[i++] = node16;
        }
        if (this.isOpen(node17 = this.findAcceptedNode(node.x + 1, node.y, node.z + 1)) && this.hasMalus(node2) && this.hasMalus(node4)) {
            nodes[i++] = node17;
        }
        if (this.isOpen(node18 = this.findAcceptedNode(node.x - 1, node.y, node.z - 1)) && this.hasMalus(node5) && this.hasMalus(node3)) {
            nodes[i++] = node18;
        }
        if (this.isOpen(node19 = this.findAcceptedNode(node.x - 1, node.y, node.z + 1)) && this.hasMalus(node2) && this.hasMalus(node3)) {
            nodes[i++] = node19;
        }
        if (this.isOpen(node20 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z - 1)) && this.hasMalus(node16) && this.hasMalus(node5) && this.hasMalus(node4) && this.hasMalus(node6) && this.hasMalus(node11) && this.hasMalus(node10)) {
            nodes[i++] = node20;
        }
        if (this.isOpen(node21 = this.findAcceptedNode(node.x + 1, node.y + 1, node.z + 1)) && this.hasMalus(node17) && this.hasMalus(node2) && this.hasMalus(node4) && this.hasMalus(node6) && this.hasMalus(node8) && this.hasMalus(node10)) {
            nodes[i++] = node21;
        }
        if (this.isOpen(node22 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z - 1)) && this.hasMalus(node18) && this.hasMalus(node5) && this.hasMalus(node3) && this.hasMalus(node6) && this.hasMalus(node11) && this.hasMalus(node9)) {
            nodes[i++] = node22;
        }
        if (this.isOpen(node23 = this.findAcceptedNode(node.x - 1, node.y + 1, node.z + 1)) && this.hasMalus(node19) && this.hasMalus(node2) && this.hasMalus(node3) && this.hasMalus(node6) && this.hasMalus(node8) && this.hasMalus(node9)) {
            nodes[i++] = node23;
        }
        if (this.isOpen(node24 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z - 1)) && this.hasMalus(node16) && this.hasMalus(node5) && this.hasMalus(node4) && this.hasMalus(node7) && this.hasMalus(node15) && this.hasMalus(node14)) {
            nodes[i++] = node24;
        }
        if (this.isOpen(node25 = this.findAcceptedNode(node.x + 1, node.y - 1, node.z + 1)) && this.hasMalus(node17) && this.hasMalus(node2) && this.hasMalus(node4) && this.hasMalus(node7) && this.hasMalus(node12) && this.hasMalus(node14)) {
            nodes[i++] = node25;
        }
        if (this.isOpen(node26 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z - 1)) && this.hasMalus(node18) && this.hasMalus(node5) && this.hasMalus(node3) && this.hasMalus(node7) && this.hasMalus(node15) && this.hasMalus(node13)) {
            nodes[i++] = node26;
        }
        if (this.isOpen(node27 = this.findAcceptedNode(node.x - 1, node.y - 1, node.z + 1)) && this.hasMalus(node19) && this.hasMalus(node2) && this.hasMalus(node3) && this.hasMalus(node7) && this.hasMalus(node12) && this.hasMalus(node13)) {
            nodes[i++] = node27;
        }
        return i;
    }

    private boolean hasMalus(@Nullable Node node) {
        return node != null && node.costMalus >= 0.0f;
    }

    private boolean isOpen(@Nullable Node node) {
        return node != null && !node.closed;
    }

    @Nullable
    protected Node findAcceptedNode(int i, int j, int k) {
        Node node = null;
        BlockPathTypes blockPathTypes = this.getCachedBlockPathType(i, j, k);
        float f = this.mob.getPathfindingMalus(blockPathTypes);
        if (f >= 0.0f) {
            node = this.getNode(i, j, k);
            node.type = blockPathTypes;
            node.costMalus = Math.max(node.costMalus, f);
            if (blockPathTypes == BlockPathTypes.WALKABLE) {
                node.costMalus += 1.0f;
            }
        }
        return node;
    }

    private BlockPathTypes getCachedBlockPathType(int i, int j, int k) {
        return this.pathTypeByPosCache.computeIfAbsent(BlockPos.asLong(i, j, k), l -> this.getBlockPathType(this.level, i, j, k, this.mob));
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k, Mob mob) {
        EnumSet<BlockPathTypes> enumSet = EnumSet.noneOf(BlockPathTypes.class);
        BlockPathTypes blockPathTypes = BlockPathTypes.BLOCKED;
        BlockPos blockPos = mob.blockPosition();
        blockPathTypes = super.getBlockPathTypes(blockGetter, i, j, k, enumSet, blockPathTypes, blockPos);
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

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPathTypes blockPathTypes = FlyNodeEvaluator.getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(i, j, k));
        if (blockPathTypes == BlockPathTypes.OPEN && j >= blockGetter.getMinBuildHeight() + 1) {
            BlockPathTypes blockPathTypes2 = FlyNodeEvaluator.getBlockPathTypeRaw(blockGetter, mutableBlockPos.set(i, j - 1, k));
            if (blockPathTypes2 == BlockPathTypes.DAMAGE_FIRE || blockPathTypes2 == BlockPathTypes.LAVA) {
                blockPathTypes = BlockPathTypes.DAMAGE_FIRE;
            } else if (blockPathTypes2 == BlockPathTypes.DAMAGE_OTHER) {
                blockPathTypes = BlockPathTypes.DAMAGE_OTHER;
            } else if (blockPathTypes2 == BlockPathTypes.COCOA) {
                blockPathTypes = BlockPathTypes.COCOA;
            } else if (blockPathTypes2 == BlockPathTypes.FENCE) {
                if (!mutableBlockPos.equals(this.mob.blockPosition())) {
                    blockPathTypes = BlockPathTypes.FENCE;
                }
            } else {
                BlockPathTypes blockPathTypes3 = blockPathTypes = blockPathTypes2 == BlockPathTypes.WALKABLE || blockPathTypes2 == BlockPathTypes.OPEN || blockPathTypes2 == BlockPathTypes.WATER ? BlockPathTypes.OPEN : BlockPathTypes.WALKABLE;
            }
        }
        if (blockPathTypes == BlockPathTypes.WALKABLE || blockPathTypes == BlockPathTypes.OPEN) {
            blockPathTypes = FlyNodeEvaluator.checkNeighbourBlocks(blockGetter, mutableBlockPos.set(i, j, k), blockPathTypes);
        }
        return blockPathTypes;
    }

    private Iterable<BlockPos> iteratePathfindingStartNodeCandidatePositions(Mob mob) {
        boolean bl;
        float f = 1.0f;
        AABB aABB = mob.getBoundingBox();
        boolean bl2 = bl = aABB.getSize() < 1.0;
        if (!bl) {
            return List.of(new BlockPos(aABB.minX, (double)mob.getBlockY(), aABB.minZ), new BlockPos(aABB.minX, (double)mob.getBlockY(), aABB.maxZ), new BlockPos(aABB.maxX, (double)mob.getBlockY(), aABB.minZ), new BlockPos(aABB.maxX, (double)mob.getBlockY(), aABB.maxZ));
        }
        double d = Math.max(0.0, (1.5 - aABB.getZsize()) / 2.0);
        double e = Math.max(0.0, (1.5 - aABB.getXsize()) / 2.0);
        double g = Math.max(0.0, (1.5 - aABB.getYsize()) / 2.0);
        AABB aABB2 = aABB.inflate(e, g, d);
        return BlockPos.randomBetweenClosed(mob.getRandom(), 10, Mth.floor(aABB2.minX), Mth.floor(aABB2.minY), Mth.floor(aABB2.minZ), Mth.floor(aABB2.maxX), Mth.floor(aABB2.maxY), Mth.floor(aABB2.maxZ));
    }
}

