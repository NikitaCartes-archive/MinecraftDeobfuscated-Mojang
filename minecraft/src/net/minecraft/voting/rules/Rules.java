package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.util.valueproviders.ClampedNormalInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.voting.rules.actual.AutoJumpAlternatives;
import net.minecraft.voting.rules.actual.BinaryGameRuleRule;
import net.minecraft.voting.rules.actual.BiomeColorRule;
import net.minecraft.voting.rules.actual.BlockFlammabilityRule;
import net.minecraft.voting.rules.actual.BlockModelReplacementRule;
import net.minecraft.voting.rules.actual.BlockReplaceSingleRule;
import net.minecraft.voting.rules.actual.CaepType;
import net.minecraft.voting.rules.actual.ChickenEggRule;
import net.minecraft.voting.rules.actual.CodepointReplaceRule;
import net.minecraft.voting.rules.actual.CodepointStyleRule;
import net.minecraft.voting.rules.actual.ColorRule;
import net.minecraft.voting.rules.actual.CopySkinRule;
import net.minecraft.voting.rules.actual.DayLengthRule;
import net.minecraft.voting.rules.actual.DoubleOrHalfDamageTypeMapRule;
import net.minecraft.voting.rules.actual.DoubleOrHalfItemMapRule;
import net.minecraft.voting.rules.actual.ExplosionExtraPowerRule;
import net.minecraft.voting.rules.actual.FlailingLevel;
import net.minecraft.voting.rules.actual.FoodType;
import net.minecraft.voting.rules.actual.FootprintRule;
import net.minecraft.voting.rules.actual.GiveEffectRule;
import net.minecraft.voting.rules.actual.IntegerGameRuleRule;
import net.minecraft.voting.rules.actual.ItemDespawnTime;
import net.minecraft.voting.rules.actual.ItemEntityDespawn;
import net.minecraft.voting.rules.actual.ItemGiveRule;
import net.minecraft.voting.rules.actual.ItemModelReplacementRule;
import net.minecraft.voting.rules.actual.ItemReplacementRule;
import net.minecraft.voting.rules.actual.LavaReplaceRule;
import net.minecraft.voting.rules.actual.LightEngineMode;
import net.minecraft.voting.rules.actual.MoonRule;
import net.minecraft.voting.rules.actual.NameVisiblity;
import net.minecraft.voting.rules.actual.NaturalSpawnReplaceRule;
import net.minecraft.voting.rules.actual.OptimizationRule;
import net.minecraft.voting.rules.actual.ParentTrapRule;
import net.minecraft.voting.rules.actual.PlayerEntry;
import net.minecraft.voting.rules.actual.PlayerSetRule;
import net.minecraft.voting.rules.actual.RecipeEnableRule;
import net.minecraft.voting.rules.actual.RecipeFlip;
import net.minecraft.voting.rules.actual.ReplaceItemsRule;
import net.minecraft.voting.rules.actual.SoundEventReplacementRule;
import net.minecraft.voting.rules.actual.TheJokeRule;
import net.minecraft.voting.rules.actual.ThreadedAnvilChunkStorage;
import net.minecraft.voting.rules.actual.TransformEntityRule;
import net.minecraft.voting.rules.actual.TransformScaleRule;
import net.minecraft.voting.rules.actual.VehicleCollisionRule;
import net.minecraft.voting.rules.actual.VillagerPaymentRule;
import net.minecraft.voting.rules.actual.WeatherOption;
import net.minecraft.voting.rules.actual.WorldShape;
import net.minecraft.voting.votes.TieResolutionStrategy;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class Rules {
	private static final SimpleWeightedRandomList.Builder<Holder.Reference<Rule>> BUILDER = new SimpleWeightedRandomList.Builder<>();
	public static final int DEFAULT_MAX_APPROVE_VOTE_COUNT = 5;
	public static final int DEFAULT_MAX_REPEAL_VOTE_COUNT = 2;
	public static final int DEFAULT_MAX_VOTE_COUNT = 7;
	public static final int WEIGHT_COMMON = 1000;
	public static final int WEIGHT_DEFAULT = 1000;
	public static final int WEIGHT_RARE = 500;
	public static final int WEIGHT_DISASTER = 125;
	public static final BooleanRule TEST_RULE_PLEASE_IGNORE = register("test_rule_please_ignore", 7, new BooleanRule(Component.literal("TEST RULE PLEASE IGNORE")));
	public static final BooleanRule VOTE_RESULT_PASS_WITHOUT_VOTERS = register(
		"vote_result_pass_without_voters", 125, new BooleanRule(Component.translatable("rule.vote_result_pass_without_voters"))
	);
	public static final BooleanRule VOTE_RESULT_PASS_WITHOUT_VOTES = register(
		"vote_result_pass_without_votes", 125, new BooleanRule(Component.translatable("rule.vote_result_pass_without_votes"))
	);
	public static final BooleanRule VOTE_RESULT_DONT_SHOW_TALLY = register(
		"vote_result_show_tally", 500, new BooleanRule(Component.translatable("rule.vote_result_show_options"))
	);
	public static final BooleanRule VOTE_RESULT_SHOW_VOTERS = register(
		"vote_result_show_voters", 500, new BooleanRule(Component.translatable("rule.vote_result_show_voters"))
	);
	public static final BooleanRule VOTE_RESULT_PICK_RANDOM_IF_VOTE_FAILS = register(
		"vote_result_pick_random_if_vote_fails", 125, new BooleanRule(Component.translatable("rule.vote_result_pick_random_if_vote_fails"))
	);
	public static final BooleanRule VOTE_RESULT_REVERSE_VOTES = register(
		"vote_result_reverse_counts", 125, new BooleanRule(Component.translatable("rule.vote_result_reverse_counts"))
	);
	public static final RandomNumberRule.RandomInt VOTE_MAX_RESULTS = register("vote_max_results", 1000, new RandomNumberRule.RandomInt(1, UniformInt.of(1, 5)) {
		protected Component valueDescription(Integer integer) {
			return Component.translatable("rule.vote_max_results", integer);
		}
	});
	public static final RandomNumberRule.RandomInt NEW_VOTE_CHANCE_PER_TICK = register(
		"new_vote_chance_per_tick", 500, new RandomNumberRule.RandomInt(200, UniformInt.of(1, 2000)) {
			protected Component valueDescription(Integer integer) {
				return Component.translatable("rule.new_vote_chance_per_tick", integer);
			}
		}
	);
	public static final UniformIntRule NEW_APPROVE_VOTE_OPTION_COUNT = register(
		"new_vote_approve_option_count", 500, new UniformIntRule(UniformInt.of(1, 5), UniformInt.of(0, 4), UniformInt.of(2, 4)) {
			@Override
			protected Component valueDescription(UniformInt uniformInt) {
				return Component.translatable("rule.new_vote_approve_option_count", rangeToComponent(uniformInt));
			}
		}
	);
	public static final UniformIntRule NEW_REPEAL_VOTE_OPTION_COUNT = register(
		"new_vote_repeal_option_count", 500, new UniformIntRule(UniformInt.of(1, 5), UniformInt.of(0, 4), UniformInt.of(2, 4)) {
			@Override
			protected Component valueDescription(UniformInt uniformInt) {
				return Component.translatable("rule.new_vote_repeal_option_count", rangeToComponent(uniformInt));
			}
		}
	);
	public static final UniformIntRule NEW_VOTE_DURATION_MINUTES = register(
		"new_vote_duration_minutes", 1000, new UniformIntRule(UniformInt.of(1, 20), UniformInt.of(0, 10), UniformInt.of(8, 16)) {
			@Override
			protected Component valueDescription(UniformInt uniformInt) {
				return Component.translatable("rule.new_vote_duration_minutes", rangeToComponent(uniformInt));
			}
		}
	);
	public static final RandomNumberRule.RandomInt NEW_VOTE_EXTRA_EFFECT_CHANCE = register(
		"new_vote_extra_effect_chance", 1000, new RandomNumberRule.RandomInt(30, UniformInt.of(0, 80)) {
			protected Component valueDescription(Integer integer) {
				return Component.translatable("rule.new_vote_extra_effect_chance", integer);
			}
		}
	);
	public static final RandomNumberRule.RandomInt NEW_VOTE_EXTRA_EFFECT_MAX_COUNT = register(
		"new_vote_extra_effect_max_count", 1000, new RandomNumberRule.RandomInt(1, UniformInt.of(0, 5)) {
			protected Component valueDescription(Integer integer) {
				return Component.translatable("rule.new_vote_extra_effect_max_count", integer);
			}
		}
	);
	public static final RandomNumberRule.RandomInt NEW_VOTE_REPEAL_VOTE_CHANCE = register(
		"new_vote_repeal_vote_chance", 500, new RandomNumberRule.RandomInt(50, UniformInt.of(20, 80)) {
			protected Component valueDescription(Integer integer) {
				return Component.translatable("rule.new_vote_repeal_vote_chance", integer);
			}
		}
	);
	public static final BooleanRule NEW_VOTE_NO_OPT_OUT = register(
		"new_vote_disable_opt_out", 125, new BooleanRule(Component.translatable("rule.new_vote_disable_opt_out"))
	);
	public static final RandomNumberRule.RandomInt NEW_VOTE_MAX_APPROVE_VOTE_COUNT = register(
		"new_vote_max_approve_vote_count", 500, new RandomNumberRule.RandomInt(5, UniformInt.of(1, 10)) {
			protected Component valueDescription(Integer integer) {
				return Component.translatable("rule.new_vote_max_approve_vote_count", integer);
			}
		}
	);
	public static final RandomNumberRule.RandomInt NEW_VOTE_MAX_REPEAL_VOTE_COUNT = register(
		"new_vote_max_repeal_vote_count", 500, new RandomNumberRule.RandomInt(2, UniformInt.of(1, 10)) {
			protected Component valueDescription(Integer integer) {
				return Component.translatable("rule.new_vote_max_repeal_vote_count", integer);
			}
		}
	);
	public static final VotingCostRule NEW_VOTE_COST = register("new_vote_cost", 500, new VotingCostRule());
	public static final BooleanRule INVISIBLE_ARMOR = register("invisible_armor", 500, new BooleanRule(Component.translatable("rule.invisible_armor")));
	public static final EnumRule<WorldShape> WORLD_SHAPE = register(
		"world_shape", 1000, new EnumRule<WorldShape>(WorldShape.values(), WorldShape.NONE, WorldShape.CODEC) {
			protected Component valueDescription(WorldShape worldShape) {
				return Component.translatable("rule.change_world_shape");
			}
		}
	);
	public static final BooleanRule DISABLE_ITEM_TOOLTIPS = register(
		"disable_item_tooltips", 500, new BooleanRule(Component.translatable("rule.disable_item_tooltips"))
	);
	public static final RandomNumberRule.RandomInt QUORUM_PERCENT = register("quorum_percent", 500, new RandomNumberRule.RandomInt(0, flattenedPercents(20)) {
		protected Component valueDescription(Integer integer) {
			return Component.translatable("rule.quorum_percent", integer);
		}
	});
	public static final RandomNumberRule.RandomInt VOTES_NEEDED_PERCENT = register(
		"votes_to_win_percent", 125, new RandomNumberRule.RandomInt(0, flattenedPercents(20)) {
			protected Component valueDescription(Integer integer) {
				return Component.translatable("rule.votes_to_win_percent", integer);
			}
		}
	);
	public static final BooleanRule OTHER_PORTAL = register("other_portal", 500, new BooleanRule(Component.translatable("rule.other_portal")));
	public static final BooleanRule ANONYMIZE_SKINS = register("anonymize_skins", 500, new BooleanRule(Component.translatable("rule.anonymize_skins")));
	public static final RecipeEnableRule SPECIAL_RECIPE = register("special_recipe", 1000, new RecipeEnableRule());
	public static final FootprintRule FOOTPRINTS = register("footprints", 500, new FootprintRule());
	public static final EnumRule<TieResolutionStrategy> TIE_RESOLUTION_STRATEGY = register(
		"tie_strategy", 500, new EnumRule<TieResolutionStrategy>(TieResolutionStrategy.values(), TieResolutionStrategy.PICK_RANDOM, TieResolutionStrategy.CODEC) {
			protected Component valueDescription(TieResolutionStrategy tieResolutionStrategy) {
				return tieResolutionStrategy.getDisplayName();
			}
		}
	);
	public static final BooleanRule SILENT_VOTE = register("silent_vote", 125, new BooleanRule(Component.translatable("rule.silent_vote")));
	public static final ItemModelReplacementRule REPLACE_ITEM_MODEL = register("replace_item_model", 1000, new ItemModelReplacementRule());
	public static final BlockModelReplacementRule REPLACE_BLOCK_MODEL = register("replace_block_model", 1000, new BlockModelReplacementRule());
	public static final EnumRule<AutoJumpAlternatives> AUTO_JUMP_ALTERNATIVES = register(
		"auto_jump_alternatives", 500, new EnumRule<AutoJumpAlternatives>(AutoJumpAlternatives.values(), AutoJumpAlternatives.OFF, AutoJumpAlternatives.CODEC) {
			protected Component valueDescription(AutoJumpAlternatives autoJumpAlternatives) {
				return autoJumpAlternatives.getName();
			}
		}
	);
	public static final BooleanRule UNCONTROLABLE_LAVE = register("uncontrolable_lave", 125, new BooleanRule(Component.translatable("rule.uncontrolable_lave")));
	public static final BooleanRule WHEELS_ON_MINECARTS = register("wheels_on_minecarts", 500, new BooleanRule(Component.translatable("rule.wheels_on_minecarts")));
	public static final int LAVA_SPREAD_DEFAULT = 30;
	public static final RandomNumberRule<Integer> LAVA_SPREAD_TICK_DELAY = register(
		"lava_spread_tick_delay", 500, new RandomNumberRule.RandomInt(30, UniformInt.of(1, 9)) {
			protected Component valueDescription(Integer integer) {
				return Component.translatable("rule.lava_spread_tick_delay", integer);
			}
		}
	);
	public static final BooleanRule MIDAS_TOUCH = register("midas_touch", 125, new BooleanRule(Component.translatable("rule.midas_touch")));
	public static final BlockReplaceSingleRule COBBLESTONE_GEN_REPLACE = register(
		"cobblestone_gen_replace", 1000, new LavaReplaceRule("rule.lava_water_replace", Blocks.COBBLESTONE)
	);
	public static final BlockReplaceSingleRule STONE_GEN_REPLACE = register(
		"stone_gen_replace", 1000, new LavaReplaceRule("rule.lava_water_replace", Blocks.STONE)
	);
	public static final BlockReplaceSingleRule OBSIDIAN_GEN_REPLACE = register(
		"obsidian_gen_replace", 125, new LavaReplaceRule("rule.lava_water_replace", Blocks.OBSIDIAN)
	);
	public static final BlockReplaceSingleRule BASALT_GEN_REPLACE = register(
		"basalt_gen_replace", 125, new LavaReplaceRule("rule.lava_blue_ice_replace", Blocks.BASALT)
	);
	public static final BooleanRule ROWING_UP_THAT_HILL = register("rowing_up_that_hill", 500, new BooleanRule(Component.translatable("rule.rowing_up_that_hill")));
	public static final BooleanRule POT_GEMS = register("pot_gems", 500, new BooleanRule(Component.translatable("rule.pot_gems")));
	public static final BooleanRule DISABLE_SHIELD = register("disable_shield", 500, new BooleanRule(Component.translatable("rule.disable_shield")));
	public static final EnumRule<WeatherOption> RAIN_OPTION = register(
		"rain", 1000, new EnumRule<WeatherOption>(WeatherOption.values(), WeatherOption.DEFAULT, WeatherOption.CODEC) {
			protected Component valueDescription(WeatherOption weatherOption) {
				return weatherOption.rainDescription();
			}
		}
	);
	public static final EnumRule<WeatherOption> THUNDER_OPTION = register(
		"thunder", 1000, new EnumRule<WeatherOption>(WeatherOption.values(), WeatherOption.DEFAULT, WeatherOption.CODEC) {
			protected Component valueDescription(WeatherOption weatherOption) {
				return weatherOption.thunderDescription();
			}
		}
	);
	public static final RandomNumberRule.RandomFloat GLOBAL_PITCH = register(
		"global_pitch", 1000, new RandomNumberRule.RandomFloat(1.0F, ClampedNormalFloat.of(1.5F, 0.6F, 0.3F, 3.0F)) {
			protected Component valueDescription(Float float_) {
				return Component.translatable("rule.global_pitch", Math.round(float_ * 100.0F));
			}
		}
	);
	public static final GiveEffectRule PERMA_EFFECT = register("perma_effect", 1000, new GiveEffectRule());
	public static final RandomNumberRule.RandomFloat ITEM_USE_SPEED = register(
		"item_use_speed", 1000, new RandomNumberRule.RandomFloat(1.0F, ClampedNormalFloat.of(1.0F, 0.4F, 0.1F, 8.0F)) {
			protected Component valueDescription(Float float_) {
				return Component.translatable("rule.item_use_speed", Math.round(float_ * 100.0F));
			}
		}
	);
	public static final RandomNumberRule.RandomFloat ATTACK_KNOCKBACK = register(
		"attack_knockback", 1000, new RandomNumberRule.RandomFloat(1.0F, ClampedNormalFloat.of(1.0F, 0.4F, 0.1F, 8.0F)) {
			protected Component valueDescription(Float float_) {
				return Component.translatable("rule.attack_knockback", Math.round(float_ * 100.0F));
			}
		}
	);
	public static final BooleanRule INFINITE_CAKES = register("infinite_cakes", 500, new BooleanRule(Component.translatable("rule.infinite_cakes")));
	public static final BooleanRule GOD_OF_LIGHTNING = register("god_of_lightning", 125, new BooleanRule(Component.translatable("rule.god_of_lightning")));
	public static final BooleanRule MORROWIND_POWER_PLAYER_MOVEMENT = register(
		"morrowind_power_player_movement", 125, new BooleanRule(Component.translatable("rule.morrowind_power_player_movement"))
	);
	public static final BooleanRule EVIL_EYE = register("evil_eye", 125, new BooleanRule(Component.translatable("rule.evil_eye")));
	public static final float EVIL_EYE_RANGE = 200.0F;
	public static final BooleanRule BIG_HEAD_MODE = register("big_head_mode", 1000, new BooleanRule(Component.translatable("rule.big_heads")));
	public static final BooleanRule FLOATING_HEAD_MODE = register("floating_head_mode", 1000, new BooleanRule(Component.translatable("rule.floating_heads")));
	public static final BooleanRule TRANSPARENT_PLAYERS = register("transparent_players", 500, new BooleanRule(Component.translatable("rule.transparent_players")));
	public static final EnumRule<CaepType> CAEP = register("caep", 1000, new EnumRule<CaepType>(CaepType.values(), CaepType.NONE, CaepType.CODEC) {
		protected Component valueDescription(CaepType caepType) {
			return caepType.getDisplayName();
		}
	});
	public static final BooleanRule MINI_ME_MODE = register("minime", 500, new BooleanRule(Component.translatable("rule.mini_players")));
	public static final BooleanRule MILK_EVERY_MOB = register("milk_every_mob", 500, new BooleanRule(Component.translatable("rule.milk_every_mob")));
	public static final BooleanRule FRENCH_MODE = register("french_mode", 500, new BooleanRule(Component.translatable("rule.french_mode")));
	public static final BooleanRule MBE = register("mbe", 500, new BooleanRule(Component.translatable("rule.mbe")));
	public static final BooleanRule STICKY = register("sticky", 500, new BooleanRule(Component.translatable("rule.sticky")));
	public static final BooleanRule BUTTONS_ON_THINGS = register("buttons_on_things", 1000, new BooleanRule(Component.translatable("rule.buttons_on_things")));
	public static final RandomNumberRule<Integer> PUSH_LIMIT = register("push_limit", 1000, new RandomNumberRule.RandomInt(12, UniformInt.of(0, 23)) {
		protected Component valueDescription(Integer integer) {
			return Component.translatable("rule.push_limit", integer);
		}
	});
	public static final BooleanRule FIRE_SPONGE = register("fire_sponge", 1000, new BooleanRule(Component.translatable("rule.fire_sponge")));
	public static final BooleanRule PERSISTENT_PARROTS = register("persistent_parrots", 1000, new BooleanRule(Component.translatable("rule.persistent_parrots")));
	public static final ThreadLocal<Boolean> UPDATES = ThreadLocal.withInitial(() -> true);
	public static final BooleanRule LESS_INTERACTION_UPDATES = register(
		"less_interaction_updates", 125, new BooleanRule(Component.translatable("rule.less_interaction_updates"))
	);
	public static final BooleanRule DEAD_BUSH_RENEWABILITY = register(
		"dead_bush_renewability", 1000, new BooleanRule(Component.translatable("rule.dead_bush_renewability"))
	);
	public static final BooleanRule FOG_OFF = register("fog_off", 500, new BooleanRule(Component.translatable("rule.fog_off")));
	public static final BooleanRule FIX_QC = register("fix_qc", 500, new BooleanRule(Component.translatable("rule.fix_qc")));
	public static final BooleanRule FAST_HOPPERS = register("fast_hoppers", 500, new BooleanRule(Component.translatable("rule.fast_hoppers")));
	public static final BooleanRule LESS_GRAVITY = register("less_gravity", 125, new BooleanRule(Component.translatable("rule.less_gravity")));
	public static final BooleanRule BOUNCY_CASTLE = register("bouncy_castle", 1000, new BooleanRule(Component.translatable("rule.bouncy_castle")));
	public static final BooleanRule AIR_BLOCKS = register("air_blocks", 1000, new BooleanRule(Component.translatable("rule.air_blocks")));
	public static final BooleanRule DRINK_AIR = register("drink_air", 1000, new BooleanRule(Component.translatable("rule.drink_air")));
	public static final ReplaceItemsRule REPLACE_ITEMS_WITH_BOTTLE_OF_VOID = register(
		"replace_items_with_bottle_of_void", 500, new ReplaceItemsRule((registry, randomSource) -> Optional.of(Items.BOTTLE_OF_VOID))
	);
	public static final MoonRule BIG_MOON_MODE = register("big_moon", 500, new MoonRule());
	public static final BooleanRule OBFUSCATE_PLAYER_NAMES = register(
		"obfuscate_player_names", 500, new BooleanRule(Component.translatable("rule.obfuscate_player_names"))
	);
	public static final BooleanRule BETA_ENTITY_IDS = register("beta_entity_ids", 500, new BooleanRule(Component.translatable("rule.beta_entity_ids")));
	public static final TheJokeRule THE_JOKE_RULE = register("the_joke", 500, new TheJokeRule());
	public static final EnumRule<NameVisiblity> NORMAL_NAME_VISIBILITY = register(
		"normal_name_visibility", 1000, new EnumRule<NameVisiblity>(NameVisiblity.values(), NameVisiblity.SEE_THROUGH, NameVisiblity.CODEC) {
			protected Component valueDescription(NameVisiblity nameVisiblity) {
				return Component.translatable("rule.normal_name_visibility", nameVisiblity.getDisplayName());
			}
		}
	);
	public static final EnumRule<NameVisiblity> SNEAKING_NAME_VISIBILITY = register(
		"sneaking_name_visibility", 1000, new EnumRule<NameVisiblity>(NameVisiblity.values(), NameVisiblity.NORMAL, NameVisiblity.CODEC) {
			protected Component valueDescription(NameVisiblity nameVisiblity) {
				return Component.translatable("rule.sneaking_name_visibility", nameVisiblity.getDisplayName());
			}
		}
	);
	public static final BooleanRule ENITITY_COLLISIONS = register("entity_collisions", 500, new BooleanRule(Component.translatable("rule.entity_collisions")));
	public static final BooleanRule DAY_BEDS = register("day_beds", 500, new BooleanRule(Component.translatable("rule.day_beds")));
	public static final BooleanRule PICKAXE_BLOCK = register("pickaxe_block", 1000, new BooleanRule(Component.translatable("rule.pickaxe_block")));
	public static final BooleanRule PLACE_BLOCK = register("place_block", 1000, new BooleanRule(Component.translatable("rule.place_block")));
	public static final OneShotRule PARENT_TRAP = register("parent_trap", 500, new ParentTrapRule());
	public static final BooleanRule GLOW_BEES = register("glow_bees", 1000, new BooleanRule(Component.translatable("rule.glow_bees")));
	public static final EnumRule<FlailingLevel> FLAILING_LEVEL = register(
		"flailing_level", 1000, new EnumRule<FlailingLevel>(FlailingLevel.values(), FlailingLevel.NORMAL, FlailingLevel.CODEC) {
			protected Component valueDescription(FlailingLevel flailingLevel) {
				return flailingLevel.getName();
			}
		}
	);
	public static final EnumRule<RecipeFlip> RECIPE_FLIP = register(
		"recipe_flip", 1000, new EnumRule<RecipeFlip>(RecipeFlip.values(), RecipeFlip.BOTH, RecipeFlip.CODEC) {
			protected Component valueDescription(RecipeFlip recipeFlip) {
				return recipeFlip.getDisplayName();
			}
		}
	);
	public static final PlayerSetRule BOT_REPLACEMENTS = register("ai_attack", 500, new PlayerSetRule() {
		protected Component description(PlayerEntry playerEntry) {
			return Component.translatable("rule.ai_attack", playerEntry.displayName());
		}
	});
	public static final PlayerSetRule PRESIDENT = register("president", 1000, new PlayerSetRule() {
		protected Component description(PlayerEntry playerEntry) {
			return Component.translatable("rule.president", playerEntry.displayName());
		}

		@Override
		protected boolean add(PlayerEntry playerEntry) {
			Collection<PlayerEntry> collection = this.values();
			collection.forEach(playerEntryx -> this.remove(playerEntryx));
			return super.add(playerEntry);
		}
	});
	public static final OneShotRule COPY_SKIN = register("copy_skin", 1000, new CopySkinRule());
	public static final EnumRule<ItemEntityDespawn> ITEM_DESPAWN = register(
		"item_despawn", 125, new EnumRule<ItemEntityDespawn>(ItemEntityDespawn.values(), ItemEntityDespawn.DESPAWN_ALL, ItemEntityDespawn.CODEC) {
			protected Component valueDescription(ItemEntityDespawn itemEntityDespawn) {
				return itemEntityDespawn.getDisplayName();
			}
		}
	);
	public static final CounterRule ITEM_DESPAWN_TIME = register("item_despawn_time", 1000, new ItemDespawnTime());
	public static final DayLengthRule DAY_LENGTH = register("day_length", 1000, new DayLengthRule());
	public static final BooleanRule BEDS_ON_BANNERS = register("beds_on_banners", 500, new BooleanRule(Component.translatable("rule.beds_on_banners")));
	public static final EnumRule<FoodType> FOOD_RESTRICTION = register(
		"food_restriction", 1000, new EnumRule<FoodType>(FoodType.values(), FoodType.ANY, FoodType.CODEC) {
			protected Component valueDescription(FoodType foodType) {
				return Component.translatable("rule.food_restriction." + foodType.getSerializedName());
			}
		}
	);
	public static final CodepointStyleRule CODEPOINT_STYLE = register("codepoint_style", 1000, new CodepointStyleRule());
	public static final CodepointReplaceRule CODEPOINT_REPLACE = register("codepoint_replace", 1000, new CodepointReplaceRule());
	public static final OptimizationRule OPTIMIZATION_LEVEL = register("optimize", 500, new OptimizationRule());
	public static final BinaryGameRuleRule BINARY_GAME_RULE_RULE = register("binary_gamerule_rule", 1000, new BinaryGameRuleRule());
	public static final IntegerGameRuleRule INTEGER_GAME_RULE_RULE = register("integer_gamerule_rule", 1000, new IntegerGameRuleRule());
	public static final ResourceKeySetRule<EntityType<?>> INVERTED_ENTITIES_A = register(
		"dinnerbonize", 1000, new ResourceKeySetRule<EntityType<?>>("entity", Registries.ENTITY_TYPE) {
			@Override
			protected Component description(Component component) {
				return Component.translatable("rule.dinnerbonize", component);
			}
		}
	);
	public static final ResourceKeySetRule<EntityType<?>> INVERTED_ENTITIES_B = register(
		"grummize", 1000, new ResourceKeySetRule<EntityType<?>>("entity", Registries.ENTITY_TYPE) {
			@Override
			protected Component description(Component component) {
				return Component.translatable("rule.grummize", component);
			}
		}
	);
	public static final ItemGiveRule GIVE_ITEM_RULE = register("give_item", 1000, new ItemGiveRule());
	public static final ColorRule DEFAULT_SHEEP_COLOR_RULE = register("default_sheep_color", 500, new ColorRule(DyeColor.WHITE) {
		protected Component valueDescription(DyeColor dyeColor) {
			return Component.translatable("rule.default_sheep_color", Component.translatable("color.minecraft." + dyeColor.getName()));
		}
	});
	public static final BooleanRule FLINTSPLODER = register("flintsploder", 500, new BooleanRule(Component.translatable("rule.flintsploder")));
	public static final BooleanRule FIX_PISTON = register("fix_piston", 125, new BooleanRule(Component.translatable("rule.fix_piston")));
	public static final BooleanRule PLAYER_HEAD_DROP = register("player_head_drop", 500, new BooleanRule(Component.translatable("rule.player_head_drop")));
	public static final BooleanRule CHARGED_CREEPERS = register("charged_creepers", 500, new BooleanRule(Component.translatable("rule.charged_creepers")));
	public static final FixedOrRandomKeyRule<Item> EGG_FREE = register("egg_free", 1000, new ChickenEggRule());
	public static final FixedOrRandomKeyRule<Item> VILLAGER_GEM = register("villager_gem", 1000, new VillagerPaymentRule());
	public static final BooleanRule UNSTABLE_TNT = register("unstable_tnt", 500, new BooleanRule(Component.translatable("rule.unstable_tnt")));
	public static final BooleanRule TNT_TENNIS = register("tnt_tennis", 500, new BooleanRule(Component.translatable("rule.tnt_tennis")));
	public static final BooleanRule UNDEAD_PLAYERS = register("undead_players", 125, new BooleanRule(Component.translatable("rule.undead_players")));
	public static final BooleanRule HAUNTED_WORLD = register("haunted_world", 500, new BooleanRule(Component.translatable("rule.haunted_world")));
	public static final CounterRule EXTRA_EXPLOSION_POWER = register("explosion_power", 500, new ExplosionExtraPowerRule());
	public static final ItemReplacementRule REPLACE_LOOT_DROPS = register("replace_loot_drop", 1000, new ItemReplacementRule("rule.replace_loot_drop"));
	public static final DoubleOrHalfItemMapRule DOUBLE_OR_HALF_ITEM_DROPS = register(
		"loot_double_or_half", 1000, new DoubleOrHalfItemMapRule("rule.loot_double_or_half", -4, 4)
	);
	public static final ItemReplacementRule REPLACE_RECIPE_OUTPUT = register("replace_recipe_output", 1000, new ItemReplacementRule("rule.replace_recipe_output"));
	public static final DoubleOrHalfItemMapRule DOUBLE_OR_HALF_RECIPE_OUTPUT = register(
		"recipe_double_or_half", 1000, new DoubleOrHalfItemMapRule("rule.recipe_double_or_half", -4, 4)
	);
	public static final int MAX_STACK_SIZE_LEVEL = 4;
	public static final int MAX_STACK_SIZE_MODIFIER = 16;
	public static final DoubleOrHalfItemMapRule DOUBLE_OR_HALF_STACK_SIZE = register(
		"stack_size_double_or_half", 1000, new DoubleOrHalfItemMapRule("rule.stack_size_double_or_half", -6, 4)
	);
	public static final DoubleOrHalfDamageTypeMapRule DAMAGE_MODIFIER = register(
		"damage_modifier", 1000, new DoubleOrHalfDamageTypeMapRule("rule.damage_modifier", -3, 10)
	);
	public static final BlockFlammabilityRule BLOCK_FLAMMABILITY = register("inflammability", 1000, new BlockFlammabilityRule());
	public static final BooleanRule MINECART_LIES = register("minecart_lies", 500, new BooleanRule(Component.translatable("rule.minecart_lies")));
	public static final Rule FIX_BEFORE_RELEASE_WIP = register("wipwipwi-_-pwipwip", 1, new ThreadedAnvilChunkStorage());
	public static final BooleanRule SWAP_SKY = register("swap_sky", 500, new BooleanRule(Component.translatable("rule.swap_skies")));
	public static final ResourceKeySetRule<EntityType<?>> NATURAL_SPAWN_DISABLE = register(
		"natural_spawn_disable", 1000, new ResourceKeySetRule<EntityType<?>>("entity", Registries.ENTITY_TYPE) {
			@Override
			protected Component description(Component component) {
				return Component.translatable("rule.natural_spawn_disable", component);
			}
		}
	);
	public static final NaturalSpawnReplaceRule NATURAL_SPAWN_REPLACEMENT = register("natural_spawn_replacement", 1000, new NaturalSpawnReplaceRule());
	public static final SoundEventReplacementRule SOUND_REPLACE = register("sound_replace", 1000, new SoundEventReplacementRule());
	public static final EnumRule<VehicleCollisionRule> MINECART_COLLISIONS = register(
		"minecart_collisions", 125, new EnumRule<VehicleCollisionRule>(VehicleCollisionRule.values(), VehicleCollisionRule.NONE, VehicleCollisionRule.CODEC) {
			protected Component valueDescription(VehicleCollisionRule vehicleCollisionRule) {
				return Component.translatable("rule.minecart_collisions." + vehicleCollisionRule.getSerializedName());
			}
		}
	);
	public static final EnumRule<VehicleCollisionRule> BOAT_COLLISIONS = register(
		"boat_collisions", 125, new EnumRule<VehicleCollisionRule>(VehicleCollisionRule.values(), VehicleCollisionRule.NONE, VehicleCollisionRule.CODEC) {
			protected Component valueDescription(VehicleCollisionRule vehicleCollisionRule) {
				return Component.translatable("rule.boat_collisions." + vehicleCollisionRule.getSerializedName());
			}
		}
	);
	public static final BiomeColorRule BIOME_GRASS_COLOR = register("biome_grass_color", 1000, new BiomeColorRule("rule.biome_color.grass"));
	public static final BiomeColorRule BIOME_FOLIAGE_COLOR = register("biome_foliage_color", 1000, new BiomeColorRule("rule.biome_color.foliage"));
	public static final BiomeColorRule BIOME_SKY_COLOR = register("biome_sky_color", 1000, new BiomeColorRule("rule.biome_color.sky"));
	public static final BiomeColorRule BIOME_WATER_COLOR = register("biome_water_color", 1000, new BiomeColorRule("rule.biome_color.water"));
	public static final BiomeColorRule BIOME_FOG_COLOR = register("biome_fog_color", 1000, new BiomeColorRule("rule.biome_color.fog"));
	public static final BiomeColorRule BIOME_WATER_FOG_COLOR = register("biome_water_fog_color", 1000, new BiomeColorRule("rule.biome_color.water_fog"));
	public static final BooleanRule EMERALD_TO_RUBY = register("rubies", 500, new BooleanRule(Component.translatable("rule.rubies")));
	public static final TransformScaleRule TRANSFORM_SCALE = register("transform_scale", 1000, new TransformScaleRule());
	public static final TransformEntityRule TRANSFORM_ENTITY = register("transform_entity", 125, new TransformEntityRule());
	public static final BooleanRule ULTRA_REALISTIC_MODE = register(
		"ultra_realistic_mode", 500, new BooleanRule(Component.translatable("rule.ultra_realistic_mode"))
	);
	public static final BooleanRule REMOVE_PHANTOMS = register("remove_phantoms", 125, new BooleanRule(Component.translatable("rule.remove_phantoms")));
	public static final BooleanRule PHANTOM_PHANTOM = register("phantom_phantom", 500, new BooleanRule(Component.translatable("rule.phantom_phantom")));
	public static final ReplaceItemsRule REPLACE_ITEMS = register(
		"replace_items", 1000, new ReplaceItemsRule((registry, randomSource) -> registry.getRandom(randomSource).map(Holder::value))
	);
	public static final BooleanRule DREAM_MODE = register("dream_mode", 500, new BooleanRule(Component.translatable("rule.dream_mode")));
	public static final BooleanRule INSTACHEESE = register("instacheese", 500, new BooleanRule(Component.translatable("rule.instacheese")));
	public static final BooleanRule UNIVERSAL_JEB = register("universal_jeb", 500, new BooleanRule(Component.translatable("rule.universal_jeb")));
	public static final BooleanRule WORLD_OF_GIANTS = register("world_of_giants", 125, new BooleanRule(Component.translatable("rule.world_of_giants")));
	public static final BooleanRule RAY_TRACING = register("ray_tracing", 125, new BooleanRule(Component.translatable("rule.ray_tracing")));
	public static final ColorRule COLORED_LIGHT = register("colored_light", 125, new ColorRule(DyeColor.WHITE) {
		protected Component valueDescription(DyeColor dyeColor) {
			return Component.translatable("rule.colored_light", Component.translatable("color.minecraft." + dyeColor.getName()));
		}
	});
	public static final BooleanRule GLOWING_GLOW_SQUIDS = register("glowing_glow_squids", 500, new BooleanRule(Component.translatable("rule.glowing_glow_squids")));
	public static final BooleanRule BEDROCK_SHADOWS = register("bedrock_shadows", 1000, new BooleanRule(Component.translatable("rule.bedrock_shadows")));
	public static final BooleanRule ALWAYS_FLYING = register("always_flying", 125, new BooleanRule(Component.translatable("rule.always_flying")));
	public static final BooleanRule COPPER_SINK = register("copper_sink", 500, new BooleanRule(Component.translatable("rule.copper_sink")));
	public static final BooleanRule BED_PVP = register("bed_pvp", 125, new BooleanRule(Component.translatable("rule.bed_pvp")));
	public static final BooleanRule NBT_CRAFTING = register("nbt_crafting", 500, new BooleanRule(Component.translatable("rule.nbt_crafting")));
	public static final BooleanRule POTIONS_OF_BIG = register("potions_of_big", 1000, new BooleanRule(Component.translatable("rule.potions_of_big")));
	public static final BooleanRule POTIONS_OF_SMALL = register("potions_of_small", 1000, new BooleanRule(Component.translatable("rule.potions_of_small")));
	public static final BooleanRule KEEP_YOUR_FRIENDS_CLOSE = register(
		"keep_friends_close", 125, new BooleanRule(Component.translatable("rule.keep_friends_close"))
	);
	public static final BooleanRule PREVENT_FLOATING_TREES = register(
		"prevent_floating_trees", 500, new BooleanRule(Component.translatable("rule.prevent_floating_trees"))
	);
	public static final BooleanRule RANDOM_TNT_FUSE = register("random_tnt_fuse", 500, new BooleanRule(Component.translatable("rule.random_tnt_fuse")));
	public static final BooleanRule EXPLODING_PHANTOMS = register("exploding_phantoms", 125, new BooleanRule(Component.translatable("rule.exploding_phantoms")));
	public static final BooleanRule BUFF_FISHING = register("buff_fishing", 500, new BooleanRule(Component.translatable("rule.buff_fishing")));
	public static final BooleanRule ZOMBIE_APOCALYPSE = register("zombie_apocalypse", 125, new BooleanRule(Component.translatable("rule.zombie_apocalypse")));
	public static final RandomNumberRule.RandomInt DUPE_HACK_OCCURRENCE_CHANCE = register(
		"dupe_hack_occurrence_chance", 500, new RandomNumberRule.RandomInt(0, ClampedNormalInt.of(30.0F, 30.0F, 0, 500)) {
			protected Component valueDescription(Integer integer) {
				return Component.translatable("rule.dupe_hack_occurrence_chance", integer);
			}
		}
	);
	public static final RandomNumberRule.RandomInt DUPE_HACK_BREAK_CHANCE = register(
		"dupe_hack_break_chance", 500, new RandomNumberRule.RandomInt(30, ClampedNormalInt.of(30.0F, 30.0F, 0, 100)) {
			protected Component valueDescription(Integer integer) {
				return Component.translatable("rule.dupe_hack_break_chance", integer);
			}
		}
	);
	public static final RandomNumberRule.RandomInt SPAWN_EGG_CHANCE = register(
		"spawn_egg_chance", 500, new RandomNumberRule.RandomInt(0, ClampedNormalInt.of(10.0F, 30.0F, 0, 100)) {
			protected Component valueDescription(Integer integer) {
				return Component.translatable("rule.spawn_egg_chance", integer);
			}
		}
	);
	public static final EnumRule<LightEngineMode> OPTIMIZE_LIGHT_ENGINE = register(
		"optimize_light_engine", 125, new EnumRule<LightEngineMode>(LightEngineMode.values(), LightEngineMode.NONE, LightEngineMode.CODEC) {
			protected Component valueDescription(LightEngineMode lightEngineMode) {
				return lightEngineMode.displayName();
			}
		}
	);
	public static final ResourceKeySetRule<EntityType<?>> RIDEABLE_ENTITIES = register(
		"rideable_entities", 1000, new ResourceKeySetRule<EntityType<?>>("entity", Registries.ENTITY_TYPE) {
			@Override
			protected Component description(Component component) {
				return Component.translatable("rule.rideable_entities", component);
			}
		}
	);
	public static final BooleanRule ENDERMEN_PICK_UP_ANYTHING = register(
		"endermen_pick_up_anything", 500, new BooleanRule(Component.translatable("rule.endermen_pick_up_anything"))
	);
	public static final BooleanRule ENDERMAN_BLOCK_UPDATE = register(
		"endermen_block_update", 500, new BooleanRule(Component.translatable("rule.endermen_block_update"))
	);
	public static final BooleanRule VOTING_FIREWORKS = register("voting_fireworks", 500, new BooleanRule(Component.translatable("rule.voting_fireworks")));
	public static final BooleanRule SNITCH = register("snitch", 500, new BooleanRule(Component.translatable("rule.snitch")));
	public static final BooleanRule GRAPPLING_FISHING_RODS = register(
		"grappling_fishing_rods", 500, new BooleanRule(Component.translatable("rule.grappling_fishing_rods"))
	);
	public static final BooleanRule BEELOONS = register("beeloons", 1000, new BooleanRule(Component.translatable("rule.beeloons")));
	public static final BooleanRule FISH_ANYTHING = register("fish_anything", 500, new BooleanRule(Component.translatable("rule.fish_anything")));
	public static final BooleanRule ONLY_MENDING_TRADES = register("only_mending_trades", 500, new BooleanRule(Component.translatable("rule.only_mending_trades")));
	public static final BooleanRule TRAILS_AND_TAILS = register("trails_and_tails", 500, new BooleanRule(Component.translatable("rule.trails_and_tails")));
	private static final SimpleWeightedRandomList<Holder.Reference<Rule>> WEIGHTED_LIST = BUILDER.build();

	private static ClampedInt flattenedPercents(int i) {
		return ClampedInt.of(UniformInt.of(-i, 100 + i), 0, 100);
	}

	private static <R extends Rule> R register(String string, int i, R rule) {
		Holder.Reference<Rule> reference = Registry.registerForHolder(BuiltInRegistries.RULE, new ResourceLocation(string), rule);
		BUILDER.add(reference, i);
		return rule;
	}

	public static final Rule bootstrap(Registry<Rule> registry) {
		return TEST_RULE_PLEASE_IGNORE;
	}

	public static final Holder.Reference<Rule> getRandomRule(RandomSource randomSource) {
		return (Holder.Reference<Rule>)WEIGHTED_LIST.getRandomValue(randomSource).orElseThrow();
	}

	public static final Holder.Reference<Rule> getRandomRuleUnweighted(RandomSource randomSource) {
		return (Holder.Reference<Rule>)WEIGHTED_LIST.getRandomValueUnweighted(randomSource).orElseThrow();
	}
}
