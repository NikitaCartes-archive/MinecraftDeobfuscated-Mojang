package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;

public class AbstractArrowPickupFix extends DataFix {
	public AbstractArrowPickupFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Schema schema = this.getInputSchema();
		return this.fixTypeEverywhereTyped("AbstractArrowPickupFix", schema.getType(References.ENTITY), this::updateProjectiles);
	}

	private Typed<?> updateProjectiles(Typed<?> typed) {
		typed = this.updateEntity(typed, "minecraft:arrow", AbstractArrowPickupFix::updatePickup);
		typed = this.updateEntity(typed, "minecraft:spectral_arrow", AbstractArrowPickupFix::updatePickup);
		return this.updateEntity(typed, "minecraft:trident", AbstractArrowPickupFix::updatePickup);
	}

	private static Dynamic<?> updatePickup(Dynamic<?> dynamic) {
		if (dynamic.get("pickup").result().isPresent()) {
			return dynamic;
		} else {
			boolean bl = dynamic.get("player").asBoolean(true);
			return dynamic.set("pickup", dynamic.createByte((byte)(bl ? 1 : 0))).remove("player");
		}
	}

	private Typed<?> updateEntity(Typed<?> typed, String string, Function<Dynamic<?>, Dynamic<?>> function) {
		Type<?> type = this.getInputSchema().getChoiceType(References.ENTITY, string);
		Type<?> type2 = this.getOutputSchema().getChoiceType(References.ENTITY, string);
		return typed.updateTyped(DSL.namedChoice(string, type), type2, typedx -> typedx.update(DSL.remainderFinder(), function));
	}
}
