package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V3819 extends NamespacedSchema {
	public V3819(int i, Schema schema) {
		super(i, schema);
	}

	@Override
	public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
		Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
		schema.registerSimple(map, "minecraft:big_brain");
		schema.registerSimple(map, "minecraft:poisonous_potato_cutter");
		schema.registerSimple(map, "minecraft:fletching");
		schema.registerSimple(map, "minecraft:potato_refinery");
		return map;
	}

	@Override
	public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
		Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
		V1460.registerMob(schema, map, "minecraft:batato");
		V1460.registerMob(schema, map, "minecraft:toxifin");
		V1460.registerMob(schema, map, "minecraft:plaguewhale");
		V1460.registerMob(schema, map, "minecraft:poisonous_potato_zombie");
		V1460.registerMob(schema, map, "minecraft:mega_spud");
		schema.registerSimple(map, "minecraft:grid_carrier");
		schema.registerSimple(map, "minecraft:vine_projectile");
		schema.registerSimple(map, "minecraft:eye_of_potato");
		return map;
	}
}
