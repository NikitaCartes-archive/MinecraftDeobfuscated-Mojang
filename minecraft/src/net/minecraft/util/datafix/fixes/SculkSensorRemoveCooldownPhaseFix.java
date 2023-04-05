package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class SculkSensorRemoveCooldownPhaseFix extends DataFix {
	public SculkSensorRemoveCooldownPhaseFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	private static Dynamic<?> fix(Dynamic<?> dynamic) {
		Optional<String> optional = dynamic.get("Name").asString().result();
		return !optional.equals(Optional.of("minecraft:sculk_sensor")) && !optional.equals(Optional.of("minecraft:calibrated_sculk_sensor"))
			? dynamic
			: dynamic.update("Properties", dynamicx -> {
				String string = dynamicx.get("sculk_sensor_phase").asString("");
				return string.equals("cooldown") ? dynamicx.set("sculk_sensor_phase", dynamicx.createString("inactive")) : dynamicx;
			});
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"sculk_sensor_remove_cooldown_phase_fix",
			this.getInputSchema().getType(References.BLOCK_STATE),
			typed -> typed.update(DSL.remainderFinder(), SculkSensorRemoveCooldownPhaseFix::fix)
		);
	}
}
