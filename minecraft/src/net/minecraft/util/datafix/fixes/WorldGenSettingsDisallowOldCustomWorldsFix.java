package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;

public class WorldGenSettingsDisallowOldCustomWorldsFix extends DataFix {
	public WorldGenSettingsDisallowOldCustomWorldsFix(Schema schema) {
		super(schema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		Type<?> type = this.getInputSchema().getType(References.WORLD_GEN_SETTINGS);
		OpticFinder<?> opticFinder = type.findField("dimensions");
		return this.fixTypeEverywhereTyped(
			"WorldGenSettingsDisallowOldCustomWorldsFix_" + this.getOutputSchema().getVersionKey(), type, typed -> typed.updateTyped(opticFinder, typedx -> {
					typedx.write().map(dynamic -> dynamic.getMapValues().map(map -> {
							map.forEach((dynamicx, dynamic2) -> {
								if (dynamic2.get("type").asString().result().isEmpty()) {
									throw new IllegalStateException("Unable load old custom worlds.");
								}
							});
							return map;
						}));
					return typedx;
				})
		);
	}
}
