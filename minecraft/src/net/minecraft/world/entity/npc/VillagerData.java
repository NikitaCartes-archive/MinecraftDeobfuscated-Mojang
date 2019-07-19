package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class VillagerData {
	private static final int[] NEXT_LEVEL_XP_THRESHOLDS = new int[]{0, 10, 70, 150, 250};
	private final VillagerType type;
	private final VillagerProfession profession;
	private final int level;

	public VillagerData(VillagerType villagerType, VillagerProfession villagerProfession, int i) {
		this.type = villagerType;
		this.profession = villagerProfession;
		this.level = Math.max(1, i);
	}

	public VillagerData(Dynamic<?> dynamic) {
		this(
			Registry.VILLAGER_TYPE.get(ResourceLocation.tryParse(dynamic.get("type").asString(""))),
			Registry.VILLAGER_PROFESSION.get(ResourceLocation.tryParse(dynamic.get("profession").asString(""))),
			dynamic.get("level").asInt(1)
		);
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

	public <T> T serialize(DynamicOps<T> dynamicOps) {
		return dynamicOps.createMap(
			ImmutableMap.of(
				dynamicOps.createString("type"),
				dynamicOps.createString(Registry.VILLAGER_TYPE.getKey(this.type).toString()),
				dynamicOps.createString("profession"),
				dynamicOps.createString(Registry.VILLAGER_PROFESSION.getKey(this.profession).toString()),
				dynamicOps.createString("level"),
				dynamicOps.createInt(this.level)
			)
		);
	}

	@Environment(EnvType.CLIENT)
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
