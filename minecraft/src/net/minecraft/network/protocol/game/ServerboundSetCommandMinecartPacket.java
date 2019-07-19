package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;

public class ServerboundSetCommandMinecartPacket implements Packet<ServerGamePacketListener> {
	private int entity;
	private String command;
	private boolean trackOutput;

	public ServerboundSetCommandMinecartPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundSetCommandMinecartPacket(int i, String string, boolean bl) {
		this.entity = i;
		this.command = string;
		this.trackOutput = bl;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.entity = friendlyByteBuf.readVarInt();
		this.command = friendlyByteBuf.readUtf(32767);
		this.trackOutput = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.entity);
		friendlyByteBuf.writeUtf(this.command);
		friendlyByteBuf.writeBoolean(this.trackOutput);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetCommandMinecart(this);
	}

	@Nullable
	public BaseCommandBlock getCommandBlock(Level level) {
		Entity entity = level.getEntity(this.entity);
		return entity instanceof MinecartCommandBlock ? ((MinecartCommandBlock)entity).getCommandBlock() : null;
	}

	public String getCommand() {
		return this.command;
	}

	public boolean isTrackOutput() {
		return this.trackOutput;
	}
}
