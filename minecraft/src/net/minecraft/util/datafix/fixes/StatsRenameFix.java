package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class StatsRenameFix extends DataFix {
	private final String name;
	private final Map<String, String> renames;

	public StatsRenameFix(Schema schema, String string, Map<String, String> map) {
		super(schema, false);
		this.name = string;
		this.renames = map;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getOutputSchema().getType(References.STATS);
		Type<?> type2 = this.getInputSchema().getType(References.STATS);
		OpticFinder<?> opticFinder = type2.findField("stats");
		OpticFinder<?> opticFinder2 = opticFinder.type().findField("minecraft:custom");
		OpticFinder<String> opticFinder3 = NamespacedSchema.namespacedString().finder();
		return this.fixTypeEverywhereTyped(
			this.name,
			type2,
			type,
			typed -> typed.updateTyped(opticFinder, typedx -> typedx.updateTyped(opticFinder2, typedxx -> typedxx.update(opticFinder3, string -> {
							for (Entry<String, String> entry : this.renames.entrySet()) {
								if (string.equals(entry.getKey())) {
									return (String)entry.getValue();
								}
							}

							return string;
						})))
		);
	}
}
