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
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemWaterPotionFix extends DataFix {
	public ItemWaterPotionFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		OpticFinder<?> opticFinder2 = type.findField("tag");
		return this.fixTypeEverywhereTyped(
			"ItemWaterPotionFix",
			type,
			typed -> {
				Optional<Pair<String, String>> optional = typed.getOptional(opticFinder);
				if (optional.isPresent()) {
					String string = (String)((Pair)optional.get()).getSecond();
					if ("minecraft:potion".equals(string)
						|| "minecraft:splash_potion".equals(string)
						|| "minecraft:lingering_potion".equals(string)
						|| "minecraft:tipped_arrow".equals(string)) {
						Typed<?> typed2 = typed.getOrCreateTyped(opticFinder2);
						Dynamic<?> dynamic = typed2.get(DSL.remainderFinder());
						if (dynamic.get("Potion").asString().result().isEmpty()) {
							dynamic = dynamic.set("Potion", dynamic.createString("minecraft:water"));
						}

						return typed.set(opticFinder2, typed2.set(DSL.remainderFinder(), dynamic));
					}
				}

				return typed;
			}
		);
	}
}
