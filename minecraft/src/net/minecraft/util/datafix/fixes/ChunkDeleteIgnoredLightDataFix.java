package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class ChunkDeleteIgnoredLightDataFix extends DataFix {
	public ChunkDeleteIgnoredLightDataFix(Schema schema) {
		super(schema, true);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.CHUNK);
		OpticFinder<?> opticFinder = type.findField("sections");
		return this.fixTypeEverywhereTyped(
			"ChunkDeleteIgnoredLightDataFix",
			type,
			typed -> {
				boolean bl = typed.get(DSL.remainderFinder()).get("isLightOn").asBoolean(false);
				return !bl
					? typed.updateTyped(opticFinder, typedx -> typedx.update(DSL.remainderFinder(), dynamic -> dynamic.remove("BlockLight").remove("SkyLight")))
					: typed;
			}
		);
	}
}
