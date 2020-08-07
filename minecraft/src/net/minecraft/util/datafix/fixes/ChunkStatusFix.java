package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class ChunkStatusFix extends DataFix {
	public ChunkStatusFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.CHUNK);
		Type<?> type2 = type.findFieldType("Level");
		OpticFinder<?> opticFinder = DSL.fieldFinder("Level", type2);
		return this.fixTypeEverywhereTyped(
			"ChunkStatusFix", type, this.getOutputSchema().getType(References.CHUNK), typed -> typed.updateTyped(opticFinder, typedx -> {
					Dynamic<?> dynamic = typedx.get(DSL.remainderFinder());
					String string = dynamic.get("Status").asString("empty");
					if (Objects.equals(string, "postprocessed")) {
						dynamic = dynamic.set("Status", dynamic.createString("fullchunk"));
					}

					return typedx.set(DSL.remainderFinder(), dynamic);
				})
		);
	}
}
