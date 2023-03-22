package net.minecraft.world.level.block.entity;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class DecoratedPotPatterns {
	private static final String BASE_NAME = "decorated_pot_base";
	public static final ResourceKey<String> BASE = create("decorated_pot_base");
	private static final String BRICK_NAME = "decorated_pot_side";
	private static final String ANGLER_NAME = "angler_pottery_pattern";
	private static final String ARCHER_NAME = "archer_pottery_pattern";
	private static final String ARMS_UP_NAME = "arms_up_pottery_pattern";
	private static final String BLADE_NAME = "blade_pottery_pattern";
	private static final String BREWER_NAME = "brewer_pottery_pattern";
	private static final String BURN_NAME = "burn_pottery_pattern";
	private static final String DANGER_NAME = "danger_pottery_pattern";
	private static final String EXPLORER_NAME = "explorer_pottery_pattern";
	private static final String FRIEND_NAME = "friend_pottery_pattern";
	private static final String HEART_NAME = "heart_pottery_pattern";
	private static final String HEARTBREAK_NAME = "heartbreak_pottery_pattern";
	private static final String HOWL_NAME = "howl_pottery_pattern";
	private static final String MINER_NAME = "miner_pottery_pattern";
	private static final String MOURNER_NAME = "mourner_pottery_pattern";
	private static final String PLENTY_NAME = "plenty_pottery_pattern";
	private static final String PRIZE_NAME = "prize_pottery_pattern";
	private static final String SHEAF_NAME = "sheaf_pottery_pattern";
	private static final String SHELTER_NAME = "shelter_pottery_pattern";
	private static final String SKULL_NAME = "skull_pottery_pattern";
	private static final String SNORT_NAME = "snort_pottery_pattern";
	private static final ResourceKey<String> BRICK = create("decorated_pot_side");
	private static final ResourceKey<String> ANGLER = create("angler_pottery_pattern");
	private static final ResourceKey<String> ARCHER = create("archer_pottery_pattern");
	private static final ResourceKey<String> ARMS_UP = create("arms_up_pottery_pattern");
	private static final ResourceKey<String> BLADE = create("blade_pottery_pattern");
	private static final ResourceKey<String> BREWER = create("brewer_pottery_pattern");
	private static final ResourceKey<String> BURN = create("burn_pottery_pattern");
	private static final ResourceKey<String> DANGER = create("danger_pottery_pattern");
	private static final ResourceKey<String> EXPLORER = create("explorer_pottery_pattern");
	private static final ResourceKey<String> FRIEND = create("friend_pottery_pattern");
	private static final ResourceKey<String> HEART = create("heart_pottery_pattern");
	private static final ResourceKey<String> HEARTBREAK = create("heartbreak_pottery_pattern");
	private static final ResourceKey<String> HOWL = create("howl_pottery_pattern");
	private static final ResourceKey<String> MINER = create("miner_pottery_pattern");
	private static final ResourceKey<String> MOURNER = create("mourner_pottery_pattern");
	private static final ResourceKey<String> PLENTY = create("plenty_pottery_pattern");
	private static final ResourceKey<String> PRIZE = create("prize_pottery_pattern");
	private static final ResourceKey<String> SHEAF = create("sheaf_pottery_pattern");
	private static final ResourceKey<String> SHELTER = create("shelter_pottery_pattern");
	private static final ResourceKey<String> SKULL = create("skull_pottery_pattern");
	private static final ResourceKey<String> SNORT = create("snort_pottery_pattern");
	private static final Map<Item, ResourceKey<String>> ITEM_TO_POT_TEXTURE = Map.ofEntries(
		Map.entry(Items.BRICK, BRICK),
		Map.entry(Items.ANGLER_POTTERY_SHARD, ANGLER),
		Map.entry(Items.ARCHER_POTTERY_SHARD, ARCHER),
		Map.entry(Items.ARMS_UP_POTTERY_SHARD, ARMS_UP),
		Map.entry(Items.BLADE_POTTERY_SHARD, BLADE),
		Map.entry(Items.BREWER_POTTERY_SHARD, BREWER),
		Map.entry(Items.BURN_POTTERY_SHARD, BURN),
		Map.entry(Items.DANGER_POTTERY_SHARD, DANGER),
		Map.entry(Items.EXPLORER_POTTERY_SHARD, EXPLORER),
		Map.entry(Items.FRIEND_POTTERY_SHARD, FRIEND),
		Map.entry(Items.HEART_POTTERY_SHARD, HEART),
		Map.entry(Items.HEARTBREAK_POTTERY_SHARD, HEARTBREAK),
		Map.entry(Items.HOWL_POTTERY_SHARD, HOWL),
		Map.entry(Items.MINER_POTTERY_SHARD, MINER),
		Map.entry(Items.MOURNER_POTTERY_SHARD, MOURNER),
		Map.entry(Items.PLENTY_POTTERY_SHARD, PLENTY),
		Map.entry(Items.PRIZE_POTTERY_SHARD, PRIZE),
		Map.entry(Items.SHEAF_POTTERY_SHARD, SHEAF),
		Map.entry(Items.SHELTER_POTTERY_SHARD, SHELTER),
		Map.entry(Items.SKULL_POTTERY_SHARD, SKULL),
		Map.entry(Items.SNORT_POTTERY_SHARD, SNORT)
	);

	private static ResourceKey<String> create(String string) {
		return ResourceKey.create(Registries.DECORATED_POT_PATTERNS, new ResourceLocation(string));
	}

	public static ResourceLocation location(ResourceKey<String> resourceKey) {
		return resourceKey.location().withPrefix("entity/decorated_pot/");
	}

	@Nullable
	public static ResourceKey<String> getResourceKey(Item item) {
		return (ResourceKey<String>)ITEM_TO_POT_TEXTURE.get(item);
	}

	public static String bootstrap(Registry<String> registry) {
		Registry.register(registry, BRICK, "decorated_pot_side");
		Registry.register(registry, ANGLER, "angler_pottery_pattern");
		Registry.register(registry, ARCHER, "archer_pottery_pattern");
		Registry.register(registry, ARMS_UP, "arms_up_pottery_pattern");
		Registry.register(registry, BLADE, "blade_pottery_pattern");
		Registry.register(registry, BREWER, "brewer_pottery_pattern");
		Registry.register(registry, BURN, "burn_pottery_pattern");
		Registry.register(registry, DANGER, "danger_pottery_pattern");
		Registry.register(registry, EXPLORER, "explorer_pottery_pattern");
		Registry.register(registry, FRIEND, "friend_pottery_pattern");
		Registry.register(registry, HEART, "heart_pottery_pattern");
		Registry.register(registry, HEARTBREAK, "heartbreak_pottery_pattern");
		Registry.register(registry, HOWL, "howl_pottery_pattern");
		Registry.register(registry, MINER, "miner_pottery_pattern");
		Registry.register(registry, MOURNER, "mourner_pottery_pattern");
		Registry.register(registry, PLENTY, "plenty_pottery_pattern");
		Registry.register(registry, PRIZE, "prize_pottery_pattern");
		Registry.register(registry, SHEAF, "sheaf_pottery_pattern");
		Registry.register(registry, SHELTER, "shelter_pottery_pattern");
		Registry.register(registry, SKULL, "skull_pottery_pattern");
		Registry.register(registry, SNORT, "snort_pottery_pattern");
		return Registry.register(registry, BASE, "decorated_pot_base");
	}
}
