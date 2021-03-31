package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class Path {
	private final List<Node> nodes;
	private Node[] openSet = new Node[0];
	private Node[] closedSet = new Node[0];
	private Set<Target> targetNodes;
	private int nextNodeIndex;
	private final BlockPos target;
	private final float distToTarget;
	private final boolean reached;

	public Path(List<Node> list, BlockPos blockPos, boolean bl) {
		this.nodes = list;
		this.target = blockPos;
		this.distToTarget = list.isEmpty() ? Float.MAX_VALUE : ((Node)this.nodes.get(this.nodes.size() - 1)).distanceManhattan(this.target);
		this.reached = bl;
	}

	public void advance() {
		this.nextNodeIndex++;
	}

	public boolean notStarted() {
		return this.nextNodeIndex <= 0;
	}

	public boolean isDone() {
		return this.nextNodeIndex >= this.nodes.size();
	}

	@Nullable
	public Node getEndNode() {
		return !this.nodes.isEmpty() ? (Node)this.nodes.get(this.nodes.size() - 1) : null;
	}

	public Node getNode(int i) {
		return (Node)this.nodes.get(i);
	}

	public void truncateNodes(int i) {
		if (this.nodes.size() > i) {
			this.nodes.subList(i, this.nodes.size()).clear();
		}
	}

	public void replaceNode(int i, Node node) {
		this.nodes.set(i, node);
	}

	public int getNodeCount() {
		return this.nodes.size();
	}

	public int getNextNodeIndex() {
		return this.nextNodeIndex;
	}

	public void setNextNodeIndex(int i) {
		this.nextNodeIndex = i;
	}

	public Vec3 getEntityPosAtNode(Entity entity, int i) {
		Node node = (Node)this.nodes.get(i);
		double d = (double)node.x + (double)((int)(entity.getBbWidth() + 1.0F)) * 0.5;
		double e = (double)node.y;
		double f = (double)node.z + (double)((int)(entity.getBbWidth() + 1.0F)) * 0.5;
		return new Vec3(d, e, f);
	}

	public BlockPos getNodePos(int i) {
		return ((Node)this.nodes.get(i)).asBlockPos();
	}

	public Vec3 getNextEntityPos(Entity entity) {
		return this.getEntityPosAtNode(entity, this.nextNodeIndex);
	}

	public BlockPos getNextNodePos() {
		return ((Node)this.nodes.get(this.nextNodeIndex)).asBlockPos();
	}

	public Node getNextNode() {
		return (Node)this.nodes.get(this.nextNodeIndex);
	}

	@Nullable
	public Node getPreviousNode() {
		return this.nextNodeIndex > 0 ? (Node)this.nodes.get(this.nextNodeIndex - 1) : null;
	}

	public boolean sameAs(@Nullable Path path) {
		if (path == null) {
			return false;
		} else if (path.nodes.size() != this.nodes.size()) {
			return false;
		} else {
			for (int i = 0; i < this.nodes.size(); i++) {
				Node node = (Node)this.nodes.get(i);
				Node node2 = (Node)path.nodes.get(i);
				if (node.x != node2.x || node.y != node2.y || node.z != node2.z) {
					return false;
				}
			}

			return true;
		}
	}

	public boolean canReach() {
		return this.reached;
	}

	@VisibleForDebug
	void setDebug(Node[] nodes, Node[] nodes2, Set<Target> set) {
		this.openSet = nodes;
		this.closedSet = nodes2;
		this.targetNodes = set;
	}

	@VisibleForDebug
	public Node[] getOpenSet() {
		return this.openSet;
	}

	@VisibleForDebug
	public Node[] getClosedSet() {
		return this.closedSet;
	}

	public void writeToStream(FriendlyByteBuf friendlyByteBuf) {
		if (this.targetNodes != null && !this.targetNodes.isEmpty()) {
			friendlyByteBuf.writeBoolean(this.reached);
			friendlyByteBuf.writeInt(this.nextNodeIndex);
			friendlyByteBuf.writeInt(this.targetNodes.size());
			this.targetNodes.forEach(target -> target.writeToStream(friendlyByteBuf));
			friendlyByteBuf.writeInt(this.target.getX());
			friendlyByteBuf.writeInt(this.target.getY());
			friendlyByteBuf.writeInt(this.target.getZ());
			friendlyByteBuf.writeInt(this.nodes.size());

			for (Node node : this.nodes) {
				node.writeToStream(friendlyByteBuf);
			}

			friendlyByteBuf.writeInt(this.openSet.length);

			for (Node node2 : this.openSet) {
				node2.writeToStream(friendlyByteBuf);
			}

			friendlyByteBuf.writeInt(this.closedSet.length);

			for (Node node2 : this.closedSet) {
				node2.writeToStream(friendlyByteBuf);
			}
		}
	}

	public static Path createFromStream(FriendlyByteBuf friendlyByteBuf) {
		boolean bl = friendlyByteBuf.readBoolean();
		int i = friendlyByteBuf.readInt();
		int j = friendlyByteBuf.readInt();
		Set<Target> set = Sets.<Target>newHashSet();

		for (int k = 0; k < j; k++) {
			set.add(Target.createFromStream(friendlyByteBuf));
		}

		BlockPos blockPos = new BlockPos(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
		List<Node> list = Lists.<Node>newArrayList();
		int l = friendlyByteBuf.readInt();

		for (int m = 0; m < l; m++) {
			list.add(Node.createFromStream(friendlyByteBuf));
		}

		Node[] nodes = new Node[friendlyByteBuf.readInt()];

		for (int n = 0; n < nodes.length; n++) {
			nodes[n] = Node.createFromStream(friendlyByteBuf);
		}

		Node[] nodes2 = new Node[friendlyByteBuf.readInt()];

		for (int o = 0; o < nodes2.length; o++) {
			nodes2[o] = Node.createFromStream(friendlyByteBuf);
		}

		Path path = new Path(list, blockPos, bl);
		path.openSet = nodes;
		path.closedSet = nodes2;
		path.targetNodes = set;
		path.nextNodeIndex = i;
		return path;
	}

	public String toString() {
		return "Path(length=" + this.nodes.size() + ")";
	}

	public BlockPos getTarget() {
		return this.target;
	}

	public float getDistToTarget() {
		return this.distToTarget;
	}
}
