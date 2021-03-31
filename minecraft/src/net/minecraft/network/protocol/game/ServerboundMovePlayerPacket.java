package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public abstract class ServerboundMovePlayerPacket implements Packet<ServerGamePacketListener> {
	protected final double x;
	protected final double y;
	protected final double z;
	protected final float yRot;
	protected final float xRot;
	protected final boolean onGround;
	protected final boolean hasPos;
	protected final boolean hasRot;

	protected ServerboundMovePlayerPacket(double d, double e, double f, float g, float h, boolean bl, boolean bl2, boolean bl3) {
		this.x = d;
		this.y = e;
		this.z = f;
		this.yRot = g;
		this.xRot = h;
		this.onGround = bl;
		this.hasPos = bl2;
		this.hasRot = bl3;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleMovePlayer(this);
	}

	public double getX(double d) {
		return this.hasPos ? this.x : d;
	}

	public double getY(double d) {
		return this.hasPos ? this.y : d;
	}

	public double getZ(double d) {
		return this.hasPos ? this.z : d;
	}

	public float getYRot(float f) {
		return this.hasRot ? this.yRot : f;
	}

	public float getXRot(float f) {
		return this.hasRot ? this.xRot : f;
	}

	public boolean isOnGround() {
		return this.onGround;
	}

	public boolean hasPosition() {
		return this.hasPos;
	}

	public boolean hasRotation() {
		return this.hasRot;
	}

	public static class Pos extends ServerboundMovePlayerPacket {
		public Pos(double d, double e, double f, boolean bl) {
			super(d, e, f, 0.0F, 0.0F, bl, true, false);
		}

		public static ServerboundMovePlayerPacket.Pos read(FriendlyByteBuf friendlyByteBuf) {
			double d = friendlyByteBuf.readDouble();
			double e = friendlyByteBuf.readDouble();
			double f = friendlyByteBuf.readDouble();
			boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
			return new ServerboundMovePlayerPacket.Pos(d, e, f, bl);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeDouble(this.x);
			friendlyByteBuf.writeDouble(this.y);
			friendlyByteBuf.writeDouble(this.z);
			friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
		}
	}

	public static class PosRot extends ServerboundMovePlayerPacket {
		public PosRot(double d, double e, double f, float g, float h, boolean bl) {
			super(d, e, f, g, h, bl, true, true);
		}

		public static ServerboundMovePlayerPacket.PosRot read(FriendlyByteBuf friendlyByteBuf) {
			double d = friendlyByteBuf.readDouble();
			double e = friendlyByteBuf.readDouble();
			double f = friendlyByteBuf.readDouble();
			float g = friendlyByteBuf.readFloat();
			float h = friendlyByteBuf.readFloat();
			boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
			return new ServerboundMovePlayerPacket.PosRot(d, e, f, g, h, bl);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeDouble(this.x);
			friendlyByteBuf.writeDouble(this.y);
			friendlyByteBuf.writeDouble(this.z);
			friendlyByteBuf.writeFloat(this.yRot);
			friendlyByteBuf.writeFloat(this.xRot);
			friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
		}
	}

	public static class Rot extends ServerboundMovePlayerPacket {
		public Rot(float f, float g, boolean bl) {
			super(0.0, 0.0, 0.0, f, g, bl, false, true);
		}

		public static ServerboundMovePlayerPacket.Rot read(FriendlyByteBuf friendlyByteBuf) {
			float f = friendlyByteBuf.readFloat();
			float g = friendlyByteBuf.readFloat();
			boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
			return new ServerboundMovePlayerPacket.Rot(f, g, bl);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeFloat(this.yRot);
			friendlyByteBuf.writeFloat(this.xRot);
			friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
		}
	}

	public static class StatusOnly extends ServerboundMovePlayerPacket {
		public StatusOnly(boolean bl) {
			super(0.0, 0.0, 0.0, 0.0F, 0.0F, bl, false, false);
		}

		public static ServerboundMovePlayerPacket.StatusOnly read(FriendlyByteBuf friendlyByteBuf) {
			boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
			return new ServerboundMovePlayerPacket.StatusOnly(bl);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
		}
	}
}
