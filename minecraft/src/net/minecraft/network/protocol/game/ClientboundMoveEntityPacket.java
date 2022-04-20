package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public abstract class ClientboundMoveEntityPacket implements Packet<ClientGamePacketListener> {
	protected final int entityId;
	protected final short xa;
	protected final short ya;
	protected final short za;
	protected final byte yRot;
	protected final byte xRot;
	protected final boolean onGround;
	protected final boolean hasRot;
	protected final boolean hasPos;

	protected ClientboundMoveEntityPacket(int i, short s, short t, short u, byte b, byte c, boolean bl, boolean bl2, boolean bl3) {
		this.entityId = i;
		this.xa = s;
		this.ya = t;
		this.za = u;
		this.yRot = b;
		this.xRot = c;
		this.onGround = bl;
		this.hasRot = bl2;
		this.hasPos = bl3;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleMoveEntity(this);
	}

	public String toString() {
		return "Entity_" + super.toString();
	}

	@Nullable
	public Entity getEntity(Level level) {
		return level.getEntity(this.entityId);
	}

	public short getXa() {
		return this.xa;
	}

	public short getYa() {
		return this.ya;
	}

	public short getZa() {
		return this.za;
	}

	public byte getyRot() {
		return this.yRot;
	}

	public byte getxRot() {
		return this.xRot;
	}

	public boolean hasRotation() {
		return this.hasRot;
	}

	public boolean hasPosition() {
		return this.hasPos;
	}

	public boolean isOnGround() {
		return this.onGround;
	}

	public static class Pos extends ClientboundMoveEntityPacket {
		public Pos(int i, short s, short t, short u, boolean bl) {
			super(i, s, t, u, (byte)0, (byte)0, bl, false, true);
		}

		public static ClientboundMoveEntityPacket.Pos read(FriendlyByteBuf friendlyByteBuf) {
			int i = friendlyByteBuf.readVarInt();
			short s = friendlyByteBuf.readShort();
			short t = friendlyByteBuf.readShort();
			short u = friendlyByteBuf.readShort();
			boolean bl = friendlyByteBuf.readBoolean();
			return new ClientboundMoveEntityPacket.Pos(i, s, t, u, bl);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeVarInt(this.entityId);
			friendlyByteBuf.writeShort(this.xa);
			friendlyByteBuf.writeShort(this.ya);
			friendlyByteBuf.writeShort(this.za);
			friendlyByteBuf.writeBoolean(this.onGround);
		}
	}

	public static class PosRot extends ClientboundMoveEntityPacket {
		public PosRot(int i, short s, short t, short u, byte b, byte c, boolean bl) {
			super(i, s, t, u, b, c, bl, true, true);
		}

		public static ClientboundMoveEntityPacket.PosRot read(FriendlyByteBuf friendlyByteBuf) {
			int i = friendlyByteBuf.readVarInt();
			short s = friendlyByteBuf.readShort();
			short t = friendlyByteBuf.readShort();
			short u = friendlyByteBuf.readShort();
			byte b = friendlyByteBuf.readByte();
			byte c = friendlyByteBuf.readByte();
			boolean bl = friendlyByteBuf.readBoolean();
			return new ClientboundMoveEntityPacket.PosRot(i, s, t, u, b, c, bl);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeVarInt(this.entityId);
			friendlyByteBuf.writeShort(this.xa);
			friendlyByteBuf.writeShort(this.ya);
			friendlyByteBuf.writeShort(this.za);
			friendlyByteBuf.writeByte(this.yRot);
			friendlyByteBuf.writeByte(this.xRot);
			friendlyByteBuf.writeBoolean(this.onGround);
		}
	}

	public static class Rot extends ClientboundMoveEntityPacket {
		public Rot(int i, byte b, byte c, boolean bl) {
			super(i, (short)0, (short)0, (short)0, b, c, bl, true, false);
		}

		public static ClientboundMoveEntityPacket.Rot read(FriendlyByteBuf friendlyByteBuf) {
			int i = friendlyByteBuf.readVarInt();
			byte b = friendlyByteBuf.readByte();
			byte c = friendlyByteBuf.readByte();
			boolean bl = friendlyByteBuf.readBoolean();
			return new ClientboundMoveEntityPacket.Rot(i, b, c, bl);
		}

		@Override
		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeVarInt(this.entityId);
			friendlyByteBuf.writeByte(this.yRot);
			friendlyByteBuf.writeByte(this.xRot);
			friendlyByteBuf.writeBoolean(this.onGround);
		}
	}
}
