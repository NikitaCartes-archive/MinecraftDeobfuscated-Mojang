/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.pathfinder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Target;
import org.jetbrains.annotations.Nullable;

public class PathFinder {
    private static final float FUDGING = 1.5f;
    private final Node[] neighbors = new Node[32];
    private final int maxVisitedNodes;
    private final NodeEvaluator nodeEvaluator;
    private static final boolean DEBUG = false;
    private final BinaryHeap openSet = new BinaryHeap();

    public PathFinder(NodeEvaluator nodeEvaluator, int i) {
        this.nodeEvaluator = nodeEvaluator;
        this.maxVisitedNodes = i;
    }

    @Nullable
    public Path findPath(PathNavigationRegion pathNavigationRegion, Mob mob, Set<BlockPos> set, float f, int i, float g) {
        this.openSet.clear();
        this.nodeEvaluator.prepare(pathNavigationRegion, mob);
        Node node = this.nodeEvaluator.getStart();
        if (node == null) {
            return null;
        }
        Map<Target, BlockPos> map = set.stream().collect(Collectors.toMap(blockPos -> this.nodeEvaluator.getGoal(blockPos.getX(), blockPos.getY(), blockPos.getZ()), Function.identity()));
        Path path = this.findPath(pathNavigationRegion.getProfiler(), node, map, f, i, g);
        this.nodeEvaluator.done();
        return path;
    }

    @Nullable
    private Path findPath(ProfilerFiller profilerFiller, Node node, Map<Target, BlockPos> map, float f, int i, float g) {
        profilerFiller.push("find_path");
        profilerFiller.markForCharting(MetricCategory.PATH_FINDING);
        Set<Target> set = map.keySet();
        node.g = 0.0f;
        node.f = node.h = this.getBestH(node, set);
        this.openSet.clear();
        this.openSet.insert(node);
        ImmutableSet set2 = ImmutableSet.of();
        int j = 0;
        HashSet<Target> set3 = Sets.newHashSetWithExpectedSize(set.size());
        int k = (int)((float)this.maxVisitedNodes * g);
        while (!this.openSet.isEmpty() && ++j < k) {
            Node node2 = this.openSet.pop();
            node2.closed = true;
            for (Target target2 : set) {
                if (!(node2.distanceManhattan(target2) <= (float)i)) continue;
                target2.setReached();
                set3.add(target2);
            }
            if (!set3.isEmpty()) break;
            if (node2.distanceTo(node) >= f) continue;
            int l = this.nodeEvaluator.getNeighbors(this.neighbors, node2);
            for (int m = 0; m < l; ++m) {
                Node node3 = this.neighbors[m];
                float h = node2.distanceTo(node3);
                node3.walkedDistance = node2.walkedDistance + h;
                float n = node2.g + h + node3.costMalus;
                if (!(node3.walkedDistance < f) || node3.inOpenSet() && !(n < node3.g)) continue;
                node3.cameFrom = node2;
                node3.g = n;
                node3.h = this.getBestH(node3, set) * 1.5f;
                if (node3.inOpenSet()) {
                    this.openSet.changeCost(node3, node3.g + node3.h);
                    continue;
                }
                node3.f = node3.g + node3.h;
                this.openSet.insert(node3);
            }
        }
        Optional<Path> optional = !set3.isEmpty() ? set3.stream().map(target -> this.reconstructPath(target.getBestNode(), (BlockPos)map.get(target), true)).min(Comparator.comparingInt(Path::getNodeCount)) : set.stream().map(target -> this.reconstructPath(target.getBestNode(), (BlockPos)map.get(target), false)).min(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount));
        profilerFiller.pop();
        if (!optional.isPresent()) {
            return null;
        }
        Path path = optional.get();
        return path;
    }

    private float getBestH(Node node, Set<Target> set) {
        float f = Float.MAX_VALUE;
        for (Target target : set) {
            float g = node.distanceTo(target);
            target.updateBest(g, node);
            f = Math.min(g, f);
        }
        return f;
    }

    private Path reconstructPath(Node node, BlockPos blockPos, boolean bl) {
        ArrayList<Node> list = Lists.newArrayList();
        Node node2 = node;
        list.add(0, node2);
        while (node2.cameFrom != null) {
            node2 = node2.cameFrom;
            list.add(0, node2);
        }
        return new Path(list, blockPos, bl);
    }
}

