package net.minecraft.client.model.geom;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.state.properties.WoodType;

@Environment(EnvType.CLIENT)
public class ModelLayers {
	private static final String DEFAULT_LAYER = "main";
	private static final Set<ModelLayerLocation> ALL_MODELS = Sets.<ModelLayerLocation>newHashSet();
	public static final ModelLayerLocation ALLAY = register("allay");
	public static final ModelLayerLocation ARMADILLO = register("armadillo");
	public static final ModelLayerLocation ARMADILLO_BABY = register("armadillo_baby");
	public static final ModelLayerLocation ARMOR_STAND = register("armor_stand");
	public static final ModelLayerLocation ARMOR_STAND_INNER_ARMOR = registerInnerArmor("armor_stand");
	public static final ModelLayerLocation ARMOR_STAND_OUTER_ARMOR = registerOuterArmor("armor_stand");
	public static final ModelLayerLocation ARMOR_STAND_SMALL = register("armor_stand_small");
	public static final ModelLayerLocation ARMOR_STAND_SMALL_INNER_ARMOR = registerInnerArmor("armor_stand_small");
	public static final ModelLayerLocation ARMOR_STAND_SMALL_OUTER_ARMOR = registerOuterArmor("armor_stand_small");
	public static final ModelLayerLocation ARROW = register("arrow");
	public static final ModelLayerLocation AXOLOTL = register("axolotl");
	public static final ModelLayerLocation AXOLOTL_BABY = register("axolotl_baby");
	public static final ModelLayerLocation BANNER = register("banner");
	public static final ModelLayerLocation BAT = register("bat");
	public static final ModelLayerLocation BED_FOOT = register("bed_foot");
	public static final ModelLayerLocation BED_HEAD = register("bed_head");
	public static final ModelLayerLocation BEE = register("bee");
	public static final ModelLayerLocation BEE_BABY = register("bee_baby");
	public static final ModelLayerLocation BEE_STINGER = register("bee_stinger");
	public static final ModelLayerLocation BELL = register("bell");
	public static final ModelLayerLocation BLAZE = register("blaze");
	public static final ModelLayerLocation BOAT_WATER_PATCH = register("boat", "water_patch");
	public static final ModelLayerLocation BOGGED = register("bogged");
	public static final ModelLayerLocation BOGGED_INNER_ARMOR = registerInnerArmor("bogged");
	public static final ModelLayerLocation BOGGED_OUTER_ARMOR = registerOuterArmor("bogged");
	public static final ModelLayerLocation BOGGED_OUTER_LAYER = register("bogged", "outer");
	public static final ModelLayerLocation BOOK = register("book");
	public static final ModelLayerLocation BREEZE = register("breeze");
	public static final ModelLayerLocation BREEZE_WIND = register("breeze_wind");
	public static final ModelLayerLocation CAT = register("cat");
	public static final ModelLayerLocation CAT_BABY = register("cat_baby");
	public static final ModelLayerLocation CAT_COLLAR = register("cat", "collar");
	public static final ModelLayerLocation CAT_BABY_COLLAR = register("cat_baby", "collar");
	public static final ModelLayerLocation CAMEL = register("camel");
	public static final ModelLayerLocation CAMEL_BABY = register("camel_baby");
	public static final ModelLayerLocation CAVE_SPIDER = register("cave_spider");
	public static final ModelLayerLocation CHEST = register("chest");
	public static final ModelLayerLocation CHEST_MINECART = register("chest_minecart");
	public static final ModelLayerLocation CHICKEN = register("chicken");
	public static final ModelLayerLocation CHICKEN_BABY = register("chicken_baby");
	public static final ModelLayerLocation COD = register("cod");
	public static final ModelLayerLocation COMMAND_BLOCK_MINECART = register("command_block_minecart");
	public static final ModelLayerLocation CONDUIT_CAGE = register("conduit", "cage");
	public static final ModelLayerLocation CONDUIT_EYE = register("conduit", "eye");
	public static final ModelLayerLocation CONDUIT_SHELL = register("conduit", "shell");
	public static final ModelLayerLocation CONDUIT_WIND = register("conduit", "wind");
	public static final ModelLayerLocation COW = register("cow");
	public static final ModelLayerLocation COW_BABY = register("cow_baby");
	public static final ModelLayerLocation CREEPER = register("creeper");
	public static final ModelLayerLocation CREEPER_ARMOR = register("creeper", "armor");
	public static final ModelLayerLocation CREEPER_HEAD = register("creeper_head");
	public static final ModelLayerLocation DECORATED_POT_BASE = register("decorated_pot_base");
	public static final ModelLayerLocation DECORATED_POT_SIDES = register("decorated_pot_sides");
	public static final ModelLayerLocation DOLPHIN = register("dolphin");
	public static final ModelLayerLocation DOLPHIN_BABY = register("dolphin_baby");
	public static final ModelLayerLocation DONKEY = register("donkey");
	public static final ModelLayerLocation DONKEY_BABY = register("donkey_baby");
	public static final ModelLayerLocation DOUBLE_CHEST_LEFT = register("double_chest_left");
	public static final ModelLayerLocation DOUBLE_CHEST_RIGHT = register("double_chest_right");
	public static final ModelLayerLocation DRAGON_SKULL = register("dragon_skull");
	public static final ModelLayerLocation DROWNED = register("drowned");
	public static final ModelLayerLocation DROWNED_INNER_ARMOR = registerInnerArmor("drowned");
	public static final ModelLayerLocation DROWNED_OUTER_ARMOR = registerOuterArmor("drowned");
	public static final ModelLayerLocation DROWNED_OUTER_LAYER = register("drowned", "outer");
	public static final ModelLayerLocation DROWNED_BABY = register("drowned_baby");
	public static final ModelLayerLocation DROWNED_BABY_INNER_ARMOR = registerInnerArmor("drowned_baby");
	public static final ModelLayerLocation DROWNED_BABY_OUTER_ARMOR = registerOuterArmor("drowned_baby");
	public static final ModelLayerLocation DROWNED_BABY_OUTER_LAYER = register("drowned_baby", "outer");
	public static final ModelLayerLocation ELDER_GUARDIAN = register("elder_guardian");
	public static final ModelLayerLocation ELYTRA = register("elytra");
	public static final ModelLayerLocation ELYTRA_BABY = register("elytra_baby");
	public static final ModelLayerLocation ENDERMAN = register("enderman");
	public static final ModelLayerLocation ENDERMITE = register("endermite");
	public static final ModelLayerLocation ENDER_DRAGON = register("ender_dragon");
	public static final ModelLayerLocation END_CRYSTAL = register("end_crystal");
	public static final ModelLayerLocation EVOKER = register("evoker");
	public static final ModelLayerLocation EVOKER_FANGS = register("evoker_fangs");
	public static final ModelLayerLocation FOX = register("fox");
	public static final ModelLayerLocation FOX_BABY = register("fox_baby");
	public static final ModelLayerLocation FROG = register("frog");
	public static final ModelLayerLocation FURNACE_MINECART = register("furnace_minecart");
	public static final ModelLayerLocation GHAST = register("ghast");
	public static final ModelLayerLocation GIANT = register("giant");
	public static final ModelLayerLocation GIANT_INNER_ARMOR = registerInnerArmor("giant");
	public static final ModelLayerLocation GIANT_OUTER_ARMOR = registerOuterArmor("giant");
	public static final ModelLayerLocation GLOW_SQUID = register("glow_squid");
	public static final ModelLayerLocation GLOW_SQUID_BABY = register("glow_squid_baby");
	public static final ModelLayerLocation GOAT = register("goat");
	public static final ModelLayerLocation GOAT_BABY = register("goat_baby");
	public static final ModelLayerLocation GUARDIAN = register("guardian");
	public static final ModelLayerLocation HOGLIN = register("hoglin");
	public static final ModelLayerLocation HOGLIN_BABY = register("hoglin_baby");
	public static final ModelLayerLocation HOPPER_MINECART = register("hopper_minecart");
	public static final ModelLayerLocation HORSE = register("horse");
	public static final ModelLayerLocation HORSE_BABY = register("horse_baby");
	public static final ModelLayerLocation HORSE_ARMOR = register("horse_armor");
	public static final ModelLayerLocation HORSE_BABY_ARMOR = register("horse_armor_baby");
	public static final ModelLayerLocation HUSK = register("husk");
	public static final ModelLayerLocation HUSK_INNER_ARMOR = registerInnerArmor("husk");
	public static final ModelLayerLocation HUSK_OUTER_ARMOR = registerOuterArmor("husk");
	public static final ModelLayerLocation HUSK_BABY = register("husk_baby");
	public static final ModelLayerLocation HUSK_BABY_INNER_ARMOR = registerInnerArmor("husk_baby");
	public static final ModelLayerLocation HUSK_BABY_OUTER_ARMOR = registerOuterArmor("husk_baby");
	public static final ModelLayerLocation ILLUSIONER = register("illusioner");
	public static final ModelLayerLocation IRON_GOLEM = register("iron_golem");
	public static final ModelLayerLocation LEASH_KNOT = register("leash_knot");
	public static final ModelLayerLocation LLAMA = register("llama");
	public static final ModelLayerLocation LLAMA_BABY = register("llama_baby");
	public static final ModelLayerLocation LLAMA_DECOR = register("llama", "decor");
	public static final ModelLayerLocation LLAMA_BABY_DECOR = register("llama_baby", "decor");
	public static final ModelLayerLocation LLAMA_SPIT = register("llama_spit");
	public static final ModelLayerLocation MAGMA_CUBE = register("magma_cube");
	public static final ModelLayerLocation MINECART = register("minecart");
	public static final ModelLayerLocation MOOSHROOM = register("mooshroom");
	public static final ModelLayerLocation MOOSHROOM_BABY = register("mooshroom_baby");
	public static final ModelLayerLocation MULE = register("mule");
	public static final ModelLayerLocation MULE_BABY = register("mule_baby");
	public static final ModelLayerLocation OCELOT = register("ocelot");
	public static final ModelLayerLocation OCELOT_BABY = register("ocelot_baby");
	public static final ModelLayerLocation PANDA = register("panda");
	public static final ModelLayerLocation PANDA_BABY = register("panda_baby");
	public static final ModelLayerLocation PARROT = register("parrot");
	public static final ModelLayerLocation PHANTOM = register("phantom");
	public static final ModelLayerLocation PIG = register("pig");
	public static final ModelLayerLocation PIG_BABY = register("pig_baby");
	public static final ModelLayerLocation PIG_SADDLE = register("pig", "saddle");
	public static final ModelLayerLocation PIG_BABY_SADDLE = register("pig_baby", "saddle");
	public static final ModelLayerLocation PIGLIN = register("piglin");
	public static final ModelLayerLocation PIGLIN_BRUTE = register("piglin_brute");
	public static final ModelLayerLocation PIGLIN_BRUTE_INNER_ARMOR = registerInnerArmor("piglin_brute");
	public static final ModelLayerLocation PIGLIN_BRUTE_OUTER_ARMOR = registerOuterArmor("piglin_brute");
	public static final ModelLayerLocation PIGLIN_HEAD = register("piglin_head");
	public static final ModelLayerLocation PIGLIN_INNER_ARMOR = registerInnerArmor("piglin");
	public static final ModelLayerLocation PIGLIN_OUTER_ARMOR = registerOuterArmor("piglin");
	public static final ModelLayerLocation PIGLIN_BABY = register("piglin_baby");
	public static final ModelLayerLocation PIGLIN_BABY_INNER_ARMOR = registerInnerArmor("piglin_baby");
	public static final ModelLayerLocation PIGLIN_BABY_OUTER_ARMOR = registerOuterArmor("piglin_baby");
	public static final ModelLayerLocation PILLAGER = register("pillager");
	public static final ModelLayerLocation PLAYER = register("player");
	public static final ModelLayerLocation PLAYER_EARS = register("player", "ears");
	public static final ModelLayerLocation PLAYER_CAPE = register("player", "cape");
	public static final ModelLayerLocation PLAYER_HEAD = register("player_head");
	public static final ModelLayerLocation PLAYER_INNER_ARMOR = registerInnerArmor("player");
	public static final ModelLayerLocation PLAYER_OUTER_ARMOR = registerOuterArmor("player");
	public static final ModelLayerLocation PLAYER_SLIM = register("player_slim");
	public static final ModelLayerLocation PLAYER_SLIM_INNER_ARMOR = registerInnerArmor("player_slim");
	public static final ModelLayerLocation PLAYER_SLIM_OUTER_ARMOR = registerOuterArmor("player_slim");
	public static final ModelLayerLocation PLAYER_SPIN_ATTACK = register("spin_attack");
	public static final ModelLayerLocation POLAR_BEAR = register("polar_bear");
	public static final ModelLayerLocation POLAR_BEAR_BABY = register("polar_bear_baby");
	public static final ModelLayerLocation PUFFERFISH_BIG = register("pufferfish_big");
	public static final ModelLayerLocation PUFFERFISH_MEDIUM = register("pufferfish_medium");
	public static final ModelLayerLocation PUFFERFISH_SMALL = register("pufferfish_small");
	public static final ModelLayerLocation RABBIT = register("rabbit");
	public static final ModelLayerLocation RABBIT_BABY = register("rabbit_baby");
	public static final ModelLayerLocation RAVAGER = register("ravager");
	public static final ModelLayerLocation SALMON = register("salmon");
	public static final ModelLayerLocation SALMON_SMALL = register("salmon_small");
	public static final ModelLayerLocation SALMON_LARGE = register("salmon_large");
	public static final ModelLayerLocation SHEEP = register("sheep");
	public static final ModelLayerLocation SHEEP_BABY = register("sheep_baby");
	public static final ModelLayerLocation SHEEP_WOOL = register("sheep", "wool");
	public static final ModelLayerLocation SHEEP_BABY_WOOL = register("sheep_baby", "wool");
	public static final ModelLayerLocation SHIELD = register("shield");
	public static final ModelLayerLocation SHULKER = register("shulker");
	public static final ModelLayerLocation SHULKER_BOX = register("shulker_box");
	public static final ModelLayerLocation SHULKER_BULLET = register("shulker_bullet");
	public static final ModelLayerLocation SILVERFISH = register("silverfish");
	public static final ModelLayerLocation SKELETON = register("skeleton");
	public static final ModelLayerLocation SKELETON_HORSE = register("skeleton_horse");
	public static final ModelLayerLocation SKELETON_HORSE_BABY = register("skeleton_horse_baby");
	public static final ModelLayerLocation SKELETON_INNER_ARMOR = registerInnerArmor("skeleton");
	public static final ModelLayerLocation SKELETON_OUTER_ARMOR = registerOuterArmor("skeleton");
	public static final ModelLayerLocation SKELETON_SKULL = register("skeleton_skull");
	public static final ModelLayerLocation SLIME = register("slime");
	public static final ModelLayerLocation SLIME_OUTER = register("slime", "outer");
	public static final ModelLayerLocation SNIFFER = register("sniffer");
	public static final ModelLayerLocation SNIFFER_BABY = register("sniffer_baby");
	public static final ModelLayerLocation SNOW_GOLEM = register("snow_golem");
	public static final ModelLayerLocation SPAWNER_MINECART = register("spawner_minecart");
	public static final ModelLayerLocation SPIDER = register("spider");
	public static final ModelLayerLocation SQUID = register("squid");
	public static final ModelLayerLocation SQUID_BABY = register("squid_baby");
	public static final ModelLayerLocation STRAY = register("stray");
	public static final ModelLayerLocation STRAY_INNER_ARMOR = registerInnerArmor("stray");
	public static final ModelLayerLocation STRAY_OUTER_ARMOR = registerOuterArmor("stray");
	public static final ModelLayerLocation STRAY_OUTER_LAYER = register("stray", "outer");
	public static final ModelLayerLocation STRIDER = register("strider");
	public static final ModelLayerLocation STRIDER_SADDLE = register("strider", "saddle");
	public static final ModelLayerLocation TADPOLE = register("tadpole");
	public static final ModelLayerLocation TNT_MINECART = register("tnt_minecart");
	public static final ModelLayerLocation TRADER_LLAMA = register("trader_llama");
	public static final ModelLayerLocation TRADER_LLAMA_BABY = register("trader_llama_baby");
	public static final ModelLayerLocation TRIDENT = register("trident");
	public static final ModelLayerLocation TROPICAL_FISH_LARGE = register("tropical_fish_large");
	public static final ModelLayerLocation TROPICAL_FISH_LARGE_PATTERN = register("tropical_fish_large", "pattern");
	public static final ModelLayerLocation TROPICAL_FISH_SMALL = register("tropical_fish_small");
	public static final ModelLayerLocation TROPICAL_FISH_SMALL_PATTERN = register("tropical_fish_small", "pattern");
	public static final ModelLayerLocation TURTLE = register("turtle");
	public static final ModelLayerLocation TURTLE_BABY = register("turtle_baby");
	public static final ModelLayerLocation VEX = register("vex");
	public static final ModelLayerLocation VILLAGER = register("villager");
	public static final ModelLayerLocation VINDICATOR = register("vindicator");
	public static final ModelLayerLocation WARDEN = register("warden");
	public static final ModelLayerLocation WANDERING_TRADER = register("wandering_trader");
	public static final ModelLayerLocation WIND_CHARGE = register("wind_charge");
	public static final ModelLayerLocation WITCH = register("witch");
	public static final ModelLayerLocation WITHER = register("wither");
	public static final ModelLayerLocation WITHER_ARMOR = register("wither", "armor");
	public static final ModelLayerLocation WITHER_SKELETON = register("wither_skeleton");
	public static final ModelLayerLocation WITHER_SKELETON_INNER_ARMOR = registerInnerArmor("wither_skeleton");
	public static final ModelLayerLocation WITHER_SKELETON_OUTER_ARMOR = registerOuterArmor("wither_skeleton");
	public static final ModelLayerLocation WITHER_SKELETON_SKULL = register("wither_skeleton_skull");
	public static final ModelLayerLocation WITHER_SKULL = register("wither_skull");
	public static final ModelLayerLocation WOLF = register("wolf");
	public static final ModelLayerLocation WOLF_ARMOR = register("wolf_armor");
	public static final ModelLayerLocation WOLF_BABY = register("wolf_baby");
	public static final ModelLayerLocation WOLF_BABY_ARMOR = register("wolf_baby_armor");
	public static final ModelLayerLocation ZOGLIN = register("zoglin");
	public static final ModelLayerLocation ZOGLIN_BABY = register("zoglin_baby");
	public static final ModelLayerLocation ZOMBIE = register("zombie");
	public static final ModelLayerLocation ZOMBIE_HEAD = register("zombie_head");
	public static final ModelLayerLocation ZOMBIE_HORSE = register("zombie_horse");
	public static final ModelLayerLocation ZOMBIE_HORSE_BABY = register("zombie_horse_baby");
	public static final ModelLayerLocation ZOMBIE_INNER_ARMOR = registerInnerArmor("zombie");
	public static final ModelLayerLocation ZOMBIE_OUTER_ARMOR = registerOuterArmor("zombie");
	public static final ModelLayerLocation ZOMBIE_BABY = register("zombie_baby");
	public static final ModelLayerLocation ZOMBIE_BABY_INNER_ARMOR = registerInnerArmor("zombie_baby");
	public static final ModelLayerLocation ZOMBIE_BABY_OUTER_ARMOR = registerOuterArmor("zombie_baby");
	public static final ModelLayerLocation ZOMBIE_VILLAGER = register("zombie_villager");
	public static final ModelLayerLocation ZOMBIE_VILLAGER_INNER_ARMOR = registerInnerArmor("zombie_villager");
	public static final ModelLayerLocation ZOMBIE_VILLAGER_OUTER_ARMOR = registerOuterArmor("zombie_villager");
	public static final ModelLayerLocation ZOMBIE_VILLAGER_BABY = register("zombie_villager_baby");
	public static final ModelLayerLocation ZOMBIE_VILLAGER_BABY_INNER_ARMOR = registerInnerArmor("zombie_villager_baby");
	public static final ModelLayerLocation ZOMBIE_VILLAGER_BABY_OUTER_ARMOR = registerOuterArmor("zombie_villager_baby");
	public static final ModelLayerLocation ZOMBIFIED_PIGLIN = register("zombified_piglin");
	public static final ModelLayerLocation ZOMBIFIED_PIGLIN_INNER_ARMOR = registerInnerArmor("zombified_piglin");
	public static final ModelLayerLocation ZOMBIFIED_PIGLIN_OUTER_ARMOR = registerOuterArmor("zombified_piglin");
	public static final ModelLayerLocation ZOMBIFIED_PIGLIN_BABY = register("zombified_piglin_baby");
	public static final ModelLayerLocation ZOMBIFIED_PIGLIN_BABY_INNER_ARMOR = registerInnerArmor("zombified_piglin_baby");
	public static final ModelLayerLocation ZOMBIFIED_PIGLIN_BABY_OUTER_ARMOR = registerOuterArmor("zombified_piglin_baby");

