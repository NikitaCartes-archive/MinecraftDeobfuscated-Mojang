package net.minecraft.world.level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;

public class SpawnData extends WeightedEntry.IntrusiveBase {
	public static final int DEFAULT_WEIGHT = 1;
	public static final String DEFAULT_TYPE = "minecraft:pig";
	public static final String CUSTOM_SPAWN_RULES_TAG = "CustomeSpawnRules";
	public static final String BLOCK_LIGHT_LIMIT_TAG = "BlockLightLimit";
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
		ResourceLocation resourceLocation = ResourceLocation.tryParse(compoundTag.getString("id"));
		if (resourceLocation != null) {
			compoundTag.putString("id", resourceLocation.toString());
		} else {
			compoundTag.putString("id", "minecraft:pig");
		}
	}

	public CompoundTag save() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.put("Entity", this.tag);
		compoundTag.putInt("Weight", this.getWeight().asInt());
		return compoundTag;
	}

	public CompoundTag getTag() {
		return this.tag;
	}
}
