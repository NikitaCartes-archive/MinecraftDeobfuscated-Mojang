package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;

public class EntityMinecartIdentifiersFix extends EntityRenameFix {
	public EntityMinecartIdentifiersFix(Schema schema) {
		super("EntityMinecartIdentifiersFix", schema, true);
	}

	@Override
	protected Pair<String, Typed<?>> fix(String string, Typed<?> typed) {
		if (!string.equals("Minecart")) {
			return Pair.of(string, typed);
		} else {
			int i = typed.getOrCreate(DSL.remainderFinder()).get("Type").asInt(0);

			String string2 = switch (i) {
				case 1 -> "MinecartChest";
				case 2 -> "MinecartFurnace";
				default -> "MinecartRideable";
			};
			Type<?> type = (Type<?>)this.getOutputSchema().findChoiceType(References.ENTITY).types().get(string2);
			return Pair.of(string2, Util.writeAndReadTypedOrThrow(typed, type, dynamic -> dynamic.remove("Type")));
		}
	}
}
