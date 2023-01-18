package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsAccessibilityOnboardFix extends DataFix {
	public OptionsAccessibilityOnboardFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"OptionsAccessibilityOnboardFix",
			this.getInputSchema().getType(References.OPTIONS),
			typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.set("onboardAccessibility", dynamic.createBoolean(false)))
		);
	}
}
