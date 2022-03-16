package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.SectionPos;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlendingDataFix extends DataFix {
	private final String name;
	private static final Set<String> STATUSES_TO_SKIP_BLENDING = Set.of(
		"minecraft:empty", "minecraft:structure_starts", "minecraft:structure_references", "minecraft:biomes"
	);

	public BlendingDataFix(Schema schema, String string) {
		super(schema, false);
		this.name = string;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getOutputSchema().getType(References.CHUNK);
		return this.fixTypeEverywhereTyped(this.name, type, typed -> typed.update(DSL.remainderFinder(), BlendingDataFix::updateChunkTag));
	}

	private static Dynamic<?> updateChunkTag(Dynamic<?> dynamic) {
		dynamic = dynamic.remove("blending_data");
		Optional<? extends Dynamic<?>> optional = dynamic.get("Status").result();
		if (optional.isPresent()) {
			String string = NamespacedSchema.ensureNamespaced(((Dynamic)optional.get()).asString("empty"));
			Optional<? extends Dynamic<?>> optional2 = dynamic.get("below_zero_retrogen").result();
			if (!STATUSES_TO_SKIP_BLENDING.contains(string)) {
				dynamic = updateBlendingData(dynamic, 384, -64);
			} else if (optional2.isPresent()) {
				Dynamic<?> dynamic2 = (Dynamic<?>)optional2.get();
				String string2 = NamespacedSchema.ensureNamespaced(dynamic2.get("target_status").asString("empty"));
				if (!STATUSES_TO_SKIP_BLENDING.contains(string2)) {
					dynamic = updateBlendingData(dynamic, 256, 0);
				}
			}
		}

		return dynamic;
	}

	private static Dynamic<?> updateBlendingData(Dynamic<?> dynamic, int i, int j) {
		return dynamic.set(
			"blending_data",
			dynamic.createMap(
				Map.of(
					dynamic.createString("min_section"),
					dynamic.createInt(SectionPos.blockToSectionCoord(j)),
					dynamic.createString("max_section"),
					dynamic.createInt(SectionPos.blockToSectionCoord(j + i))
				)
			)
		);
	}
}
