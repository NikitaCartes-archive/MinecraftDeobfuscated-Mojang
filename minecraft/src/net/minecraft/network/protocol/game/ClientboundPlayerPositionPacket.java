package net.minecraft.network.protocol.game;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPlayerPositionPacket implements Packet<ClientGamePacketListener> {
	private final double x;
	private final double y;
	private final double z;
	private final float yRot;
	private final float xRot;
	private final Set<ClientboundPlayerPositionPacket.RelativeArgument> relativeArguments;
	private final int id;
	private final boolean dismountVehicle;

	public ClientboundPlayerPositionPacket(
		double d, double e, double f, float g, float h, Set<ClientboundPlayerPositionPacket.RelativeArgument> set, int i, boolean bl
	) {
		this.x = d;
		this.y = e;
		this.z = f;
		this.yRot = g;
		this.xRot = h;
		this.relativeArguments = set;
		this.id = i;
		this.dismountVehicle = bl;
	}

	public ClientboundPlayerPositionPacket(FriendlyByteBuf friendlyByteBuf) {
		this.x = friendlyByteBuf.readDouble();
		this.y = friendlyByteBuf.readDouble();
		this.z = friendlyByteBuf.readDouble();
		this.yRot = friendlyByteBuf.readFloat();
		this.xRot = friendlyByteBuf.readFloat();
		this.relativeArguments = ClientboundPlayerPositionPacket.RelativeArgument.unpack(friendlyByteBuf.readUnsignedByte());
		this.id = friendlyByteBuf.readVarInt();
		this.dismountVehicle = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeDouble(this.x);
		friendlyByteBuf.writeDouble(this.y);
		friendlyByteBuf.writeDouble(this.z);
		friendlyByteBuf.writeFloat(this.yRot);
		friendlyByteBuf.writeFloat(this.xRot);
		friendlyByteBuf.writeByte(ClientboundPlayerPositionPacket.RelativeArgument.pack(this.relativeArguments));
		friendlyByteBuf.writeVarInt(this.id);
		friendlyByteBuf.writeBoolean(this.dismountVehicle);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleMovePlayer(this);
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getZ() {
		return this.z;
	}

	public float getYRot() {
		return this.yRot;
	}

	public float getXRot() {
		return this.xRot;
	}

	public int getId() {
		return this.id;
	}

	public boolean requestDismountVehicle() {
		return this.dismountVehicle;
	}

	public Set<ClientboundPlayerPositionPacket.RelativeArgument> getRelativeArguments() {
		return this.relativeArguments;
	}

	public static enum RelativeArgument {
		X(0),
		Y(1),
		Z(2),
		Y_ROT(3),
		X_ROT(4);

		private final int bit;

		private RelativeArgument(int j) {
			this.bit = j;
		}

		private int getMask() {
			return 1 << this.bit;
		}

		private boolean isSet(int i) {
			return (i & this.getMask()) == this.getMask();
		}

		public static Set<ClientboundPlayerPositionPacket.RelativeArgument> unpack(int i) {
			Set<ClientboundPlayerPositionPacket.RelativeArgument> set = EnumSet.noneOf(ClientboundPlayerPositionPacket.RelativeArgument.class);

			for (ClientboundPlayerPositionPacket.RelativeArgument relativeArgument : values()) {
				if (relativeArgument.isSet(i)) {
					set.add(relativeArgument);
				}
			}

			return set;
		}

		public static int pack(Set<ClientboundPlayerPositionPacket.RelativeArgument> set) {
			int i = 0;

			for (ClientboundPlayerPositionPacket.RelativeArgument relativeArgument : set) {
				i |= relativeArgument.getMask();
			}

			return i;
		}
	}
}
