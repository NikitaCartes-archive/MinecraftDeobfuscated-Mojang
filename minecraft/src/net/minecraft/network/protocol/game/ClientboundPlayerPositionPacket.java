package net.minecraft.network.protocol.game;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;

public record ClientboundPlayerPositionPacket(int id, Vec3 position, Vec3 deltaMovement, float yRot, float xRot, Set<Relative> relativeArguments)
	implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundPlayerPositionPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		ClientboundPlayerPositionPacket::id,
		Vec3.STREAM_CODEC,
		ClientboundPlayerPositionPacket::position,
		Vec3.STREAM_CODEC,
		ClientboundPlayerPositionPacket::deltaMovement,
		ByteBufCodecs.FLOAT,
		ClientboundPlayerPositionPacket::yRot,
		ByteBufCodecs.FLOAT,
		ClientboundPlayerPositionPacket::xRot,
		Relative.SET_STREAM_CODEC,
		ClientboundPlayerPositionPacket::relativeArguments,
		ClientboundPlayerPositionPacket::new
	);

	public static ClientboundPlayerPositionPacket of(int i, PositionMoveRotation positionMoveRotation, Set<Relative> set) {
		return new ClientboundPlayerPositionPacket(
			i, positionMoveRotation.position(), positionMoveRotation.deltaMovement(), positionMoveRotation.yRot(), positionMoveRotation.xRot(), set
		);
	}

	@Override
	public PacketType<ClientboundPlayerPositionPacket> type() {
		return GamePacketTypes.CLIENTBOUND_PLAYER_POSITION;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleMovePlayer(this);
	}
}
