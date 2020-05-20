package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class VillagerDataFix extends NamedEntityFix {
	public VillagerDataFix(Schema schema, String string) {
		super(schema, false, "Villager profession data fix (" + string + ")", References.ENTITY, string);
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
		return typed.set(
			DSL.remainderFinder(),
			dynamic.remove("Profession")
				.remove("Career")
				.remove("CareerLevel")
				.set(
					"VillagerData",
					dynamic.createMap(
						ImmutableMap.of(
							dynamic.createString("type"),
							dynamic.createString("minecraft:plains"),
							dynamic.createString("profession"),
							dynamic.createString(upgradeData(dynamic.get("Profession").asInt(0), dynamic.get("Career").asInt(0))),
							dynamic.createString("level"),
							DataFixUtils.orElse(dynamic.get("CareerLevel").result(), dynamic.createInt(1))
						)
					)
				)
		);
	}

	private static String upgradeData(int i, int j) {
		if (i == 0) {
			if (j == 2) {
				return "minecraft:fisherman";
			} else if (j == 3) {
				return "minecraft:shepherd";
			} else {
				return j == 4 ? "minecraft:fletcher" : "minecraft:farmer";
			}
		} else if (i == 1) {
			return j == 2 ? "minecraft:cartographer" : "minecraft:librarian";
		} else if (i == 2) {
			return "minecraft:cleric";
		} else if (i == 3) {
			if (j == 2) {
				return "minecraft:weaponsmith";
			} else {
				return j == 3 ? "minecraft:toolsmith" : "minecraft:armorer";
			}
		} else if (i == 4) {
			return j == 2 ? "minecraft:leatherworker" : "minecraft:butcher";
		} else {
			return i == 5 ? "minecraft:nitwit" : "minecraft:none";
		}
	}
}
