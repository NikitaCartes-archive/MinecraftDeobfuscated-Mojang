package net.minecraft.util.datafix.fixes;

import com.google.common.base.Splitter;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.ComponentDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemStackComponentizationFix extends DataFix {
	private static final int HIDE_ENCHANTMENTS = 1;
	private static final int HIDE_MODIFIERS = 2;
	private static final int HIDE_UNBREAKABLE = 4;
	private static final int HIDE_CAN_DESTROY = 8;
	private static final int HIDE_CAN_PLACE = 16;
	private static final int HIDE_ADDITIONAL = 32;
	private static final int HIDE_DYE = 64;
	private static final int HIDE_UPGRADES = 128;
	private static final Set<String> POTION_HOLDER_IDS = Set.of(
		"minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow"
	);
	private static final Set<String> BUCKETED_MOB_IDS = Set.of(
		"minecraft:pufferfish_bucket",
		"minecraft:salmon_bucket",
		"minecraft:cod_bucket",
		"minecraft:tropical_fish_bucket",
		"minecraft:axolotl_bucket",
		"minecraft:tadpole_bucket"
	);
	private static final List<String> BUCKETED_MOB_TAGS = List.of(
		"NoAI", "Silent", "NoGravity", "Glowing", "Invulnerable", "Health", "Age", "Variant", "HuntingCooldown", "BucketVariantTag"
	);
	private static final Set<String> BOOLEAN_BLOCK_STATE_PROPERTIES = Set.of(
		"attached",
		"bottom",
		"conditional",
		"disarmed",
		"drag",
		"enabled",
		"extended",
		"eye",
		"falling",
		"hanging",
		"has_bottle_0",
		"has_bottle_1",
		"has_bottle_2",
		"has_record",
		"has_book",
		"inverted",
		"in_wall",
		"lit",
		"locked",
		"occupied",
		"open",
		"persistent",
		"powered",
		"short",
		"signal_fire",
		"snowy",
		"triggered",
		"unstable",
		"waterlogged",
		"berries",
		"bloom",
		"shrieking",
		"can_summon",
		"up",
		"down",
		"north",
		"east",
		"south",
		"west",
		"slot_0_occupied",
		"slot_1_occupied",
		"slot_2_occupied",
		"slot_3_occupied",
		"slot_4_occupied",
		"slot_5_occupied",
		"cracked",
		"crafting"
	);
	private static final Splitter PROPERTY_SPLITTER = Splitter.on(',');

	public ItemStackComponentizationFix(Schema schema) {
		super(schema, true);
	}

	private static void fixItemStack(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic) {
		int i = itemStackData.removeTag("HideFlags").asInt(0);
		itemStackData.moveTagToComponent("Damage", "minecraft:damage", dynamic.createInt(0));
		itemStackData.moveTagToComponent("RepairCost", "minecraft:repair_cost", dynamic.createInt(0));
		itemStackData.moveTagToComponent("CustomModelData", "minecraft:custom_model_data");
		itemStackData.removeTag("BlockStateTag").result().ifPresent(dynamicx -> itemStackData.setComponent("minecraft:block_state", fixBlockStateTag(dynamicx)));
		itemStackData.moveTagToComponent("EntityTag", "minecraft:entity_data");
		itemStackData.fixSubTag("BlockEntityTag", false, dynamicx -> {
			String string = NamespacedSchema.ensureNamespaced(dynamicx.get("id").asString(""));
			dynamicx = fixBlockEntityTag(itemStackData, dynamicx, string);
			Dynamic<?> dynamic2 = dynamicx.remove("id");
			return dynamic2.equals(dynamicx.emptyMap()) ? dynamic2 : dynamicx;
		});
		itemStackData.moveTagToComponent("BlockEntityTag", "minecraft:block_entity_data");
		if (itemStackData.removeTag("Unbreakable").asBoolean(false)) {
			Dynamic<?> dynamic2 = dynamic.emptyMap();
			if ((i & 4) != 0) {
				dynamic2 = dynamic2.set("show_in_tooltip", dynamic.createBoolean(false));
			}

			itemStackData.setComponent("minecraft:unbreakable", dynamic2);
		}

		fixEnchantments(itemStackData, dynamic, "Enchantments", "minecraft:enchantments", (i & 1) != 0);
		if (itemStackData.is("minecraft:enchanted_book")) {
			fixEnchantments(itemStackData, dynamic, "StoredEnchantments", "minecraft:stored_enchantments", (i & 32) != 0);
		}

		itemStackData.fixSubTag("display", false, dynamicx -> fixDisplay(itemStackData, dynamicx, i));
		fixAdventureModeChecks(itemStackData, dynamic, i);
		fixAttributeModifiers(itemStackData, dynamic, i);
		Optional<? extends Dynamic<?>> optional = itemStackData.removeTag("Trim").result();
		if (optional.isPresent()) {
			Dynamic<?> dynamic3 = (Dynamic<?>)optional.get();
			if ((i & 128) != 0) {
				dynamic3 = dynamic3.set("show_in_tooltip", dynamic3.createBoolean(false));
			}

			itemStackData.setComponent("minecraft:trim", dynamic3);
		}

		if ((i & 32) != 0) {
			itemStackData.setComponent("minecraft:hide_additional_tooltip", dynamic.emptyMap());
		}

		if (itemStackData.is("minecraft:crossbow")) {
			itemStackData.removeTag("Charged");
			itemStackData.moveTagToComponent("ChargedProjectiles", "minecraft:charged_projectiles", dynamic.createList(Stream.empty()));
		}

		if (itemStackData.is("minecraft:bundle")) {
			itemStackData.moveTagToComponent("Items", "minecraft:bundle_contents", dynamic.createList(Stream.empty()));
		}

		if (itemStackData.is("minecraft:filled_map")) {
			itemStackData.moveTagToComponent("map", "minecraft:map_id");
			Map<? extends Dynamic<?>, ? extends Dynamic<?>> map = (Map<? extends Dynamic<?>, ? extends Dynamic<?>>)itemStackData.removeTag("Decorations")
				.asStream()
				.map(ItemStackComponentizationFix::fixMapDecoration)
				.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (dynamicx, dynamic2) -> dynamicx));
			if (!map.isEmpty()) {
				itemStackData.setComponent("minecraft:map_decorations", dynamic.createMap(map));
			}
		}

		if (itemStackData.is(POTION_HOLDER_IDS)) {
			fixPotionContents(itemStackData, dynamic);
		}

		if (itemStackData.is("minecraft:writable_book")) {
			fixWritableBook(itemStackData, dynamic);
		}

		if (itemStackData.is("minecraft:written_book")) {
			fixWrittenBook(itemStackData, dynamic);
		}

		if (itemStackData.is("minecraft:suspicious_stew")) {
			itemStackData.moveTagToComponent("effects", "minecraft:suspicious_stew_effects");
		}

		if (itemStackData.is("minecraft:debug_stick")) {
			itemStackData.moveTagToComponent("DebugProperty", "minecraft:debug_stick_state");
		}

		if (itemStackData.is(BUCKETED_MOB_IDS)) {
			fixBucketedMobData(itemStackData, dynamic);
		}

		if (itemStackData.is("minecraft:goat_horn")) {
			itemStackData.moveTagToComponent("instrument", "minecraft:instrument");
		}

		if (itemStackData.is("minecraft:knowledge_book")) {
			itemStackData.moveTagToComponent("Recipes", "minecraft:recipes");
		}

		if (itemStackData.is("minecraft:compass")) {
			fixLodestoneTracker(itemStackData, dynamic);
		}

		if (itemStackData.is("minecraft:firework_rocket")) {
			fixFireworkRocket(itemStackData);
		}

		if (itemStackData.is("minecraft:firework_star")) {
			fixFireworkStar(itemStackData);
		}

		if (itemStackData.is("minecraft:player_head")) {
			itemStackData.removeTag("SkullOwner").result().ifPresent(dynamicx -> itemStackData.setComponent("minecraft:profile", fixProfile(dynamicx)));
		}
	}

	private static Dynamic<?> fixBlockStateTag(Dynamic<?> dynamic) {
		return DataFixUtils.orElse(dynamic.asMapOpt().result().map(stream -> (Map)stream.collect(Collectors.toMap(Pair::getFirst, pair -> {
				String string = ((Dynamic)pair.getFirst()).asString("");
				Dynamic<?> dynamicx = (Dynamic<?>)pair.getSecond();
				if (BOOLEAN_BLOCK_STATE_PROPERTIES.contains(string)) {
					Optional<Boolean> optional = dynamicx.asBoolean().result();
					if (optional.isPresent()) {
						return dynamicx.createString(String.valueOf(optional.get()));
					}
				}

				Optional<Number> optional = dynamicx.asNumber().result();
				return optional.isPresent() ? dynamicx.createString(((Number)optional.get()).toString()) : dynamicx;
			}))).map(dynamic::createMap), dynamic);
	}

	private static Dynamic<?> fixDisplay(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic, int i) {
		itemStackData.setComponent("minecraft:custom_name", dynamic.get("Name"));
		itemStackData.setComponent("minecraft:lore", dynamic.get("Lore"));
		Optional<Integer> optional = dynamic.get("color").asNumber().result().map(Number::intValue);
		boolean bl = (i & 64) != 0;
		if (optional.isPresent() || bl) {
			Dynamic<?> dynamic2 = dynamic.emptyMap().set("rgb", dynamic.createInt((Integer)optional.orElse(10511680)));
			if (bl) {
				dynamic2 = dynamic2.set("show_in_tooltip", dynamic.createBoolean(false));
			}

			itemStackData.setComponent("minecraft:dyed_color", dynamic2);
		}

		Optional<String> optional2 = dynamic.get("LocName").asString().result();
		if (optional2.isPresent()) {
			itemStackData.setComponent("minecraft:item_name", ComponentDataFixUtils.createTranslatableComponent(dynamic.getOps(), (String)optional2.get()));
		}

		if (itemStackData.is("minecraft:filled_map")) {
			itemStackData.setComponent("minecraft:map_color", dynamic.get("MapColor"));
			dynamic = dynamic.remove("MapColor");
		}

		return dynamic.remove("Name").remove("Lore").remove("color").remove("LocName");
	}

	private static <T> Dynamic<T> fixBlockEntityTag(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<T> dynamic, String string) {
		itemStackData.setComponent("minecraft:lock", dynamic.get("Lock"));
		dynamic = dynamic.remove("Lock");
		Optional<Dynamic<T>> optional = dynamic.get("LootTable").result();
		if (optional.isPresent()) {
			Dynamic<T> dynamic2 = dynamic.emptyMap().set("loot_table", (Dynamic<?>)optional.get());
			long l = dynamic.get("LootTableSeed").asLong(0L);
			if (l != 0L) {
				dynamic2 = dynamic2.set("seed", dynamic.createLong(l));
			}

			itemStackData.setComponent("minecraft:container_loot", dynamic2);
			dynamic = dynamic.remove("LootTable").remove("LootTableSeed");
		}
		return switch (string) {
			case "minecraft:skull" -> {
				itemStackData.setComponent("minecraft:note_block_sound", dynamic.get("note_block_sound"));
				yield dynamic.remove("note_block_sound");
			}
			case "minecraft:decorated_pot" -> {
				itemStackData.setComponent("minecraft:pot_decorations", dynamic.get("sherds"));
				Optional<Dynamic<T>> optional2 = dynamic.get("item").result();
				if (optional2.isPresent()) {
					itemStackData.setComponent(
						"minecraft:container", dynamic.createList(Stream.of(dynamic.emptyMap().set("slot", dynamic.createInt(0)).set("item", (Dynamic<?>)optional2.get())))
					);
				}

				yield dynamic.remove("sherds").remove("item");
			}
			case "minecraft:banner" -> {
				itemStackData.setComponent("minecraft:banner_patterns", dynamic.get("patterns"));
				Optional<Number> optional2 = dynamic.get("Base").asNumber().result();
				if (optional2.isPresent()) {
					itemStackData.setComponent("minecraft:base_color", dynamic.createString(BannerPatternFormatFix.fixColor(((Number)optional2.get()).intValue())));
				}

				yield dynamic.remove("patterns").remove("Base");
			}
			case "minecraft:shulker_box", "minecraft:chest", "minecraft:trapped_chest", "minecraft:furnace", "minecraft:ender_chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:brewing_stand", "minecraft:hopper", "minecraft:barrel", "minecraft:smoker", "minecraft:blast_furnace", "minecraft:campfire", "minecraft:chiseled_bookshelf", "minecraft:crafter" -> {
				List<Dynamic<T>> list = dynamic.get("Items")
					.asList(dynamicx -> dynamicx.emptyMap().set("slot", dynamicx.createInt(dynamicx.get("Slot").asByte((byte)0) & 255)).set("item", dynamicx.remove("Slot")));
				if (!list.isEmpty()) {
					itemStackData.setComponent("minecraft:container", dynamic.createList(list.stream()));
				}

				yield dynamic.remove("Items");
			}
			case "minecraft:beehive" -> {
				itemStackData.setComponent("minecraft:bees", dynamic.get("bees"));
				yield dynamic.remove("bees");
			}
			default -> dynamic;
		};
	}

	private static void fixEnchantments(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic, String string, String string2, boolean bl) {
		OptionalDynamic<?> optionalDynamic = itemStackData.removeTag(string);
		List<Pair<String, Integer>> list = optionalDynamic.asList(Function.identity()).stream().flatMap(dynamicx -> parseEnchantment(dynamicx).stream()).toList();
		if (!list.isEmpty() || bl) {
			Dynamic<?> dynamic2 = dynamic.emptyMap();
			Dynamic<?> dynamic3 = dynamic.emptyMap();

			for (Pair<String, Integer> pair : list) {
				dynamic3 = dynamic3.set(pair.getFirst(), dynamic.createInt(pair.getSecond()));
			}

			dynamic2 = dynamic2.set("levels", dynamic3);
			if (bl) {
				dynamic2 = dynamic2.set("show_in_tooltip", dynamic.createBoolean(false));
			}

			itemStackData.setComponent(string2, dynamic2);
		}

		if (optionalDynamic.result().isPresent() && list.isEmpty()) {
			itemStackData.setComponent("minecraft:enchantment_glint_override", dynamic.createBoolean(true));
		}
	}

	private static Optional<Pair<String, Integer>> parseEnchantment(Dynamic<?> dynamic) {
		return dynamic.get("id")
			.asString()
			.<Number, Pair<String, Integer>>apply2stable((string, number) -> Pair.of(string, Mth.clamp(number.intValue(), 0, 255)), dynamic.get("lvl").asNumber())
			.result();
	}

	private static void fixAdventureModeChecks(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic, int i) {
		fixBlockStatePredicates(itemStackData, dynamic, "CanDestroy", "minecraft:can_break", (i & 8) != 0);
		fixBlockStatePredicates(itemStackData, dynamic, "CanPlaceOn", "minecraft:can_place_on", (i & 16) != 0);
	}

	private static void fixBlockStatePredicates(
		ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic, String string, String string2, boolean bl
	) {
		Optional<? extends Dynamic<?>> optional = itemStackData.removeTag(string).result();
		if (!optional.isEmpty()) {
			Dynamic<?> dynamic2 = dynamic.emptyMap()
				.set(
					"predicates",
					dynamic.createList(
						((Dynamic)optional.get())
							.asStream()
							.map(dynamicx -> DataFixUtils.orElse(dynamicx.asString().map(stringx -> fixBlockStatePredicate(dynamicx, stringx)).result(), dynamicx))
					)
				);
			if (bl) {
				dynamic2 = dynamic2.set("show_in_tooltip", dynamic.createBoolean(false));
			}

			itemStackData.setComponent(string2, dynamic2);
		}
	}

	private static Dynamic<?> fixBlockStatePredicate(Dynamic<?> dynamic, String string) {
		int i = string.indexOf(91);
		int j = string.indexOf(123);
		int k = string.length();
		if (i != -1) {
			k = i;
		}

		if (j != -1) {
			k = Math.min(k, j);
		}

		String string2 = string.substring(0, k);
		Dynamic<?> dynamic2 = dynamic.emptyMap().set("blocks", dynamic.createString(string2.trim()));
		int l = string.indexOf(93);
		if (i != -1 && l != -1) {
			Dynamic<?> dynamic3 = dynamic.emptyMap();

			for (String string3 : PROPERTY_SPLITTER.split(string.substring(i + 1, l))) {
				int m = string3.indexOf(61);
				if (m != -1) {
					String string4 = string3.substring(0, m).trim();
					String string5 = string3.substring(m + 1).trim();
					dynamic3 = dynamic3.set(string4, dynamic.createString(string5));
				}
			}

			dynamic2 = dynamic2.set("state", dynamic3);
		}

		int n = string.indexOf(125);
		if (j != -1 && n != -1) {
			dynamic2 = dynamic2.set("nbt", dynamic.createString(string.substring(j, n + 1)));
		}

		return dynamic2;
	}

	private static void fixAttributeModifiers(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic, int i) {
		OptionalDynamic<?> optionalDynamic = itemStackData.removeTag("AttributeModifiers");
		if (!optionalDynamic.result().isEmpty()) {
			boolean bl = (i & 2) != 0;
			List<? extends Dynamic<?>> list = optionalDynamic.asList(ItemStackComponentizationFix::fixAttributeModifier);
			Dynamic<?> dynamic2 = dynamic.emptyMap().set("modifiers", dynamic.createList(list.stream()));
			if (bl) {
				dynamic2 = dynamic2.set("show_in_tooltip", dynamic.createBoolean(false));
			}

			itemStackData.setComponent("minecraft:attribute_modifiers", dynamic2);
		}
	}

	private static Dynamic<?> fixAttributeModifier(Dynamic<?> dynamic) {
		Dynamic<?> dynamic2 = dynamic.emptyMap()
			.set("name", dynamic.createString(""))
			.set("amount", dynamic.createDouble(0.0))
			.set("operation", dynamic.createString("add_value"));
		dynamic2 = Dynamic.copyField(dynamic, "AttributeName", dynamic2, "type");
		dynamic2 = Dynamic.copyField(dynamic, "Slot", dynamic2, "slot");
		dynamic2 = Dynamic.copyField(dynamic, "UUID", dynamic2, "uuid");
		dynamic2 = Dynamic.copyField(dynamic, "Name", dynamic2, "name");
		dynamic2 = Dynamic.copyField(dynamic, "Amount", dynamic2, "amount");
		return Dynamic.copyAndFixField(dynamic, "Operation", dynamic2, "operation", dynamicx -> {
			return dynamicx.createString(switch (dynamicx.asInt(0)) {
				case 1 -> "add_multiplied_base";
				case 2 -> "add_multiplied_total";
				default -> "add_value";
			});
		});
	}

	private static Pair<Dynamic<?>, Dynamic<?>> fixMapDecoration(Dynamic<?> dynamic) {
		Dynamic<?> dynamic2 = DataFixUtils.orElseGet(dynamic.get("id").result(), () -> dynamic.createString(""));
		Dynamic<?> dynamic3 = dynamic.emptyMap()
			.set("type", dynamic.createString(fixMapDecorationType(dynamic.get("type").asInt(0))))
			.set("x", dynamic.createDouble(dynamic.get("x").asDouble(0.0)))
			.set("z", dynamic.createDouble(dynamic.get("z").asDouble(0.0)))
			.set("rotation", dynamic.createFloat((float)dynamic.get("rot").asDouble(0.0)));
		return Pair.of(dynamic2, dynamic3);
	}

	private static String fixMapDecorationType(int i) {
		return switch (i) {
			case 1 -> "frame";
			case 2 -> "red_marker";
			case 3 -> "blue_marker";
			case 4 -> "target_x";
			case 5 -> "target_point";
			case 6 -> "player_off_map";
			case 7 -> "player_off_limits";
			case 8 -> "mansion";
			case 9 -> "monument";
			case 10 -> "banner_white";
			case 11 -> "banner_orange";
			case 12 -> "banner_magenta";
			case 13 -> "banner_light_blue";
			case 14 -> "banner_yellow";
			case 15 -> "banner_lime";
			case 16 -> "banner_pink";
			case 17 -> "banner_gray";
			case 18 -> "banner_light_gray";
			case 19 -> "banner_cyan";
			case 20 -> "banner_purple";
			case 21 -> "banner_blue";
			case 22 -> "banner_brown";
			case 23 -> "banner_green";
			case 24 -> "banner_red";
			case 25 -> "banner_black";
			case 26 -> "red_x";
			case 27 -> "village_desert";
			case 28 -> "village_plains";
			case 29 -> "village_savanna";
			case 30 -> "village_snowy";
			case 31 -> "village_taiga";
			case 32 -> "jungle_temple";
			case 33 -> "swamp_hut";
			default -> "player";
		};
	}

	private static void fixPotionContents(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic) {
		Dynamic<?> dynamic2 = dynamic.emptyMap();
		Optional<String> optional = itemStackData.removeTag("Potion").asString().result().filter(string -> !string.equals("minecraft:empty"));
		if (optional.isPresent()) {
			dynamic2 = dynamic2.set("potion", dynamic.createString((String)optional.get()));
		}

		dynamic2 = itemStackData.moveTagInto("CustomPotionColor", dynamic2, "custom_color");
		dynamic2 = itemStackData.moveTagInto("custom_potion_effects", dynamic2, "custom_effects");
		if (!dynamic2.equals(dynamic.emptyMap())) {
			itemStackData.setComponent("minecraft:potion_contents", dynamic2);
		}
	}

	private static void fixWritableBook(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic) {
		Dynamic<?> dynamic2 = fixBookPages(itemStackData, dynamic);
		if (dynamic2 != null) {
			itemStackData.setComponent("minecraft:writable_book_content", dynamic.emptyMap().set("pages", dynamic2));
		}
	}

	private static void fixWrittenBook(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic) {
		Dynamic<?> dynamic2 = fixBookPages(itemStackData, dynamic);
		String string = itemStackData.removeTag("title").asString("");
		Optional<String> optional = itemStackData.removeTag("filtered_title").asString().result();
		Dynamic<?> dynamic3 = dynamic.emptyMap();
		dynamic3 = dynamic3.set("title", createFilteredText(dynamic, string, optional));
		dynamic3 = itemStackData.moveTagInto("author", dynamic3, "author");
		dynamic3 = itemStackData.moveTagInto("resolved", dynamic3, "resolved");
		dynamic3 = itemStackData.moveTagInto("generation", dynamic3, "generation");
		if (dynamic2 != null) {
			dynamic3 = dynamic3.set("pages", dynamic2);
		}

		itemStackData.setComponent("minecraft:written_book_content", dynamic3);
	}

	@Nullable
	private static Dynamic<?> fixBookPages(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic) {
		List<String> list = itemStackData.removeTag("pages").asList(dynamicx -> dynamicx.asString(""));
		Map<String, String> map = itemStackData.removeTag("filtered_pages").asMap(dynamicx -> dynamicx.asString("0"), dynamicx -> dynamicx.asString(""));
		if (list.isEmpty()) {
			return null;
		} else {
			List<Dynamic<?>> list2 = new ArrayList(list.size());

			for (int i = 0; i < list.size(); i++) {
				String string = (String)list.get(i);
				String string2 = (String)map.get(String.valueOf(i));
				list2.add(createFilteredText(dynamic, string, Optional.ofNullable(string2)));
			}

			return dynamic.createList(list2.stream());
		}
	}

	private static Dynamic<?> createFilteredText(Dynamic<?> dynamic, String string, Optional<String> optional) {
		Dynamic<?> dynamic2 = dynamic.emptyMap().set("raw", dynamic.createString(string));
		if (optional.isPresent()) {
			dynamic2 = dynamic2.set("filtered", dynamic.createString((String)optional.get()));
		}

		return dynamic2;
	}

	private static void fixBucketedMobData(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic) {
		Dynamic<?> dynamic2 = dynamic.emptyMap();

		for (String string : BUCKETED_MOB_TAGS) {
			dynamic2 = itemStackData.moveTagInto(string, dynamic2, string);
		}

		if (!dynamic2.equals(dynamic.emptyMap())) {
			itemStackData.setComponent("minecraft:bucket_entity_data", dynamic2);
		}
	}

	private static void fixLodestoneTracker(ItemStackComponentizationFix.ItemStackData itemStackData, Dynamic<?> dynamic) {
		Optional<? extends Dynamic<?>> optional = itemStackData.removeTag("LodestonePos").result();
		Optional<? extends Dynamic<?>> optional2 = itemStackData.removeTag("LodestoneDimension").result();
		if (!optional.isEmpty() || !optional2.isEmpty()) {
			boolean bl = itemStackData.removeTag("LodestoneTracked").asBoolean(true);
			Dynamic<?> dynamic2 = dynamic.emptyMap();
			if (optional.isPresent() && optional2.isPresent()) {
				dynamic2 = dynamic2.set("target", dynamic.emptyMap().set("pos", (Dynamic<?>)optional.get()).set("dimension", (Dynamic<?>)optional2.get()));
			}

			if (!bl) {
				dynamic2 = dynamic2.set("tracked", dynamic.createBoolean(false));
			}

			itemStackData.setComponent("minecraft:lodestone_tracker", dynamic2);
		}
	}

	private static void fixFireworkStar(ItemStackComponentizationFix.ItemStackData itemStackData) {
		itemStackData.fixSubTag("Explosion", true, dynamic -> {
			itemStackData.setComponent("minecraft:firework_explosion", fixFireworkExplosion(dynamic));
			return dynamic.remove("Type").remove("Colors").remove("FadeColors").remove("Trail").remove("Flicker");
		});
	}

	private static void fixFireworkRocket(ItemStackComponentizationFix.ItemStackData itemStackData) {
		itemStackData.fixSubTag(
			"Fireworks",
			true,
			dynamic -> {
				Stream<? extends Dynamic<?>> stream = dynamic.get("Explosions").asStream().map(ItemStackComponentizationFix::fixFireworkExplosion);
				int i = dynamic.get("Flight").asInt(0);
				itemStackData.setComponent(
					"minecraft:fireworks", dynamic.emptyMap().set("explosions", dynamic.createList(stream)).set("flight_duration", dynamic.createByte((byte)i))
				);
				return dynamic.remove("Explosions").remove("Flight");
			}
		);
	}

	private static Dynamic<?> fixFireworkExplosion(Dynamic<?> dynamic) {
		dynamic = dynamic.set("shape", dynamic.createString(switch (dynamic.get("Type").asInt(0)) {
			case 1 -> "large_ball";
			case 2 -> "star";
			case 3 -> "creeper";
			case 4 -> "burst";
			default -> "small_ball";
		})).remove("Type");
		dynamic = dynamic.renameField("Colors", "colors");
		dynamic = dynamic.renameField("FadeColors", "fade_colors");
		dynamic = dynamic.renameField("Trail", "has_trail");
		return dynamic.renameField("Flicker", "has_twinkle");
	}

	public static Dynamic<?> fixProfile(Dynamic<?> dynamic) {
		Optional<String> optional = dynamic.asString().result();
		if (optional.isPresent()) {
			return isValidPlayerName((String)optional.get()) ? dynamic.emptyMap().set("name", dynamic.createString((String)optional.get())) : dynamic.emptyMap();
		} else {
			String string = dynamic.get("Name").asString("");
			Optional<? extends Dynamic<?>> optional2 = dynamic.get("Id").result();
			Dynamic<?> dynamic2 = fixProfileProperties(dynamic.get("Properties"));
			Dynamic<?> dynamic3 = dynamic.emptyMap();
			if (isValidPlayerName(string)) {
				dynamic3 = dynamic3.set("name", dynamic.createString(string));
			}

			if (optional2.isPresent()) {
				dynamic3 = dynamic3.set("id", (Dynamic<?>)optional2.get());
			}

			if (dynamic2 != null) {
				dynamic3 = dynamic3.set("properties", dynamic2);
			}

			return dynamic3;
		}
	}

	private static boolean isValidPlayerName(String string) {
		return string.length() > 16 ? false : string.chars().filter(i -> i <= 32 || i >= 127).findAny().isEmpty();
	}

	@Nullable
	private static Dynamic<?> fixProfileProperties(OptionalDynamic<?> optionalDynamic) {
		Map<String, List<Pair<String, Optional<String>>>> map = optionalDynamic.asMap(dynamic -> dynamic.asString(""), dynamic -> dynamic.asList(dynamicx -> {
				String string = dynamicx.get("Value").asString("");
				Optional<String> optional = dynamicx.get("Signature").asString().result();
				return Pair.of(string, optional);
			}));
		return map.isEmpty()
			? null
			: optionalDynamic.createList(
				map.entrySet()
					.stream()
					.flatMap(
						entry -> ((List)entry.getValue())
								.stream()
								.map(
									pair -> {
										Dynamic<?> dynamic = optionalDynamic.emptyMap()
											.set("name", optionalDynamic.createString((String)entry.getKey()))
											.set("value", optionalDynamic.createString((String)pair.getFirst()));
										Optional<String> optional = (Optional<String>)pair.getSecond();
										return optional.isPresent() ? dynamic.set("signature", optionalDynamic.createString((String)optional.get())) : dynamic;
									}
								)
					)
			);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.writeFixAndRead(
			"ItemStack componentization", this.getInputSchema().getType(References.ITEM_STACK), this.getOutputSchema().getType(References.ITEM_STACK), dynamic -> {
				Optional<? extends Dynamic<?>> optional = ItemStackComponentizationFix.ItemStackData.read(dynamic).map(itemStackData -> {
					fixItemStack(itemStackData, itemStackData.tag);
					return itemStackData.write();
				});
				return DataFixUtils.orElse(optional, dynamic);
			}
		);
	}

	static class ItemStackData {
		private final String item;
		private final int count;
		private Dynamic<?> components;
		private final Dynamic<?> remainder;
		Dynamic<?> tag;

		private ItemStackData(String string, int i, Dynamic<?> dynamic) {
			this.item = NamespacedSchema.ensureNamespaced(string);
			this.count = i;
			this.components = dynamic.emptyMap();
			this.tag = dynamic.get("tag").orElseEmptyMap();
			this.remainder = dynamic.remove("tag");
		}

		public static Optional<ItemStackComponentizationFix.ItemStackData> read(Dynamic<?> dynamic) {
			return dynamic.get("id")
				.asString()
				.<Number, ItemStackComponentizationFix.ItemStackData>apply2stable(
					(string, number) -> new ItemStackComponentizationFix.ItemStackData(string, number.intValue(), dynamic.remove("id").remove("Count")),
					dynamic.get("Count").asNumber()
				)
				.result();
		}

		public OptionalDynamic<?> removeTag(String string) {
			OptionalDynamic<?> optionalDynamic = this.tag.get(string);
			this.tag = this.tag.remove(string);
			return optionalDynamic;
		}

		public void setComponent(String string, Dynamic<?> dynamic) {
			this.components = this.components.set(string, dynamic);
		}

		public void setComponent(String string, OptionalDynamic<?> optionalDynamic) {
			optionalDynamic.result().ifPresent(dynamic -> this.components = this.components.set(string, dynamic));
		}

		public Dynamic<?> moveTagInto(String string, Dynamic<?> dynamic, String string2) {
			Optional<? extends Dynamic<?>> optional = this.removeTag(string).result();
			return optional.isPresent() ? dynamic.set(string2, (Dynamic<?>)optional.get()) : dynamic;
		}

		public void moveTagToComponent(String string, String string2, Dynamic<?> dynamic) {
			Optional<? extends Dynamic<?>> optional = this.removeTag(string).result();
			if (optional.isPresent() && !((Dynamic)optional.get()).equals(dynamic)) {
				this.setComponent(string2, (Dynamic<?>)optional.get());
			}
		}

		public void moveTagToComponent(String string, String string2) {
			this.removeTag(string).result().ifPresent(dynamic -> this.setComponent(string2, dynamic));
		}

		public void fixSubTag(String string, boolean bl, UnaryOperator<Dynamic<?>> unaryOperator) {
			OptionalDynamic<?> optionalDynamic = this.tag.get(string);
			if (!bl || !optionalDynamic.result().isEmpty()) {
				Dynamic<?> dynamic = optionalDynamic.orElseEmptyMap();
				dynamic = (Dynamic<?>)unaryOperator.apply(dynamic);
				if (dynamic.equals(dynamic.emptyMap())) {
					this.tag = this.tag.remove(string);
				} else {
					this.tag = this.tag.set(string, dynamic);
				}
			}
		}

		public Dynamic<?> write() {
			Dynamic<?> dynamic = this.tag.emptyMap().set("id", this.tag.createString(this.item)).set("count", this.tag.createInt(this.count));
			if (!this.tag.equals(this.tag.emptyMap())) {
				this.components = this.components.set("minecraft:custom_data", this.tag);
			}

			if (!this.components.equals(this.tag.emptyMap())) {
				dynamic = dynamic.set("components", this.components);
			}

			return mergeRemainder(dynamic, this.remainder);
		}

		private static <T> Dynamic<T> mergeRemainder(Dynamic<T> dynamic, Dynamic<?> dynamic2) {
			DynamicOps<T> dynamicOps = dynamic.getOps();
			return (Dynamic<T>)dynamicOps.getMap(dynamic.getValue())
				.flatMap(mapLike -> dynamicOps.mergeToMap(dynamic2.convert(dynamicOps).getValue(), mapLike))
				.map(object -> new Dynamic<>(dynamicOps, (T)object))
				.result()
				.orElse(dynamic);
		}

		public boolean is(String string) {
			return this.item.equals(string);
		}

		public boolean is(Set<String> set) {
			return set.contains(this.item);
		}

		public boolean hasComponent(String string) {
			return this.components.get(string).result().isPresent();
		}
	}
}
