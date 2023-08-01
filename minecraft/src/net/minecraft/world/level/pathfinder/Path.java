package net.minecraft.world.level.pathfinder;

import java.util.HashSet;
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
	@Nullable
	private Path.DebugData debugData;
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
		this.debugData = new Path.DebugData(nodes, nodes2, set);
	}

	@Nullable
	public Path.DebugData debugData() {
		return this.debugData;
	}

	public void writeToStream(FriendlyByteBuf friendlyByteBuf) {
		if (this.debugData != null && !this.debugData.targetNodes.isEmpty()) {
			friendlyByteBuf.writeBoolean(this.reached);
			friendlyByteBuf.writeInt(this.nextNodeIndex);
			friendlyByteBuf.writeBlockPos(this.target);
			friendlyByteBuf.writeCollection(this.nodes, (friendlyByteBufx, node) -> node.writeToStream(friendlyByteBufx));
			this.debugData.write(friendlyByteBuf);
		}
	}

	public static Path createFromStream(FriendlyByteBuf friendlyByteBuf) {
		boolean bl = friendlyByteBuf.readBoolean();
		int i = friendlyByteBuf.readInt();
		BlockPos blockPos = friendlyByteBuf.readBlockPos();
		List<Node> list = friendlyByteBuf.readList(Node::createFromStream);
		Path.DebugData debugData = Path.DebugData.read(friendlyByteBuf);
		Path path = new Path(list, blockPos, bl);
		path.debugData = debugData;
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

	static Node[] readNodeArray(FriendlyByteBuf friendlyByteBuf) {
		Node[] nodes = new Node[friendlyByteBuf.readVarInt()];

		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = Node.createFromStream(friendlyByteBuf);
		}

		return nodes;
	}

	static void writeNodeArray(FriendlyByteBuf friendlyByteBuf, Node[] nodes) {
		friendlyByteBuf.writeVarInt(nodes.length);

		for (Node node : nodes) {
			node.writeToStream(friendlyByteBuf);
		}
	}

	public Path copy() {
		Path path = new Path(this.nodes, this.target, this.reached);
		path.debugData = this.debugData;
		path.nextNodeIndex = this.nextNodeIndex;
		return path;
	}

	public static record DebugData(Node[] openSet, Node[] closedSet, Set<Target> targetNodes) {

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeCollection(this.targetNodes, (friendlyByteBufx, target) -> target.writeToStream(friendlyByteBufx));
			Path.writeNodeArray(friendlyByteBuf, this.openSet);
			Path.writeNodeArray(friendlyByteBuf, this.closedSet);
		}

		public static Path.DebugData read(FriendlyByteBuf friendlyByteBuf) {
			HashSet<Target> hashSet = friendlyByteBuf.readCollection(HashSet::new, Target::createFromStream);
			Node[] nodes = Path.readNodeArray(friendlyByteBuf);
			Node[] nodes2 = Path.readNodeArray(friendlyByteBuf);
			return new Path.DebugData(nodes, nodes2, hashSet);
		}
	}
}