	private static ModelLayerLocation register(String string) {
		return register(string, "main");
	}

	private static ModelLayerLocation register(String string, String string2) {
		ModelLayerLocation modelLayerLocation = createLocation(string, string2);
		if (!ALL_MODELS.add(modelLayerLocation)) {
			throw new IllegalStateException("Duplicate registration for " + modelLayerLocation);
		} else {
			return modelLayerLocation;
		}
	}

	private static ModelLayerLocation createLocation(String string, String string2) {
		return new ModelLayerLocation(ResourceLocation.withDefaultNamespace(string), string2);
	}

	private static ModelLayerLocation registerInnerArmor(String string) {
		return register(string, "inner_armor");
	}

	private static ModelLayerLocation registerOuterArmor(String string) {
		return register(string, "outer_armor");
	}

	public static ModelLayerLocation createBoatModelName(Boat.Type type) {
		return createLocation("boat/" + type.getName(), "main");
	}

	public static ModelLayerLocation createChestBoatModelName(Boat.Type type) {
		return createLocation("chest_boat/" + type.getName(), "main");
	}

	public static ModelLayerLocation createStandingSignModelName(WoodType woodType) {
		return createLocation("sign/standing/" + woodType.name(), "main");
	}

	public static ModelLayerLocation createWallSignModelName(WoodType woodType) {
		return createLocation("sign/wall/" + woodType.name(), "main");
	}

	public static ModelLayerLocation createHangingSignModelName(WoodType woodType) {
		return createLocation("hanging_sign/" + woodType.name(), "main");
	}

	public static Stream<ModelLayerLocation> getKnownLocations() {
		return ALL_MODELS.stream();
	}
}
