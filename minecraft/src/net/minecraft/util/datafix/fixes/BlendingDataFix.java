package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
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

	public BlendingDataFix(Schema schema) {
		super(schema, false);
		this.name = "Blending Data Fix v" + schema.getVersionKey();
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getOutputSchema().getType(References.CHUNK);
		return this.fixTypeEverywhereTyped(
			this.name, type, typed -> typed.update(DSL.remainderFinder(), dynamic -> updateChunkTag(dynamic, dynamic.get("__context")))
		);
	}

	private static Dynamic<?> updateChunkTag(Dynamic<?> dynamic, OptionalDynamic<?> optionalDynamic) {
		dynamic = dynamic.remove("blending_data");
		boolean bl = "minecraft:overworld".equals(optionalDynamic.get("dimension").asString().result().orElse(""));
		Optional<? extends Dynamic<?>> optional = dynamic.get("Status").result();
		if (bl && optional.isPresent()) {
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
