package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class LegacyDragonFightFix extends DataFix {
	public LegacyDragonFightFix(Schema schema) {
		super(schema, false);
	}

	private static <T> Dynamic<T> fixDragonFight(Dynamic<T> dynamic) {
		return dynamic.update("ExitPortalLocation", ExtraDataFixUtils::fixBlockPos);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped(
			"LegacyDragonFightFix", this.getInputSchema().getType(References.LEVEL), typed -> typed.update(DSL.remainderFinder(), dynamic -> {
					OptionalDynamic<?> optionalDynamic = dynamic.get("DragonFight");
					if (optionalDynamic.result().isPresent()) {
						return dynamic;
					} else {
						Dynamic<?> dynamic2 = dynamic.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap();
						return dynamic.set("DragonFight", fixDragonFight(dynamic2));
					}
				})
		);
	}
}
