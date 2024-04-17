package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.Util;

public class EntityHorseSplitFix extends EntityRenameFix {
	public EntityHorseSplitFix(Schema schema, boolean bl) {
		super("EntityHorseSplitFix", schema, bl);
	}

	@Override
	protected Pair<String, Typed<?>> fix(String string, Typed<?> typed) {
		if (Objects.equals("EntityHorse", string)) {
			Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
			int i = dynamic.get("Type").asInt(0);

			String string2 = switch (i) {
				case 1 -> "Donkey";
				case 2 -> "Mule";
				case 3 -> "ZombieHorse";
				case 4 -> "SkeletonHorse";
				default -> "Horse";
			};
			Type<?> type = (Type<?>)this.getOutputSchema().findChoiceType(References.ENTITY).types().get(string2);
			return Pair.of(string2, Util.writeAndReadTypedOrThrow(typed, type, dynamicx -> dynamicx.remove("Type")));
		} else {
			return Pair.of(string, typed);
		}
	}
}
