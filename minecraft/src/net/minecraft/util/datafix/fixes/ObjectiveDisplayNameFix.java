package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.ComponentDataFixUtils;

public class ObjectiveDisplayNameFix extends DataFix {
	public ObjectiveDisplayNameFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.OBJECTIVE);
		return this.fixTypeEverywhereTyped(
			"ObjectiveDisplayNameFix",
			type,
			typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("DisplayName", ComponentDataFixUtils::wrapLiteralStringAsComponent))
		);
	}
}
