package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EncoderCache;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.LockCode;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.saveddata.maps.MapId;

public class DataComponents {
	static final EncoderCache ENCODER_CACHE = new EncoderCache(512);
	public static final DataComponentType<CustomData> CUSTOM_DATA = register("custom_data", builder -> builder.persistent(CustomData.CODEC));
	public static final DataComponentType<Integer> MAX_STACK_SIZE = register(
		"max_stack_size", builder -> builder.persistent(ExtraCodecs.intRange(1, 99)).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static final DataComponentType<Integer> MAX_DAMAGE = register(
		"max_damage", builder -> builder.persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static final DataComponentType<Integer> DAMAGE = register(
		"damage", builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static final DataComponentType<Unbreakable> UNBREAKABLE = register(
		"unbreakable", builder -> builder.persistent(Unbreakable.CODEC).networkSynchronized(Unbreakable.STREAM_CODEC)
	);
	public static final DataComponentType<Component> CUSTOM_NAME = register(
		"custom_name", builder -> builder.persistent(ComponentSerialization.FLAT_CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<Component> ITEM_NAME = register(
		"item_name", builder -> builder.persistent(ComponentSerialization.FLAT_CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<ResourceLocation> ITEM_MODEL = register(
		"item_model", builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<ItemLore> LORE = register(
		"lore", builder -> builder.persistent(ItemLore.CODEC).networkSynchronized(ItemLore.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<Rarity> RARITY = register("rarity", builder -> builder.persistent(Rarity.CODEC).networkSynchronized(Rarity.STREAM_CODEC));
	public static final DataComponentType<ItemEnchantments> ENCHANTMENTS = register(
		"enchantments", builder -> builder.persistent(ItemEnchantments.CODEC).networkSynchronized(ItemEnchantments.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<AdventureModePredicate> CAN_PLACE_ON = register(
		"can_place_on", builder -> builder.persistent(AdventureModePredicate.CODEC).networkSynchronized(AdventureModePredicate.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<AdventureModePredicate> CAN_BREAK = register(
		"can_break", builder -> builder.persistent(AdventureModePredicate.CODEC).networkSynchronized(AdventureModePredicate.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<ItemAttributeModifiers> ATTRIBUTE_MODIFIERS = register(
		"attribute_modifiers", builder -> builder.persistent(ItemAttributeModifiers.CODEC).networkSynchronized(ItemAttributeModifiers.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<CustomModelData> CUSTOM_MODEL_DATA = register(
		"custom_model_data", builder -> builder.persistent(CustomModelData.CODEC).networkSynchronized(CustomModelData.STREAM_CODEC)
	);
	public static final DataComponentType<Unit> HIDE_ADDITIONAL_TOOLTIP = register(
		"hide_additional_tooltip", builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
	);
	public static final DataComponentType<Unit> HIDE_TOOLTIP = register(
		"hide_tooltip", builder -> builder.persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
	);
	public static final DataComponentType<Integer> REPAIR_COST = register(
		"repair_cost", builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);
	public static final DataComponentType<Unit> CREATIVE_SLOT_LOCK = register(
		"creative_slot_lock", builder -> builder.networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
	);
	public static final DataComponentType<Boolean> ENCHANTMENT_GLINT_OVERRIDE = register(
		"enchantment_glint_override", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);
	public static final DataComponentType<Unit> INTANGIBLE_PROJECTILE = register("intangible_projectile", builder -> builder.persistent(Unit.CODEC));
	public static final DataComponentType<FoodProperties> FOOD = register(
		"food", builder -> builder.persistent(FoodProperties.DIRECT_CODEC).networkSynchronized(FoodProperties.DIRECT_STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<Consumable> CONSUMABLE = register(
		"consumable", builder -> builder.persistent(Consumable.CODEC).networkSynchronized(Consumable.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<UseRemainder> USE_REMAINDER = register(
		"use_remainder", builder -> builder.persistent(UseRemainder.CODEC).networkSynchronized(UseRemainder.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<UseCooldown> USE_COOLDOWN = register(
		"use_cooldown", builder -> builder.persistent(UseCooldown.CODEC).networkSynchronized(UseCooldown.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<DamageResistant> DAMAGE_RESISTANT = register(
		"damage_resistant", builder -> builder.persistent(DamageResistant.CODEC).networkSynchronized(DamageResistant.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<Tool> TOOL = register(
		"tool", builder -> builder.persistent(Tool.CODEC).networkSynchronized(Tool.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<Enchantable> ENCHANTABLE = register(
		"enchantable", builder -> builder.persistent(Enchantable.CODEC).networkSynchronized(Enchantable.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<Equippable> EQUIPPABLE = register(
		"equippable", builder -> builder.persistent(Equippable.CODEC).networkSynchronized(Equippable.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<Repairable> REPAIRABLE = register(
		"repairable", builder -> builder.persistent(Repairable.CODEC).networkSynchronized(Repairable.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<Unit> GLIDER = register(
		"glider", builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
	);
	public static final DataComponentType<ResourceLocation> TOOLTIP_STYLE = register(
		"tooltip_style", builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<DeathProtection> DEATH_PROTECTION = register(
		"death_protection", builder -> builder.persistent(DeathProtection.CODEC).networkSynchronized(DeathProtection.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<ItemEnchantments> STORED_ENCHANTMENTS = register(
		"stored_enchantments", builder -> builder.persistent(ItemEnchantments.CODEC).networkSynchronized(ItemEnchantments.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<DyedItemColor> DYED_COLOR = register(
		"dyed_color", builder -> builder.persistent(DyedItemColor.CODEC).networkSynchronized(DyedItemColor.STREAM_CODEC)
	);
	public static final DataComponentType<MapItemColor> MAP_COLOR = register(
		"map_color", builder -> builder.persistent(MapItemColor.CODEC).networkSynchronized(MapItemColor.STREAM_CODEC)
	);
	public static final DataComponentType<MapId> MAP_ID = register("map_id", builder -> builder.persistent(MapId.CODEC).networkSynchronized(MapId.STREAM_CODEC));
	public static final DataComponentType<MapDecorations> MAP_DECORATIONS = register(
		"map_decorations", builder -> builder.persistent(MapDecorations.CODEC).cacheEncoding()
	);
	public static final DataComponentType<MapPostProcessing> MAP_POST_PROCESSING = register(
		"map_post_processing", builder -> builder.networkSynchronized(MapPostProcessing.STREAM_CODEC)
	);
	public static final DataComponentType<ChargedProjectiles> CHARGED_PROJECTILES = register(
		"charged_projectiles", builder -> builder.persistent(ChargedProjectiles.CODEC).networkSynchronized(ChargedProjectiles.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<BundleContents> BUNDLE_CONTENTS = register(
		"bundle_contents", builder -> builder.persistent(BundleContents.CODEC).networkSynchronized(BundleContents.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<PotionContents> POTION_CONTENTS = register(
		"potion_contents", builder -> builder.persistent(PotionContents.CODEC).networkSynchronized(PotionContents.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<SuspiciousStewEffects> SUSPICIOUS_STEW_EFFECTS = register(
		"suspicious_stew_effects", builder -> builder.persistent(SuspiciousStewEffects.CODEC).networkSynchronized(SuspiciousStewEffects.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<WritableBookContent> WRITABLE_BOOK_CONTENT = register(
		"writable_book_content", builder -> builder.persistent(WritableBookContent.CODEC).networkSynchronized(WritableBookContent.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<WrittenBookContent> WRITTEN_BOOK_CONTENT = register(
		"written_book_content", builder -> builder.persistent(WrittenBookContent.CODEC).networkSynchronized(WrittenBookContent.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<ArmorTrim> TRIM = register(
		"trim", builder -> builder.persistent(ArmorTrim.CODEC).networkSynchronized(ArmorTrim.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<DebugStickState> DEBUG_STICK_STATE = register(
		"debug_stick_state", builder -> builder.persistent(DebugStickState.CODEC).cacheEncoding()
	);
	public static final DataComponentType<CustomData> ENTITY_DATA = register(
		"entity_data", builder -> builder.persistent(CustomData.CODEC_WITH_ID).networkSynchronized(CustomData.STREAM_CODEC)
	);
	public static final DataComponentType<CustomData> BUCKET_ENTITY_DATA = register(
		"bucket_entity_data", builder -> builder.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC)
	);
	public static final DataComponentType<CustomData> BLOCK_ENTITY_DATA = register(
		"block_entity_data", builder -> builder.persistent(CustomData.CODEC_WITH_ID).networkSynchronized(CustomData.STREAM_CODEC)
	);
	public static final DataComponentType<Holder<Instrument>> INSTRUMENT = register(
		"instrument", builder -> builder.persistent(Instrument.CODEC).networkSynchronized(Instrument.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<OminousBottleAmplifier> OMINOUS_BOTTLE_AMPLIFIER = register(
		"ominous_bottle_amplifier", builder -> builder.persistent(OminousBottleAmplifier.CODEC).networkSynchronized(OminousBottleAmplifier.STREAM_CODEC)
	);
	public static final DataComponentType<JukeboxPlayable> JUKEBOX_PLAYABLE = register(
		"jukebox_playable", builder -> builder.persistent(JukeboxPlayable.CODEC).networkSynchronized(JukeboxPlayable.STREAM_CODEC)
	);
	public static final DataComponentType<List<ResourceKey<Recipe<?>>>> RECIPES = register(
		"recipes", builder -> builder.persistent(ResourceKey.codec(Registries.RECIPE).listOf()).cacheEncoding()
	);
	public static final DataComponentType<LodestoneTracker> LODESTONE_TRACKER = register(
		"lodestone_tracker", builder -> builder.persistent(LodestoneTracker.CODEC).networkSynchronized(LodestoneTracker.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<FireworkExplosion> FIREWORK_EXPLOSION = register(
		"firework_explosion", builder -> builder.persistent(FireworkExplosion.CODEC).networkSynchronized(FireworkExplosion.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<Fireworks> FIREWORKS = register(
		"fireworks", builder -> builder.persistent(Fireworks.CODEC).networkSynchronized(Fireworks.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<ResolvableProfile> PROFILE = register(
		"profile", builder -> builder.persistent(ResolvableProfile.CODEC).networkSynchronized(ResolvableProfile.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<ResourceLocation> NOTE_BLOCK_SOUND = register(
		"note_block_sound", builder -> builder.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC)
	);
	public static final DataComponentType<BannerPatternLayers> BANNER_PATTERNS = register(
		"banner_patterns", builder -> builder.persistent(BannerPatternLayers.CODEC).networkSynchronized(BannerPatternLayers.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<DyeColor> BASE_COLOR = register(
		"base_color", builder -> builder.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC)
	);
	public static final DataComponentType<PotDecorations> POT_DECORATIONS = register(
		"pot_decorations", builder -> builder.persistent(PotDecorations.CODEC).networkSynchronized(PotDecorations.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<ItemContainerContents> CONTAINER = register(
		"container", builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<BlockItemStateProperties> BLOCK_STATE = register(
		"block_state", builder -> builder.persistent(BlockItemStateProperties.CODEC).networkSynchronized(BlockItemStateProperties.STREAM_CODEC).cacheEncoding()
	);
	public static final DataComponentType<List<BeehiveBlockEntity.Occupant>> BEES = register(
		"bees",
		builder -> builder.persistent(BeehiveBlockEntity.Occupant.LIST_CODEC)
				.networkSynchronized(BeehiveBlockEntity.Occupant.STREAM_CODEC.apply(ByteBufCodecs.list()))
				.cacheEncoding()
	);
	public static final DataComponentType<LockCode> LOCK = register("lock", builder -> builder.persistent(LockCode.CODEC));
	public static final DataComponentType<SeededContainerLoot> CONTAINER_LOOT = register(
		"container_loot", builder -> builder.persistent(SeededContainerLoot.CODEC)
	);
	public static final DataComponentMap COMMON_ITEM_COMPONENTS = DataComponentMap.builder()
		.set(MAX_STACK_SIZE, 64)
		.set(LORE, ItemLore.EMPTY)
		.set(ENCHANTMENTS, ItemEnchantments.EMPTY)
		.set(REPAIR_COST, 0)
		.set(ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
		.set(RARITY, Rarity.COMMON)
		.build();

	public static DataComponentType<?> bootstrap(Registry<DataComponentType<?>> registry) {
		return CUSTOM_DATA;
	}

	private static <T> DataComponentType<T> register(String string, UnaryOperator<DataComponentType.Builder<T>> unaryOperator) {
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, string, ((DataComponentType.Builder)unaryOperator.apply(DataComponentType.builder())).build());
	}
}
