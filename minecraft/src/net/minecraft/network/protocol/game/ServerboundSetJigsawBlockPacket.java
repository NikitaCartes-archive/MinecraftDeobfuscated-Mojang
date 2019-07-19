package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ServerboundSetJigsawBlockPacket implements Packet<ServerGamePacketListener> {
	private BlockPos pos;
	private ResourceLocation attachementType;
	private ResourceLocation targetPool;
	private String finalState;

	public ServerboundSetJigsawBlockPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundSetJigsawBlockPacket(BlockPos blockPos, ResourceLocation resourceLocation, ResourceLocation resourceLocation2, String string) {
		this.pos = blockPos;
		this.attachementType = resourceLocation;
		this.targetPool = resourceLocation2;
		this.finalState = string;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.pos = friendlyByteBuf.readBlockPos();
		this.attachementType = friendlyByteBuf.readResourceLocation();
		this.targetPool = friendlyByteBuf.readResourceLocation();
		this.finalState = friendlyByteBuf.readUtf(32767);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeResourceLocation(this.attachementType);
		friendlyByteBuf.writeResourceLocation(this.targetPool);
		friendlyByteBuf.writeUtf(this.finalState);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetJigsawBlock(this);
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public ResourceLocation getTargetPool() {
		return this.targetPool;
	}

	public ResourceLocation getAttachementType() {
		return this.attachementType;
	}

	public String getFinalState() {
		return this.finalState;
	}
}
