package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;

public class ZombieVillagerRebuildXpFix extends NamedEntityFix {
	public ZombieVillagerRebuildXpFix(Schema schema, boolean bl) {
		super(schema, bl, "Zombie Villager XP rebuild", References.ENTITY, "minecraft:zombie_villager");
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), dynamic -> {
			Optional<Number> optional = dynamic.get("Xp").asNumber();
			if (!optional.isPresent()) {
				int i = ((Number)dynamic.get("VillagerData").get("level").asNumber().orElse(1)).intValue();
				return dynamic.set("Xp", dynamic.createInt(VillagerRebuildLevelAndXpFix.getMinXpPerLevel(i)));
			} else {
				return dynamic;
			}
		});
	}
}
