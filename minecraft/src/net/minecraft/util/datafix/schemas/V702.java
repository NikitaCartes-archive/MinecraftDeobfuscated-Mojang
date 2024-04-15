package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V702 extends Schema {
	public V702(int i, Schema schema) {
		super(i, schema);
	}

	@Override
	public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
		Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
		schema.register(
			map,
			"ZombieVillager",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields(
					"Offers", DSL.optionalFields("Recipes", DSL.list(References.VILLAGER_TRADE.in(schema))), V100.equipment(schema)
				))
		);
		schema.register(map, "Husk", (Supplier<TypeTemplate>)(() -> V100.equipment(schema)));
		return map;
	}
}
