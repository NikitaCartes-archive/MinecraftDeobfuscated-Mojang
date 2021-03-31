package net.minecraft.world.entity.npc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;

public class VillagerData {
	public static final int MIN_VILLAGER_LEVEL = 1;
	public static final int MAX_VILLAGER_LEVEL = 5;
	private static final int[] NEXT_LEVEL_XP_THRESHOLDS = new int[]{0, 10, 70, 150, 250};
	public static final Codec<VillagerData> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Registry.VILLAGER_TYPE.fieldOf("type").orElseGet(() -> VillagerType.PLAINS).forGetter(villagerData -> villagerData.type),
					Registry.VILLAGER_PROFESSION.fieldOf("profession").orElseGet(() -> VillagerProfession.NONE).forGetter(villagerData -> villagerData.profession),
					Codec.INT.fieldOf("level").orElse(1).forGetter(villagerData -> villagerData.level)
				)
				.apply(instance, VillagerData::new)
	);
	private final VillagerType type;
	private final VillagerProfession profession;
	private final int level;

	public VillagerData(VillagerType villagerType, VillagerProfession villagerProfession, int i) {
		this.type = villagerType;
		this.profession = villagerProfession;
		this.level = Math.max(1, i);
	}

	public VillagerType getType() {
		return this.type;
	}

	public VillagerProfession getProfession() {
		return this.profession;
	}

	public int getLevel() {
		return this.level;
	}

	public VillagerData setType(VillagerType villagerType) {
		return new VillagerData(villagerType, this.profession, this.level);
	}

	public VillagerData setProfession(VillagerProfession villagerProfession) {
		return new VillagerData(this.type, villagerProfession, this.level);
	}

	public VillagerData setLevel(int i) {
		return new VillagerData(this.type, this.profession, i);
	}

	public static int getMinXpPerLevel(int i) {
		return canLevelUp(i) ? NEXT_LEVEL_XP_THRESHOLDS[i - 1] : 0;
	}

	public static int getMaxXpPerLevel(int i) {
		return canLevelUp(i) ? NEXT_LEVEL_XP_THRESHOLDS[i] : 0;
	}

	public static boolean canLevelUp(int i) {
		return i >= 1 && i < 5;
	}
}
