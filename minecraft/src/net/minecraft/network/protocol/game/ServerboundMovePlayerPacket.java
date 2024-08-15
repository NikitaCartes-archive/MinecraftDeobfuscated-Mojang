package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public abstract class ServerboundMovePlayerPacket implements Packet<ServerGamePacketListener> {
	private static final int FLAG_ON_GROUND = 1;
	private static final int FLAG_HORIZONTAL_COLLISION = 2;
	protected final double x;
	protected final double y;
	protected final double z;
	protected final float yRot;
	protected final float xRot;
	protected final boolean onGround;
	protected final boolean horizontalCollision;
	protected final boolean hasPos;
	protected final boolean hasRot;

	static int packFlags(boolean bl, boolean bl2) {
		int i = 0;
		if (bl) {
			i |= 1;
		}

		if (bl2) {
			i |= 2;
		}

		return i;
	}

	static boolean unpackOnGround(int i) {
		return (i & 1) != 0;
	}

	static boolean unpackHorizontalCollision(int i) {
		return (i & 2) != 0;
	}

	protected ServerboundMovePlayerPacket(double d, double e, double f, float g, float h, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		this.x = d;
		this.y = e;
		this.z = f;
		this.yRot = g;
		this.xRot = h;
		this.onGround = bl;
		this.horizontalCollision = bl2;
		this.hasPos = bl3;
		this.hasRot = bl4;
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

	public boolean horizontalCollision() {
		return this.horizontalCollision;
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

		public Pos(double d, double e, double f, boolean bl, boolean bl2) {
			super(d, e, f, 0.0F, 0.0F, bl, bl2, true, false);
		}

		private static ServerboundMovePlayerPacket.Pos read(FriendlyByteBuf friendlyByteBuf) {
			double d = friendlyByteBuf.readDouble();
			double e = friendlyByteBuf.readDouble();
			double f = friendlyByteBuf.readDouble();
			short s = friendlyByteBuf.readUnsignedByte();
			boolean bl = ServerboundMovePlayerPacket.unpackOnGround(s);
			boolean bl2 = ServerboundMovePlayerPacket.unpackHorizontalCollision(s);
			return new ServerboundMovePlayerPacket.Pos(d, e, f, bl, bl2);
		}

		private void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeDouble(this.x);
			friendlyByteBuf.writeDouble(this.y);
			friendlyByteBuf.writeDouble(this.z);
			friendlyByteBuf.writeByte(ServerboundMovePlayerPacket.packFlags(this.onGround, this.horizontalCollision));
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

		public PosRot(double d, double e, double f, float g, float h, boolean bl, boolean bl2) {
			super(d, e, f, g, h, bl, bl2, true, true);
		}

		private static ServerboundMovePlayerPacket.PosRot read(FriendlyByteBuf friendlyByteBuf) {
			double d = friendlyByteBuf.readDouble();
			double e = friendlyByteBuf.readDouble();
			double f = friendlyByteBuf.readDouble();
			float g = friendlyByteBuf.readFloat();
			float h = friendlyByteBuf.readFloat();
			short s = friendlyByteBuf.readUnsignedByte();
			boolean bl = ServerboundMovePlayerPacket.unpackOnGround(s);
			boolean bl2 = ServerboundMovePlayerPacket.unpackHorizontalCollision(s);
			return new ServerboundMovePlayerPacket.PosRot(d, e, f, g, h, bl, bl2);
		}

		private void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeDouble(this.x);
			friendlyByteBuf.writeDouble(this.y);
			friendlyByteBuf.writeDouble(this.z);
			friendlyByteBuf.writeFloat(this.yRot);
			friendlyByteBuf.writeFloat(this.xRot);
			friendlyByteBuf.writeByte(ServerboundMovePlayerPacket.packFlags(this.onGround, this.horizontalCollision));
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

		public Rot(float f, float g, boolean bl, boolean bl2) {
			super(0.0, 0.0, 0.0, f, g, bl, bl2, false, true);
		}

		private static ServerboundMovePlayerPacket.Rot read(FriendlyByteBuf friendlyByteBuf) {
			float f = friendlyByteBuf.readFloat();
			float g = friendlyByteBuf.readFloat();
			short s = friendlyByteBuf.readUnsignedByte();
			boolean bl = ServerboundMovePlayerPacket.unpackOnGround(s);
			boolean bl2 = ServerboundMovePlayerPacket.unpackHorizontalCollision(s);
			return new ServerboundMovePlayerPacket.Rot(f, g, bl, bl2);
		}

		private void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeFloat(this.yRot);
			friendlyByteBuf.writeFloat(this.xRot);
			friendlyByteBuf.writeByte(ServerboundMovePlayerPacket.packFlags(this.onGround, this.horizontalCollision));
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

		public StatusOnly(boolean bl, boolean bl2) {
			super(0.0, 0.0, 0.0, 0.0F, 0.0F, bl, bl2, false, false);
		}

		private static ServerboundMovePlayerPacket.StatusOnly read(FriendlyByteBuf friendlyByteBuf) {
			short s = friendlyByteBuf.readUnsignedByte();
			boolean bl = ServerboundMovePlayerPacket.unpackOnGround(s);
			boolean bl2 = ServerboundMovePlayerPacket.unpackHorizontalCollision(s);
			return new ServerboundMovePlayerPacket.StatusOnly(bl, bl2);
		}

		private void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeByte(ServerboundMovePlayerPacket.packFlags(this.onGround, this.horizontalCollision));
		}

		@Override
		public PacketType<ServerboundMovePlayerPacket.StatusOnly> type() {
			return GamePacketTypes.SERVERBOUND_MOVE_PLAYER_STATUS_ONLY;
		}
	}
}
