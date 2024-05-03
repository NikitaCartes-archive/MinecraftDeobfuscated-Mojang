package net.minecraft.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public abstract class EntityLootSubProvider implements LootTableSubProvider {
	private static final Set<EntityType<?>> SPECIAL_LOOT_TABLE_TYPES = ImmutableSet.of(
		EntityType.PLAYER, EntityType.ARMOR_STAND, EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.VILLAGER
	);
	protected final HolderLookup.Provider registries;
	private final FeatureFlagSet allowed;
	private final FeatureFlagSet required;
	private final Map<EntityType<?>, Map<ResourceKey<LootTable>, LootTable.Builder>> map = Maps.<EntityType<?>, Map<ResourceKey<LootTable>, LootTable.Builder>>newHashMap();

	protected final AnyOfCondition.Builder shouldSmeltLoot() {
		HolderLookup.RegistryLookup<Enchantment> registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
		return AnyOfCondition.anyOf(
			LootItemEntityPropertyCondition.hasProperties(
				LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().flags(EntityFlagsPredicate.Builder.flags().setOnFire(true))
			),
			LootItemEntityPropertyCondition.hasProperties(
				LootContext.EntityTarget.DIRECT_ATTACKER,
				EntityPredicate.Builder.entity()
					.equipment(
						EntityEquipmentPredicate.Builder.equipment()
							.mainhand(
								ItemPredicate.Builder.item()
									.withSubPredicate(
										ItemSubPredicates.ENCHANTMENTS,
										ItemEnchantmentsPredicate.enchantments(
											List.of(new EnchantmentPredicate(registryLookup.getOrThrow(EnchantmentTags.SMELTS_LOOT), MinMaxBounds.Ints.ANY))
										)
									)
							)
					)
			)
		);
	}

	protected EntityLootSubProvider(FeatureFlagSet featureFlagSet, HolderLookup.Provider provider) {
		this(featureFlagSet, featureFlagSet, provider);
	}

	protected EntityLootSubProvider(FeatureFlagSet featureFlagSet, FeatureFlagSet featureFlagSet2, HolderLookup.Provider provider) {
		this.allowed = featureFlagSet;
		this.required = featureFlagSet2;
		this.registries = provider;
	}

	protected static LootTable.Builder createSheepTable(ItemLike itemLike) {
		return LootTable.lootTable()
			.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(itemLike)))
			.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(NestedLootTable.lootTableReference(EntityType.SHEEP.getDefaultLootTable())));
	}

	public abstract void generate();

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
		this.generate();
		Set<ResourceKey<LootTable>> set = new HashSet();
		BuiltInRegistries.ENTITY_TYPE
			.holders()
			.forEach(
				reference -> {
					EntityType<?> entityType = (EntityType<?>)reference.value();
					if (entityType.isEnabled(this.allowed)) {
						if (canHaveLootTable(entityType)) {
							Map<ResourceKey<LootTable>, LootTable.Builder> map = (Map<ResourceKey<LootTable>, LootTable.Builder>)this.map.remove(entityType);
							ResourceKey<LootTable> resourceKey = entityType.getDefaultLootTable();
							if (resourceKey != BuiltInLootTables.EMPTY && entityType.isEnabled(this.required) && (map == null || !map.containsKey(resourceKey))) {
								throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", resourceKey, reference.key().location()));
							}

							if (map != null) {
								map.forEach((resourceKeyx, builder) -> {
									if (!set.add(resourceKeyx)) {
										throw new IllegalStateException(String.format(Locale.ROOT, "Duplicate loottable '%s' for '%s'", resourceKeyx, reference.key().location()));
									} else {
										biConsumer.accept(resourceKeyx, builder);
									}
								});
							}
						} else {
							Map<ResourceKey<LootTable>, LootTable.Builder> mapx = (Map<ResourceKey<LootTable>, LootTable.Builder>)this.map.remove(entityType);
							if (mapx != null) {
								throw new IllegalStateException(
									String.format(
										Locale.ROOT,
										"Weird loottables '%s' for '%s', not a LivingEntity so should not have loot",
										mapx.keySet().stream().map(resourceKeyx -> resourceKeyx.location().toString()).collect(Collectors.joining(",")),
										reference.key().location()
									)
								);
							}
						}
					}
				}
			);
		if (!this.map.isEmpty()) {
			throw new IllegalStateException("Created loot tables for entities not supported by datapack: " + this.map.keySet());
		}
	}

	private static boolean canHaveLootTable(EntityType<?> entityType) {
		return SPECIAL_LOOT_TABLE_TYPES.contains(entityType) || entityType.getCategory() != MobCategory.MISC;
	}

	protected LootItemCondition.Builder killedByFrog() {
		return DamageSourceCondition.hasDamageSource(DamageSourcePredicate.Builder.damageType().source(EntityPredicate.Builder.entity().of(EntityType.FROG)));
	}

	protected LootItemCondition.Builder killedByFrogVariant(ResourceKey<FrogVariant> resourceKey) {
		return DamageSourceCondition.hasDamageSource(
			DamageSourcePredicate.Builder.damageType()
				.source(
					EntityPredicate.Builder.entity()
						.of(EntityType.FROG)
						.subPredicate(EntitySubPredicates.frogVariant(BuiltInRegistries.FROG_VARIANT.getHolderOrThrow(resourceKey)))
				)
		);
	}

	protected void add(EntityType<?> entityType, LootTable.Builder builder) {
		this.add(entityType, entityType.getDefaultLootTable(), builder);
	}

	protected void add(EntityType<?> entityType, ResourceKey<LootTable> resourceKey, LootTable.Builder builder) {
		((Map)this.map.computeIfAbsent(entityType, entityTypex -> new HashMap())).put(resourceKey, builder);
	}
}
