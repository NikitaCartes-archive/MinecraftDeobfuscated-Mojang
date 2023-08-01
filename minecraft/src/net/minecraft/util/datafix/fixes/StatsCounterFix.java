package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.util.datafix.schemas.V1451_6;
import org.apache.commons.lang3.StringUtils;

public class StatsCounterFix extends DataFix {
	private static final Set<String> SPECIAL_OBJECTIVE_CRITERIA = Set.of(
		"dummy",
		"trigger",
		"deathCount",
		"playerKillCount",
		"totalKillCount",
		"health",
		"food",
		"air",
		"armor",
		"xp",
		"level",
		"killedByTeam.aqua",
		"killedByTeam.black",
		"killedByTeam.blue",
		"killedByTeam.dark_aqua",
		"killedByTeam.dark_blue",
		"killedByTeam.dark_gray",
		"killedByTeam.dark_green",
		"killedByTeam.dark_purple",
		"killedByTeam.dark_red",
		"killedByTeam.gold",
		"killedByTeam.gray",
		"killedByTeam.green",
		"killedByTeam.light_purple",
		"killedByTeam.red",
		"killedByTeam.white",
		"killedByTeam.yellow",
		"teamkill.aqua",
		"teamkill.black",
		"teamkill.blue",
		"teamkill.dark_aqua",
		"teamkill.dark_blue",
		"teamkill.dark_gray",
		"teamkill.dark_green",
		"teamkill.dark_purple",
		"teamkill.dark_red",
		"teamkill.gold",
		"teamkill.gray",
		"teamkill.green",
		"teamkill.light_purple",
		"teamkill.red",
		"teamkill.white",
		"teamkill.yellow"
	);
	private static final Set<String> SKIP = ImmutableSet.<String>builder()
		.add("stat.craftItem.minecraft.spawn_egg")
		.add("stat.useItem.minecraft.spawn_egg")
		.add("stat.breakItem.minecraft.spawn_egg")
		.add("stat.pickup.minecraft.spawn_egg")
		.add("stat.drop.minecraft.spawn_egg")
		.build();
	private static final Map<String, String> CUSTOM_MAP = ImmutableMap.<String, String>builder()
		.put("stat.leaveGame", "minecraft:leave_game")
		.put("stat.playOneMinute", "minecraft:play_one_minute")
		.put("stat.timeSinceDeath", "minecraft:time_since_death")
		.put("stat.sneakTime", "minecraft:sneak_time")
		.put("stat.walkOneCm", "minecraft:walk_one_cm")
		.put("stat.crouchOneCm", "minecraft:crouch_one_cm")
		.put("stat.sprintOneCm", "minecraft:sprint_one_cm")
		.put("stat.swimOneCm", "minecraft:swim_one_cm")
		.put("stat.fallOneCm", "minecraft:fall_one_cm")
		.put("stat.climbOneCm", "minecraft:climb_one_cm")
		.put("stat.flyOneCm", "minecraft:fly_one_cm")
		.put("stat.diveOneCm", "minecraft:dive_one_cm")
		.put("stat.minecartOneCm", "minecraft:minecart_one_cm")
		.put("stat.boatOneCm", "minecraft:boat_one_cm")
		.put("stat.pigOneCm", "minecraft:pig_one_cm")
		.put("stat.horseOneCm", "minecraft:horse_one_cm")
		.put("stat.aviateOneCm", "minecraft:aviate_one_cm")
		.put("stat.jump", "minecraft:jump")
		.put("stat.drop", "minecraft:drop")
		.put("stat.damageDealt", "minecraft:damage_dealt")
		.put("stat.damageTaken", "minecraft:damage_taken")
		.put("stat.deaths", "minecraft:deaths")
		.put("stat.mobKills", "minecraft:mob_kills")
		.put("stat.animalsBred", "minecraft:animals_bred")
		.put("stat.playerKills", "minecraft:player_kills")
		.put("stat.fishCaught", "minecraft:fish_caught")
		.put("stat.talkedToVillager", "minecraft:talked_to_villager")
		.put("stat.tradedWithVillager", "minecraft:traded_with_villager")
		.put("stat.cakeSlicesEaten", "minecraft:eat_cake_slice")
		.put("stat.cauldronFilled", "minecraft:fill_cauldron")
		.put("stat.cauldronUsed", "minecraft:use_cauldron")
		.put("stat.armorCleaned", "minecraft:clean_armor")
		.put("stat.bannerCleaned", "minecraft:clean_banner")
		.put("stat.brewingstandInteraction", "minecraft:interact_with_brewingstand")
		.put("stat.beaconInteraction", "minecraft:interact_with_beacon")
		.put("stat.dropperInspected", "minecraft:inspect_dropper")
		.put("stat.hopperInspected", "minecraft:inspect_hopper")
		.put("stat.dispenserInspected", "minecraft:inspect_dispenser")
		.put("stat.noteblockPlayed", "minecraft:play_noteblock")
		.put("stat.noteblockTuned", "minecraft:tune_noteblock")
		.put("stat.flowerPotted", "minecraft:pot_flower")
		.put("stat.trappedChestTriggered", "minecraft:trigger_trapped_chest")
		.put("stat.enderchestOpened", "minecraft:open_enderchest")
		.put("stat.itemEnchanted", "minecraft:enchant_item")
		.put("stat.recordPlayed", "minecraft:play_record")
		.put("stat.furnaceInteraction", "minecraft:interact_with_furnace")
		.put("stat.craftingTableInteraction", "minecraft:interact_with_crafting_table")
		.put("stat.chestOpened", "minecraft:open_chest")
		.put("stat.sleepInBed", "minecraft:sleep_in_bed")
		.put("stat.shulkerBoxOpened", "minecraft:open_shulker_box")
		.build();
	private static final String BLOCK_KEY = "stat.mineBlock";
	private static final String NEW_BLOCK_KEY = "minecraft:mined";
	private static final Map<String, String> ITEM_KEYS = ImmutableMap.<String, String>builder()
		.put("stat.craftItem", "minecraft:crafted")
		.put("stat.useItem", "minecraft:used")
		.put("stat.breakItem", "minecraft:broken")
		.put("stat.pickup", "minecraft:picked_up")
		.put("stat.drop", "minecraft:dropped")
		.build();
	private static final Map<String, String> ENTITY_KEYS = ImmutableMap.<String, String>builder()
		.put("stat.entityKilledBy", "minecraft:killed_by")
		.put("stat.killEntity", "minecraft:killed")
		.build();
	private static final Map<String, String> ENTITIES = ImmutableMap.<String, String>builder()
		.put("Bat", "minecraft:bat")
		.put("Blaze", "minecraft:blaze")
		.put("CaveSpider", "minecraft:cave_spider")
		.put("Chicken", "minecraft:chicken")
		.put("Cow", "minecraft:cow")
		.put("Creeper", "minecraft:creeper")
		.put("Donkey", "minecraft:donkey")
		.put("ElderGuardian", "minecraft:elder_guardian")
		.put("Enderman", "minecraft:enderman")
		.put("Endermite", "minecraft:endermite")
		.put("EvocationIllager", "minecraft:evocation_illager")
		.put("Ghast", "minecraft:ghast")
		.put("Guardian", "minecraft:guardian")
		.put("Horse", "minecraft:horse")
		.put("Husk", "minecraft:husk")
		.put("Llama", "minecraft:llama")
		.put("LavaSlime", "minecraft:magma_cube")
		.put("MushroomCow", "minecraft:mooshroom")
		.put("Mule", "minecraft:mule")
		.put("Ozelot", "minecraft:ocelot")
		.put("Parrot", "minecraft:parrot")
		.put("Pig", "minecraft:pig")
		.put("PolarBear", "minecraft:polar_bear")
		.put("Rabbit", "minecraft:rabbit")
		.put("Sheep", "minecraft:sheep")
		.put("Shulker", "minecraft:shulker")
		.put("Silverfish", "minecraft:silverfish")
		.put("SkeletonHorse", "minecraft:skeleton_horse")
		.put("Skeleton", "minecraft:skeleton")
		.put("Slime", "minecraft:slime")
		.put("Spider", "minecraft:spider")
		.put("Squid", "minecraft:squid")
		.put("Stray", "minecraft:stray")
		.put("Vex", "minecraft:vex")
		.put("Villager", "minecraft:villager")
		.put("VindicationIllager", "minecraft:vindication_illager")
		.put("Witch", "minecraft:witch")
		.put("WitherSkeleton", "minecraft:wither_skeleton")
		.put("Wolf", "minecraft:wolf")
		.put("ZombieHorse", "minecraft:zombie_horse")
		.put("PigZombie", "minecraft:zombie_pigman")
		.put("ZombieVillager", "minecraft:zombie_villager")
		.put("Zombie", "minecraft:zombie")
		.build();
	private static final String NEW_CUSTOM_KEY = "minecraft:custom";

