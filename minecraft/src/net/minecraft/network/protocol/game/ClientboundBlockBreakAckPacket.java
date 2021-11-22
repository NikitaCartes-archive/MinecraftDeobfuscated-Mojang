package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record ClientboundBlockBreakAckPacket(BlockPos pos, BlockState state, ServerboundPlayerActionPacket.Action action, boolean allGood)
	implements Packet<ClientGamePacketListener> {
	private static final Logger LOGGER = LogManager.getLogger();

	public ClientboundBlockBreakAckPacket(BlockPos blockPos, BlockState blockState, ServerboundPlayerActionPacket.Action action, boolean bl, String string) {
		this(blockPos, blockState, action, bl);
	}

	public ClientboundBlockBreakAckPacket(BlockPos pos, BlockState state, ServerboundPlayerActionPacket.Action action, boolean allGood) {
		pos = pos.immutable();
		this.pos = pos;
		this.state = state;
		this.action = action;
		this.allGood = allGood;
	}

	public ClientboundBlockBreakAckPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readBlockPos(),
			Block.BLOCK_STATE_REGISTRY.byId(friendlyByteBuf.readVarInt()),
			friendlyByteBuf.readEnum(ServerboundPlayerActionPacket.Action.class),
			friendlyByteBuf.readBoolean()
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeVarInt(Block.getId(this.state));
		friendlyByteBuf.writeEnum(this.action);
		friendlyByteBuf.writeBoolean(this.allGood);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBlockBreakAck(this);
	}
}
