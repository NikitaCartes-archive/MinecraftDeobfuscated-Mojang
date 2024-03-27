package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

public class EnchantmentHelper {
	private static final float SWIFT_SNEAK_EXTRA_FACTOR = 0.15F;

	public static int getItemEnchantmentLevel(Enchantment enchantment, ItemStack itemStack) {
		ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
		return itemEnchantments.getLevel(enchantment);
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

	public static float getSweepingDamageRatio(int i) {
		return 1.0F - 1.0F / (float)(i + 1);
	}

	private static void runIterationOnItem(EnchantmentHelper.EnchantmentVisitor enchantmentVisitor, ItemStack itemStack) {
		ItemEnchantments itemEnchantments = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

		for (Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
			enchantmentVisitor.accept((Enchantment)((Holder)entry.getKey()).value(), entry.getIntValue());
		}
	}

	private static void runIterationOnInventory(EnchantmentHelper.EnchantmentVisitor enchantmentVisitor, Iterable<ItemStack> iterable) {
		for (ItemStack itemStack : iterable) {
			runIterationOnItem(enchantmentVisitor, itemStack);
		}
	}

	public static int getDamageProtection(Iterable<ItemStack> iterable, DamageSource damageSource) {
		MutableInt mutableInt = new MutableInt();
		runIterationOnInventory((enchantment, i) -> mutableInt.add(enchantment.getDamageProtection(i, damageSource)), iterable);
		return mutableInt.intValue();
	}

	public static float getDamageBonus(ItemStack itemStack, @Nullable EntityType<?> entityType) {
		MutableFloat mutableFloat = new MutableFloat();
		runIterationOnItem((enchantment, i) -> mutableFloat.add(enchantment.getDamageBonus(i, entityType)), itemStack);
		return mutableFloat.floatValue();
	}

	public static float getSweepingDamageRatio(LivingEntity livingEntity) {
		int i = getEnchantmentLevel(Enchantments.SWEEPING_EDGE, livingEntity);
		return i > 0 ? getSweepingDamageRatio(i) : 0.0F;
	}

	public static float calculateArmorBreach(@Nullable Entity entity, float f) {
		if (entity instanceof LivingEntity livingEntity) {
			int i = getEnchantmentLevel(Enchantments.BREACH, livingEntity);
			if (i > 0) {
				return BreachEnchantment.calculateArmorBreach((float)i, f);
			}
		}

		return f;
	}

	public static void doPostHurtEffects(LivingEntity livingEntity, Entity entity) {
		EnchantmentHelper.EnchantmentVisitor enchantmentVisitor = (enchantment, i) -> enchantment.doPostHurt(livingEntity, entity, i);
		if (livingEntity != null) {
			runIterationOnInventory(enchantmentVisitor, livingEntity.getAllSlots());
		}

		if (entity instanceof Player) {
			runIterationOnItem(enchantmentVisitor, livingEntity.getMainHandItem());
		}
	}

	public static void doPostDamageEffects(LivingEntity livingEntity, Entity entity) {
		EnchantmentHelper.EnchantmentVisitor enchantmentVisitor = (enchantment, i) -> enchantment.doPostAttack(livingEntity, entity, i);
		if (livingEntity != null) {
			runIterationOnInventory(enchantmentVisitor, livingEntity.getAllSlots());
		}

		if (livingEntity instanceof Player) {
			runIterationOnItem(enchantmentVisitor, livingEntity.getMainHandItem());
		}
	}

	public static void doPostItemStackHurtEffects(LivingEntity livingEntity, Entity entity, ItemEnchantments itemEnchantments) {
		for (Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
			((Enchantment)((Holder)entry.getKey()).value()).doPostItemStackHurt(livingEntity, entity, entry.getIntValue());
		}
	}

	public static int getEnchantmentLevel(Enchantment enchantment, LivingEntity livingEntity) {
		Iterable<ItemStack> iterable = enchantment.getSlotItems(livingEntity).values();
		if (iterable == null) {
			return 0;
		} else {
			int i = 0;

			for (ItemStack itemStack : iterable) {
				int j = getItemEnchantmentLevel(enchantment, itemStack);
				if (j > i) {
					i = j;
				}
			}

			return i;
		}
	}

	public static float getSneakingSpeedBonus(LivingEntity livingEntity) {
		return (float)getEnchantmentLevel(Enchantments.SWIFT_SNEAK, livingEntity) * 0.15F;
	}

	public static int getKnockbackBonus(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.KNOCKBACK, livingEntity);
	}

