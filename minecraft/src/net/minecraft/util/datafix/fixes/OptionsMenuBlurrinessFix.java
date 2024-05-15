package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsMenuBlurrinessFix extends DataFix {
	public OptionsMenuBlurrinessFix(Schema schema) {
		super(schema, false);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"OptionsMenuBlurrinessFix",
			this.getInputSchema().getType(References.OPTIONS),
			typed -> typed.update(
					DSL.remainderFinder(),
					dynamic -> dynamic.update("menuBackgroundBlurriness", dynamicx -> dynamicx.createInt(this.convertToIntRange(dynamicx.asString("0.5"))))
				)
		);
	}

	private int convertToIntRange(String string) {
		try {
			return Math.round(Float.parseFloat(string) * 10.0F);
		} catch (NumberFormatException var3) {
			return 5;
		}
	}
}
