package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2501 extends NamespacedSchema {
	public V2501(int i, Schema schema) {
		super(i, schema);
	}

	private static void registerFurnace(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
		schema.register(
			map,
			string,
			(Supplier<TypeTemplate>)(() -> DSL.optionalFields(
					"Items", DSL.list(References.ITEM_STACK.in(schema)), "RecipesUsed", DSL.compoundList(References.RECIPE.in(schema), DSL.constType(DSL.intType()))
				))
		);
	}

	@Override
	public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
		Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
		registerFurnace(schema, map, "minecraft:furnace");
		registerFurnace(schema, map, "minecraft:smoker");
		registerFurnace(schema, map, "minecraft:blast_furnace");
		return map;
	}

	@Override
	public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
		super.registerTypes(schema, map, map2);
		Map<String, Supplier<TypeTemplate>> map3 = ImmutableMap.<String, Supplier<TypeTemplate>>builder()
			.put("default", DSL::remainder)
			.put("largeBiomes", DSL::remainder)
			.put("amplified", DSL::remainder)
			.put("customized", DSL::remainder)
			.put("debug_all_block_states", DSL::remainder)
			.put("default_1_1", DSL::remainder)
			.put(
				"flat", () -> DSL.optionalFields("biome", References.BIOME.in(schema), "layers", DSL.list(DSL.optionalFields("block", References.BLOCK_NAME.in(schema))))
			)
			.put(
				"buffet",
				() -> DSL.optionalFields(
						"biome_source",
						DSL.optionalFields("options", DSL.optionalFields("biomes", DSL.list(References.BIOME.in(schema)))),
						"chunk_generator",
						DSL.optionalFields("options", DSL.optionalFields("default_block", References.BLOCK_NAME.in(schema), "default_fluid", References.BLOCK_NAME.in(schema)))
					)
			)
			.build();
		schema.registerType(false, References.CHUNK_GENERATOR_SETTINGS, () -> DSL.taggedChoiceLazy("levelType", DSL.string(), map3));
	}
}
