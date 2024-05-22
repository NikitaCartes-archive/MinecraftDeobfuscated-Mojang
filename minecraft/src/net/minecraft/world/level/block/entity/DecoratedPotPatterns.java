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
	public static final ResourceKey<DecoratedPotPattern> BLANK = create("blank");
	public static final ResourceKey<DecoratedPotPattern> ANGLER = create("angler");
	public static final ResourceKey<DecoratedPotPattern> ARCHER = create("archer");
	public static final ResourceKey<DecoratedPotPattern> ARMS_UP = create("arms_up");
	public static final ResourceKey<DecoratedPotPattern> BLADE = create("blade");
	public static final ResourceKey<DecoratedPotPattern> BREWER = create("brewer");
	public static final ResourceKey<DecoratedPotPattern> BURN = create("burn");
	public static final ResourceKey<DecoratedPotPattern> DANGER = create("danger");
	public static final ResourceKey<DecoratedPotPattern> EXPLORER = create("explorer");
	public static final ResourceKey<DecoratedPotPattern> FLOW = create("flow");
	public static final ResourceKey<DecoratedPotPattern> FRIEND = create("friend");
	public static final ResourceKey<DecoratedPotPattern> GUSTER = create("guster");
	public static final ResourceKey<DecoratedPotPattern> HEART = create("heart");
	public static final ResourceKey<DecoratedPotPattern> HEARTBREAK = create("heartbreak");
	public static final ResourceKey<DecoratedPotPattern> HOWL = create("howl");
	public static final ResourceKey<DecoratedPotPattern> MINER = create("miner");
	public static final ResourceKey<DecoratedPotPattern> MOURNER = create("mourner");
	public static final ResourceKey<DecoratedPotPattern> PLENTY = create("plenty");
	public static final ResourceKey<DecoratedPotPattern> PRIZE = create("prize");
	public static final ResourceKey<DecoratedPotPattern> SCRAPE = create("scrape");
	public static final ResourceKey<DecoratedPotPattern> SHEAF = create("sheaf");
	public static final ResourceKey<DecoratedPotPattern> SHELTER = create("shelter");
	public static final ResourceKey<DecoratedPotPattern> SKULL = create("skull");
	public static final ResourceKey<DecoratedPotPattern> SNORT = create("snort");
	private static final Map<Item, ResourceKey<DecoratedPotPattern>> ITEM_TO_POT_TEXTURE = Map.ofEntries(
		Map.entry(Items.BRICK, BLANK),
		Map.entry(Items.ANGLER_POTTERY_SHERD, ANGLER),
		Map.entry(Items.ARCHER_POTTERY_SHERD, ARCHER),
		Map.entry(Items.ARMS_UP_POTTERY_SHERD, ARMS_UP),
		Map.entry(Items.BLADE_POTTERY_SHERD, BLADE),
		Map.entry(Items.BREWER_POTTERY_SHERD, BREWER),
		Map.entry(Items.BURN_POTTERY_SHERD, BURN),
		Map.entry(Items.DANGER_POTTERY_SHERD, DANGER),
		Map.entry(Items.EXPLORER_POTTERY_SHERD, EXPLORER),
		Map.entry(Items.FLOW_POTTERY_SHERD, FLOW),
		Map.entry(Items.FRIEND_POTTERY_SHERD, FRIEND),
		Map.entry(Items.GUSTER_POTTERY_SHERD, GUSTER),
		Map.entry(Items.HEART_POTTERY_SHERD, HEART),
		Map.entry(Items.HEARTBREAK_POTTERY_SHERD, HEARTBREAK),
		Map.entry(Items.HOWL_POTTERY_SHERD, HOWL),
		Map.entry(Items.MINER_POTTERY_SHERD, MINER),
		Map.entry(Items.MOURNER_POTTERY_SHERD, MOURNER),
		Map.entry(Items.PLENTY_POTTERY_SHERD, PLENTY),
		Map.entry(Items.PRIZE_POTTERY_SHERD, PRIZE),
		Map.entry(Items.SCRAPE_POTTERY_SHERD, SCRAPE),
		Map.entry(Items.SHEAF_POTTERY_SHERD, SHEAF),
		Map.entry(Items.SHELTER_POTTERY_SHERD, SHELTER),
		Map.entry(Items.SKULL_POTTERY_SHERD, SKULL),
		Map.entry(Items.SNORT_POTTERY_SHERD, SNORT)
	);

	@Nullable
	public static ResourceKey<DecoratedPotPattern> getPatternFromItem(Item item) {
		return (ResourceKey<DecoratedPotPattern>)ITEM_TO_POT_TEXTURE.get(item);
	}

	private static ResourceKey<DecoratedPotPattern> create(String string) {
		return ResourceKey.create(Registries.DECORATED_POT_PATTERN, ResourceLocation.withDefaultNamespace(string));
	}

	public static DecoratedPotPattern bootstrap(Registry<DecoratedPotPattern> registry) {
		register(registry, ANGLER, "angler_pottery_pattern");
		register(registry, ARCHER, "archer_pottery_pattern");
		register(registry, ARMS_UP, "arms_up_pottery_pattern");
		register(registry, BLADE, "blade_pottery_pattern");
		register(registry, BREWER, "brewer_pottery_pattern");
		register(registry, BURN, "burn_pottery_pattern");
		register(registry, DANGER, "danger_pottery_pattern");
		register(registry, EXPLORER, "explorer_pottery_pattern");
		register(registry, FLOW, "flow_pottery_pattern");
		register(registry, FRIEND, "friend_pottery_pattern");
		register(registry, GUSTER, "guster_pottery_pattern");
		register(registry, HEART, "heart_pottery_pattern");
		register(registry, HEARTBREAK, "heartbreak_pottery_pattern");
		register(registry, HOWL, "howl_pottery_pattern");
		register(registry, MINER, "miner_pottery_pattern");
		register(registry, MOURNER, "mourner_pottery_pattern");
		register(registry, PLENTY, "plenty_pottery_pattern");
		register(registry, PRIZE, "prize_pottery_pattern");
		register(registry, SCRAPE, "scrape_pottery_pattern");
		register(registry, SHEAF, "sheaf_pottery_pattern");
		register(registry, SHELTER, "shelter_pottery_pattern");
		register(registry, SKULL, "skull_pottery_pattern");
		register(registry, SNORT, "snort_pottery_pattern");
		return register(registry, BLANK, "decorated_pot_side");
	}

	private static DecoratedPotPattern register(Registry<DecoratedPotPattern> registry, ResourceKey<DecoratedPotPattern> resourceKey, String string) {
		return Registry.register(registry, resourceKey, new DecoratedPotPattern(ResourceLocation.withDefaultNamespace(string)));
	}
}
