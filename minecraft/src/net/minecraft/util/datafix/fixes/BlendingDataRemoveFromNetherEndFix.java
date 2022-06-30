package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;

public class BlendingDataRemoveFromNetherEndFix extends DataFix {
	public BlendingDataRemoveFromNetherEndFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getOutputSchema().getType(References.CHUNK);
		return this.fixTypeEverywhereTyped(
			"BlendingDataRemoveFromNetherEndFix", type, typed -> typed.update(DSL.remainderFinder(), dynamic -> updateChunkTag(dynamic, dynamic.get("__context")))
		);
	}

	private static Dynamic<?> updateChunkTag(Dynamic<?> dynamic, OptionalDynamic<?> optionalDynamic) {
		boolean bl = "minecraft:overworld".equals(optionalDynamic.get("dimension").asString().result().orElse(""));
		return bl ? dynamic : dynamic.remove("blending_data");
	}
}
