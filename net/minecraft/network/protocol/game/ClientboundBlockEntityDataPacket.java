/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class ClientboundBlockEntityDataPacket
implements Packet<ClientGamePacketListener> {
    private final BlockPos pos;
    private final BlockEntityType<?> type;
    @Nullable
    private final CompoundTag tag;

    public static ClientboundBlockEntityDataPacket create(BlockEntity blockEntity) {
        return new ClientboundBlockEntityDataPacket(blockEntity.getBlockPos(), blockEntity.getType(), blockEntity.getUpdateTag());
    }

    private ClientboundBlockEntityDataPacket(BlockPos blockPos, BlockEntityType<?> blockEntityType, CompoundTag compoundTag) {
        this.pos = blockPos;
        this.type = blockEntityType;
        this.tag = compoundTag.isEmpty() ? null : compoundTag;
    }

    public ClientboundBlockEntityDataPacket(FriendlyByteBuf friendlyByteBuf) {
        this.pos = friendlyByteBuf.readBlockPos();
        this.type = (BlockEntityType)Registry.BLOCK_ENTITY_TYPE.byId(friendlyByteBuf.readVarInt());
        this.tag = friendlyByteBuf.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.pos);
        friendlyByteBuf.writeVarInt(Registry.BLOCK_ENTITY_TYPE.getId(this.type));
        friendlyByteBuf.writeNbt(this.tag);
    }

    @Override
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

