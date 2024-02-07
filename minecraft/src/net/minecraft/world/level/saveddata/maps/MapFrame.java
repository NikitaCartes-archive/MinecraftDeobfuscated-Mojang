package net.minecraft.world.level.saveddata.maps;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class MapFrame {
	private final BlockPos pos;
	private final int rotation;
	private final int entityId;

	public MapFrame(BlockPos blockPos, int i, int j) {
		this.pos = blockPos;
		this.rotation = i;
		this.entityId = j;
	}

	@Nullable
	public static MapFrame load(CompoundTag compoundTag) {
		Optional<BlockPos> optional = NbtUtils.readBlockPos(compoundTag, "pos");
		if (optional.isEmpty()) {
			return null;
		} else {
			int i = compoundTag.getInt("rotation");
			int j = compoundTag.getInt("entity_id");
			return new MapFrame((BlockPos)optional.get(), i, j);
		}
	}

	public CompoundTag save() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.put("pos", NbtUtils.writeBlockPos(this.pos));
		compoundTag.putInt("rotation", this.rotation);
		compoundTag.putInt("entity_id", this.entityId);
		return compoundTag;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public int getRotation() {
		return this.rotation;
	}

	public int getEntityId() {
		return this.entityId;
	}

	public String getId() {
		return frameId(this.pos);
	}

	public static String frameId(BlockPos blockPos) {
		return "frame-" + blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
	}
}
