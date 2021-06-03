package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import java.util.Map;
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
		return TypeRewriteRule.seq(this.createStatRule(), this.createCriteriaRule());
	}

	private TypeRewriteRule createCriteriaRule() {
		Type<?> type = this.getOutputSchema().getType(References.OBJECTIVE);
		Type<?> type2 = this.getInputSchema().getType(References.OBJECTIVE);
		OpticFinder<?> opticFinder = type2.findField("CriteriaType");
		TaggedChoiceType<?> taggedChoiceType = (TaggedChoiceType<?>)opticFinder.type()
			.findChoiceType("type", -1)
			.orElseThrow(() -> new IllegalStateException("Can't find choice type for criteria"));
		Type<?> type3 = (Type<?>)taggedChoiceType.types().get("minecraft:custom");
		if (type3 == null) {
			throw new IllegalStateException("Failed to find custom criterion type variant");
		} else {
			OpticFinder<?> opticFinder2 = DSL.namedChoice("minecraft:custom", type3);
			OpticFinder<String> opticFinder3 = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
			return this.fixTypeEverywhereTyped(
				this.name,
				type2,
				type,
				typed -> typed.updateTyped(
						opticFinder,
						typedx -> typedx.updateTyped(opticFinder2, typedxx -> typedxx.update(opticFinder3, string -> (String)this.renames.getOrDefault(string, string)))
					)
			);
		}
	}

	private TypeRewriteRule createStatRule() {
		Type<?> type = this.getOutputSchema().getType(References.STATS);
		Type<?> type2 = this.getInputSchema().getType(References.STATS);
		OpticFinder<?> opticFinder = type2.findField("stats");
		OpticFinder<?> opticFinder2 = opticFinder.type().findField("minecraft:custom");
		OpticFinder<String> opticFinder3 = NamespacedSchema.namespacedString().finder();
		return this.fixTypeEverywhereTyped(
			this.name,
			type2,
			type,
			typed -> typed.updateTyped(
					opticFinder,
					typedx -> typedx.updateTyped(opticFinder2, typedxx -> typedxx.update(opticFinder3, string -> (String)this.renames.getOrDefault(string, string)))
				)
		);
	}
}
