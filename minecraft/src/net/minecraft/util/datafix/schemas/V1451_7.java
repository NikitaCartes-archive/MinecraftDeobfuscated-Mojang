package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1451_7 extends NamespacedSchema {
	public V1451_7(int i, Schema schema) {
		super(i, schema);
	}

	@Override
	public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
		super.registerTypes(schema, map, map2);
		schema.registerType(
			false,
			References.STRUCTURE_FEATURE,
			() -> DSL.optionalFields(
					"Children",
					DSL.list(
						DSL.optionalFields(
							"CA",
							References.BLOCK_STATE.in(schema),
							"CB",
							References.BLOCK_STATE.in(schema),
							"CC",
							References.BLOCK_STATE.in(schema),
							"CD",
							References.BLOCK_STATE.in(schema)
						)
					)
				)
		);
	}
}