	public StatsCounterFix(Schema schema, boolean bl) {
		super(schema, bl);
	}

	@Nullable
	private static StatsCounterFix.StatType unpackLegacyKey(String string) {
		if (SKIP.contains(string)) {
			return null;
		} else {
			String string2 = (String)CUSTOM_MAP.get(string);
			if (string2 != null) {
				return new StatsCounterFix.StatType("minecraft:custom", string2);
			} else {
				int i = StringUtils.ordinalIndexOf(string, ".", 2);
				if (i < 0) {
					return null;
				} else {
					String string3 = string.substring(0, i);
					if ("stat.mineBlock".equals(string3)) {
						String string4 = upgradeBlock(string.substring(i + 1).replace('.', ':'));
						return new StatsCounterFix.StatType("minecraft:mined", string4);
					} else {
						String string4 = (String)ITEM_KEYS.get(string3);
						if (string4 != null) {
							String string5 = string.substring(i + 1).replace('.', ':');
							String string6 = upgradeItem(string5);
							String string7 = string6 == null ? string5 : string6;
							return new StatsCounterFix.StatType(string4, string7);
						} else {
							String string5 = (String)ENTITY_KEYS.get(string3);
							if (string5 != null) {
								String string6 = string.substring(i + 1).replace('.', ':');
								String string7 = (String)ENTITIES.getOrDefault(string6, string6);
								return new StatsCounterFix.StatType(string5, string7);
							} else {
								return null;
							}
						}
					}
				}
			}
		}
	}

