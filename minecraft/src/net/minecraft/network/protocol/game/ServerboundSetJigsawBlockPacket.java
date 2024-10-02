package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;

public class ServerboundSetJigsawBlockPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundSetJigsawBlockPacket> STREAM_CODEC = Packet.codec(
		ServerboundSetJigsawBlockPacket::write, ServerboundSetJigsawBlockPacket::new
	);
	private final BlockPos pos;
	private final ResourceLocation name;
	private final ResourceLocation target;
	private final ResourceLocation pool;
	private final String finalState;
	private final JigsawBlockEntity.JointType joint;
	private final int selectionPriority;
	private final int placementPriority;

	public ServerboundSetJigsawBlockPacket(
		BlockPos blockPos,
		ResourceLocation resourceLocation,
		ResourceLocation resourceLocation2,
		ResourceLocation resourceLocation3,
		String string,
		JigsawBlockEntity.JointType jointType,
		int i,
		int j
	) {
		this.pos = blockPos;
		this.name = resourceLocation;
		this.target = resourceLocation2;
		this.pool = resourceLocation3;
		this.finalState = string;
		this.joint = jointType;
		this.selectionPriority = i;
		this.placementPriority = j;
	}

	private ServerboundSetJigsawBlockPacket(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
		this.name = friendlyByteBuf.readResourceLocation();
		this.target = friendlyByteBuf.readResourceLocation();
		this.pool = friendlyByteBuf.readResourceLocation();
		this.finalState = friendlyByteBuf.readUtf();
		this.joint = (JigsawBlockEntity.JointType)JigsawBlockEntity.JointType.CODEC.byName(friendlyByteBuf.readUtf(), JigsawBlockEntity.JointType.ALIGNED);
		this.selectionPriority = friendlyByteBuf.readVarInt();
		this.placementPriority = friendlyByteBuf.readVarInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeResourceLocation(this.name);
		friendlyByteBuf.writeResourceLocation(this.target);
		friendlyByteBuf.writeResourceLocation(this.pool);
		friendlyByteBuf.writeUtf(this.finalState);
		friendlyByteBuf.writeUtf(this.joint.getSerializedName());
		friendlyByteBuf.writeVarInt(this.selectionPriority);
		friendlyByteBuf.writeVarInt(this.placementPriority);
	}

	@Override
	public PacketType<ServerboundSetJigsawBlockPacket> type() {
		return GamePacketTypes.SERVERBOUND_SET_JIGSAW_BLOCK;
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

	public int getSelectionPriority() {
		return this.selectionPriority;
	}

	public int getPlacementPriority() {
		return this.placementPriority;
	}
}
