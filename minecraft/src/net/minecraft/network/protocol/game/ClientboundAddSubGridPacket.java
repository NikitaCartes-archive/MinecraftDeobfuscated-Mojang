package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.grid.GridCarrier;
import net.minecraft.world.grid.SubGridBlocks;
import net.minecraft.world.level.biome.Biome;

public record ClientboundAddSubGridPacket(int id, UUID uuid, double x, double y, double z, SubGridBlocks blocks, Holder<Biome> biome)
	implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAddSubGridPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		ClientboundAddSubGridPacket::id,
		UUIDUtil.STREAM_CODEC,
		ClientboundAddSubGridPacket::uuid,
		ByteBufCodecs.DOUBLE,
		ClientboundAddSubGridPacket::x,
		ByteBufCodecs.DOUBLE,
		ClientboundAddSubGridPacket::y,
		ByteBufCodecs.DOUBLE,
		ClientboundAddSubGridPacket::z,
		SubGridBlocks.STREAM_CODEC,
		ClientboundAddSubGridPacket::blocks,
		ByteBufCodecs.holderRegistry(Registries.BIOME),
		ClientboundAddSubGridPacket::biome,
		ClientboundAddSubGridPacket::new
	);

	public ClientboundAddSubGridPacket(GridCarrier gridCarrier) {
		this(
			gridCarrier.getId(),
			gridCarrier.getUUID(),
			gridCarrier.getX(),
			gridCarrier.getY(),
			gridCarrier.getZ(),
			gridCarrier.grid().getBlocks().copy(),
			gridCarrier.grid().getBiome()
		);
	}

	@Override
	public PacketType<ClientboundAddSubGridPacket> type() {
		return GamePacketTypes.CLIENTBOUND_ADD_SUB_GRID;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddSubGrid(this);
	}
}