	@Override
	public TypeRewriteRule makeRule() {
		return TypeRewriteRule.seq(this.makeStatFixer(), this.makeObjectiveFixer());
	}

	private TypeRewriteRule makeStatFixer() {
		Type<?> type = this.getInputSchema().getType(References.STATS);
		Type<?> type2 = this.getOutputSchema().getType(References.STATS);
		return this.fixTypeEverywhereTyped(
			"StatsCounterFix",
			type,
			type2,
			typed -> {
				Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
				Map<Dynamic<?>, Dynamic<?>> map = Maps.<Dynamic<?>, Dynamic<?>>newHashMap();
				Optional<? extends Map<? extends Dynamic<?>, ? extends Dynamic<?>>> optional = dynamic.getMapValues().result();
				if (optional.isPresent()) {
					for (Entry<? extends Dynamic<?>, ? extends Dynamic<?>> entry : ((Map)optional.get()).entrySet()) {
						if (((Dynamic)entry.getValue()).asNumber().result().isPresent()) {
							String string = ((Dynamic)entry.getKey()).asString("");
							StatsCounterFix.StatType statType = unpackLegacyKey(string);
							if (statType != null) {
								Dynamic<?> dynamic2 = dynamic.createString(statType.type());
								Dynamic<?> dynamic3 = (Dynamic<?>)map.computeIfAbsent(dynamic2, dynamic2x -> dynamic.emptyMap());
								map.put(dynamic2, dynamic3.set(statType.typeKey(), (Dynamic<?>)entry.getValue()));
							}
						}
					}
				}

				return (Typed)((Pair)type2.readTyped(dynamic.emptyMap().set("stats", dynamic.createMap(map)))
						.result()
						.orElseThrow(() -> new IllegalStateException("Could not parse new stats object.")))
					.getFirst();
			}
		);
	}

	private TypeRewriteRule makeObjectiveFixer() {
		Type<?> type = this.getInputSchema().getType(References.OBJECTIVE);
		Type<?> type2 = this.getOutputSchema().getType(References.OBJECTIVE);
		return this.fixTypeEverywhereTyped("ObjectiveStatFix", type, type2, typed -> {
			Dynamic<?> dynamic = typed.get(DSL.remainderFinder());
			Dynamic<?> dynamic2 = dynamic.update("CriteriaName", dynamicx -> DataFixUtils.orElse(dynamicx.asString().result().map(string -> {
					if (SPECIAL_OBJECTIVE_CRITERIA.contains(string)) {
						return string;
					} else {
						StatsCounterFix.StatType statType = unpackLegacyKey(string);
						return statType == null ? "dummy" : V1451_6.packNamespacedWithDot(statType.type) + ":" + V1451_6.packNamespacedWithDot(statType.typeKey);
					}
				}).map(dynamicx::createString), dynamicx));
			return (Typed)((Pair)type2.readTyped(dynamic2).result().orElseThrow(() -> new IllegalStateException("Could not parse new objective object."))).getFirst();
		});
	}

	@Nullable
	private static String upgradeItem(String string) {
		return ItemStackTheFlatteningFix.updateItem(string, 0);
	}

	private static String upgradeBlock(String string) {
		return BlockStateData.upgradeBlock(string);
	}

	static record StatType(String type, String typeKey) {
	}
}
