package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;

public class ServerboundSetJigsawBlockPacket implements Packet<ServerGamePacketListener> {
	private BlockPos pos;
	private ResourceLocation name;
	private ResourceLocation target;
	private ResourceLocation pool;
	private String finalState;
	private JigsawBlockEntity.JointType joint;

	public ServerboundSetJigsawBlockPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundSetJigsawBlockPacket(
		BlockPos blockPos,
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
		ResourceLocation resourceLocation3,
		String string,
		JigsawBlockEntity.JointType jointType
	) {
		this.pos = blockPos;
		this.name = resourceLocation;
		this.target = resourceLocation2;
		this.pool = resourceLocation3;
		this.finalState = string;
		this.joint = jointType;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.pos = friendlyByteBuf.readBlockPos();
		this.name = friendlyByteBuf.readResourceLocation();
		this.target = friendlyByteBuf.readResourceLocation();
		this.pool = friendlyByteBuf.readResourceLocation();
		this.finalState = friendlyByteBuf.readUtf(32767);
		this.joint = (JigsawBlockEntity.JointType)JigsawBlockEntity.JointType.byName(friendlyByteBuf.readUtf(32767)).orElse(JigsawBlockEntity.JointType.ALIGNED);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeResourceLocation(this.name);
		friendlyByteBuf.writeResourceLocation(this.target);
		friendlyByteBuf.writeResourceLocation(this.pool);
		friendlyByteBuf.writeUtf(this.finalState);
		friendlyByteBuf.writeUtf(this.joint.getSerializedName());
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetJigsawBlock(this);
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public ResourceLocation getName() {
		return this.name;
	}

	public ResourceLocation getTarget() {
		return this.target;
	}

	public ResourceLocation getPool() {
		return this.pool;
	}

	public String getFinalState() {
		return this.finalState;
	}

	public JigsawBlockEntity.JointType getJoint() {
		return this.joint;
	}
}
