package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class EntityHorseSplitFix extends EntityRenameFix {
	public EntityHorseSplitFix(Schema schema, boolean bl) {
		super("EntityHorseSplitFix", schema, bl);
	}

	@Override
	protected Pair<String, Typed<?>> fix(String string, Typed<?> typed) {
		Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
		if (Objects.equals("EntityHorse", string)) {
			int i = dynamic.get("Type").asInt(0);
			String string2;
			switch (i) {
				case 0:
				default:
					string2 = "Horse";
					break;
				case 1:
					string2 = "Donkey";
					break;
				case 2:
					string2 = "Mule";
					break;
				case 3:
					string2 = "ZombieHorse";
					break;
				case 4:
					string2 = "SkeletonHorse";
			}

			dynamic.remove("Type");
			Type<?> type = (Type<?>)this.getOutputSchema().findChoiceType(References.ENTITY).types().get(string2);
			return Pair.of(
				string2,
				(Typed<?>)((Pair)typed.write().flatMap(type::readTyped).result().orElseThrow(() -> new IllegalStateException("Could not parse the new horse"))).getFirst()
			);
		} else {
			return Pair.of(string, typed);
		}
	}
}
