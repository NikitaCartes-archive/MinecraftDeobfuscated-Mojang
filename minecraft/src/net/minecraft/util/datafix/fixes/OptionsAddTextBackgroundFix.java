package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsAddTextBackgroundFix extends DataFix {
	public OptionsAddTextBackgroundFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"OptionsAddTextBackgroundFix",
			this.getInputSchema().getType(References.OPTIONS),
			typed -> typed.update(
					DSL.remainderFinder(),
					dynamic -> DataFixUtils.orElse(
							dynamic.get("chatOpacity")
								.asString()
								.map(string -> dynamic.set("textBackgroundOpacity", dynamic.createDouble(this.calculateBackground(string))))
								.result(),
							dynamic
						)
				)
		);
	}

	private double calculateBackground(String string) {
		try {
			double d = 0.9 * Double.parseDouble(string) + 0.1;
			return d / 2.0;
		} catch (NumberFormatException var4) {
			return 0.5;
		}
	}
}
