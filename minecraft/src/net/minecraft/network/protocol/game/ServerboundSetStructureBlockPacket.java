package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;

public class ServerboundSetStructureBlockPacket implements Packet<ServerGamePacketListener> {
	private final BlockPos pos;
	private final StructureBlockEntity.UpdateType updateType;
	private final StructureMode mode;
	private final String name;
	private final BlockPos offset;
	private final Vec3i size;
	private final Mirror mirror;
	private final Rotation rotation;
	private final String data;
	private final boolean ignoreEntities;
	private final boolean showAir;
	private final boolean showBoundingBox;
	private final float integrity;
	private final long seed;

	@Environment(EnvType.CLIENT)
	public ServerboundSetStructureBlockPacket(
		BlockPos blockPos,
		StructureBlockEntity.UpdateType updateType,
		StructureMode structureMode,
		String string,
		BlockPos blockPos2,
		Vec3i vec3i,
		Mirror mirror,
		Rotation rotation,
		String string2,
		boolean bl,
		boolean bl2,
		boolean bl3,
		float f,
		long l
	) {
		this.pos = blockPos;
		this.updateType = updateType;
		this.mode = structureMode;
		this.name = string;
		this.offset = blockPos2;
		this.size = vec3i;
		this.mirror = mirror;
		this.rotation = rotation;
		this.data = string2;
		this.ignoreEntities = bl;
		this.showAir = bl2;
		this.showBoundingBox = bl3;
		this.integrity = f;
		this.seed = l;
	}

	public ServerboundSetStructureBlockPacket(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
		this.updateType = friendlyByteBuf.readEnum(StructureBlockEntity.UpdateType.class);
		this.mode = friendlyByteBuf.readEnum(StructureMode.class);
		this.name = friendlyByteBuf.readUtf();
		int i = 48;
		this.offset = new BlockPos(
			Mth.clamp(friendlyByteBuf.readByte(), -48, 48), Mth.clamp(friendlyByteBuf.readByte(), -48, 48), Mth.clamp(friendlyByteBuf.readByte(), -48, 48)
		);
		int j = 48;
		this.size = new Vec3i(
			Mth.clamp(friendlyByteBuf.readByte(), 0, 48), Mth.clamp(friendlyByteBuf.readByte(), 0, 48), Mth.clamp(friendlyByteBuf.readByte(), 0, 48)
		);
		this.mirror = friendlyByteBuf.readEnum(Mirror.class);
		this.rotation = friendlyByteBuf.readEnum(Rotation.class);
		this.data = friendlyByteBuf.readUtf(12);
		this.integrity = Mth.clamp(friendlyByteBuf.readFloat(), 0.0F, 1.0F);
		this.seed = friendlyByteBuf.readVarLong();
		int k = friendlyByteBuf.readByte();
		this.ignoreEntities = (k & 1) != 0;
		this.showAir = (k & 2) != 0;
		this.showBoundingBox = (k & 4) != 0;
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBlockPos(this.pos);
		friendlyByteBuf.writeEnum(this.updateType);
		friendlyByteBuf.writeEnum(this.mode);
		friendlyByteBuf.writeUtf(this.name);
		friendlyByteBuf.writeByte(this.offset.getX());
		friendlyByteBuf.writeByte(this.offset.getY());
		friendlyByteBuf.writeByte(this.offset.getZ());
		friendlyByteBuf.writeByte(this.size.getX());
		friendlyByteBuf.writeByte(this.size.getY());
		friendlyByteBuf.writeByte(this.size.getZ());
		friendlyByteBuf.writeEnum(this.mirror);
		friendlyByteBuf.writeEnum(this.rotation);
		friendlyByteBuf.writeUtf(this.data);
		friendlyByteBuf.writeFloat(this.integrity);
		friendlyByteBuf.writeVarLong(this.seed);
		int i = 0;
		if (this.ignoreEntities) {
			i |= 1;
		}

		if (this.showAir) {
			i |= 2;
		}

		if (this.showBoundingBox) {
			i |= 4;
		}

		friendlyByteBuf.writeByte(i);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetStructureBlock(this);
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public StructureBlockEntity.UpdateType getUpdateType() {
		return this.updateType;
	}

	public StructureMode getMode() {
		return this.mode;
	}

	public String getName() {
		return this.name;
	}

	public BlockPos getOffset() {
		return this.offset;
	}

	public Vec3i getSize() {
		return this.size;
	}

	public Mirror getMirror() {
		return this.mirror;
	}

	public Rotation getRotation() {
		return this.rotation;
	}

	public String getData() {
		return this.data;
	}

	public boolean isIgnoreEntities() {
		return this.ignoreEntities;
	}

	public boolean isShowAir() {
		return this.showAir;
	}

	public boolean isShowBoundingBox() {
		return this.showBoundingBox;
	}

	public float getIntegrity() {
		return this.integrity;
	}

	public long getSeed() {
		return this.seed;
	}
}
