package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.RandomSource;

public class EntityZombieVillagerTypeFix extends NamedEntityFix {
	private static final int PROFESSION_MAX = 6;

	public EntityZombieVillagerTypeFix(Schema schema, boolean bl) {
		super(schema, bl, "EntityZombieVillagerTypeFix", References.ENTITY, "Zombie");
	}

	public Dynamic<?> fixTag(Dynamic<?> dynamic) {
		if (dynamic.get("IsVillager").asBoolean(false)) {
			if (dynamic.get("ZombieType").result().isEmpty()) {
				int i = this.getVillagerProfession(dynamic.get("VillagerProfession").asInt(-1));
				if (i == -1) {
					i = this.getVillagerProfession(RandomSource.create().nextInt(6));
				}

				dynamic = dynamic.set("ZombieType", dynamic.createInt(i));
			}

			dynamic = dynamic.remove("IsVillager");
		}

		return dynamic;
	}

	private int getVillagerProfession(int i) {
		return i >= 0 && i < 6 ? i : -1;
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(DSL.remainderFinder(), this::fixTag);
	}
}
