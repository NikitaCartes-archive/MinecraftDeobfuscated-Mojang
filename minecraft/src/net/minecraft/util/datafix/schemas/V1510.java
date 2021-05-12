package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V1510 extends NamespacedSchema {
	public V1510(int i, Schema schema) {
		super(i, schema);
	}

	@Override
	public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
		Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
		map.put("minecraft:command_block_minecart", (Supplier)map.remove("minecraft:commandblock_minecart"));
		map.put("minecraft:end_crystal", (Supplier)map.remove("minecraft:ender_crystal"));
		map.put("minecraft:snow_golem", (Supplier)map.remove("minecraft:snowman"));
		map.put("minecraft:evoker", (Supplier)map.remove("minecraft:evocation_illager"));
		map.put("minecraft:evoker_fangs", (Supplier)map.remove("minecraft:evocation_fangs"));
		map.put("minecraft:illusioner", (Supplier)map.remove("minecraft:illusion_illager"));
		map.put("minecraft:vindicator", (Supplier)map.remove("minecraft:vindication_illager"));
		map.put("minecraft:iron_golem", (Supplier)map.remove("minecraft:villager_golem"));
		map.put("minecraft:experience_orb", (Supplier)map.remove("minecraft:xp_orb"));
		map.put("minecraft:experience_bottle", (Supplier)map.remove("minecraft:xp_bottle"));
		map.put("minecraft:eye_of_ender", (Supplier)map.remove("minecraft:eye_of_ender_signal"));
		map.put("minecraft:firework_rocket", (Supplier)map.remove("minecraft:fireworks_rocket"));
		return map;
	}
}
