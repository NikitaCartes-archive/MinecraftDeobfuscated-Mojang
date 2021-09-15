/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record ClientboundBlockBreakAckPacket(BlockPos pos, BlockState state, ServerboundPlayerActionPacket.Action action, boolean allGood) implements Packet<ClientGamePacketListener>
{
    private static final Logger LOGGER = LogManager.getLogger();

    public ClientboundBlockBreakAckPacket(BlockPos blockPos, BlockState blockState, ServerboundPlayerActionPacket.Action action, boolean bl, String string) {
        this(blockPos, blockState, action, bl);
    }

    public ClientboundBlockBreakAckPacket(BlockPos blockPos, BlockState blockState, ServerboundPlayerActionPacket.Action action, boolean bl) {
        this.pos = blockPos = blockPos.immutable();
        this.state = blockState;
        this.action = action;
        this.allGood = bl;
    }

    public ClientboundBlockBreakAckPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readBlockPos(), Block.BLOCK_STATE_REGISTRY.byId(friendlyByteBuf.readVarInt()), friendlyByteBuf.readEnum(ServerboundPlayerActionPacket.Action.class), friendlyByteBuf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.pos);
        friendlyByteBuf.writeVarInt(Block.getId(this.state));
        friendlyByteBuf.writeEnum(this.action);
        friendlyByteBuf.writeBoolean(this.allGood);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleBlockBreakAck(this);
    }
}

