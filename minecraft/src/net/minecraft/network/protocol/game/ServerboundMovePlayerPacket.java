package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundMovePlayerPacket implements Packet<ServerGamePacketListener> {
	protected double x;
	protected double y;
	protected double z;
	protected float yRot;
	protected float xRot;
	protected boolean onGround;
	protected boolean hasPos;
	protected boolean hasRot;

	public ServerboundMovePlayerPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundMovePlayerPacket(boolean bl) {
		this.onGround = bl;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleMovePlayer(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.onGround = friendlyByteBuf.readUnsignedByte() != 0;
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
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

	public static class Pos extends ServerboundMovePlayerPacket {
		public Pos() {
			this.hasPos = true;
		}

		@Environment(EnvType.CLIENT)
		public Pos(double d, double e, double f, boolean bl) {
			this.x = d;
			this.y = e;
			this.z = f;
			this.onGround = bl;
			this.hasPos = true;
		}

		@Override
		public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
			this.x = friendlyByteBuf.readDouble();
			this.y = friendlyByteBuf.readDouble();
			this.z = friendlyByteBuf.readDouble();
			super.read(friendlyByteBuf);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
			friendlyByteBuf.writeDouble(this.x);
			friendlyByteBuf.writeDouble(this.y);
			friendlyByteBuf.writeDouble(this.z);
			super.write(friendlyByteBuf);
		}
	}

	public static class PosRot extends ServerboundMovePlayerPacket {
		public PosRot() {
			this.hasPos = true;
			this.hasRot = true;
		}

		@Environment(EnvType.CLIENT)
		public PosRot(double d, double e, double f, float g, float h, boolean bl) {
			this.x = d;
			this.y = e;
			this.z = f;
			this.yRot = g;
			this.xRot = h;
			this.onGround = bl;
			this.hasRot = true;
			this.hasPos = true;
		}

		@Override
		public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
			this.x = friendlyByteBuf.readDouble();
			this.y = friendlyByteBuf.readDouble();
			this.z = friendlyByteBuf.readDouble();
			this.yRot = friendlyByteBuf.readFloat();
			this.xRot = friendlyByteBuf.readFloat();
			super.read(friendlyByteBuf);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
			friendlyByteBuf.writeDouble(this.x);
			friendlyByteBuf.writeDouble(this.y);
			friendlyByteBuf.writeDouble(this.z);
			friendlyByteBuf.writeFloat(this.yRot);
			friendlyByteBuf.writeFloat(this.xRot);
			super.write(friendlyByteBuf);
		}
	}

	public static class Rot extends ServerboundMovePlayerPacket {
		public Rot() {
			this.hasRot = true;
		}

		@Environment(EnvType.CLIENT)
		public Rot(float f, float g, boolean bl) {
			this.yRot = f;
			this.xRot = g;
			this.onGround = bl;
			this.hasRot = true;
		}

		@Override
		public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
			this.yRot = friendlyByteBuf.readFloat();
			this.xRot = friendlyByteBuf.readFloat();
			super.read(friendlyByteBuf);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
			friendlyByteBuf.writeFloat(this.yRot);
			friendlyByteBuf.writeFloat(this.xRot);
			super.write(friendlyByteBuf);
		}
	}
}
