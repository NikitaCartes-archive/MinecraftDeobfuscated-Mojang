package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class LevelUUIDFix extends AbstractUUIDFix {
	public LevelUUIDFix(Schema schema) {
		super(schema, References.LEVEL);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"LevelUUIDFix",
			this.getInputSchema().getType(this.typeReference),
			typed -> typed.updateTyped(DSL.remainderFinder(), typedx -> typedx.update(DSL.remainderFinder(), dynamic -> {
						dynamic = this.updateCustomBossEvents(dynamic);
						dynamic = this.updateDragonFight(dynamic);
						return this.updateWanderingTrader(dynamic);
					}))
		);
	}

	private Dynamic<?> updateWanderingTrader(Dynamic<?> dynamic) {
		return (Dynamic<?>)replaceUUIDString(dynamic, "WanderingTraderId", "WanderingTraderId").orElse(dynamic);
	}

	private Dynamic<?> updateDragonFight(Dynamic<?> dynamic) {
		return dynamic.update(
			"DimensionData",
			dynamicx -> dynamicx.updateMapValues(
					pair -> pair.mapSecond(
							dynamicxx -> dynamicxx.update("DragonFight", dynamicxxx -> (Dynamic)replaceUUIDLeastMost(dynamicxxx, "DragonUUID", "Dragon").orElse(dynamicxxx))
						)
				)
		);
	}

	private Dynamic<?> updateCustomBossEvents(Dynamic<?> dynamic) {
		return dynamic.update(
			"CustomBossEvents",
			dynamicx -> dynamicx.updateMapValues(
					pair -> pair.mapSecond(
							dynamicxx -> dynamicxx.update(
									"Players", dynamic2 -> dynamicxx.createList(dynamic2.asStream().map(dynamicxxxx -> (Dynamic)createUUIDFromML(dynamicxxxx).orElseGet(() -> {
												LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
												return dynamicxxxx;
											})))
								)
						)
				)
		);
	}
}
