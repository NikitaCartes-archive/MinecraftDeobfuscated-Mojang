package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class OptionsProgrammerArtFix extends DataFix {
	public OptionsProgrammerArtFix(Schema schema) {
		super(schema, false);
	}

	@Override
	public TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"OptionsProgrammerArtFix",
			this.getInputSchema().getType(References.OPTIONS),
			typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("resourcePacks", this::fixList).update("incompatibleResourcePacks", this::fixList))
		);
	}

	private <T> Dynamic<T> fixList(Dynamic<T> dynamic) {
		return (Dynamic<T>)dynamic.asString().result().map(string -> dynamic.createString(string.replace("\"programer_art\"", "\"programmer_art\""))).orElse(dynamic);
	}
}
