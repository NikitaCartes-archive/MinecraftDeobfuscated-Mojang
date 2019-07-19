package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V703 extends Schema {
	public V703(int i, Schema schema) {
		super(i, schema);
	}

	@Override
	public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
		Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
		map.remove("EntityHorse");
		schema.register(
			map,
			"Horse",
			(Supplier<TypeTemplate>)(() -> DSL.optionalFields(
					"ArmorItem", References.ITEM_STACK.in(schema), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)
				))
		);
		schema.register(
			map,
			"Donkey",
			(Supplier<TypeTemplate>)(() -> DSL.optionalFields(
					"Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)
				))
		);
		schema.register(
			map,
			"Mule",
			(Supplier<TypeTemplate>)(() -> DSL.optionalFields(
					"Items", DSL.list(References.ITEM_STACK.in(schema)), "SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)
				))
		);
		schema.register(
			map, "ZombieHorse", (Supplier<TypeTemplate>)(() -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)))
		);
		schema.register(
			map, "SkeletonHorse", (Supplier<TypeTemplate>)(() -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(schema), V100.equipment(schema)))
		);
		return map;
	}
}
