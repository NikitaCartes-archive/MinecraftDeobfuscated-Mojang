package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EffectDurationFix extends DataFix {
	private static final Set<String> POTION_ITEMS = Set.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow");

	public EffectDurationFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Schema schema = this.getInputSchema();
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		OpticFinder<?> opticFinder2 = type.findField("tag");
		return TypeRewriteRule.seq(
			this.fixTypeEverywhereTyped("EffectDurationEntity", schema.getType(References.ENTITY), typed -> typed.update(DSL.remainderFinder(), this::updateEntity)),
			this.fixTypeEverywhereTyped("EffectDurationPlayer", schema.getType(References.PLAYER), typed -> typed.update(DSL.remainderFinder(), this::updateEntity)),
			this.fixTypeEverywhereTyped("EffectDurationItem", type, typed -> {
				if (typed.getOptional(opticFinder).filter(pair -> POTION_ITEMS.contains(pair.getSecond())).isPresent()) {
					Optional<? extends Typed<?>> optional = typed.getOptionalTyped(opticFinder2);
					if (optional.isPresent()) {
						Dynamic<?> dynamic = ((Typed)optional.get()).get(DSL.remainderFinder());
						Typed<?> typed2 = ((Typed)optional.get()).set(DSL.remainderFinder(), dynamic.update("CustomPotionEffects", this::fix));
						return typed.set(opticFinder2, typed2);
					}
				}

				return typed;
			})
		);
	}

	private Dynamic<?> fixEffect(Dynamic<?> dynamic) {
		return dynamic.update("FactorCalculationData", dynamic2 -> {
			int i = dynamic2.get("effect_changed_timestamp").asInt(-1);
			dynamic2 = dynamic2.remove("effect_changed_timestamp");
			int j = dynamic.get("Duration").asInt(-1);
			int k = i - j;
			return dynamic2.set("ticks_active", dynamic2.createInt(k));
		});
	}

	private Dynamic<?> fix(Dynamic<?> dynamic) {
		return dynamic.createList(dynamic.asStream().map(this::fixEffect));
	}

	private Dynamic<?> updateEntity(Dynamic<?> dynamic) {
		dynamic = dynamic.update("Effects", this::fix);
		dynamic = dynamic.update("ActiveEffects", this::fix);
		return dynamic.update("CustomPotionEffects", this::fix);
	}
}
