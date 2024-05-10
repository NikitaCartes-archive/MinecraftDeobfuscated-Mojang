package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;

public class EnchantmentHelper {
	public static int getItemEnchantmentLevel(Holder<Enchantment> holder, ItemStack itemStack) {
		ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		return itemEnchantments.getLevel(holder);
	}

	public static ItemEnchantments updateEnchantments(ItemStack itemStack, Consumer<ItemEnchantments.Mutable> consumer) {
		DataComponentType<ItemEnchantments> dataComponentType = getComponentType(itemStack);
		ItemEnchantments itemEnchantments = itemStack.get(dataComponentType);
		if (itemEnchantments == null) {
			return ItemEnchantments.EMPTY;
		} else {
			ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
			consumer.accept(mutable);
			ItemEnchantments itemEnchantments2 = mutable.toImmutable();
			itemStack.set(dataComponentType, itemEnchantments2);
			return itemEnchantments2;
		}
	}

	public static boolean canStoreEnchantments(ItemStack itemStack) {
		return itemStack.has(getComponentType(itemStack));
	}

	public static void setEnchantments(ItemStack itemStack, ItemEnchantments itemEnchantments) {
		itemStack.set(getComponentType(itemStack), itemEnchantments);
	}

	public static ItemEnchantments getEnchantmentsForCrafting(ItemStack itemStack) {
		return itemStack.getOrDefault(getComponentType(itemStack), ItemEnchantments.EMPTY);
	}

	private static DataComponentType<ItemEnchantments> getComponentType(ItemStack itemStack) {
		return itemStack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
	}

	public static boolean hasAnyEnchantments(ItemStack itemStack) {
		return !itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()
			|| !itemStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
	}

	public static int processDurabilityChange(ServerLevel serverLevel, ItemStack itemStack, int i) {
		MutableFloat mutableFloat = new MutableFloat((float)i);
		runIterationOnItem(itemStack, (holder, ix) -> holder.value().modifyDurabilityChange(serverLevel, ix, itemStack, mutableFloat));
		return mutableFloat.intValue();
	}

	public static int processAmmoUse(ServerLevel serverLevel, ItemStack itemStack, ItemStack itemStack2, int i) {
		MutableFloat mutableFloat = new MutableFloat((float)i);
		runIterationOnItem(itemStack, (holder, ix) -> holder.value().modifyAmmoCount(serverLevel, ix, itemStack2, mutableFloat));
		return mutableFloat.intValue();
	}

	public static int processBlockExperience(ServerLevel serverLevel, ItemStack itemStack, int i) {
		MutableFloat mutableFloat = new MutableFloat((float)i);
		runIterationOnItem(itemStack, (holder, ix) -> holder.value().modifyBlockExperience(serverLevel, ix, itemStack, mutableFloat));
		return mutableFloat.intValue();
	}

	public static int processMobExperience(ServerLevel serverLevel, @Nullable Entity entity, Entity entity2, int i) {
		if (entity instanceof LivingEntity livingEntity) {
			MutableFloat mutableFloat = new MutableFloat((float)i);
			runIterationOnEquipment(
				livingEntity,
				(holder, ix, enchantedItemInUse) -> holder.value().modifyMobExperience(serverLevel, ix, enchantedItemInUse.itemStack(), entity2, mutableFloat)
			);
			return mutableFloat.intValue();
		} else {
			return i;
		}
	}

	private static void runIterationOnItem(ItemStack itemStack, EnchantmentHelper.EnchantmentVisitor enchantmentVisitor) {
		ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

		for (Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
			enchantmentVisitor.accept((Holder<Enchantment>)entry.getKey(), entry.getIntValue());
		}
	}

	private static void runIterationOnItem(
		ItemStack itemStack, EquipmentSlot equipmentSlot, LivingEntity livingEntity, EnchantmentHelper.EnchantmentInSlotVisitor enchantmentInSlotVisitor
	) {
		if (!itemStack.isEmpty()) {
			ItemEnchantments itemEnchantments = itemStack.get(DataComponents.ENCHANTMENTS);
			if (itemEnchantments != null && !itemEnchantments.isEmpty()) {
				EnchantedItemInUse enchantedItemInUse = new EnchantedItemInUse(itemStack, equipmentSlot, livingEntity);

				for (Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
					Holder<Enchantment> holder = (Holder<Enchantment>)entry.getKey();
					if (holder.value().matchingSlot(equipmentSlot)) {
						enchantmentInSlotVisitor.accept(holder, entry.getIntValue(), enchantedItemInUse);
					}
				}
			}
		}
	}

