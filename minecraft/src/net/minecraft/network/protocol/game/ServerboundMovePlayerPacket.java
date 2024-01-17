package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

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

	@Override
	public abstract PacketType<? extends ServerboundMovePlayerPacket> type();

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
		public static final StreamCodec<FriendlyByteBuf, ServerboundMovePlayerPacket.Pos> STREAM_CODEC = Packet.codec(
			ServerboundMovePlayerPacket.Pos::write, ServerboundMovePlayerPacket.Pos::read
		);

		public Pos(double d, double e, double f, boolean bl) {
			super(d, e, f, 0.0F, 0.0F, bl, true, false);
		}

		private static ServerboundMovePlayerPacket.Pos read(FriendlyByteBuf friendlyByteBuf) {
			double d = friendlyByteBuf.readDouble();
			double e = friendlyByteBuf.readDouble();
			double f = friendlyByteBuf.readDouble();
			boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
			return new ServerboundMovePlayerPacket.Pos(d, e, f, bl);
		}

		private void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeDouble(this.x);
			friendlyByteBuf.writeDouble(this.y);
			friendlyByteBuf.writeDouble(this.z);
			friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
		}

		@Override
		public PacketType<ServerboundMovePlayerPacket.Pos> type() {
			return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_POS;
		}
	}

	public static class PosRot extends ServerboundMovePlayerPacket {
		public static final StreamCodec<FriendlyByteBuf, ServerboundMovePlayerPacket.PosRot> STREAM_CODEC = Packet.codec(
			ServerboundMovePlayerPacket.PosRot::write, ServerboundMovePlayerPacket.PosRot::read
		);

		public PosRot(double d, double e, double f, float g, float h, boolean bl) {
			super(d, e, f, g, h, bl, true, true);
		}

		private static ServerboundMovePlayerPacket.PosRot read(FriendlyByteBuf friendlyByteBuf) {
			double d = friendlyByteBuf.readDouble();
			double e = friendlyByteBuf.readDouble();
			double f = friendlyByteBuf.readDouble();
			float g = friendlyByteBuf.readFloat();
			float h = friendlyByteBuf.readFloat();
			boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
			return new ServerboundMovePlayerPacket.PosRot(d, e, f, g, h, bl);
		}

		private void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeDouble(this.x);
			friendlyByteBuf.writeDouble(this.y);
			friendlyByteBuf.writeDouble(this.z);
			friendlyByteBuf.writeFloat(this.yRot);
			friendlyByteBuf.writeFloat(this.xRot);
			friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
		}

		@Override
		public PacketType<ServerboundMovePlayerPacket.PosRot> type() {
			return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_POS_ROT;
		}
	}

	public static class Rot extends ServerboundMovePlayerPacket {
		public static final StreamCodec<FriendlyByteBuf, ServerboundMovePlayerPacket.Rot> STREAM_CODEC = Packet.codec(
			ServerboundMovePlayerPacket.Rot::write, ServerboundMovePlayerPacket.Rot::read
		);

		public Rot(float f, float g, boolean bl) {
			super(0.0, 0.0, 0.0, f, g, bl, false, true);
		}

		private static ServerboundMovePlayerPacket.Rot read(FriendlyByteBuf friendlyByteBuf) {
			float f = friendlyByteBuf.readFloat();
			float g = friendlyByteBuf.readFloat();
			boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
			return new ServerboundMovePlayerPacket.Rot(f, g, bl);
		}

		private void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeFloat(this.yRot);
			friendlyByteBuf.writeFloat(this.xRot);
			friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
		}

		@Override
		public PacketType<ServerboundMovePlayerPacket.Rot> type() {
			return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_ROT;
		}
	}

	public static class StatusOnly extends ServerboundMovePlayerPacket {
		public static final StreamCodec<FriendlyByteBuf, ServerboundMovePlayerPacket.StatusOnly> STREAM_CODEC = Packet.codec(
			ServerboundMovePlayerPacket.StatusOnly::write, ServerboundMovePlayerPacket.StatusOnly::read
		);

		public StatusOnly(boolean bl) {
			super(0.0, 0.0, 0.0, 0.0F, 0.0F, bl, false, false);
		}

		private static ServerboundMovePlayerPacket.StatusOnly read(FriendlyByteBuf friendlyByteBuf) {
			boolean bl = friendlyByteBuf.readUnsignedByte() != 0;
			return new ServerboundMovePlayerPacket.StatusOnly(bl);
		}

		private void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeByte(this.onGround ? 1 : 0);
		}

		@Override
		public PacketType<ServerboundMovePlayerPacket.StatusOnly> type() {
			return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_STATUS_ONLY;
		}
	}
}
