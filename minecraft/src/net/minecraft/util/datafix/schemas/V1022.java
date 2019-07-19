package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
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
		schema.registerType(false, References.RECIPE, () -> DSL.constType(DSL.namespacedString()));
		schema.registerType(
			false,
			References.PLAYER,
			() -> DSL.optionalFields(
					"RootVehicle",
					DSL.optionalFields("Entity", References.ENTITY_TREE.in(schema)),
					"Inventory",
					DSL.list(References.ITEM_STACK.in(schema)),
					"EnderItems",
					DSL.list(References.ITEM_STACK.in(schema)),
					DSL.optionalFields(
						"ShoulderEntityLeft",
						References.ENTITY_TREE.in(schema),
						"ShoulderEntityRight",
						References.ENTITY_TREE.in(schema),
						"recipeBook",
						DSL.optionalFields("recipes", DSL.list(References.RECIPE.in(schema)), "toBeDisplayed", DSL.list(References.RECIPE.in(schema)))
					)
				)
		);
		schema.registerType(false, References.HOTBAR, () -> DSL.compoundList(DSL.list(References.ITEM_STACK.in(schema))));
	}
}
