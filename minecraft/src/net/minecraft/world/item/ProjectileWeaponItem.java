package net.minecraft.world.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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

	public abstract int getDefaultProjectileRange();

	protected void shoot(
		ServerLevel serverLevel,
		LivingEntity livingEntity,
		InteractionHand interactionHand,
		ItemStack itemStack,
		List<ItemStack> list,
		float f,
		float g,
		boolean bl,
		@Nullable LivingEntity livingEntity2
	) {
		float h = EnchantmentHelper.processProjectileSpread(serverLevel, itemStack, livingEntity, 0.0F);
		float i = list.size() == 1 ? 0.0F : 2.0F * h / (float)(list.size() - 1);
		float j = (float)((list.size() - 1) % 2) * i / 2.0F;
		float k = 1.0F;

		for (int l = 0; l < list.size(); l++) {
			ItemStack itemStack2 = (ItemStack)list.get(l);
			if (!itemStack2.isEmpty()) {
				float m = j + k * (float)((l + 1) / 2) * i;
				k = -k;
				int n = l;
				Projectile.spawnProjectile(
					this.createProjectile(serverLevel, livingEntity, itemStack, itemStack2, bl),
					serverLevel,
					itemStack2,
					projectile -> this.shootProjectile(livingEntity, projectile, n, f, g, m, livingEntity2)
				);
				itemStack.hurtAndBreak(this.getDurabilityUse(itemStack2), livingEntity, LivingEntity.getSlotForHand(interactionHand));
				if (itemStack.isEmpty()) {
					break;
				}
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
		ArrowItem arrowItem2 = itemStack2.getItem() instanceof ArrowItem arrowItem ? arrowItem : (ArrowItem)Items.ARROW;
		AbstractArrow abstractArrow = arrowItem2.createArrow(level, itemStack2, livingEntity, itemStack);
		if (bl) {
			abstractArrow.setCritArrow(true);
		}

		return abstractArrow;
	}

	protected static List<ItemStack> draw(ItemStack itemStack, ItemStack itemStack2, LivingEntity livingEntity) {
		if (itemStack2.isEmpty()) {
			return List.of();
		} else {
			int i = livingEntity.level() instanceof ServerLevel serverLevel ? EnchantmentHelper.processProjectileCount(serverLevel, itemStack, livingEntity, 1) : 1;
			List<ItemStack> list = new ArrayList(i);
			ItemStack itemStack3 = itemStack2.copy();

			for (int j = 0; j < i; j++) {
				ItemStack itemStack4 = useAmmo(itemStack, j == 0 ? itemStack2 : itemStack3, livingEntity, j > 0);
				if (!itemStack4.isEmpty()) {
					list.add(itemStack4);
				}
			}

			return list;
		}
	}

	protected static ItemStack useAmmo(ItemStack itemStack, ItemStack itemStack2, LivingEntity livingEntity, boolean bl) {
		int i = !bl && !livingEntity.hasInfiniteMaterials() && livingEntity.level() instanceof ServerLevel serverLevel
			? EnchantmentHelper.processAmmoUse(serverLevel, itemStack, itemStack2, 1)
			: 0;
		if (i > itemStack2.getCount()) {
			return ItemStack.EMPTY;
		} else if (i == 0) {
			ItemStack itemStack3 = itemStack2.copyWithCount(1);
			itemStack3.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
			return itemStack3;
		} else {
			ItemStack itemStack3 = itemStack2.split(i);
			if (itemStack2.isEmpty() && livingEntity instanceof Player player) {
				player.getInventory().removeItem(itemStack2);
			}

			return itemStack3;
		}
	}
}
