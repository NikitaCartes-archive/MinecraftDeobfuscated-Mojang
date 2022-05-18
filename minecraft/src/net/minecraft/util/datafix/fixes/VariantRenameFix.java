package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;

public class VariantRenameFix extends NamedEntityFix {
	private final Map<String, String> renames;

	public VariantRenameFix(Schema schema, String string, TypeReference typeReference, String string2, Map<String, String> map) {
		super(schema, false, string, typeReference, string2);
		this.renames = map;
	}

	@Override
	protected Typed<?> fix(Typed<?> typed) {
		return typed.update(
			DSL.remainderFinder(),
			dynamic -> dynamic.update(
					"variant",
					dynamicx -> DataFixUtils.orElse(
							dynamicx.asString().map(string -> dynamicx.createString((String)this.renames.getOrDefault(string, string))).result(), dynamicx
						)
				)
		);
	}
}
