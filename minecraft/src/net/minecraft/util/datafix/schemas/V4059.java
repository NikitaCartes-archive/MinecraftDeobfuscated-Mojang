package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V4059 extends NamespacedSchema {
	public V4059(int i, Schema schema) {
		super(i, schema);
	}

	public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema schema) {
		SequencedMap<String, Supplier<TypeTemplate>> sequencedMap = V3818_3.components(schema);
		sequencedMap.remove("minecraft:food");
		sequencedMap.put("minecraft:use_remainder", (Supplier)() -> References.ITEM_STACK.in(schema));
		sequencedMap.put(
			"minecraft:equippable",
			(Supplier)() -> DSL.optionalFields("allowed_entities", DSL.or(References.ENTITY_NAME.in(schema), DSL.list(References.ENTITY_NAME.in(schema))))
		);
		return sequencedMap;
	}

	@Override
	public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
		super.registerTypes(schema, map, map2);
		schema.registerType(true, References.DATA_COMPONENTS, () -> DSL.optionalFieldsLazy(components(schema)));
	}
}
