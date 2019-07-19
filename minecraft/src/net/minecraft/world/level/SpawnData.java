package net.minecraft.world.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.WeighedRandom;

public class SpawnData extends WeighedRandom.WeighedRandomItem {
	private final CompoundTag tag;

	public SpawnData() {
		super(1);
		this.tag = new CompoundTag();
		this.tag.putString("id", "minecraft:pig");
	}

	public SpawnData(CompoundTag compoundTag) {
		this(compoundTag.contains("Weight", 99) ? compoundTag.getInt("Weight") : 1, compoundTag.getCompound("Entity"));
	}

	public SpawnData(int i, CompoundTag compoundTag) {
		super(i);
		this.tag = compoundTag;
	}

	public CompoundTag save() {
		CompoundTag compoundTag = new CompoundTag();
		if (!this.tag.contains("id", 8)) {
			this.tag.putString("id", "minecraft:pig");
		} else if (!this.tag.getString("id").contains(":")) {
			this.tag.putString("id", new ResourceLocation(this.tag.getString("id")).toString());
		}

		compoundTag.put("Entity", this.tag);
		compoundTag.putInt("Weight", this.weight);
		return compoundTag;
	}

	public CompoundTag getTag() {
		return this.tag;
	}
}