	private static void runIterationOnEquipment(LivingEntity livingEntity, EnchantmentHelper.EnchantmentInSlotVisitor enchantmentInSlotVisitor) {
		for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
			runIterationOnItem(livingEntity.getItemBySlot(equipmentSlot), equipmentSlot, livingEntity, enchantmentInSlotVisitor);
		}
	}

	public static boolean isImmuneToDamage(ServerLevel serverLevel, LivingEntity livingEntity, DamageSource damageSource) {
		MutableBoolean mutableBoolean = new MutableBoolean();
		runIterationOnEquipment(
			livingEntity,
			(holder, i, enchantedItemInUse) -> mutableBoolean.setValue(
					mutableBoolean.isTrue() || holder.value().isImmuneToDamage(serverLevel, i, livingEntity, damageSource)
				)
		);
		return mutableBoolean.isTrue();
	}

	public static float getDamageProtection(ServerLevel serverLevel, LivingEntity livingEntity, DamageSource damageSource) {
		MutableFloat mutableFloat = new MutableFloat(0.0F);
		runIterationOnEquipment(
			livingEntity,
			(holder, i, enchantedItemInUse) -> holder.value()
					.modifyDamageProtection(serverLevel, i, enchantedItemInUse.itemStack(), livingEntity, damageSource, mutableFloat)
		);
		return mutableFloat.floatValue();
	}

	public static float modifyDamage(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f) {
		MutableFloat mutableFloat = new MutableFloat(f);
		runIterationOnItem(itemStack, (holder, i) -> holder.value().modifyDamage(serverLevel, i, itemStack, entity, damageSource, mutableFloat));
		return mutableFloat.floatValue();
	}

	public static float modifyFallBasedDamage(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f) {
		MutableFloat mutableFloat = new MutableFloat(f);
		runIterationOnItem(itemStack, (holder, i) -> holder.value().modifyFallBasedDamage(serverLevel, i, itemStack, entity, damageSource, mutableFloat));
		return mutableFloat.floatValue();
	}

	public static float modifyArmorEffectiveness(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f) {
		MutableFloat mutableFloat = new MutableFloat(f);
		runIterationOnItem(itemStack, (holder, i) -> holder.value().modifyArmorEffectivness(serverLevel, i, itemStack, entity, damageSource, mutableFloat));
		return mutableFloat.floatValue();
	}

	public static float modifyKnockback(ServerLevel serverLevel, ItemStack itemStack, Entity entity, DamageSource damageSource, float f) {
		MutableFloat mutableFloat = new MutableFloat(f);
		runIterationOnItem(itemStack, (holder, i) -> holder.value().modifyKnockback(serverLevel, i, itemStack, entity, damageSource, mutableFloat));
		return mutableFloat.floatValue();
	}

	public static void doPostAttackEffects(ServerLevel serverLevel, Entity entity, DamageSource damageSource) {
		if (entity instanceof LivingEntity livingEntity) {
			runIterationOnEquipment(
				livingEntity,
				(holder, i, enchantedItemInUse) -> holder.value().doPostAttack(serverLevel, i, enchantedItemInUse, EnchantmentTarget.VICTIM, entity, damageSource)
			);
		}

		if (damageSource.getEntity() instanceof LivingEntity livingEntity) {
			runIterationOnItem(
				livingEntity.getMainHandItem(),
				EquipmentSlot.MAINHAND,
				livingEntity,
				(holder, i, enchantedItemInUse) -> holder.value().doPostAttack(serverLevel, i, enchantedItemInUse, EnchantmentTarget.ATTACKER, entity, damageSource)
			);
		}
	}

	public static void runLocationChangedEffects(ServerLevel serverLevel, LivingEntity livingEntity) {
		runIterationOnEquipment(
			livingEntity, (holder, i, enchantedItemInUse) -> holder.value().runLocationChangedEffects(serverLevel, i, enchantedItemInUse, livingEntity)
		);
	}

	public static void runLocationChangedEffects(ServerLevel serverLevel, ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
		runIterationOnItem(
			itemStack,
			equipmentSlot,
			livingEntity,
			(holder, i, enchantedItemInUse) -> holder.value().runLocationChangedEffects(serverLevel, i, enchantedItemInUse, livingEntity)
		);
	}

	public static void stopLocationBasedEffects(LivingEntity livingEntity) {
		runIterationOnEquipment(livingEntity, (holder, i, enchantedItemInUse) -> holder.value().stopLocationBasedEffects(i, enchantedItemInUse, livingEntity));
	}

	public static void stopLocationBasedEffects(ItemStack itemStack, LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
		runIterationOnItem(
			itemStack, equipmentSlot, livingEntity, (holder, i, enchantedItemInUse) -> holder.value().stopLocationBasedEffects(i, enchantedItemInUse, livingEntity)
		);
	}

	public static void tickEffects(ServerLevel serverLevel, LivingEntity livingEntity) {
		runIterationOnEquipment(livingEntity, (holder, i, enchantedItemInUse) -> holder.value().tick(serverLevel, i, enchantedItemInUse, livingEntity));
	}

	public static int getEnchantmentLevel(Holder<Enchantment> holder, LivingEntity livingEntity) {
		Iterable<ItemStack> iterable = holder.value().getSlotItems(livingEntity).values();
		int i = 0;

		for (ItemStack itemStack : iterable) {
			int j = getItemEnchantmentLevel(holder, itemStack);
			if (j > i) {
				i = j;
			}
		}

		return i;
	}

	public static int processProjectileCount(ServerLevel serverLevel, ItemStack itemStack, Entity entity, int i) {
		MutableFloat mutableFloat = new MutableFloat((float)i);
		runIterationOnItem(itemStack, (holder, ix) -> holder.value().modifyProjectileCount(serverLevel, ix, itemStack, entity, mutableFloat));
		return Math.max(0, mutableFloat.intValue());
	}

	public static float processProjectileSpread(ServerLevel serverLevel, ItemStack itemStack, Entity entity, float f) {
		MutableFloat mutableFloat = new MutableFloat(f);
		runIterationOnItem(itemStack, (holder, i) -> holder.value().modifyProjectileSpread(serverLevel, i, itemStack, entity, mutableFloat));
		return Math.max(0.0F, mutableFloat.floatValue());
	}

	public static int getPiercingCount(ServerLevel serverLevel, ItemStack itemStack, ItemStack itemStack2) {
		MutableFloat mutableFloat = new MutableFloat(0.0F);
		runIterationOnItem(itemStack, (holder, i) -> holder.value().modifyPiercingCount(serverLevel, i, itemStack2, mutableFloat));
		return Math.max(0, mutableFloat.intValue());
	}

	public static void onProjectileSpawned(ServerLevel serverLevel, ItemStack itemStack, AbstractArrow abstractArrow, Runnable runnable) {
		LivingEntity livingEntity2 = abstractArrow.getOwner() instanceof LivingEntity livingEntity ? livingEntity : null;
		EnchantedItemInUse enchantedItemInUse = new EnchantedItemInUse(itemStack, null, livingEntity2, runnable);
		runIterationOnItem(itemStack, (holder, i) -> holder.value().onProjectileSpawned(serverLevel, i, enchantedItemInUse, abstractArrow));
	}

	public static void onHitBlock(
		ServerLevel serverLevel,
		ItemStack itemStack,
		@Nullable LivingEntity livingEntity,
		Entity entity,
		@Nullable EquipmentSlot equipmentSlot,
		Vec3 vec3,
		Runnable runnable
	) {
		EnchantedItemInUse enchantedItemInUse = new EnchantedItemInUse(itemStack, equipmentSlot, livingEntity, runnable);
		runIterationOnItem(itemStack, (holder, i) -> holder.value().onHitBlock(serverLevel, i, enchantedItemInUse, entity, vec3));
	}

	public static int modifyDurabilityToRepairFromXp(ServerLevel serverLevel, ItemStack itemStack, int i) {
		MutableFloat mutableFloat = new MutableFloat((float)i);
		runIterationOnItem(itemStack, (holder, ix) -> holder.value().modifyDurabilityToRepairFromXp(serverLevel, ix, itemStack, mutableFloat));
		return Math.max(0, mutableFloat.intValue());
	}

	public static float processEquipmentDropChance(ServerLevel serverLevel, LivingEntity livingEntity, DamageSource damageSource, float f) {
		MutableFloat mutableFloat = new MutableFloat(f);
		RandomSource randomSource = livingEntity.getRandom();
		runIterationOnEquipment(
			livingEntity,
			(holder, i, enchantedItemInUse) -> {
				LootContext lootContext = Enchantment.damageContext(serverLevel, i, livingEntity, damageSource);
				holder.value()
					.getEffects(EnchantmentEffectComponents.EQUIPMENT_DROPS)
					.forEach(
						targetedConditionalEffect -> {
							if (targetedConditionalEffect.enchanted() == EnchantmentTarget.VICTIM
								&& targetedConditionalEffect.affected() == EnchantmentTarget.VICTIM
								&& targetedConditionalEffect.matches(lootContext)) {
								mutableFloat.setValue(((EnchantmentValueEffect)targetedConditionalEffect.effect()).process(i, randomSource, mutableFloat.floatValue()));
							}
						}
					);
			}
		);
		if (damageSource.getEntity() instanceof LivingEntity livingEntity2) {
			runIterationOnEquipment(
				livingEntity2,
				(holder, i, enchantedItemInUse) -> {
					LootContext lootContext = Enchantment.damageContext(serverLevel, i, livingEntity, damageSource);
					holder.value()
						.getEffects(EnchantmentEffectComponents.EQUIPMENT_DROPS)
						.forEach(
							targetedConditionalEffect -> {
								if (targetedConditionalEffect.enchanted() == EnchantmentTarget.ATTACKER
									&& targetedConditionalEffect.affected() == EnchantmentTarget.VICTIM
									&& targetedConditionalEffect.matches(lootContext)) {
									mutableFloat.setValue(((EnchantmentValueEffect)targetedConditionalEffect.effect()).process(i, randomSource, mutableFloat.floatValue()));
								}
							}
						);
				}
			);
		}

		return mutableFloat.floatValue();
	}

	public static void forEachModifier(ItemStack itemStack, EquipmentSlot equipmentSlot, BiConsumer<Holder<Attribute>, AttributeModifier> biConsumer) {
		runIterationOnItem(itemStack, (holder, i) -> holder.value().getEffects(EnchantmentEffectComponents.ATTRIBUTES).forEach(enchantmentAttributeEffect -> {
				if (((Enchantment)holder.value()).matchingSlot(equipmentSlot)) {
					biConsumer.accept(enchantmentAttributeEffect.attribute(), enchantmentAttributeEffect.getModifier(i));
				}
			}));
	}

	public static int getFishingLuckBonus(ServerLevel serverLevel, ItemStack itemStack, Entity entity) {
		MutableFloat mutableFloat = new MutableFloat(0.0F);
		runIterationOnItem(itemStack, (holder, i) -> holder.value().modifyFishingLuckBonus(serverLevel, i, itemStack, entity, mutableFloat));
		return Math.max(0, mutableFloat.intValue());
	}

	public static float getFishingTimeReduction(ServerLevel serverLevel, ItemStack itemStack, Entity entity) {
		MutableFloat mutableFloat = new MutableFloat(0.0F);
		runIterationOnItem(itemStack, (holder, i) -> holder.value().modifyFishingTimeReduction(serverLevel, i, itemStack, entity, mutableFloat));
		return Math.max(0.0F, mutableFloat.floatValue());
	}

	public static int getTridentReturnToOwnerAcceleration(ServerLevel serverLevel, ItemStack itemStack, Entity entity) {
		MutableFloat mutableFloat = new MutableFloat(0.0F);
		runIterationOnItem(itemStack, (holder, i) -> holder.value().modifyTridentReturnToOwnerAcceleration(serverLevel, i, itemStack, entity, mutableFloat));
		return Math.max(0, mutableFloat.intValue());
	}

	public static float modifyCrossbowChargingTime(LivingEntity livingEntity, float f) {
		MutableFloat mutableFloat = new MutableFloat(f);
		runIterationOnEquipment(livingEntity, (holder, i, enchantedItemInUse) -> holder.value().modifyCrossbowChargeTime(livingEntity.getRandom(), i, mutableFloat));
		return Math.max(0.0F, mutableFloat.floatValue());
	}

	public static float getTridentSpinAttackStrength(LivingEntity livingEntity) {
		MutableFloat mutableFloat = new MutableFloat(0.0F);
		runIterationOnEquipment(
			livingEntity, (holder, i, enchantedItemInUse) -> holder.value().modifyTridentSpinAttackStrength(livingEntity.getRandom(), i, mutableFloat)
		);
		return mutableFloat.floatValue();
	}

	public static boolean hasTag(ItemStack itemStack, TagKey<Enchantment> tagKey) {
		ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

		for (Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
			Holder<Enchantment> holder = (Holder<Enchantment>)entry.getKey();
			if (holder.is(tagKey)) {
				return true;
			}
		}

		return false;
	}

	public static boolean has(ItemStack itemStack, DataComponentType<?> dataComponentType) {
		MutableBoolean mutableBoolean = new MutableBoolean(false);
		runIterationOnItem(itemStack, (holder, i) -> {
			if (holder.value().effects().has(dataComponentType)) {
				mutableBoolean.setTrue();
			}
		});
		return mutableBoolean.booleanValue();
	}

	public static <T> Optional<T> pickHighestLevel(ItemStack itemStack, DataComponentType<List<T>> dataComponentType) {
		Pair<List<T>, Integer> pair = getHighestLevel(itemStack, dataComponentType);
		if (pair != null) {
			List<T> list = pair.getFirst();
			int i = pair.getSecond();
			return Optional.of(list.get(Math.min(i, list.size()) - 1));
		} else {
			return Optional.empty();
		}
	}

	@Nullable
	public static <T> Pair<T, Integer> getHighestLevel(ItemStack itemStack, DataComponentType<T> dataComponentType) {
		MutableObject<Pair<T, Integer>> mutableObject = new MutableObject<>();
		runIterationOnItem(itemStack, (holder, i) -> {
			if (mutableObject.getValue() == null || mutableObject.getValue().getSecond() < i) {
				T object = holder.value().effects().get(dataComponentType);
				if (object != null) {
					mutableObject.setValue(Pair.of(object, i));
				}
			}
		});
		return mutableObject.getValue();
	}

	public static Optional<EnchantedItemInUse> getRandomItemWith(DataComponentType<?> dataComponentType, LivingEntity livingEntity, Predicate<ItemStack> predicate) {
		List<EnchantedItemInUse> list = new ArrayList();

		for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
			ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
			if (predicate.test(itemStack)) {
				ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

				for (Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
					Holder<Enchantment> holder = (Holder<Enchantment>)entry.getKey();
					if (holder.value().effects().has(dataComponentType) && holder.value().matchingSlot(equipmentSlot)) {
						list.add(new EnchantedItemInUse(itemStack, equipmentSlot, livingEntity));
					}
				}
			}
		}

		return Util.getRandomSafe(list, livingEntity.getRandom());
	}

	public static int getEnchantmentCost(RandomSource randomSource, int i, int j, ItemStack itemStack) {
		Item item = itemStack.getItem();
		int k = item.getEnchantmentValue();
		if (k <= 0) {
			return 0;
		} else {
			if (j > 15) {
				j = 15;
			}

			int l = randomSource.nextInt(8) + 1 + (j >> 1) + randomSource.nextInt(j + 1);
			if (i == 0) {
				return Math.max(l / 3, 1);
			} else {
				return i == 1 ? l * 2 / 3 + 1 : Math.max(l, j * 2);
			}
		}
	}

	public static ItemStack enchantItem(
		RandomSource randomSource, ItemStack itemStack, int i, RegistryAccess registryAccess, Optional<? extends HolderSet<Enchantment>> optional
	) {
		return enchantItem(
			randomSource,
			itemStack,
			i,
			(Stream<Holder<Enchantment>>)optional.map(HolderSet::stream)
				.orElseGet(() -> registryAccess.registryOrThrow(Registries.ENCHANTMENT).holders().map(reference -> reference))
		);
	}

	public static ItemStack enchantItem(RandomSource randomSource, ItemStack itemStack, int i, Stream<Holder<Enchantment>> stream) {
		List<EnchantmentInstance> list = selectEnchantment(randomSource, itemStack, i, stream);
		if (itemStack.is(Items.BOOK)) {
			itemStack = new ItemStack(Items.ENCHANTED_BOOK);
		}

		for (EnchantmentInstance enchantmentInstance : list) {
			itemStack.enchant(enchantmentInstance.enchantment, enchantmentInstance.level);
		}

		return itemStack;
	}

	public static List<EnchantmentInstance> selectEnchantment(RandomSource randomSource, ItemStack itemStack, int i, Stream<Holder<Enchantment>> stream) {
		List<EnchantmentInstance> list = Lists.<EnchantmentInstance>newArrayList();
		Item item = itemStack.getItem();
		int j = item.getEnchantmentValue();
		if (j <= 0) {
			return list;
		} else {
			i += 1 + randomSource.nextInt(j / 4 + 1) + randomSource.nextInt(j / 4 + 1);
			float f = (randomSource.nextFloat() + randomSource.nextFloat() - 1.0F) * 0.15F;
			i = Mth.clamp(Math.round((float)i + (float)i * f), 1, Integer.MAX_VALUE);
			List<EnchantmentInstance> list2 = getAvailableEnchantmentResults(i, itemStack, stream);
			if (!list2.isEmpty()) {
				WeightedRandom.getRandomItem(randomSource, list2).ifPresent(list::add);

				while (randomSource.nextInt(50) <= i) {
					if (!list.isEmpty()) {
						filterCompatibleEnchantments(list2, Util.lastOf(list));
					}

					if (list2.isEmpty()) {
						break;
					}

					WeightedRandom.getRandomItem(randomSource, list2).ifPresent(list::add);
					i /= 2;
				}
			}

			return list;
		}
	}

	public static void filterCompatibleEnchantments(List<EnchantmentInstance> list, EnchantmentInstance enchantmentInstance) {
		list.removeIf(enchantmentInstance2 -> !Enchantment.areCompatible(enchantmentInstance.enchantment, enchantmentInstance2.enchantment));
	}

	public static boolean isEnchantmentCompatible(Collection<Holder<Enchantment>> collection, Holder<Enchantment> holder) {
		for (Holder<Enchantment> holder2 : collection) {
			if (!Enchantment.areCompatible(holder2, holder)) {
				return false;
			}
		}

		return true;
	}

	public static List<EnchantmentInstance> getAvailableEnchantmentResults(int i, ItemStack itemStack, Stream<Holder<Enchantment>> stream) {
		List<EnchantmentInstance> list = Lists.<EnchantmentInstance>newArrayList();
		boolean bl = itemStack.is(Items.BOOK);
		stream.filter(holder -> ((Enchantment)holder.value()).isPrimaryItem(itemStack) || bl).forEach(holder -> {
			Enchantment enchantment = (Enchantment)holder.value();

			for (int j = enchantment.getMaxLevel(); j >= enchantment.getMinLevel(); j--) {
				if (i >= enchantment.getMinCost(j) && i <= enchantment.getMaxCost(j)) {
					list.add(new EnchantmentInstance(holder, j));
					break;
				}
			}
		});
		return list;
	}

	public static void enchantItemFromProvider(
		ItemStack itemStack, ResourceKey<EnchantmentProvider> resourceKey, Level level, BlockPos blockPos, RandomSource randomSource
	) {
		EnchantmentProvider enchantmentProvider = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT_PROVIDER).get(resourceKey);
		if (enchantmentProvider != null) {
			updateEnchantments(itemStack, mutable -> enchantmentProvider.enchant(itemStack, mutable, randomSource, level, blockPos));
		}
	}

	@FunctionalInterface
	interface EnchantmentInSlotVisitor {
		void accept(Holder<Enchantment> holder, int i, EnchantedItemInUse enchantedItemInUse);
	}

	@FunctionalInterface
	interface EnchantmentVisitor {
		void accept(Holder<Enchantment> holder, int i);
	}
}
