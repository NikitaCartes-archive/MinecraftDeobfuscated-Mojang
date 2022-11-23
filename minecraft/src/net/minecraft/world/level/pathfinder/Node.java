package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Node {
	public final int x;
	public final int y;
	public final int z;
	private final int hash;
	public int heapIdx = -1;
	public float g;
	public float h;
	public float f;
	@Nullable
	public Node cameFrom;
	public boolean closed;
	public float walkedDistance;
	public float costMalus;
	public BlockPathTypes type = BlockPathTypes.BLOCKED;

	public Node(int i, int j, int k) {
		this.x = i;
		this.y = j;
		this.z = k;
		this.hash = createHash(i, j, k);
	}

	public Node cloneAndMove(int i, int j, int k) {
		Node node = new Node(i, j, k);
		node.heapIdx = this.heapIdx;
		node.g = this.g;
		node.h = this.h;
		node.f = this.f;
		node.cameFrom = this.cameFrom;
		node.closed = this.closed;
		node.walkedDistance = this.walkedDistance;
		node.costMalus = this.costMalus;
		node.type = this.type;
		return node;
	}

	public static int createHash(int i, int j, int k) {
		return j & 0xFF | (i & 32767) << 8 | (k & 32767) << 24 | (i < 0 ? Integer.MIN_VALUE : 0) | (k < 0 ? 32768 : 0);
	}

	public float distanceTo(Node node) {
		float f = (float)(node.x - this.x);
		float g = (float)(node.y - this.y);
		float h = (float)(node.z - this.z);
		return Mth.sqrt(f * f + g * g + h * h);
	}

	public float distanceToXZ(Node node) {
		float f = (float)(node.x - this.x);
		float g = (float)(node.z - this.z);
		return Mth.sqrt(f * f + g * g);
	}

	public float distanceTo(BlockPos blockPos) {
		float f = (float)(blockPos.getX() - this.x);
		float g = (float)(blockPos.getY() - this.y);
		float h = (float)(blockPos.getZ() - this.z);
		return Mth.sqrt(f * f + g * g + h * h);
	}

	public float distanceToSqr(Node node) {
		float f = (float)(node.x - this.x);
		float g = (float)(node.y - this.y);
		float h = (float)(node.z - this.z);
		return f * f + g * g + h * h;
	}

	public float distanceToSqr(BlockPos blockPos) {
		float f = (float)(blockPos.getX() - this.x);
		float g = (float)(blockPos.getY() - this.y);
		float h = (float)(blockPos.getZ() - this.z);
		return f * f + g * g + h * h;
	}

	public float distanceManhattan(Node node) {
		float f = (float)Math.abs(node.x - this.x);
		float g = (float)Math.abs(node.y - this.y);
		float h = (float)Math.abs(node.z - this.z);
		return f + g + h;
	}

	public float distanceManhattan(BlockPos blockPos) {
		float f = (float)Math.abs(blockPos.getX() - this.x);
		float g = (float)Math.abs(blockPos.getY() - this.y);
		float h = (float)Math.abs(blockPos.getZ() - this.z);
		return f + g + h;
	}

	public BlockPos asBlockPos() {
		return new BlockPos(this.x, this.y, this.z);
	}

	public Vec3 asVec3() {
		return new Vec3((double)this.x, (double)this.y, (double)this.z);
	}

	public boolean equals(Object object) {
		return !(object instanceof Node node) ? false : this.hash == node.hash && this.x == node.x && this.y == node.y && this.z == node.z;
	}

	public int hashCode() {
		return this.hash;
	}

	public boolean inOpenSet() {
		return this.heapIdx >= 0;
	}

	public String toString() {
		return "Node{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
	}

	public void writeToStream(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.x);
		friendlyByteBuf.writeInt(this.y);
		friendlyByteBuf.writeInt(this.z);
		friendlyByteBuf.writeFloat(this.walkedDistance);
		friendlyByteBuf.writeFloat(this.costMalus);
		friendlyByteBuf.writeBoolean(this.closed);
		friendlyByteBuf.writeEnum(this.type);
		friendlyByteBuf.writeFloat(this.f);
	}

	public static Node createFromStream(FriendlyByteBuf friendlyByteBuf) {
		Node node = new Node(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
		readContents(friendlyByteBuf, node);
		return node;
	}

	protected static void readContents(FriendlyByteBuf friendlyByteBuf, Node node) {
		node.walkedDistance = friendlyByteBuf.readFloat();
		node.costMalus = friendlyByteBuf.readFloat();
		node.closed = friendlyByteBuf.readBoolean();
		node.type = friendlyByteBuf.readEnum(BlockPathTypes.class);
		node.f = friendlyByteBuf.readFloat();
	}
}
