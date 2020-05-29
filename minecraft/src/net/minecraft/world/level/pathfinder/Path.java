package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class Path {
	private final List<Node> nodes;
	private Node[] openSet = new Node[0];
	private Node[] closedSet = new Node[0];
	@Environment(EnvType.CLIENT)
	private Set<Target> targetNodes;
	private int index;
	private final BlockPos target;
	private final float distToTarget;
	private final boolean reached;

	public Path(List<Node> list, BlockPos blockPos, boolean bl) {
		this.nodes = list;
		this.target = blockPos;
		this.distToTarget = list.isEmpty() ? Float.MAX_VALUE : ((Node)this.nodes.get(this.nodes.size() - 1)).distanceManhattan(this.target);
		this.reached = bl;
	}

	public void next() {
		this.index++;
	}

	public boolean isDone() {
		return this.index >= this.nodes.size();
	}

	@Nullable
	public Node last() {
		return !this.nodes.isEmpty() ? (Node)this.nodes.get(this.nodes.size() - 1) : null;
	}

	public Node get(int i) {
		return (Node)this.nodes.get(i);
	}

	public List<Node> getNodes() {
		return this.nodes;
	}

	public void truncate(int i) {
		if (this.nodes.size() > i) {
			this.nodes.subList(i, this.nodes.size()).clear();
		}
	}

	public void set(int i, Node node) {
		this.nodes.set(i, node);
	}

	public int getSize() {
		return this.nodes.size();
	}

	public int getIndex() {
		return this.index;
	}

	public void setIndex(int i) {
		this.index = i;
	}

	public Vec3 getPos(Entity entity, int i) {
		Node node = (Node)this.nodes.get(i);
		double d = (double)node.x + (double)((int)(entity.getBbWidth() + 1.0F)) * 0.5;
		double e = (double)node.y;
		double f = (double)node.z + (double)((int)(entity.getBbWidth() + 1.0F)) * 0.5;
		return new Vec3(d, e, f);
	}

	public Vec3 currentPos(Entity entity) {
		return this.getPos(entity, this.index);
	}

	public Vec3i currentPos() {
		Node node = this.currentNode();
		return new Vec3i(node.x, node.y, node.z);
	}

	public Node currentNode() {
		return (Node)this.nodes.get(this.index);
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

	@Environment(EnvType.CLIENT)
	public Node[] getOpenSet() {
		return this.openSet;
	}

	@Environment(EnvType.CLIENT)
	public Node[] getClosedSet() {
		return this.closedSet;
	}

	@Environment(EnvType.CLIENT)
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
		path.index = i;
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
