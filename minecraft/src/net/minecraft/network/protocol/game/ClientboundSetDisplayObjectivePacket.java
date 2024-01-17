package net.minecraft.network.protocol.game;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

public class ClientboundSetDisplayObjectivePacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetDisplayObjectivePacket> STREAM_CODEC = Packet.codec(
		ClientboundSetDisplayObjectivePacket::write, ClientboundSetDisplayObjectivePacket::new
	);
	private final DisplaySlot slot;
	private final String objectiveName;

	public ClientboundSetDisplayObjectivePacket(DisplaySlot displaySlot, @Nullable Objective objective) {
		this.slot = displaySlot;
		if (objective == null) {
			this.objectiveName = "";
		} else {
			this.objectiveName = objective.getName();
		}
	}

	private ClientboundSetDisplayObjectivePacket(FriendlyByteBuf friendlyByteBuf) {
		this.slot = friendlyByteBuf.readById(DisplaySlot.BY_ID);
		this.objectiveName = friendlyByteBuf.readUtf();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeById(DisplaySlot::id, this.slot);
		friendlyByteBuf.writeUtf(this.objectiveName);
	}

	@Override
	public PacketType<ClientboundSetDisplayObjectivePacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_DISPLAY_OBJECTIVE;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetDisplayObjective(this);
	}

	public DisplaySlot getSlot() {
		return this.slot;
	}

	@Nullable
	public String getObjectiveName() {
		return Objects.equals(this.objectiveName, "") ? null : this.objectiveName;
	}
}
