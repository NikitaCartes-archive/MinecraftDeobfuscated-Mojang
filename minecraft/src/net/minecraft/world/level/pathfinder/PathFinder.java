package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;

public class PathFinder {
	private final BinaryHeap openSet = new BinaryHeap();
	private final Set<Node> closedSet = Sets.<Node>newHashSet();
	private final Node[] neighbors = new Node[32];
	private final int maxVisitedNodes;
	private final NodeEvaluator nodeEvaluator;

	public PathFinder(NodeEvaluator nodeEvaluator, int i) {
		this.nodeEvaluator = nodeEvaluator;
		this.maxVisitedNodes = i;
	}

	@Nullable
	public Path findPath(PathNavigationRegion pathNavigationRegion, Mob mob, Set<BlockPos> set, float f, int i, float g) {
		this.openSet.clear();
		this.nodeEvaluator.prepare(pathNavigationRegion, mob);
		Node node = this.nodeEvaluator.getStart();
		Map<Target, BlockPos> map = (Map<Target, BlockPos>)set.stream()
			.collect(
				Collectors.toMap(blockPos -> this.nodeEvaluator.getGoal((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()), Function.identity())
			);
		Path path = this.findPath(node, map, f, i, g);
		this.nodeEvaluator.done();
		return path;
	}

	@Nullable
	private Path findPath(Node node, Map<Target, BlockPos> map, float f, int i, float g) {
		Set<Target> set = map.keySet();
		node.g = 0.0F;
		node.h = this.getBestH(node, set);
		node.f = node.h;
		this.openSet.clear();
		this.closedSet.clear();
		this.openSet.insert(node);
		int j = 0;
		int k = (int)((float)this.maxVisitedNodes * g);

		while (!this.openSet.isEmpty()) {
			if (++j >= k) {
				break;
			}

			Node node2 = this.openSet.pop();
			node2.closed = true;
			set.stream().filter(target -> node2.distanceManhattan(target) <= (float)i).forEach(Target::setReached);
			if (set.stream().anyMatch(Target::isReached)) {
				break;
			}

			if (!(node2.distanceTo(node) >= f)) {
				int l = this.nodeEvaluator.getNeighbors(this.neighbors, node2);

				for (int m = 0; m < l; m++) {
					Node node3 = this.neighbors[m];
					float h = node2.distanceTo(node3);
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

		Stream<Path> stream;
		if (set.stream().anyMatch(Target::isReached)) {
			stream = set.stream()
				.filter(Target::isReached)
				.map(target -> this.reconstructPath(target.getBestNode(), (BlockPos)map.get(target), true))
				.sorted(Comparator.comparingInt(Path::getSize));
		} else {
			stream = set.stream()
				.map(target -> this.reconstructPath(target.getBestNode(), (BlockPos)map.get(target), false))
				.sorted(Comparator.comparingDouble(Path::getDistToTarget).thenComparingInt(Path::getSize));
		}

		Optional<Path> optional = stream.findFirst();
		return !optional.isPresent() ? null : (Path)optional.get();
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
