package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V102 extends Schema {
	public V102(int i, Schema schema) {
		super(i, schema);
	}

	@Override
	public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
		super.registerTypes(schema, map, map2);
		schema.registerType(
			true,
			References.ITEM_STACK,
			() -> DSL.hook(
					DSL.optionalFields(
						"id",
						References.ITEM_NAME.in(schema),
						"tag",
						DSL.optionalFields(
							"EntityTag",
							References.ENTITY_TREE.in(schema),
							"BlockEntityTag",
							References.BLOCK_ENTITY.in(schema),
							"CanDestroy",
							DSL.list(References.BLOCK_NAME.in(schema)),
							"CanPlaceOn",
							DSL.list(References.BLOCK_NAME.in(schema)),
							"Items",
							DSL.list(References.ITEM_STACK.in(schema))
						)
					),
					V99.ADD_NAMES,
					HookFunction.IDENTITY
				)
		);
	}
}
