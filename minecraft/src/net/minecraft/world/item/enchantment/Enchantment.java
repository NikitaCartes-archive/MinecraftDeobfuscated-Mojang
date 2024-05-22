package net.minecraft.world.item.enchantment;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.effects.DamageImmunity;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableFloat;

public record Enchantment(Component description, Enchantment.EnchantmentDefinition definition, HolderSet<Enchantment> exclusiveSet, DataComponentMap effects) {
	public static final int MAX_LEVEL = 255;
	public static final Codec<Enchantment> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ComponentSerialization.CODEC.fieldOf("description").forGetter(Enchantment::description),
					Enchantment.EnchantmentDefinition.CODEC.forGetter(Enchantment::definition),
					RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("exclusive_set", HolderSet.direct()).forGetter(Enchantment::exclusiveSet),
					EnchantmentEffectComponents.CODEC.optionalFieldOf("effects", DataComponentMap.EMPTY).forGetter(Enchantment::effects)
				)
				.apply(instance, Enchantment::new)
	);
	public static final Codec<Holder<Enchantment>> CODEC = RegistryFixedCodec.create(Registries.ENCHANTMENT);
	public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Enchantment>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ENCHANTMENT);

	public static Enchantment.Cost constantCost(int i) {
		return new Enchantment.Cost(i, 0);
	}

	public static Enchantment.Cost dynamicCost(int i, int j) {
		return new Enchantment.Cost(i, j);
	}

	public static Enchantment.EnchantmentDefinition definition(
		HolderSet<Item> holderSet,
		HolderSet<Item> holderSet2,
		int i,
		int j,
		Enchantment.Cost cost,
		Enchantment.Cost cost2,
		int k,
		EquipmentSlotGroup... equipmentSlotGroups
	) {
		return new Enchantment.EnchantmentDefinition(holderSet, Optional.of(holderSet2), i, j, cost, cost2, k, List.of(equipmentSlotGroups));
	}

	public static Enchantment.EnchantmentDefinition definition(
		HolderSet<Item> holderSet, int i, int j, Enchantment.Cost cost, Enchantment.Cost cost2, int k, EquipmentSlotGroup... equipmentSlotGroups
	) {
		return new Enchantment.EnchantmentDefinition(holderSet, Optional.empty(), i, j, cost, cost2, k, List.of(equipmentSlotGroups));
	}

	public Map<EquipmentSlot, ItemStack> getSlotItems(LivingEntity livingEntity) {
		Map<EquipmentSlot, ItemStack> map = Maps.newEnumMap(EquipmentSlot.class);

		for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
			if (this.matchingSlot(equipmentSlot)) {
				ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
				if (!itemStack.isEmpty()) {
					map.put(equipmentSlot, itemStack);
				}
			}
		}

		return map;
	}

	public HolderSet<Item> getSupportedItems() {
		return this.definition.supportedItems();
	}

	public boolean matchingSlot(EquipmentSlot equipmentSlot) {
		return this.definition.slots().stream().anyMatch(equipmentSlotGroup -> equipmentSlotGroup.test(equipmentSlot));
	}

	public boolean isPrimaryItem(ItemStack itemStack) {
		return this.isSupportedItem(itemStack) && (this.definition.primaryItems.isEmpty() || itemStack.is((HolderSet<Item>)this.definition.primaryItems.get()));
	}

	public boolean isSupportedItem(ItemStack itemStack) {
		return itemStack.is(this.definition.supportedItems);
	}

	public int getWeight() {
		return this.definition.weight();
	}

	public int getAnvilCost() {
		return this.definition.anvilCost();
	}

	public int getMinLevel() {
		return 1;
	}

	public int getMaxLevel() {
		return this.definition.maxLevel();
	}

	public int getMinCost(int i) {
		return this.definition.minCost().calculate(i);
	}

	public int getMaxCost(int i) {
		return this.definition.maxCost().calculate(i);
	}

	public String toString() {
		return "Enchantment " + this.description.getString();
	}

	public static boolean areCompatible(Holder<Enchantment> holder, Holder<Enchantment> holder2) {
		return !holder.equals(holder2) && !holder.value().exclusiveSet.contains(holder2) && !holder2.value().exclusiveSet.contains(holder);
	}

	public static Component getFullname(Holder<Enchantment> holder, int i) {
		MutableComponent mutableComponent = holder.value().description.copy();
		if (holder.is(EnchantmentTags.CURSE)) {
			ComponentUtils.mergeStyles(mutableComponent, Style.EMPTY.withColor(ChatFormatting.RED));
		} else {
			ComponentUtils.mergeStyles(mutableComponent, Style.EMPTY.withColor(ChatFormatting.GRAY));
		}

		if (i != 1 || holder.value().getMaxLevel() != 1) {
			mutableComponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + i));
		}

		return mutableComponent;
	}

	public boolean canEnchant(ItemStack itemStack) {
		return this.definition.supportedItems().contains(itemStack.getItemHolder());
	}

	public <T> List<T> getEffects(DataComponentType<List<T>> dataComponentType) {
		return this.effects.getOrDefault(dataComponentType, List.of());
	}

	public boolean isImmuneToDamage(ServerLevel serverLevel, int i, Entity entity, DamageSource damageSource) {
		LootContext lootContext = damageContext(serverLevel, i, entity, damageSource);

		for (ConditionalEffect<DamageImmunity> conditionalEffect : this.getEffects(EnchantmentEffectComponents.DAMAGE_IMMUNITY)) {
			if (conditionalEffect.matches(lootContext)) {
				return true;
			}
		}

		return false;
	}

	public void modifyDamageProtection(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, DamageSource damageSource, MutableFloat mutableFloat) {
		LootContext lootContext = damageContext(serverLevel, i, entity, damageSource);

		for (ConditionalEffect<EnchantmentValueEffect> conditionalEffect : this.getEffects(EnchantmentEffectComponents.DAMAGE_PROTECTION)) {
			if (conditionalEffect.matches(lootContext)) {
				mutableFloat.setValue(conditionalEffect.effect().process(i, entity.getRandom(), mutableFloat.floatValue()));
			}
		}
	}

	public void modifyDurabilityChange(ServerLevel serverLevel, int i, ItemStack itemStack, MutableFloat mutableFloat) {
		this.modifyItemFilteredCount(EnchantmentEffectComponents.ITEM_DAMAGE, serverLevel, i, itemStack, mutableFloat);
	}

	public void modifyAmmoCount(ServerLevel serverLevel, int i, ItemStack itemStack, MutableFloat mutableFloat) {
		this.modifyItemFilteredCount(EnchantmentEffectComponents.AMMO_USE, serverLevel, i, itemStack, mutableFloat);
	}

	public void modifyPiercingCount(ServerLevel serverLevel, int i, ItemStack itemStack, MutableFloat mutableFloat) {
		this.modifyItemFilteredCount(EnchantmentEffectComponents.PROJECTILE_PIERCING, serverLevel, i, itemStack, mutableFloat);
	}

	public void modifyBlockExperience(ServerLevel serverLevel, int i, ItemStack itemStack, MutableFloat mutableFloat) {
		this.modifyItemFilteredCount(EnchantmentEffectComponents.BLOCK_EXPERIENCE, serverLevel, i, itemStack, mutableFloat);
	}

	public void modifyMobExperience(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
		this.modifyEntityFilteredValue(EnchantmentEffectComponents.MOB_EXPERIENCE, serverLevel, i, itemStack, entity, mutableFloat);
	}

	public void modifyDurabilityToRepairFromXp(ServerLevel serverLevel, int i, ItemStack itemStack, MutableFloat mutableFloat) {
		this.modifyItemFilteredCount(EnchantmentEffectComponents.REPAIR_WITH_XP, serverLevel, i, itemStack, mutableFloat);
	}

	public void modifyTridentReturnToOwnerAcceleration(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
		this.modifyEntityFilteredValue(EnchantmentEffectComponents.TRIDENT_RETURN_ACCELERATION, serverLevel, i, itemStack, entity, mutableFloat);
	}

	public void modifyTridentSpinAttackStrength(RandomSource randomSource, int i, MutableFloat mutableFloat) {
		this.modifyUnfilteredValue(EnchantmentEffectComponents.TRIDENT_SPIN_ATTACK_STRENGTH, randomSource, i, mutableFloat);
	}

	public void modifyFishingTimeReduction(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
		this.modifyEntityFilteredValue(EnchantmentEffectComponents.FISHING_TIME_REDUCTION, serverLevel, i, itemStack, entity, mutableFloat);
	}

	public void modifyFishingLuckBonus(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
		this.modifyEntityFilteredValue(EnchantmentEffectComponents.FISHING_LUCK_BONUS, serverLevel, i, itemStack, entity, mutableFloat);
	}

	public void modifyDamage(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, DamageSource damageSource, MutableFloat mutableFloat) {
		this.modifyDamageFilteredValue(EnchantmentEffectComponents.DAMAGE, serverLevel, i, itemStack, entity, damageSource, mutableFloat);
	}

	public void modifyFallBasedDamage(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, DamageSource damageSource, MutableFloat mutableFloat) {
		this.modifyDamageFilteredValue(EnchantmentEffectComponents.SMASH_DAMAGE_PER_FALLEN_BLOCK, serverLevel, i, itemStack, entity, damageSource, mutableFloat);
	}

	public void modifyKnockback(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, DamageSource damageSource, MutableFloat mutableFloat) {
		this.modifyDamageFilteredValue(EnchantmentEffectComponents.KNOCKBACK, serverLevel, i, itemStack, entity, damageSource, mutableFloat);
	}

	public void modifyArmorEffectivness(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, DamageSource damageSource, MutableFloat mutableFloat) {
		this.modifyDamageFilteredValue(EnchantmentEffectComponents.ARMOR_EFFECTIVENESS, serverLevel, i, itemStack, entity, damageSource, mutableFloat);
	}

	public static void doPostAttack(
		TargetedConditionalEffect<EnchantmentEntityEffect> targetedConditionalEffect,
		ServerLevel serverLevel,
		int i,
		EnchantedItemInUse enchantedItemInUse,
		Entity entity,
		DamageSource damageSource
	) {
		if (targetedConditionalEffect.matches(damageContext(serverLevel, i, entity, damageSource))) {
			Entity entity2 = switch (targetedConditionalEffect.affected()) {
				case ATTACKER -> damageSource.getEntity();
				case DAMAGING_ENTITY -> damageSource.getDirectEntity();
				case VICTIM -> entity;
			};
			if (entity2 != null) {
				targetedConditionalEffect.effect().apply(serverLevel, i, enchantedItemInUse, entity2, entity2.position());
			}
		}
	}

	public void doPostAttack(
		ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, EnchantmentTarget enchantmentTarget, Entity entity, DamageSource damageSource
	) {
		for (TargetedConditionalEffect<EnchantmentEntityEffect> targetedConditionalEffect : this.getEffects(EnchantmentEffectComponents.POST_ATTACK)) {
			if (enchantmentTarget == targetedConditionalEffect.enchanted()) {
				doPostAttack(targetedConditionalEffect, serverLevel, i, enchantedItemInUse, entity, damageSource);
			}
		}
	}

	public void modifyProjectileCount(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
		this.modifyEntityFilteredValue(EnchantmentEffectComponents.PROJECTILE_COUNT, serverLevel, i, itemStack, entity, mutableFloat);
	}

	public void modifyProjectileSpread(ServerLevel serverLevel, int i, ItemStack itemStack, Entity entity, MutableFloat mutableFloat) {
		this.modifyEntityFilteredValue(EnchantmentEffectComponents.PROJECTILE_SPREAD, serverLevel, i, itemStack, entity, mutableFloat);
	}

	public void modifyCrossbowChargeTime(RandomSource randomSource, int i, MutableFloat mutableFloat) {
		this.modifyUnfilteredValue(EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME, randomSource, i, mutableFloat);
	}

	public void modifyUnfilteredValue(DataComponentType<EnchantmentValueEffect> dataComponentType, RandomSource randomSource, int i, MutableFloat mutableFloat) {
		EnchantmentValueEffect enchantmentValueEffect = this.effects.get(dataComponentType);
		if (enchantmentValueEffect != null) {
			mutableFloat.setValue(enchantmentValueEffect.process(i, randomSource, mutableFloat.floatValue()));
		}
	}

	public void tick(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity) {
		applyEffects(
			this.getEffects(EnchantmentEffectComponents.TICK),
			entityContext(serverLevel, i, entity, entity.position()),
			enchantmentEntityEffect -> enchantmentEntityEffect.apply(serverLevel, i, enchantedItemInUse, entity, entity.position())
		);
	}

	public void onProjectileSpawned(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity) {
		applyEffects(
			this.getEffects(EnchantmentEffectComponents.PROJECTILE_SPAWNED),
			entityContext(serverLevel, i, entity, entity.position()),
			enchantmentEntityEffect -> enchantmentEntityEffect.apply(serverLevel, i, enchantedItemInUse, entity, entity.position())
		);
	}

	public void onHitBlock(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3, BlockState blockState) {
		applyEffects(
			this.getEffects(EnchantmentEffectComponents.HIT_BLOCK),
			blockHitContext(serverLevel, i, entity, vec3, blockState),
			enchantmentEntityEffect -> enchantmentEntityEffect.apply(serverLevel, i, enchantedItemInUse, entity, vec3)
		);
	}

	private void modifyItemFilteredCount(
		DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> dataComponentType,
		ServerLevel serverLevel,
		int i,
		ItemStack itemStack,
		MutableFloat mutableFloat
	) {
		applyEffects(
			this.getEffects(dataComponentType),
			itemContext(serverLevel, i, itemStack),
			enchantmentValueEffect -> mutableFloat.setValue(enchantmentValueEffect.process(i, serverLevel.getRandom(), mutableFloat.getValue()))
		);
	}

	private void modifyEntityFilteredValue(
		DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> dataComponentType,
		ServerLevel serverLevel,
		int i,
		ItemStack itemStack,
		Entity entity,
		MutableFloat mutableFloat
	) {
		applyEffects(
			this.getEffects(dataComponentType),
			entityContext(serverLevel, i, entity, entity.position()),
			enchantmentValueEffect -> mutableFloat.setValue(enchantmentValueEffect.process(i, entity.getRandom(), mutableFloat.floatValue()))
		);
	}

	private void modifyDamageFilteredValue(
		DataComponentType<List<ConditionalEffect<EnchantmentValueEffect>>> dataComponentType,
		ServerLevel serverLevel,
		int i,
		ItemStack itemStack,
		Entity entity,
		DamageSource damageSource,
		MutableFloat mutableFloat
	) {
		applyEffects(
			this.getEffects(dataComponentType),
			damageContext(serverLevel, i, entity, damageSource),
			enchantmentValueEffect -> mutableFloat.setValue(enchantmentValueEffect.process(i, entity.getRandom(), mutableFloat.floatValue()))
		);
	}

	public static LootContext damageContext(ServerLevel serverLevel, int i, Entity entity, DamageSource damageSource) {
		LootParams lootParams = new LootParams.Builder(serverLevel)
			.withParameter(LootContextParams.THIS_ENTITY, entity)
			.withParameter(LootContextParams.ENCHANTMENT_LEVEL, i)
			.withParameter(LootContextParams.ORIGIN, entity.position())
			.withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
			.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.getEntity())
			.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.getDirectEntity())
			.create(LootContextParamSets.ENCHANTED_DAMAGE);
		return new LootContext.Builder(lootParams).create(Optional.empty());
	}

	private static LootContext itemContext(ServerLevel serverLevel, int i, ItemStack itemStack) {
		LootParams lootParams = new LootParams.Builder(serverLevel)
			.withParameter(LootContextParams.TOOL, itemStack)
			.withParameter(LootContextParams.ENCHANTMENT_LEVEL, i)
			.create(LootContextParamSets.ENCHANTED_ITEM);
		return new LootContext.Builder(lootParams).create(Optional.empty());
	}

	private static LootContext locationContext(ServerLevel serverLevel, int i, Entity entity, boolean bl) {
		LootParams lootParams = new LootParams.Builder(serverLevel)
			.withParameter(LootContextParams.THIS_ENTITY, entity)
			.withParameter(LootContextParams.ENCHANTMENT_LEVEL, i)
			.withParameter(LootContextParams.ORIGIN, entity.position())
			.withParameter(LootContextParams.ENCHANTMENT_ACTIVE, bl)
			.create(LootContextParamSets.ENCHANTED_LOCATION);
		return new LootContext.Builder(lootParams).create(Optional.empty());
	}

	private static LootContext entityContext(ServerLevel serverLevel, int i, Entity entity, Vec3 vec3) {
		LootParams lootParams = new LootParams.Builder(serverLevel)
			.withParameter(LootContextParams.THIS_ENTITY, entity)
			.withParameter(LootContextParams.ENCHANTMENT_LEVEL, i)
			.withParameter(LootContextParams.ORIGIN, vec3)
			.create(LootContextParamSets.ENCHANTED_ENTITY);
		return new LootContext.Builder(lootParams).create(Optional.empty());
	}

	private static LootContext blockHitContext(ServerLevel serverLevel, int i, Entity entity, Vec3 vec3, BlockState blockState) {
		LootParams lootParams = new LootParams.Builder(serverLevel)
			.withParameter(LootContextParams.THIS_ENTITY, entity)
			.withParameter(LootContextParams.ENCHANTMENT_LEVEL, i)
			.withParameter(LootContextParams.ORIGIN, vec3)
			.withParameter(LootContextParams.BLOCK_STATE, blockState)
			.create(LootContextParamSets.HIT_BLOCK);
		return new LootContext.Builder(lootParams).create(Optional.empty());
	}

	private static <T> void applyEffects(List<ConditionalEffect<T>> list, LootContext lootContext, Consumer<T> consumer) {
		for (ConditionalEffect<T> conditionalEffect : list) {
			if (conditionalEffect.matches(lootContext)) {
				consumer.accept(conditionalEffect.effect());
			}
		}
	}

	public void runLocationChangedEffects(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, LivingEntity livingEntity) {
		if (enchantedItemInUse.inSlot() != null && !this.matchingSlot(enchantedItemInUse.inSlot())) {
			Set<EnchantmentLocationBasedEffect> set = (Set<EnchantmentLocationBasedEffect>)livingEntity.activeLocationDependentEnchantments().remove(this);
			if (set != null) {
				set.forEach(enchantmentLocationBasedEffectx -> enchantmentLocationBasedEffectx.onDeactivated(enchantedItemInUse, livingEntity, livingEntity.position(), i));
			}
		} else {
			Set<EnchantmentLocationBasedEffect> set = (Set<EnchantmentLocationBasedEffect>)livingEntity.activeLocationDependentEnchantments().get(this);

			for (ConditionalEffect<EnchantmentLocationBasedEffect> conditionalEffect : this.getEffects(EnchantmentEffectComponents.LOCATION_CHANGED)) {
				EnchantmentLocationBasedEffect enchantmentLocationBasedEffect = conditionalEffect.effect();
				boolean bl = set != null && set.contains(enchantmentLocationBasedEffect);
				if (conditionalEffect.matches(locationContext(serverLevel, i, livingEntity, bl))) {
					if (!bl) {
						if (set == null) {
							set = new ObjectArraySet<>();
							livingEntity.activeLocationDependentEnchantments().put(this, set);
						}

						set.add(enchantmentLocationBasedEffect);
					}

					enchantmentLocationBasedEffect.onChangedBlock(serverLevel, i, enchantedItemInUse, livingEntity, livingEntity.position(), !bl);
				} else if (set != null && set.remove(enchantmentLocationBasedEffect)) {
					enchantmentLocationBasedEffect.onDeactivated(enchantedItemInUse, livingEntity, livingEntity.position(), i);
				}
			}

			if (set != null && set.isEmpty()) {
				livingEntity.activeLocationDependentEnchantments().remove(this);
			}
		}
	}

	public void stopLocationBasedEffects(int i, EnchantedItemInUse enchantedItemInUse, LivingEntity livingEntity) {
		Set<EnchantmentLocationBasedEffect> set = (Set<EnchantmentLocationBasedEffect>)livingEntity.activeLocationDependentEnchantments().remove(this);
		if (set != null) {
			for (EnchantmentLocationBasedEffect enchantmentLocationBasedEffect : set) {
				enchantmentLocationBasedEffect.onDeactivated(enchantedItemInUse, livingEntity, livingEntity.position(), i);
			}
		}
	}

	public static Enchantment.Builder enchantment(Enchantment.EnchantmentDefinition enchantmentDefinition) {
		return new Enchantment.Builder(enchantmentDefinition);
	}

	public static class Builder {
		private final Enchantment.EnchantmentDefinition definition;
		private HolderSet<Enchantment> exclusiveSet = HolderSet.direct();
		private final Map<DataComponentType<?>, List<?>> effectLists = new HashMap();
		private final DataComponentMap.Builder effectMapBuilder = DataComponentMap.builder();

		public Builder(Enchantment.EnchantmentDefinition enchantmentDefinition) {
			this.definition = enchantmentDefinition;
		}

		public Enchantment.Builder exclusiveWith(HolderSet<Enchantment> holderSet) {
			this.exclusiveSet = holderSet;
			return this;
		}

		public <E> Enchantment.Builder withEffect(DataComponentType<List<ConditionalEffect<E>>> dataComponentType, E object, LootItemCondition.Builder builder) {
			this.getEffectsList(dataComponentType).add(new ConditionalEffect<>(object, Optional.of(builder.build())));
			return this;
		}

		public <E> Enchantment.Builder withEffect(DataComponentType<List<ConditionalEffect<E>>> dataComponentType, E object) {
			this.getEffectsList(dataComponentType).add(new ConditionalEffect<>(object, Optional.empty()));
			return this;
		}

		public <E> Enchantment.Builder withEffect(
			DataComponentType<List<TargetedConditionalEffect<E>>> dataComponentType,
			EnchantmentTarget enchantmentTarget,
			EnchantmentTarget enchantmentTarget2,
			E object,
			LootItemCondition.Builder builder
		) {
			this.getEffectsList(dataComponentType).add(new TargetedConditionalEffect<>(enchantmentTarget, enchantmentTarget2, object, Optional.of(builder.build())));
			return this;
		}

		public <E> Enchantment.Builder withEffect(
			DataComponentType<List<TargetedConditionalEffect<E>>> dataComponentType, EnchantmentTarget enchantmentTarget, EnchantmentTarget enchantmentTarget2, E object
		) {
			this.getEffectsList(dataComponentType).add(new TargetedConditionalEffect<>(enchantmentTarget, enchantmentTarget2, object, Optional.empty()));
			return this;
		}

		public Enchantment.Builder withEffect(
			DataComponentType<List<EnchantmentAttributeEffect>> dataComponentType, EnchantmentAttributeEffect enchantmentAttributeEffect
		) {
			this.getEffectsList(dataComponentType).add(enchantmentAttributeEffect);
			return this;
		}

		public <E> Enchantment.Builder withSpecialEffect(DataComponentType<E> dataComponentType, E object) {
			this.effectMapBuilder.set(dataComponentType, object);
			return this;
		}

		public Enchantment.Builder withEffect(DataComponentType<Unit> dataComponentType) {
			this.effectMapBuilder.set(dataComponentType, Unit.INSTANCE);
			return this;
		}

		private <E> List<E> getEffectsList(DataComponentType<List<E>> dataComponentType) {
			return (List<E>)this.effectLists.computeIfAbsent(dataComponentType, dataComponentType2 -> {
				ArrayList<E> arrayList = new ArrayList();
				this.effectMapBuilder.set(dataComponentType, arrayList);
				return arrayList;
			});
		}

		public Enchantment build(ResourceLocation resourceLocation) {
			return new Enchantment(
				Component.translatable(Util.makeDescriptionId("enchantment", resourceLocation)), this.definition, this.exclusiveSet, this.effectMapBuilder.build()
			);
		}
	}

	public static record Cost(int base, int perLevelAboveFirst) {
		public static final Codec<Enchantment.Cost> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.INT.fieldOf("base").forGetter(Enchantment.Cost::base), Codec.INT.fieldOf("per_level_above_first").forGetter(Enchantment.Cost::perLevelAboveFirst)
					)
					.apply(instance, Enchantment.Cost::new)
		);

		public int calculate(int i) {
			return this.base + this.perLevelAboveFirst * (i - 1);
		}
	}

	public static record EnchantmentDefinition(
		HolderSet<Item> supportedItems,
		Optional<HolderSet<Item>> primaryItems,
		int weight,
		int maxLevel,
		Enchantment.Cost minCost,
		Enchantment.Cost maxCost,
		int anvilCost,
		List<EquipmentSlotGroup> slots
	) {
		public static final MapCodec<Enchantment.EnchantmentDefinition> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("supported_items").forGetter(Enchantment.EnchantmentDefinition::supportedItems),
						RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("primary_items").forGetter(Enchantment.EnchantmentDefinition::primaryItems),
						ExtraCodecs.intRange(1, 1024).fieldOf("weight").forGetter(Enchantment.EnchantmentDefinition::weight),
						ExtraCodecs.intRange(1, 255).fieldOf("max_level").forGetter(Enchantment.EnchantmentDefinition::maxLevel),
						Enchantment.Cost.CODEC.fieldOf("min_cost").forGetter(Enchantment.EnchantmentDefinition::minCost),
						Enchantment.Cost.CODEC.fieldOf("max_cost").forGetter(Enchantment.EnchantmentDefinition::maxCost),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("anvil_cost").forGetter(Enchantment.EnchantmentDefinition::anvilCost),
						EquipmentSlotGroup.CODEC.listOf().fieldOf("slots").forGetter(Enchantment.EnchantmentDefinition::slots)
					)
					.apply(instance, Enchantment.EnchantmentDefinition::new)
		);
	}
}
