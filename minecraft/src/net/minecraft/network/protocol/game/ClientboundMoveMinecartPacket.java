package net.minecraft.network.protocol.game;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.level.Level;

public record ClientboundMoveMinecartPacket(int entityId, List<NewMinecartBehavior.MinecartStep> lerpSteps) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundMoveMinecartPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		ClientboundMoveMinecartPacket::entityId,
		NewMinecartBehavior.MinecartStep.STREAM_CODEC.apply(ByteBufCodecs.list()),
		ClientboundMoveMinecartPacket::lerpSteps,
		ClientboundMoveMinecartPacket::new
	);

	@Override
	public PacketType<ClientboundMoveMinecartPacket> type() {
		return GamePacketTypes.CLIENTBOUND_MOVE_MINECART_ALONG_TRACK;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleMinecartAlongTrack(this);
	}

	@Nullable
	public Entity getEntity(Level level) {
		return level.getEntity(this.entityId);
	}
}
