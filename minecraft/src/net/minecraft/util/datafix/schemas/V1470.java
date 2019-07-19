package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1470 extends NamespacedSchema {
	public V1470(int i, Schema schema) {
		super(i, schema);
	}

	protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
		schema.register(map, string, (Supplier<TypeTemplate>)(() -> V100.equipment(schema)));
	}

	@Override
	public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
		Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
		registerMob(schema, map, "minecraft:turtle");
		registerMob(schema, map, "minecraft:cod_mob");
		registerMob(schema, map, "minecraft:tropical_fish");
		registerMob(schema, map, "minecraft:salmon_mob");
		registerMob(schema, map, "minecraft:puffer_fish");
		registerMob(schema, map, "minecraft:phantom");
		registerMob(schema, map, "minecraft:dolphin");
		registerMob(schema, map, "minecraft:drowned");
		schema.register(map, "minecraft:trident", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(schema))));
		return map;
	}
}
