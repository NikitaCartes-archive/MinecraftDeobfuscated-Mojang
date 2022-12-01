package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsAmbientOcclusionFix extends DataFix {
	public OptionsAmbientOcclusionFix(Schema schema) {
		super(schema, false);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"OptionsAmbientOcclusionFix",
			this.getInputSchema().getType(References.OPTIONS),
			typed -> typed.update(
					DSL.remainderFinder(),
					dynamic -> DataFixUtils.orElse(dynamic.get("ao").asString().map(string -> dynamic.set("ao", dynamic.createString(updateValue(string)))).result(), dynamic)
				)
		);
	}

	private static String updateValue(String string) {
		return switch (string) {
			case "0" -> "false";
			case "1", "2" -> "true";
			default -> string;
		};
	}
}
