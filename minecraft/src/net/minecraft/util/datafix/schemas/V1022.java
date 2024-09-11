package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1022 extends Schema {
	public V1022(int i, Schema schema) {
		super(i, schema);
	}

	@Override
	public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
		super.registerTypes(schema, map, map2);
		schema.registerType(false, References.RECIPE, () -> DSL.constType(NamespacedSchema.namespacedString()));
		schema.registerType(
			false,
			References.PLAYER,
			() -> DSL.optionalFields(
					Pair.of("RootVehicle", DSL.optionalFields("Entity", References.ENTITY_TREE.in(schema))),
					Pair.of("ender_pearls", DSL.list(References.ENTITY_TREE.in(schema))),
					Pair.of("Inventory", DSL.list(References.ITEM_STACK.in(schema))),
					Pair.of("EnderItems", DSL.list(References.ITEM_STACK.in(schema))),
					Pair.of("ShoulderEntityLeft", References.ENTITY_TREE.in(schema)),
					Pair.of("ShoulderEntityRight", References.ENTITY_TREE.in(schema)),
					Pair.of("recipeBook", DSL.optionalFields("recipes", DSL.list(References.RECIPE.in(schema)), "toBeDisplayed", DSL.list(References.RECIPE.in(schema))))
				)
		);
		schema.registerType(false, References.HOTBAR, () -> DSL.compoundList(DSL.list(References.ITEM_STACK.in(schema))));
	}
}
