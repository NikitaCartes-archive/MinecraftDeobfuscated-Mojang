package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityBlockStateFix extends DataFix {
	private static final Map<String, Integer> MAP = DataFixUtils.make(Maps.<String, Integer>newHashMap(), hashMap -> {
		hashMap.put("minecraft:air", 0);
		hashMap.put("minecraft:stone", 1);
		hashMap.put("minecraft:grass", 2);
		hashMap.put("minecraft:dirt", 3);
		hashMap.put("minecraft:cobblestone", 4);
		hashMap.put("minecraft:planks", 5);
		hashMap.put("minecraft:sapling", 6);
		hashMap.put("minecraft:bedrock", 7);
		hashMap.put("minecraft:flowing_water", 8);
		hashMap.put("minecraft:water", 9);
		hashMap.put("minecraft:flowing_lava", 10);
		hashMap.put("minecraft:lava", 11);
		hashMap.put("minecraft:sand", 12);
		hashMap.put("minecraft:gravel", 13);
		hashMap.put("minecraft:gold_ore", 14);
		hashMap.put("minecraft:iron_ore", 15);
		hashMap.put("minecraft:coal_ore", 16);
		hashMap.put("minecraft:log", 17);
		hashMap.put("minecraft:leaves", 18);
		hashMap.put("minecraft:sponge", 19);
		hashMap.put("minecraft:glass", 20);
		hashMap.put("minecraft:lapis_ore", 21);
		hashMap.put("minecraft:lapis_block", 22);
		hashMap.put("minecraft:dispenser", 23);
		hashMap.put("minecraft:sandstone", 24);
		hashMap.put("minecraft:noteblock", 25);
		hashMap.put("minecraft:bed", 26);
		hashMap.put("minecraft:golden_rail", 27);
		hashMap.put("minecraft:detector_rail", 28);
		hashMap.put("minecraft:sticky_piston", 29);
		hashMap.put("minecraft:web", 30);
		hashMap.put("minecraft:tallgrass", 31);
		hashMap.put("minecraft:deadbush", 32);
		hashMap.put("minecraft:piston", 33);
		hashMap.put("minecraft:piston_head", 34);
		hashMap.put("minecraft:wool", 35);
		hashMap.put("minecraft:piston_extension", 36);
		hashMap.put("minecraft:yellow_flower", 37);
		hashMap.put("minecraft:red_flower", 38);
		hashMap.put("minecraft:brown_mushroom", 39);
		hashMap.put("minecraft:red_mushroom", 40);
		hashMap.put("minecraft:gold_block", 41);
		hashMap.put("minecraft:iron_block", 42);
		hashMap.put("minecraft:double_stone_slab", 43);
		hashMap.put("minecraft:stone_slab", 44);
		hashMap.put("minecraft:brick_block", 45);
		hashMap.put("minecraft:tnt", 46);
		hashMap.put("minecraft:bookshelf", 47);
		hashMap.put("minecraft:mossy_cobblestone", 48);
		hashMap.put("minecraft:obsidian", 49);
		hashMap.put("minecraft:torch", 50);
		hashMap.put("minecraft:fire", 51);
		hashMap.put("minecraft:mob_spawner", 52);
		hashMap.put("minecraft:oak_stairs", 53);
		hashMap.put("minecraft:chest", 54);
		hashMap.put("minecraft:redstone_wire", 55);
		hashMap.put("minecraft:diamond_ore", 56);
		hashMap.put("minecraft:diamond_block", 57);
		hashMap.put("minecraft:crafting_table", 58);
		hashMap.put("minecraft:wheat", 59);
		hashMap.put("minecraft:farmland", 60);
		hashMap.put("minecraft:furnace", 61);
		hashMap.put("minecraft:lit_furnace", 62);
		hashMap.put("minecraft:standing_sign", 63);
		hashMap.put("minecraft:wooden_door", 64);
		hashMap.put("minecraft:ladder", 65);
		hashMap.put("minecraft:rail", 66);
		hashMap.put("minecraft:stone_stairs", 67);
		hashMap.put("minecraft:wall_sign", 68);
		hashMap.put("minecraft:lever", 69);
		hashMap.put("minecraft:stone_pressure_plate", 70);
		hashMap.put("minecraft:iron_door", 71);
		hashMap.put("minecraft:wooden_pressure_plate", 72);
		hashMap.put("minecraft:redstone_ore", 73);
		hashMap.put("minecraft:lit_redstone_ore", 74);
		hashMap.put("minecraft:unlit_redstone_torch", 75);
		hashMap.put("minecraft:redstone_torch", 76);
		hashMap.put("minecraft:stone_button", 77);
		hashMap.put("minecraft:snow_layer", 78);
		hashMap.put("minecraft:ice", 79);
		hashMap.put("minecraft:snow", 80);
		hashMap.put("minecraft:cactus", 81);
		hashMap.put("minecraft:clay", 82);
		hashMap.put("minecraft:reeds", 83);
		hashMap.put("minecraft:jukebox", 84);
		hashMap.put("minecraft:fence", 85);
		hashMap.put("minecraft:pumpkin", 86);
		hashMap.put("minecraft:netherrack", 87);
		hashMap.put("minecraft:soul_sand", 88);
		hashMap.put("minecraft:glowstone", 89);
		hashMap.put("minecraft:portal", 90);
		hashMap.put("minecraft:lit_pumpkin", 91);
		hashMap.put("minecraft:cake", 92);
		hashMap.put("minecraft:unpowered_repeater", 93);
		hashMap.put("minecraft:powered_repeater", 94);
		hashMap.put("minecraft:stained_glass", 95);
		hashMap.put("minecraft:trapdoor", 96);
		hashMap.put("minecraft:monster_egg", 97);
		hashMap.put("minecraft:stonebrick", 98);
		hashMap.put("minecraft:brown_mushroom_block", 99);
		hashMap.put("minecraft:red_mushroom_block", 100);
		hashMap.put("minecraft:iron_bars", 101);
		hashMap.put("minecraft:glass_pane", 102);
		hashMap.put("minecraft:melon_block", 103);
		hashMap.put("minecraft:pumpkin_stem", 104);
		hashMap.put("minecraft:melon_stem", 105);
		hashMap.put("minecraft:vine", 106);
		hashMap.put("minecraft:fence_gate", 107);
		hashMap.put("minecraft:brick_stairs", 108);
		hashMap.put("minecraft:stone_brick_stairs", 109);
		hashMap.put("minecraft:mycelium", 110);
		hashMap.put("minecraft:waterlily", 111);
		hashMap.put("minecraft:nether_brick", 112);
		hashMap.put("minecraft:nether_brick_fence", 113);
		hashMap.put("minecraft:nether_brick_stairs", 114);
		hashMap.put("minecraft:nether_wart", 115);
		hashMap.put("minecraft:enchanting_table", 116);
		hashMap.put("minecraft:brewing_stand", 117);
		hashMap.put("minecraft:cauldron", 118);
		hashMap.put("minecraft:end_portal", 119);
		hashMap.put("minecraft:end_portal_frame", 120);
		hashMap.put("minecraft:end_stone", 121);
		hashMap.put("minecraft:dragon_egg", 122);
		hashMap.put("minecraft:redstone_lamp", 123);
		hashMap.put("minecraft:lit_redstone_lamp", 124);
		hashMap.put("minecraft:double_wooden_slab", 125);
		hashMap.put("minecraft:wooden_slab", 126);
		hashMap.put("minecraft:cocoa", 127);
		hashMap.put("minecraft:sandstone_stairs", 128);
		hashMap.put("minecraft:emerald_ore", 129);
		hashMap.put("minecraft:ender_chest", 130);
		hashMap.put("minecraft:tripwire_hook", 131);
		hashMap.put("minecraft:tripwire", 132);
		hashMap.put("minecraft:emerald_block", 133);
		hashMap.put("minecraft:spruce_stairs", 134);
		hashMap.put("minecraft:birch_stairs", 135);
		hashMap.put("minecraft:jungle_stairs", 136);
		hashMap.put("minecraft:command_block", 137);
		hashMap.put("minecraft:beacon", 138);
		hashMap.put("minecraft:cobblestone_wall", 139);
		hashMap.put("minecraft:flower_pot", 140);
		hashMap.put("minecraft:carrots", 141);
		hashMap.put("minecraft:potatoes", 142);
		hashMap.put("minecraft:wooden_button", 143);
		hashMap.put("minecraft:skull", 144);
		hashMap.put("minecraft:anvil", 145);
		hashMap.put("minecraft:trapped_chest", 146);
		hashMap.put("minecraft:light_weighted_pressure_plate", 147);
		hashMap.put("minecraft:heavy_weighted_pressure_plate", 148);
		hashMap.put("minecraft:unpowered_comparator", 149);
		hashMap.put("minecraft:powered_comparator", 150);
		hashMap.put("minecraft:daylight_detector", 151);
		hashMap.put("minecraft:redstone_block", 152);
		hashMap.put("minecraft:quartz_ore", 153);
		hashMap.put("minecraft:hopper", 154);
		hashMap.put("minecraft:quartz_block", 155);
		hashMap.put("minecraft:quartz_stairs", 156);
		hashMap.put("minecraft:activator_rail", 157);
		hashMap.put("minecraft:dropper", 158);
		hashMap.put("minecraft:stained_hardened_clay", 159);
		hashMap.put("minecraft:stained_glass_pane", 160);
		hashMap.put("minecraft:leaves2", 161);
		hashMap.put("minecraft:log2", 162);
		hashMap.put("minecraft:acacia_stairs", 163);
		hashMap.put("minecraft:dark_oak_stairs", 164);
		hashMap.put("minecraft:slime", 165);
		hashMap.put("minecraft:barrier", 166);
		hashMap.put("minecraft:iron_trapdoor", 167);
		hashMap.put("minecraft:prismarine", 168);
		hashMap.put("minecraft:sea_lantern", 169);
		hashMap.put("minecraft:hay_block", 170);
		hashMap.put("minecraft:carpet", 171);
		hashMap.put("minecraft:hardened_clay", 172);
		hashMap.put("minecraft:coal_block", 173);
		hashMap.put("minecraft:packed_ice", 174);
		hashMap.put("minecraft:double_plant", 175);
		hashMap.put("minecraft:standing_banner", 176);
		hashMap.put("minecraft:wall_banner", 177);
		hashMap.put("minecraft:daylight_detector_inverted", 178);
		hashMap.put("minecraft:red_sandstone", 179);
		hashMap.put("minecraft:red_sandstone_stairs", 180);
		hashMap.put("minecraft:double_stone_slab2", 181);
		hashMap.put("minecraft:stone_slab2", 182);
		hashMap.put("minecraft:spruce_fence_gate", 183);
		hashMap.put("minecraft:birch_fence_gate", 184);
		hashMap.put("minecraft:jungle_fence_gate", 185);
		hashMap.put("minecraft:dark_oak_fence_gate", 186);
		hashMap.put("minecraft:acacia_fence_gate", 187);
		hashMap.put("minecraft:spruce_fence", 188);
		hashMap.put("minecraft:birch_fence", 189);
		hashMap.put("minecraft:jungle_fence", 190);
		hashMap.put("minecraft:dark_oak_fence", 191);
		hashMap.put("minecraft:acacia_fence", 192);
		hashMap.put("minecraft:spruce_door", 193);
		hashMap.put("minecraft:birch_door", 194);
		hashMap.put("minecraft:jungle_door", 195);
		hashMap.put("minecraft:acacia_door", 196);
		hashMap.put("minecraft:dark_oak_door", 197);
		hashMap.put("minecraft:end_rod", 198);
		hashMap.put("minecraft:chorus_plant", 199);
		hashMap.put("minecraft:chorus_flower", 200);
		hashMap.put("minecraft:purpur_block", 201);
		hashMap.put("minecraft:purpur_pillar", 202);
		hashMap.put("minecraft:purpur_stairs", 203);
		hashMap.put("minecraft:purpur_double_slab", 204);
		hashMap.put("minecraft:purpur_slab", 205);
		hashMap.put("minecraft:end_bricks", 206);
		hashMap.put("minecraft:beetroots", 207);
		hashMap.put("minecraft:grass_path", 208);
		hashMap.put("minecraft:end_gateway", 209);
		hashMap.put("minecraft:repeating_command_block", 210);
		hashMap.put("minecraft:chain_command_block", 211);
		hashMap.put("minecraft:frosted_ice", 212);
		hashMap.put("minecraft:magma", 213);
		hashMap.put("minecraft:nether_wart_block", 214);
		hashMap.put("minecraft:red_nether_brick", 215);
		hashMap.put("minecraft:bone_block", 216);
		hashMap.put("minecraft:structure_void", 217);
		hashMap.put("minecraft:observer", 218);
		hashMap.put("minecraft:white_shulker_box", 219);
		hashMap.put("minecraft:orange_shulker_box", 220);
		hashMap.put("minecraft:magenta_shulker_box", 221);
		hashMap.put("minecraft:light_blue_shulker_box", 222);
		hashMap.put("minecraft:yellow_shulker_box", 223);
		hashMap.put("minecraft:lime_shulker_box", 224);
		hashMap.put("minecraft:pink_shulker_box", 225);
		hashMap.put("minecraft:gray_shulker_box", 226);
		hashMap.put("minecraft:silver_shulker_box", 227);
		hashMap.put("minecraft:cyan_shulker_box", 228);
		hashMap.put("minecraft:purple_shulker_box", 229);
		hashMap.put("minecraft:blue_shulker_box", 230);
		hashMap.put("minecraft:brown_shulker_box", 231);
		hashMap.put("minecraft:green_shulker_box", 232);
		hashMap.put("minecraft:red_shulker_box", 233);
		hashMap.put("minecraft:black_shulker_box", 234);
		hashMap.put("minecraft:white_glazed_terracotta", 235);
		hashMap.put("minecraft:orange_glazed_terracotta", 236);
		hashMap.put("minecraft:magenta_glazed_terracotta", 237);
		hashMap.put("minecraft:light_blue_glazed_terracotta", 238);
		hashMap.put("minecraft:yellow_glazed_terracotta", 239);
		hashMap.put("minecraft:lime_glazed_terracotta", 240);
		hashMap.put("minecraft:pink_glazed_terracotta", 241);
		hashMap.put("minecraft:gray_glazed_terracotta", 242);
		hashMap.put("minecraft:silver_glazed_terracotta", 243);
		hashMap.put("minecraft:cyan_glazed_terracotta", 244);
		hashMap.put("minecraft:purple_glazed_terracotta", 245);
		hashMap.put("minecraft:blue_glazed_terracotta", 246);
		hashMap.put("minecraft:brown_glazed_terracotta", 247);
		hashMap.put("minecraft:green_glazed_terracotta", 248);
		hashMap.put("minecraft:red_glazed_terracotta", 249);
		hashMap.put("minecraft:black_glazed_terracotta", 250);
		hashMap.put("minecraft:concrete", 251);
		hashMap.put("minecraft:concrete_powder", 252);
		hashMap.put("minecraft:structure_block", 255);
	});

	public EntityBlockStateFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	public static int getBlockId(String string) {
		Integer integer = (Integer)MAP.get(string);
		return integer == null ? 0 : integer;
	}

	@Override
	public TypeRewriteRule makeRule() {
		Schema schema = this.getInputSchema();
		Schema schema2 = this.getOutputSchema();
		Function<Typed<?>, Typed<?>> function = typed -> this.updateBlockToBlockState(typed, "DisplayTile", "DisplayData", "DisplayState");
		Function<Typed<?>, Typed<?>> function2 = typed -> this.updateBlockToBlockState(typed, "inTile", "inData", "inBlockState");
		Type<Pair<Either<Pair<String, Either<Integer, String>>, Unit>, Dynamic<?>>> type = DSL.and(
			DSL.optional(DSL.field("inTile", DSL.named(References.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), NamespacedSchema.namespacedString())))),
			DSL.remainderType()
		);
		Function<Typed<?>, Typed<?>> function3 = typed -> typed.update(type.finder(), DSL.remainderType(), Pair::getSecond);
		return this.fixTypeEverywhereTyped("EntityBlockStateFix", schema.getType(References.ENTITY), schema2.getType(References.ENTITY), typed -> {
			typed = this.updateEntity(typed, "minecraft:falling_block", this::updateFallingBlock);
			typed = this.updateEntity(typed, "minecraft:enderman", typedx -> this.updateBlockToBlockState(typedx, "carried", "carriedData", "carriedBlockState"));
			typed = this.updateEntity(typed, "minecraft:arrow", function2);
			typed = this.updateEntity(typed, "minecraft:spectral_arrow", function2);
			typed = this.updateEntity(typed, "minecraft:egg", function3);
			typed = this.updateEntity(typed, "minecraft:ender_pearl", function3);
			typed = this.updateEntity(typed, "minecraft:fireball", function3);
			typed = this.updateEntity(typed, "minecraft:potion", function3);
			typed = this.updateEntity(typed, "minecraft:small_fireball", function3);
			typed = this.updateEntity(typed, "minecraft:snowball", function3);
			typed = this.updateEntity(typed, "minecraft:wither_skull", function3);
			typed = this.updateEntity(typed, "minecraft:xp_bottle", function3);
			typed = this.updateEntity(typed, "minecraft:commandblock_minecart", function);
			typed = this.updateEntity(typed, "minecraft:minecart", function);
			typed = this.updateEntity(typed, "minecraft:chest_minecart", function);
			typed = this.updateEntity(typed, "minecraft:furnace_minecart", function);
			typed = this.updateEntity(typed, "minecraft:tnt_minecart", function);
			typed = this.updateEntity(typed, "minecraft:hopper_minecart", function);
			return this.updateEntity(typed, "minecraft:spawner_minecart", function);
		});
	}

	private Typed<?> updateFallingBlock(Typed<?> typed) {
		Type<Either<Pair<String, Either<Integer, String>>, Unit>> type = DSL.optional(
			DSL.field("Block", DSL.named(References.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), NamespacedSchema.namespacedString())))
		);
		Type<Either<Pair<String, Dynamic<?>>, Unit>> type2 = DSL.optional(DSL.field("BlockState", DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType())));
		Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
		return typed.update(type.finder(), type2, either -> {
			int i = either.<Integer>map(pair -> ((Either)pair.getSecond()).map(integer -> integer, EntityBlockStateFix::getBlockId), unit -> {
				Optional<Number> optional = dynamic.get("TileID").asNumber().result();
				return (Integer)optional.map(Number::intValue).orElseGet(() -> dynamic.get("Tile").asByte((byte)0) & 0xFF);
			});
			int j = dynamic.get("Data").asInt(0) & 15;
			return Either.left(Pair.of(References.BLOCK_STATE.typeName(), BlockStateData.getTag(i << 4 | j)));
		}).set(DSL.remainderFinder(), dynamic.remove("Data").remove("TileID").remove("Tile"));
	}

	private Typed<?> updateBlockToBlockState(Typed<?> typed, String string, String string2, String string3) {
		Type<Pair<String, Either<Integer, String>>> type = DSL.field(
			string, DSL.named(References.BLOCK_NAME.typeName(), DSL.or(DSL.intType(), NamespacedSchema.namespacedString()))
		);
		Type<Pair<String, Dynamic<?>>> type2 = DSL.field(string3, DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType()));
		Dynamic<?> dynamic = typed.getOrCreate(DSL.remainderFinder());
		return typed.update(type.finder(), type2, pair -> {
			int i = ((Either)pair.getSecond()).<Integer>map(integer -> integer, EntityBlockStateFix::getBlockId);
			int j = dynamic.get(string2).asInt(0) & 15;
			return Pair.of(References.BLOCK_STATE.typeName(), BlockStateData.getTag(i << 4 | j));
		}).set(DSL.remainderFinder(), dynamic.remove(string2));
	}

	private Typed<?> updateEntity(Typed<?> typed, String string, Function<Typed<?>, Typed<?>> function) {
		Type<?> type = this.getInputSchema().getChoiceType(References.ENTITY, string);
		Type<?> type2 = this.getOutputSchema().getChoiceType(References.ENTITY, string);
		return typed.updateTyped(DSL.namedChoice(string, type), type2, function);
	}
}
