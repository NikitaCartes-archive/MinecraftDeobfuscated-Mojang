package net.minecraft.world.item.enchantment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

public class EnchantmentHelper {
	public static int getItemEnchantmentLevel(Enchantment enchantment, ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return 0;
		} else {
			ResourceLocation resourceLocation = Registry.ENCHANTMENT.getKey(enchantment);
			ListTag listTag = itemStack.getEnchantmentTags();

			for (int i = 0; i < listTag.size(); i++) {
				CompoundTag compoundTag = listTag.getCompound(i);
				ResourceLocation resourceLocation2 = ResourceLocation.tryParse(compoundTag.getString("id"));
				if (resourceLocation2 != null && resourceLocation2.equals(resourceLocation)) {
					return Mth.clamp(compoundTag.getInt("lvl"), 0, 255);
				}
			}

			return 0;
		}
	}

	public static Map<Enchantment, Integer> getEnchantments(ItemStack itemStack) {
		ListTag listTag = itemStack.getItem() == Items.ENCHANTED_BOOK ? EnchantedBookItem.getEnchantments(itemStack) : itemStack.getEnchantmentTags();
		return deserializeEnchantments(listTag);
	}

	public static Map<Enchantment, Integer> deserializeEnchantments(ListTag listTag) {
		Map<Enchantment, Integer> map = Maps.<Enchantment, Integer>newLinkedHashMap();

		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(compoundTag.getString("id"))).ifPresent(enchantment -> {
				Integer var10000 = (Integer)map.put(enchantment, compoundTag.getInt("lvl"));
			});
		}

		return map;
	}

	public static void setEnchantments(Map<Enchantment, Integer> map, ItemStack itemStack) {
		ListTag listTag = new ListTag();

		for (Entry<Enchantment, Integer> entry : map.entrySet()) {
			Enchantment enchantment = (Enchantment)entry.getKey();
			if (enchantment != null) {
				int i = (Integer)entry.getValue();
				CompoundTag compoundTag = new CompoundTag();
				compoundTag.putString("id", String.valueOf(Registry.ENCHANTMENT.getKey(enchantment)));
				compoundTag.putShort("lvl", (short)i);
				listTag.add(compoundTag);
				if (itemStack.getItem() == Items.ENCHANTED_BOOK) {
					EnchantedBookItem.addEnchantment(itemStack, new EnchantmentInstance(enchantment, i));
				}
			}
		}

		if (listTag.isEmpty()) {
			itemStack.removeTagKey("Enchantments");
		} else if (itemStack.getItem() != Items.ENCHANTED_BOOK) {
			itemStack.addTagElement("Enchantments", listTag);
		}
	}

	private static void runIterationOnItem(EnchantmentHelper.EnchantmentVisitor enchantmentVisitor, ItemStack itemStack) {
		if (!itemStack.isEmpty()) {
			ListTag listTag = itemStack.getEnchantmentTags();

			for (int i = 0; i < listTag.size(); i++) {
				String string = listTag.getCompound(i).getString("id");
				int j = listTag.getCompound(i).getInt("lvl");
				Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(string)).ifPresent(enchantment -> enchantmentVisitor.accept(enchantment, j));
			}
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

	public static float getDamageBonus(ItemStack itemStack, LivingEntity livingEntity) {
		MutableFloat mutableFloat = new MutableFloat();
		runIterationOnItem((enchantment, i) -> mutableFloat.add(enchantment.getDamageBonus(i, livingEntity)), itemStack);
		return mutableFloat.floatValue();
	}

	public static float getSweepingDamageRatio(LivingEntity livingEntity) {
		int i = getEnchantmentLevel(Enchantments.SWEEPING_EDGE, livingEntity);
		return i > 0 ? SweepingEdgeEnchantment.getSweepingDamageRatio(i) : 0.0F;
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

	public static int getKnockbackBonus(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.KNOCKBACK, livingEntity);
	}

	public static int getFireAspect(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.FIRE_ASPECT, livingEntity);
	}

	public static int getChopping(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.CLEAVING, livingEntity);
	}

	public static int getRespiration(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.RESPIRATION, livingEntity);
	}

	public static int getDepthStrider(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.DEPTH_STRIDER, livingEntity);
	}

	public static int getDiggingEfficiency(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.DIGGING_EFFICIENCY, livingEntity);
	}

	public static int getFishingLuckBonus(ItemStack itemStack) {
		return getItemEnchantmentLevel(Enchantments.FISHING_LUCK, itemStack);
	}

	public static int getFishingSpeedBonus(ItemStack itemStack) {
		return getItemEnchantmentLevel(Enchantments.FISHING_SPEED, itemStack);
	}

	public static int getMobLooting(LivingEntity livingEntity) {
		return getEnchantmentLevel(Enchantments.MOB_LOOTING, livingEntity);
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
	public static Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment enchantment, LivingEntity livingEntity) {
		return getRandomItemWith(enchantment, livingEntity, itemStack -> true);
	}

	@Nullable
	public static Entry<EquipmentSlot, ItemStack> getRandomItemWith(Enchantment enchantment, LivingEntity livingEntity, Predicate<ItemStack> predicate) {
		Map<EquipmentSlot, ItemStack> map = enchantment.getSlotItems(livingEntity);
		if (map.isEmpty()) {
			return null;
		} else {
			List<Entry<EquipmentSlot, ItemStack>> list = Lists.<Entry<EquipmentSlot, ItemStack>>newArrayList();

			for (Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
				ItemStack itemStack = (ItemStack)entry.getValue();
				if (!itemStack.isEmpty() && getItemEnchantmentLevel(enchantment, itemStack) > 0 && predicate.test(itemStack)) {
					list.add(entry);
				}
			}

			return list.isEmpty() ? null : (Entry)list.get(livingEntity.getRandom().nextInt(list.size()));
		}
	}

	public static int getEnchantmentCost(Random random, int i, int j, ItemStack itemStack) {
		Item item = itemStack.getItem();
		int k = item.getEnchantmentValue();
		if (k <= 0) {
			return 0;
		} else {
			if (j > 15) {
				j = 15;
			}

			int l = random.nextInt(8) + 1 + (j >> 1) + random.nextInt(j + 1);
			if (i == 0) {
				return Math.max(l / 3, 1);
			} else {
				return i == 1 ? l * 2 / 3 + 1 : Math.max(l, j * 2);
			}
		}
	}

	public static ItemStack enchantItem(Random random, ItemStack itemStack, int i, boolean bl) {
		List<EnchantmentInstance> list = selectEnchantment(random, itemStack, i, bl);
		boolean bl2 = itemStack.getItem() == Items.BOOK;
		if (bl2) {
			itemStack = new ItemStack(Items.ENCHANTED_BOOK);
		}

		for (EnchantmentInstance enchantmentInstance : list) {
			if (bl2) {
				EnchantedBookItem.addEnchantment(itemStack, enchantmentInstance);
			} else {
				itemStack.enchant(enchantmentInstance.enchantment, enchantmentInstance.level);
			}
		}

		return itemStack;
	}

	public static List<EnchantmentInstance> selectEnchantment(Random random, ItemStack itemStack, int i, boolean bl) {
		List<EnchantmentInstance> list = Lists.<EnchantmentInstance>newArrayList();
		Item item = itemStack.getItem();
		int j = item.getEnchantmentValue();
		if (j <= 0) {
			return list;
		} else {
			i += 1 + random.nextInt(j / 4 + 1) + random.nextInt(j / 4 + 1);
			float f = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
			i = Mth.clamp(Math.round((float)i + (float)i * f), 1, Integer.MAX_VALUE);
			List<EnchantmentInstance> list2 = getAvailableEnchantmentResults(i, itemStack, bl);
			if (!list2.isEmpty()) {
				list.add(WeighedRandom.getRandomItem(random, list2));

				while (random.nextInt(50) <= i) {
					filterCompatibleEnchantments(list2, Util.lastOf(list));
					if (list2.isEmpty()) {
						break;
					}

					list.add(WeighedRandom.getRandomItem(random, list2));
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

	public static boolean isEnchantmentCompatible(Collection<Enchantment> collection, Enchantment enchantment) {
		for (Enchantment enchantment2 : collection) {
			if (!enchantment2.isCompatibleWith(enchantment)) {
				return false;
			}
		}

		return true;
	}

	public static List<EnchantmentInstance> getAvailableEnchantmentResults(int i, ItemStack itemStack, boolean bl) {
		List<EnchantmentInstance> list = Lists.<EnchantmentInstance>newArrayList();
		Item item = itemStack.getItem();
		boolean bl2 = itemStack.getItem() == Items.BOOK;

		for (Enchantment enchantment : Registry.ENCHANTMENT) {
			if ((!enchantment.isTreasureOnly() || bl) && enchantment.isDiscoverable() && (enchantment.category.canEnchant(item, false) || bl2)) {
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
