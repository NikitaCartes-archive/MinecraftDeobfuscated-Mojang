package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public abstract class ProjectileWeaponItem extends Item {
	public static final Predicate<ItemStack> ARROW_ONLY = itemStack -> itemStack.is(ItemTags.ARROWS);
	public static final Predicate<ItemStack> ARROW_OR_FIREWORK = ARROW_ONLY.or(itemStack -> itemStack.is(Items.FIREWORK_ROCKET));

	public ProjectileWeaponItem(Item.Properties properties) {
		super(properties);
	}

	public Predicate<ItemStack> getSupportedHeldProjectiles() {
		return this.getAllSupportedProjectiles();
	}

	public abstract Predicate<ItemStack> getAllSupportedProjectiles();

	public static ItemStack getHeldProjectile(LivingEntity livingEntity, Predicate<ItemStack> predicate) {
		if (predicate.test(livingEntity.getItemInHand(InteractionHand.OFF_HAND))) {
			return livingEntity.getItemInHand(InteractionHand.OFF_HAND);
		} else {
			return predicate.test(livingEntity.getItemInHand(InteractionHand.MAIN_HAND)) ? livingEntity.getItemInHand(InteractionHand.MAIN_HAND) : ItemStack.EMPTY;
		}
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
	}

	public abstract int getDefaultProjectileRange();

	protected void shoot(
		Level level,
		LivingEntity livingEntity,
		InteractionHand interactionHand,
		ItemStack itemStack,
		List<ItemStack> list,
		float f,
		float g,
		boolean bl,
		@Nullable LivingEntity livingEntity2
	) {
		float h = 10.0F;
		float i = list.size() == 1 ? 0.0F : 20.0F / (float)(list.size() - 1);
		float j = (float)((list.size() - 1) % 2) * i / 2.0F;
		float k = 1.0F;

		for(int l = 0; l < list.size(); ++l) {
			ItemStack itemStack2 = (ItemStack)list.get(l);
			if (!itemStack2.isEmpty()) {
				float m = j + k * (float)((l + 1) / 2) * i;
				k = -k;
				itemStack.hurtAndBreak(this.getDurabilityUse(itemStack2), livingEntity, LivingEntity.getSlotForHand(interactionHand));
				Projectile projectile = this.createProjectile(level, livingEntity, itemStack, itemStack2, bl);
				this.shootProjectile(livingEntity, projectile, l, f, g, m, livingEntity2);
				level.addFreshEntity(projectile);
			}
		}
	}

	protected int getDurabilityUse(ItemStack itemStack) {
		return 1;
	}

	protected abstract void shootProjectile(
		LivingEntity livingEntity, Projectile projectile, int i, float f, float g, float h, @Nullable LivingEntity livingEntity2
	);

	protected Projectile createProjectile(Level level, LivingEntity livingEntity, ItemStack itemStack, ItemStack itemStack2, boolean bl) {
		Item i = itemStack2.getItem();
		ArrowItem arrowItem2 = i instanceof ArrowItem arrowItem ? arrowItem : (ArrowItem)Items.ARROW;
		AbstractArrow abstractArrow = arrowItem2.createArrow(level, itemStack2, livingEntity);
		if (bl) {
			abstractArrow.setCritArrow(true);
		}

		int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER, itemStack);
		if (i > 0) {
			abstractArrow.setBaseDamage(abstractArrow.getBaseDamage() + (double)i * 0.5 + 0.5);
		}

		int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH, itemStack);
		if (j > 0) {
			abstractArrow.setKnockback(j);
		}

		if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAME, itemStack) > 0) {
			abstractArrow.igniteForSeconds(100);
		}

		int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, itemStack);
		if (k > 0) {
			abstractArrow.setPierceLevel((byte)k);
		}

		return abstractArrow;
	}

	protected static boolean hasInfiniteArrows(ItemStack itemStack, ItemStack itemStack2, boolean bl) {
		return bl || itemStack2.is(Items.ARROW) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY, itemStack) > 0;
	}

	protected static List<ItemStack> draw(ItemStack itemStack, ItemStack itemStack2, LivingEntity livingEntity) {
		if (itemStack2.isEmpty()) {
			return List.of();
		} else {
			int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, itemStack);
			int j = i == 0 ? 1 : 3;
			List<ItemStack> list = new ArrayList(j);
			ItemStack itemStack3 = itemStack2.copy();

			for(int k = 0; k < j; ++k) {
				list.add(useAmmo(itemStack, k == 0 ? itemStack2 : itemStack3, livingEntity, k > 0));
			}

			return list;
		}
	}

	protected static ItemStack useAmmo(ItemStack itemStack, ItemStack itemStack2, LivingEntity livingEntity, boolean bl) {
		boolean bl2 = !bl && !hasInfiniteArrows(itemStack, itemStack2, livingEntity.hasInfiniteMaterials());
		if (!bl2) {
			ItemStack itemStack3 = itemStack2.copyWithCount(1);
			itemStack3.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
			return itemStack3;
		} else {
			ItemStack itemStack3 = itemStack2.split(1);
			if (itemStack2.isEmpty() && livingEntity instanceof Player player) {
				player.getInventory().removeItem(itemStack2);
			}

			return itemStack3;
		}
	}
}
