package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.function.Supplier;
import net.minecraft.Util;

public class EntityZombieSplitFix extends EntityRenameFix {
	private final Supplier<Type<?>> zombieVillagerType = Suppliers.memoize(() -> this.getOutputSchema().getChoiceType(References.ENTITY, "ZombieVillager"));

	public EntityZombieSplitFix(Schema schema) {
		super("EntityZombieSplitFix", schema, true);
	}

	@Override
	protected Pair<String, Typed<?>> fix(String string, Typed<?> typed) {
		if (!string.equals("Zombie")) {
			return Pair.of(string, typed);
		} else {
			Dynamic<?> dynamic = (Dynamic<?>)typed.getOptional(DSL.remainderFinder()).orElseThrow();
			int i = dynamic.get("ZombieType").asInt(0);
			String string2;
			Typed<?> typed2;
			switch (i) {
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
					string2 = "ZombieVillager";
					typed2 = this.changeSchemaToZombieVillager(typed, i - 1);
					break;
				case 6:
					string2 = "Husk";
					typed2 = typed;
					break;
				default:
					string2 = "Zombie";
					typed2 = typed;
			}

			return Pair.of(string2, typed2.update(DSL.remainderFinder(), dynamicx -> dynamicx.remove("ZombieType")));
		}
	}

	private Typed<?> changeSchemaToZombieVillager(Typed<?> typed, int i) {
		return Util.writeAndReadTypedOrThrow(typed, (Type)this.zombieVillagerType.get(), dynamic -> dynamic.set("Profession", dynamic.createInt(i)));
	}
}
