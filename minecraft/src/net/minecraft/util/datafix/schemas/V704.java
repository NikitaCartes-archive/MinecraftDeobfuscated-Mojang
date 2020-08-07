package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V704 extends Schema {
	protected static final Map<String, String> ITEM_TO_BLOCKENTITY = DataFixUtils.make(Maps.<String, String>newHashMap(), hashMap -> {
		hashMap.put("minecraft:furnace", "minecraft:furnace");
		hashMap.put("minecraft:lit_furnace", "minecraft:furnace");
		hashMap.put("minecraft:chest", "minecraft:chest");
		hashMap.put("minecraft:trapped_chest", "minecraft:chest");
		hashMap.put("minecraft:ender_chest", "minecraft:ender_chest");
		hashMap.put("minecraft:jukebox", "minecraft:jukebox");
		hashMap.put("minecraft:dispenser", "minecraft:dispenser");
		hashMap.put("minecraft:dropper", "minecraft:dropper");
		hashMap.put("minecraft:sign", "minecraft:sign");
		hashMap.put("minecraft:mob_spawner", "minecraft:mob_spawner");
		hashMap.put("minecraft:noteblock", "minecraft:noteblock");
		hashMap.put("minecraft:brewing_stand", "minecraft:brewing_stand");
		hashMap.put("minecraft:enhanting_table", "minecraft:enchanting_table");
		hashMap.put("minecraft:command_block", "minecraft:command_block");
		hashMap.put("minecraft:beacon", "minecraft:beacon");
		hashMap.put("minecraft:skull", "minecraft:skull");
		hashMap.put("minecraft:daylight_detector", "minecraft:daylight_detector");
		hashMap.put("minecraft:hopper", "minecraft:hopper");
		hashMap.put("minecraft:banner", "minecraft:banner");
		hashMap.put("minecraft:flower_pot", "minecraft:flower_pot");
		hashMap.put("minecraft:repeating_command_block", "minecraft:command_block");
		hashMap.put("minecraft:chain_command_block", "minecraft:command_block");
		hashMap.put("minecraft:shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:white_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:orange_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:magenta_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:light_blue_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:yellow_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:lime_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:pink_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:gray_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:silver_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:cyan_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:purple_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:blue_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:brown_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:green_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:red_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:black_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:bed", "minecraft:bed");
		hashMap.put("minecraft:light_gray_shulker_box", "minecraft:shulker_box");
		hashMap.put("minecraft:banner", "minecraft:banner");
		hashMap.put("minecraft:white_banner", "minecraft:banner");
		hashMap.put("minecraft:orange_banner", "minecraft:banner");
		hashMap.put("minecraft:magenta_banner", "minecraft:banner");
		hashMap.put("minecraft:light_blue_banner", "minecraft:banner");
		hashMap.put("minecraft:yellow_banner", "minecraft:banner");
		hashMap.put("minecraft:lime_banner", "minecraft:banner");
		hashMap.put("minecraft:pink_banner", "minecraft:banner");
		hashMap.put("minecraft:gray_banner", "minecraft:banner");
		hashMap.put("minecraft:silver_banner", "minecraft:banner");
		hashMap.put("minecraft:cyan_banner", "minecraft:banner");
		hashMap.put("minecraft:purple_banner", "minecraft:banner");
		hashMap.put("minecraft:blue_banner", "minecraft:banner");
		hashMap.put("minecraft:brown_banner", "minecraft:banner");
		hashMap.put("minecraft:green_banner", "minecraft:banner");
		hashMap.put("minecraft:red_banner", "minecraft:banner");
		hashMap.put("minecraft:black_banner", "minecraft:banner");
		hashMap.put("minecraft:standing_sign", "minecraft:sign");
		hashMap.put("minecraft:wall_sign", "minecraft:sign");
		hashMap.put("minecraft:piston_head", "minecraft:piston");
		hashMap.put("minecraft:daylight_detector_inverted", "minecraft:daylight_detector");
		hashMap.put("minecraft:unpowered_comparator", "minecraft:comparator");
		hashMap.put("minecraft:powered_comparator", "minecraft:comparator");
		hashMap.put("minecraft:wall_banner", "minecraft:banner");
		hashMap.put("minecraft:standing_banner", "minecraft:banner");
		hashMap.put("minecraft:structure_block", "minecraft:structure_block");
		hashMap.put("minecraft:end_portal", "minecraft:end_portal");
		hashMap.put("minecraft:end_gateway", "minecraft:end_gateway");
		hashMap.put("minecraft:sign", "minecraft:sign");
		hashMap.put("minecraft:shield", "minecraft:banner");
	});
	protected static final HookFunction ADD_NAMES = new HookFunction() {
		@Override
		public <T> T apply(DynamicOps<T> dynamicOps, T object) {
			return V99.addNames(new Dynamic<>(dynamicOps, object), V704.ITEM_TO_BLOCKENTITY, "ArmorStand");
		}
	};

	public V704(int i, Schema schema) {
		super(i, schema);
	}

	protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String string) {
		schema.register(map, string, (Supplier<TypeTemplate>)(() -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)))));
	}

	@Override
	public Type<?> getChoiceType(TypeReference typeReference, String string) {
		return Objects.equals(typeReference.typeName(), References.BLOCK_ENTITY.typeName())
			? super.getChoiceType(typeReference, NamespacedSchema.ensureNamespaced(string))
			: super.getChoiceType(typeReference, string);
	}

	@Override
	public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
		Map<String, Supplier<TypeTemplate>> map = Maps.<String, Supplier<TypeTemplate>>newHashMap();
		registerInventory(schema, map, "minecraft:furnace");
		registerInventory(schema, map, "minecraft:chest");
		schema.registerSimple(map, "minecraft:ender_chest");
		schema.register(map, "minecraft:jukebox", (Function<String, TypeTemplate>)(string -> DSL.optionalFields("RecordItem", References.ITEM_STACK.in(schema))));
		registerInventory(schema, map, "minecraft:dispenser");
		registerInventory(schema, map, "minecraft:dropper");
		schema.registerSimple(map, "minecraft:sign");
		schema.register(map, "minecraft:mob_spawner", (Function<String, TypeTemplate>)(string -> References.UNTAGGED_SPAWNER.in(schema)));
		schema.registerSimple(map, "minecraft:noteblock");
		schema.registerSimple(map, "minecraft:piston");
		registerInventory(schema, map, "minecraft:brewing_stand");
		schema.registerSimple(map, "minecraft:enchanting_table");
		schema.registerSimple(map, "minecraft:end_portal");
		schema.registerSimple(map, "minecraft:beacon");
		schema.registerSimple(map, "minecraft:skull");
		schema.registerSimple(map, "minecraft:daylight_detector");
		registerInventory(schema, map, "minecraft:hopper");
		schema.registerSimple(map, "minecraft:comparator");
		schema.register(
			map,
			"minecraft:flower_pot",
			(Function<String, TypeTemplate>)(string -> DSL.optionalFields("Item", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in(schema))))
		);
		schema.registerSimple(map, "minecraft:banner");
		schema.registerSimple(map, "minecraft:structure_block");
		schema.registerSimple(map, "minecraft:end_gateway");
		schema.registerSimple(map, "minecraft:command_block");
		return map;
	}

	@Override
	public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
		super.registerTypes(schema, map, map2);
		schema.registerType(false, References.BLOCK_ENTITY, () -> DSL.taggedChoiceLazy("id", NamespacedSchema.namespacedString(), map2));
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
							DSL.list(References.BLOCK_NAME.in(schema))
						)
					),
					ADD_NAMES,
					HookFunction.IDENTITY
				)
		);
	}
}
