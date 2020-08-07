package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ClientboundMoveEntityPacket implements Packet<ClientGamePacketListener> {
	protected int entityId;
	protected short xa;
	protected short ya;
	protected short za;
	protected byte yRot;
	protected byte xRot;
	protected boolean onGround;
	protected boolean hasRot;
	protected boolean hasPos;

	public static long entityToPacket(double d) {
		return Mth.lfloor(d * 4096.0);
	}

	@Environment(EnvType.CLIENT)
	public static double packetToEntity(long l) {
		return (double)l / 4096.0;
	}

	@Environment(EnvType.CLIENT)
	public Vec3 updateEntityPosition(Vec3 vec3) {
		double d = this.xa == 0 ? vec3.x : packetToEntity(entityToPacket(vec3.x) + (long)this.xa);
		double e = this.ya == 0 ? vec3.y : packetToEntity(entityToPacket(vec3.y) + (long)this.ya);
		double f = this.za == 0 ? vec3.z : packetToEntity(entityToPacket(vec3.z) + (long)this.za);
		return new Vec3(d, e, f);
	}

	public static Vec3 packetToEntity(long l, long m, long n) {
		return new Vec3((double)l, (double)m, (double)n).scale(2.4414062E-4F);
	}

	public ClientboundMoveEntityPacket() {
	}

	public ClientboundMoveEntityPacket(int i) {
		this.entityId = i;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.entityId = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.entityId);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleMoveEntity(this);
	}

	public String toString() {
		return "Entity_" + super.toString();
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public Entity getEntity(Level level) {
		return level.getEntity(this.entityId);
	}

	@Environment(EnvType.CLIENT)
	public byte getyRot() {
		return this.yRot;
	}

	@Environment(EnvType.CLIENT)
	public byte getxRot() {
		return this.xRot;
	}

	@Environment(EnvType.CLIENT)
	public boolean hasRotation() {
		return this.hasRot;
	}

	@Environment(EnvType.CLIENT)
	public boolean hasPosition() {
		return this.hasPos;
	}

	@Environment(EnvType.CLIENT)
	public boolean isOnGround() {
		return this.onGround;
	}

	public static class Pos extends ClientboundMoveEntityPacket {
		public Pos() {
			this.hasPos = true;
		}

		public Pos(int i, short s, short t, short u, boolean bl) {
			super(i);
			this.xa = s;
			this.ya = t;
			this.za = u;
			this.onGround = bl;
			this.hasPos = true;
		}

		@Override
		public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
			super.read(friendlyByteBuf);
			this.xa = friendlyByteBuf.readShort();
			this.ya = friendlyByteBuf.readShort();
			this.za = friendlyByteBuf.readShort();
			this.onGround = friendlyByteBuf.readBoolean();
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
			super.write(friendlyByteBuf);
			friendlyByteBuf.writeShort(this.xa);
			friendlyByteBuf.writeShort(this.ya);
			friendlyByteBuf.writeShort(this.za);
			friendlyByteBuf.writeBoolean(this.onGround);
		}
	}

	public static class PosRot extends ClientboundMoveEntityPacket {
		public PosRot() {
			this.hasRot = true;
			this.hasPos = true;
		}

		public PosRot(int i, short s, short t, short u, byte b, byte c, boolean bl) {
			super(i);
			this.xa = s;
			this.ya = t;
			this.za = u;
			this.yRot = b;
			this.xRot = c;
			this.onGround = bl;
			this.hasRot = true;
			this.hasPos = true;
		}

		@Override
		public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
			super.read(friendlyByteBuf);
			this.xa = friendlyByteBuf.readShort();
			this.ya = friendlyByteBuf.readShort();
			this.za = friendlyByteBuf.readShort();
			this.yRot = friendlyByteBuf.readByte();
			this.xRot = friendlyByteBuf.readByte();
			this.onGround = friendlyByteBuf.readBoolean();
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
			super.write(friendlyByteBuf);
			friendlyByteBuf.writeShort(this.xa);
			friendlyByteBuf.writeShort(this.ya);
			friendlyByteBuf.writeShort(this.za);
			friendlyByteBuf.writeByte(this.yRot);
			friendlyByteBuf.writeByte(this.xRot);
			friendlyByteBuf.writeBoolean(this.onGround);
		}
	}

	public static class Rot extends ClientboundMoveEntityPacket {
		public Rot() {
			this.hasRot = true;
		}

		public Rot(int i, byte b, byte c, boolean bl) {
			super(i);
			this.yRot = b;
			this.xRot = c;
			this.hasRot = true;
			this.onGround = bl;
		}

		@Override
		public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
			super.read(friendlyByteBuf);
			this.yRot = friendlyByteBuf.readByte();
			this.xRot = friendlyByteBuf.readByte();
			this.onGround = friendlyByteBuf.readBoolean();
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
			super.write(friendlyByteBuf);
			friendlyByteBuf.writeByte(this.yRot);
			friendlyByteBuf.writeByte(this.xRot);
			friendlyByteBuf.writeBoolean(this.onGround);
		}
	}
}
