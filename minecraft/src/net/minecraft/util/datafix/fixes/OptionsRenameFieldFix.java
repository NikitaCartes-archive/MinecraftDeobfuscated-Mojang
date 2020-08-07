package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsRenameFieldFix extends DataFix {
	private final String fixName;
	private final String fieldFrom;
	private final String fieldTo;

	public OptionsRenameFieldFix(Schema schema, boolean bl, String string, String string2, String string3) {
		super(schema, bl);
		this.fixName = string;
		this.fieldFrom = string2;
		this.fieldTo = string3;
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			this.fixName,
			this.getInputSchema().getType(References.OPTIONS),
			typed -> typed.update(
					DSL.remainderFinder(),
					dynamic -> DataFixUtils.orElse(dynamic.get(this.fieldFrom).result().map(dynamic2 -> dynamic.set(this.fieldTo, dynamic2).remove(this.fieldFrom)), dynamic)
				)
		);
	}
}
