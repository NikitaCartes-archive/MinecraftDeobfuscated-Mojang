package net.minecraft.network.protocol.game;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ClientboundBlockEntityDataPacket implements Packet<ClientGamePacketListener> {
	private final BlockPos pos;
	private final BlockEntityType<?> type;
	@Nullable
	private final CompoundTag tag;

	public static ClientboundBlockEntityDataPacket create(BlockEntity blockEntity, Function<BlockEntity, CompoundTag> function) {
		return new ClientboundBlockEntityDataPacket(blockEntity.getBlockPos(), blockEntity.getType(), (CompoundTag)function.apply(blockEntity));
	}

	public static ClientboundBlockEntityDataPacket create(BlockEntity blockEntity) {
		return create(blockEntity, BlockEntity::getUpdateTag);
	}

	private ClientboundBlockEntityDataPacket(BlockPos blockPos, BlockEntityType<?> blockEntityType, CompoundTag compoundTag) {
		this.pos = blockPos;
		this.type = blockEntityType;
		this.tag = compoundTag.isEmpty() ? null : compoundTag;
	}

	public ClientboundBlockEntityDataPacket(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
		this.type = friendlyByteBuf.readById(Registry.BLOCK_ENTITY_TYPE);
		this.tag = friendlyByteBuf.readNbt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeId(Registry.BLOCK_ENTITY_TYPE, this.type);
		friendlyByteBuf.writeNbt(this.tag);
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

	@Nullable
	public CompoundTag getTag() {
		return this.tag;
	}
}
