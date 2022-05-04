package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class GoatHornIdFix extends DataFix {
	private static final String[] INSTRUMENTS = new String[]{
		"minecraft:ponder_goat_horn",
		"minecraft:sing_goat_horn",
		"minecraft:seek_goat_horn",
		"minecraft:feel_goat_horn",
		"minecraft:admire_goat_horn",
		"minecraft:call_goat_horn",
		"minecraft:yearn_goat_horn",
		"minecraft:dream_goat_horn"
	};

	public GoatHornIdFix(Schema schema) {
		super(schema, false);
	}

	@Override
	public TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.ITEM_STACK);
		OpticFinder<Pair<String, String>> opticFinder = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
		OpticFinder<?> opticFinder2 = type.findField("tag");
		return this.fixTypeEverywhereTyped(
			"GoatHornIdFix",
			type,
			typed -> {
				Optional<Pair<String, String>> optional = typed.getOptional(opticFinder);
				return optional.isPresent() && Objects.equals(((Pair)optional.get()).getSecond(), "minecraft:goat_horn")
					? typed.updateTyped(opticFinder2, typedx -> typedx.update(DSL.remainderFinder(), dynamic -> {
							int i = dynamic.get("SoundVariant").asInt(0);
							String string = INSTRUMENTS[i >= 0 && i < INSTRUMENTS.length ? i : 0];
							return dynamic.remove("SoundVariant").set("instrument", dynamic.createString(string));
						}))
					: typed;
			}
		);
	}
}
