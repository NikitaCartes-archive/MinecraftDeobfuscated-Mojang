package net.minecraft.world.level.pathfinder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;

public class PathFinder {
	private static final float FUDGING = 1.5F;
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
		} else {
			Map<Target, BlockPos> map = (Map<Target, BlockPos>)set.stream()
				.collect(
					Collectors.toMap(blockPos -> this.nodeEvaluator.getGoal((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), Function.identity())
				);
			Path path = this.findPath(pathNavigationRegion.getProfiler(), node, map, f, i, g);
			this.nodeEvaluator.done();
			return path;
		}
	}

	@Nullable
	private Path findPath(ProfilerFiller profilerFiller, Node node, Map<Target, BlockPos> map, float f, int i, float g) {
		profilerFiller.push("find_path");
		profilerFiller.markForCharting(MetricCategory.PATH_FINDING);
		Set<Target> set = map.keySet();
		node.g = 0.0F;
		node.h = this.getBestH(node, set);
		node.f = node.h;
		this.openSet.clear();
		this.openSet.insert(node);
		Set<Node> set2 = ImmutableSet.of();
		int j = 0;
		Set<Target> set3 = Sets.<Target>newHashSetWithExpectedSize(set.size());
		int k = (int)((float)this.maxVisitedNodes * g);

		while (!this.openSet.isEmpty()) {
			if (++j >= k) {
				break;
			}

			Node node2 = this.openSet.pop();
			node2.closed = true;

			for (Target target : set) {
				if (node2.distanceManhattan(target) <= (float)i) {
					target.setReached();
					set3.add(target);
				}
			}

			if (!set3.isEmpty()) {
				break;
			}

			if (!(node2.distanceTo(node) >= f)) {
				int l = this.nodeEvaluator.getNeighbors(this.neighbors, node2);

				for (int m = 0; m < l; m++) {
					Node node3 = this.neighbors[m];
					float h = this.distance(node2, node3);
					node3.walkedDistance = node2.walkedDistance + h;
					float n = node2.g + h + node3.costMalus;
					if (node3.walkedDistance < f && (!node3.inOpenSet() || n < node3.g)) {
						node3.cameFrom = node2;
						node3.g = n;
						node3.h = this.getBestH(node3, set) * 1.5F;
						if (node3.inOpenSet()) {
							this.openSet.changeCost(node3, node3.g + node3.h);
						} else {
							node3.f = node3.g + node3.h;
							this.openSet.insert(node3);
						}
					}
				}
			}
		}

		Optional<Path> optional = !set3.isEmpty()
			? set3.stream()
				.map(targetx -> this.reconstructPath(targetx.getBestNode(), (BlockPos)map.get(targetx), true))
				.min(Comparator.comparingInt(Path::getNodeCount))
			: set.stream()
				.map(targetx -> this.reconstructPath(targetx.getBestNode(), (BlockPos)map.get(targetx), false))
				.min(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getNodeCount));
		profilerFiller.pop();
		return optional.isEmpty() ? null : (Path)optional.get();
	}

	protected float distance(Node node, Node node2) {
		return node.distanceTo(node2);
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
		List<Node> list = Lists.<Node>newArrayList();
		Node node2 = node;
		list.add(0, node);

		while (node2.cameFrom != null) {
			node2 = node2.cameFrom;
			list.add(0, node2);
		}

		return new Path(list, blockPos, bl);
	}
}
