package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientboundBlockBreakAckPacket implements Packet<ClientGamePacketListener> {
	private static final Logger LOGGER = LogManager.getLogger();
	private BlockPos pos;
	private BlockState state;
	ServerboundPlayerActionPacket.Action action;
	private boolean allGood;

	public ClientboundBlockBreakAckPacket() {
	}

	public ClientboundBlockBreakAckPacket(BlockPos blockPos, BlockState blockState, ServerboundPlayerActionPacket.Action action, boolean bl, String string) {
		this.pos = blockPos.immutable();
		this.state = blockState;
		this.action = action;
		this.allGood = bl;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.pos = friendlyByteBuf.readBlockPos();
		this.state = Block.BLOCK_STATE_REGISTRY.byId(friendlyByteBuf.readVarInt());
		this.action = friendlyByteBuf.readEnum(ServerboundPlayerActionPacket.Action.class);
		this.allGood = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeVarInt(Block.getId(this.state));
		friendlyByteBuf.writeEnum(this.action);
		friendlyByteBuf.writeBoolean(this.allGood);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBlockBreakAck(this);
	}

	@Environment(EnvType.CLIENT)
	public BlockState getState() {
		return this.state;
	}

	@Environment(EnvType.CLIENT)
	public BlockPos getPos() {
		return this.pos;
	}

	@Environment(EnvType.CLIENT)
	public boolean allGood() {
		return this.allGood;
	}

	@Environment(EnvType.CLIENT)
	public ServerboundPlayerActionPacket.Action action() {
		return this.action;
	}
}
