package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class SwimStatsRenameFix extends DataFix {
	public SwimStatsRenameFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getOutputSchema().getType(References.STATS);
		Type<?> type2 = this.getInputSchema().getType(References.STATS);
		OpticFinder<?> opticFinder = type2.findField("stats");
		OpticFinder<?> opticFinder2 = opticFinder.type().findField("minecraft:custom");
		OpticFinder<String> opticFinder3 = NamespacedSchema.namespacedString().finder();
		return this.fixTypeEverywhereTyped(
			"SwimStatsRenameFix",
			type2,
			type,
			typed -> typed.updateTyped(opticFinder, typedx -> typedx.updateTyped(opticFinder2, typedxx -> typedxx.update(opticFinder3, string -> {
							if (string.equals("minecraft:swim_one_cm")) {
								return "minecraft:walk_on_water_one_cm";
							} else {
								return string.equals("minecraft:dive_one_cm") ? "minecraft:walk_under_water_one_cm" : string;
							}
						})))
		);
	}
}