	public static int getFireAspect(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.FIRE_ASPECT, livingEntity);
	}

	public static int getRespiration(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.RESPIRATION, livingEntity);
	}

	public static int getDepthStrider(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.DEPTH_STRIDER, livingEntity);
	}

	public static int getBlockEfficiency(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.EFFICIENCY, livingEntity);
	}

	public static int getFishingLuckBonus(ItemStack itemStack) {
		return getItemEnchantmentLevel(Enchantments.LUCK_OF_THE_SEA, itemStack);
	}

	public static int getFishingSpeedBonus(ItemStack itemStack) {
		return getItemEnchantmentLevel(Enchantments.LURE, itemStack);
	}

	public static int getMobLooting(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.LOOTING, livingEntity);
	}

	public static boolean hasAquaAffinity(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.AQUA_AFFINITY, livingEntity) > 0;
	}

	public static boolean hasFrostWalker(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.FROST_WALKER, livingEntity) > 0;
	}

	public static boolean hasSoulSpeed(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.SOUL_SPEED, livingEntity) > 0;
	}

	public static boolean hasBindingCurse(ItemStack itemStack) {
		return getItemEnchantmentLevel(Enchantments.BINDING_CURSE, itemStack) > 0;
	}

	public static boolean hasVanishingCurse(ItemStack itemStack) {
		return getItemEnchantmentLevel(Enchantments.VANISHING_CURSE, itemStack) > 0;
	}

	public static boolean hasSilkTouch(ItemStack itemStack) {
		return getItemEnchantmentLevel(Enchantments.SILK_TOUCH, itemStack) > 0;
	}

	public static int getLoyalty(ItemStack itemStack) {
		return getItemEnchantmentLevel(Enchantments.LOYALTY, itemStack);
	}

	public static int getRiptide(ItemStack itemStack) {
		return getItemEnchantmentLevel(Enchantments.RIPTIDE, itemStack);
	}

	public static boolean hasChanneling(ItemStack itemStack) {
		return getItemEnchantmentLevel(Enchantments.CHANNELING, itemStack) > 0;
	}

	@Nullable
	public static java.util.Map.Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment enchantment, LivingEntity livingEntity) {
		return getRandomItemWith(enchantment, livingEntity, itemStack -> true);
	}

	@Nullable
	public static java.util.Map.Entry<EquipmentSlot, ItemStack> getRandomItemWith(
		Enchantment enchantment, LivingEntity livingEntity, Predicate<ItemStack> predicate
	) {
		Map<EquipmentSlot, ItemStack> map = enchantment.getSlotItems(livingEntity);
		if (map.isEmpty()) {
			return null;
		} else {
			List<java.util.Map.Entry<EquipmentSlot, ItemStack>> list = Lists.<java.util.Map.Entry<EquipmentSlot, ItemStack>>newArrayList();

			for (java.util.Map.Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
				ItemStack itemStack = (ItemStack)entry.getValue();
				if (!itemStack.isEmpty() && getItemEnchantmentLevel(enchantment, itemStack) > 0 && predicate.test(itemStack)) {
					list.add(entry);
				}
			}

			return list.isEmpty() ? null : (java.util.Map.Entry)list.get(livingEntity.getRandom().nextInt(list.size()));
		}
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

	public static ItemStack enchantItem(FeatureFlagSet featureFlagSet, RandomSource randomSource, ItemStack itemStack, int i, boolean bl) {
		List<EnchantmentInstance> list = selectEnchantment(featureFlagSet, randomSource, itemStack, i, bl);
		if (itemStack.is(Items.BOOK)) {
			itemStack = new ItemStack(Items.ENCHANTED_BOOK);
		}

		for (EnchantmentInstance enchantmentInstance : list) {
			itemStack.enchant(enchantmentInstance.enchantment, enchantmentInstance.level);
		}

		return itemStack;
	}

	public static List<EnchantmentInstance> selectEnchantment(FeatureFlagSet featureFlagSet, RandomSource randomSource, ItemStack itemStack, int i, boolean bl) {
		List<EnchantmentInstance> list = Lists.<EnchantmentInstance>newArrayList();
		Item item = itemStack.getItem();
		int j = item.getEnchantmentValue();
		if (j <= 0) {
			return list;
		} else {
			i += 1 + randomSource.nextInt(j / 4 + 1) + randomSource.nextInt(j / 4 + 1);
			float f = (randomSource.nextFloat() + randomSource.nextFloat() - 1.0F) * 0.15F;
			i = Mth.clamp(Math.round((float)i + (float)i * f), 1, Integer.MAX_VALUE);
			List<EnchantmentInstance> list2 = getAvailableEnchantmentResults(featureFlagSet, i, itemStack, bl);
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
		Iterator<EnchantmentInstance> iterator = list.iterator();

		while (iterator.hasNext()) {
			if (!enchantmentInstance.enchantment.isCompatibleWith(((EnchantmentInstance)iterator.next()).enchantment)) {
				iterator.remove();
			}
		}
	}

	public static boolean isEnchantmentCompatible(Collection<Holder<Enchantment>> collection, Enchantment enchantment) {
		for (Holder<Enchantment> holder : collection) {
			if (!holder.value().isCompatibleWith(enchantment)) {
				return false;
			}
		}

		return true;
	}

	public static List<EnchantmentInstance> getAvailableEnchantmentResults(FeatureFlagSet featureFlagSet, int i, ItemStack itemStack, boolean bl) {
		List<EnchantmentInstance> list = Lists.<EnchantmentInstance>newArrayList();
		boolean bl2 = itemStack.is(Items.BOOK);

		for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
			if (enchantment.isEnabled(featureFlagSet)
				&& (!enchantment.isTreasureOnly() || bl)
				&& enchantment.isDiscoverable()
				&& (bl2 || enchantment.canEnchant(itemStack) && enchantment.isPrimaryItem(itemStack))) {
				for (int j = enchantment.getMaxLevel(); j > enchantment.getMinLevel() - 1; j--) {
					if (i >= enchantment.getMinCost(j) && i <= enchantment.getMaxCost(j)) {
						list.add(new EnchantmentInstance(enchantment, j));
						break;
					}
				}
			}
		}

		return list;
	}

	@FunctionalInterface
	interface EnchantmentVisitor {
		void accept(Enchantment enchantment, int i);
	}
}
