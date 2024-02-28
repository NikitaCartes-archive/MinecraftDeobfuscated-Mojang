package net.minecraft.network.protocol.game;

import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ClientboundBlockEntityDataPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBlockEntityDataPacket> STREAM_CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		ClientboundBlockEntityDataPacket::getPos,
		ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE),
		ClientboundBlockEntityDataPacket::getType,
		ByteBufCodecs.TRUSTED_COMPOUND_TAG,
		ClientboundBlockEntityDataPacket::getTag,
		ClientboundBlockEntityDataPacket::new
	);
	private final BlockPos pos;
	private final BlockEntityType<?> type;
	private final CompoundTag tag;

	public static ClientboundBlockEntityDataPacket create(BlockEntity blockEntity, BiFunction<BlockEntity, RegistryAccess, CompoundTag> biFunction) {
		RegistryAccess registryAccess = blockEntity.getLevel().registryAccess();
		return new ClientboundBlockEntityDataPacket(blockEntity.getBlockPos(), blockEntity.getType(), (CompoundTag)biFunction.apply(blockEntity, registryAccess));
	}

	public static ClientboundBlockEntityDataPacket create(BlockEntity blockEntity) {
		return create(blockEntity, BlockEntity::getUpdateTag);
	}

	private ClientboundBlockEntityDataPacket(BlockPos blockPos, BlockEntityType<?> blockEntityType, CompoundTag compoundTag) {
		this.pos = blockPos;
		this.type = blockEntityType;
		this.tag = compoundTag;
	}

	@Override
	public PacketType<ClientboundBlockEntityDataPacket> type() {
		return GamePacketTypes.CLIENTBOUND_BLOCK_ENTITY_DATA;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBlockEntityData(this);
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public BlockEntityType<?> getType() {
		return this.type;
	}

	public CompoundTag getTag() {
		return this.tag;
	}
}
