package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;

public class StructureSettingsFlattenFix extends DataFix {
	public StructureSettingsFlattenFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.WORLD_GEN_SETTINGS);
		OpticFinder<?> opticFinder = type.findField("dimensions");
		return this.fixTypeEverywhereTyped("StructureSettingsFlatten", type, typed -> typed.updateTyped(opticFinder, typedx -> {
				Dynamic<?> dynamic = (Dynamic<?>)typedx.write().result().orElseThrow();
				Dynamic<?> dynamic2 = dynamic.updateMapValues(StructureSettingsFlattenFix::fixDimension);
				return (Typed)((Pair)opticFinder.type().readTyped(dynamic2).result().orElseThrow()).getFirst();
			}));
	}

	private static Pair<Dynamic<?>, Dynamic<?>> fixDimension(Pair<Dynamic<?>, Dynamic<?>> pair) {
		Dynamic<?> dynamic = pair.getSecond();
		return Pair.of(
			pair.getFirst(),
			dynamic.update("generator", dynamicx -> dynamicx.update("settings", dynamicxx -> dynamicxx.update("structures", StructureSettingsFlattenFix::fixStructures)))
		);
	}

	private static Dynamic<?> fixStructures(Dynamic<?> dynamic) {
		Dynamic<?> dynamic2 = dynamic.get("structures")
			.orElseEmptyMap()
			.updateMapValues(pair -> pair.mapSecond(dynamic2x -> dynamic2x.set("type", dynamic.createString("minecraft:random_spread"))));
		return DataFixUtils.orElse(
			dynamic.get("stronghold")
				.result()
				.map(dynamic3 -> dynamic2.set("minecraft:stronghold", dynamic3.set("type", dynamic.createString("minecraft:concentric_rings")))),
			dynamic2
		);
	}
}
