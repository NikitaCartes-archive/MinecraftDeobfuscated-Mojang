package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3818_3 extends NamespacedSchema {
	public V3818_3(int i, Schema schema) {
		super(i, schema);
	}

	public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema schema) {
		SequencedMap<String, Supplier<TypeTemplate>> sequencedMap = new LinkedHashMap();
		sequencedMap.put("minecraft:bees", (Supplier)() -> DSL.list(DSL.optionalFields("entity_data", References.ENTITY_TREE.in(schema))));
		sequencedMap.put("minecraft:block_entity_data", (Supplier)() -> References.BLOCK_ENTITY.in(schema));
		sequencedMap.put("minecraft:bundle_contents", (Supplier)() -> DSL.list(References.ITEM_STACK.in(schema)));
		sequencedMap.put(
			"minecraft:can_break",
			(Supplier)() -> DSL.optionalFields(
					"predicates", DSL.list(DSL.optionalFields("blocks", DSL.or(References.BLOCK_NAME.in(schema), DSL.list(References.BLOCK_NAME.in(schema)))))
				)
		);
		sequencedMap.put(
			"minecraft:can_place_on",
			(Supplier)() -> DSL.optionalFields(
					"predicates", DSL.list(DSL.optionalFields("blocks", DSL.or(References.BLOCK_NAME.in(schema), DSL.list(References.BLOCK_NAME.in(schema)))))
				)
		);
		sequencedMap.put("minecraft:charged_projectiles", (Supplier)() -> DSL.list(References.ITEM_STACK.in(schema)));
		sequencedMap.put("minecraft:container", (Supplier)() -> DSL.list(DSL.optionalFields("item", References.ITEM_STACK.in(schema))));
		sequencedMap.put("minecraft:entity_data", (Supplier)() -> References.ENTITY_TREE.in(schema));
		sequencedMap.put("minecraft:pot_decorations", (Supplier)() -> DSL.list(References.ITEM_NAME.in(schema)));
		sequencedMap.put("minecraft:food", (Supplier)() -> DSL.optionalFields("using_converts_to", References.ITEM_STACK.in(schema)));
		return sequencedMap;
	}

	@Override
	public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
		super.registerTypes(schema, map, map2);
		schema.registerType(true, References.DATA_COMPONENTS, () -> DSL.optionalFieldsLazy(components(schema)));
	}
}
