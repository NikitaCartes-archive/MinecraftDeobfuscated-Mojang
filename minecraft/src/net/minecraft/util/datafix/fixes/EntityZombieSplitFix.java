package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityZombieSplitFix extends SimpleEntityRenameFix {
	public EntityZombieSplitFix(Schema schema, boolean bl) {
		super("EntityZombieSplitFix", schema, bl);
	}

	@Override
	protected Pair<String, Dynamic<?>> getNewNameAndTag(String string, Dynamic<?> dynamic) {
		if (Objects.equals("Zombie", string)) {
			String string2 = "Zombie";
			int i = dynamic.get("ZombieType").asInt(0);
			switch (i) {
				case 0:
				default:
					break;
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
					string2 = "ZombieVillager";
					dynamic = dynamic.set("Profession", dynamic.createInt(i - 1));
					break;
				case 6:
					string2 = "Husk";
			}

			dynamic = dynamic.remove("ZombieType");
			return Pair.of(string2, dynamic);
		} else {
			return Pair.of(string, dynamic);
		}
	}
}
