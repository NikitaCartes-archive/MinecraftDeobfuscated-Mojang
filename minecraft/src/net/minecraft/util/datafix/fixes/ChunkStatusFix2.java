package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Objects;

public class ChunkStatusFix2 extends DataFix {
	private static final Map<String, String> RENAMES_AND_DOWNGRADES = ImmutableMap.<String, String>builder()
		.put("structure_references", "empty")
		.put("biomes", "empty")
		.put("base", "surface")
		.put("carved", "carvers")
		.put("liquid_carved", "liquid_carvers")
		.put("decorated", "features")
		.put("lighted", "light")
		.put("mobs_spawned", "spawn")
		.put("finalized", "heightmaps")
		.put("fullchunk", "full")
		.build();

	public ChunkStatusFix2(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.CHUNK);
		Type<?> type2 = type.findFieldType("Level");
		OpticFinder<?> opticFinder = DSL.fieldFinder("Level", type2);
		return this.fixTypeEverywhereTyped(
			"ChunkStatusFix2", type, this.getOutputSchema().getType(References.CHUNK), typed -> typed.updateTyped(opticFinder, typedx -> {
					Dynamic<?> dynamic = typedx.get(DSL.remainderFinder());
					String string = dynamic.get("Status").asString("empty");
					String string2 = (String)RENAMES_AND_DOWNGRADES.getOrDefault(string, "empty");
					return Objects.equals(string, string2) ? typedx : typedx.set(DSL.remainderFinder(), dynamic.set("Status", dynamic.createString(string2)));
				})
		);
	}
}
