package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class ServerboundSetCommandBlockPacket implements Packet<ServerGamePacketListener> {
	private BlockPos pos;
	private String command;
	private boolean trackOutput;
	private boolean conditional;
	private boolean automatic;
	private CommandBlockEntity.Mode mode;

	public ServerboundSetCommandBlockPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundSetCommandBlockPacket(BlockPos blockPos, String string, CommandBlockEntity.Mode mode, boolean bl, boolean bl2, boolean bl3) {
		this.pos = blockPos;
		this.command = string;
		this.trackOutput = bl;
		this.conditional = bl2;
		this.automatic = bl3;
		this.mode = mode;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.pos = friendlyByteBuf.readBlockPos();
		this.command = friendlyByteBuf.readUtf(32767);
		this.mode = friendlyByteBuf.readEnum(CommandBlockEntity.Mode.class);
		int i = friendlyByteBuf.readByte();
		this.trackOutput = (i & 1) != 0;
		this.conditional = (i & 2) != 0;
		this.automatic = (i & 4) != 0;
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeUtf(this.command);
		friendlyByteBuf.writeEnum(this.mode);
		int i = 0;
		if (this.trackOutput) {
			i |= 1;
		}

		if (this.conditional) {
			i |= 2;
		}

		if (this.automatic) {
			i |= 4;
		}

		friendlyByteBuf.writeByte(i);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetCommandBlock(this);
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public String getCommand() {
		return this.command;
	}

	public boolean isTrackOutput() {
		return this.trackOutput;
	}

	public boolean isConditional() {
		return this.conditional;
	}

	public boolean isAutomatic() {
		return this.automatic;
	}

	public CommandBlockEntity.Mode getMode() {
		return this.mode;
	}
}
