package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class BowItem extends ProjectileWeaponItem {
	public static final int MAX_DRAW_DURATION = 20;
	public static final int DEFAULT_RANGE = 15;

	public BowItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
		if (livingEntity instanceof Player) {
			Player player = (Player)livingEntity;
			ItemStack itemStack2 = player.getProjectile(itemStack);
			if (!itemStack2.isEmpty()) {
				int j = this.getUseDuration(itemStack) - i;
				float f = getPowerForTime(j);
				if (!((double)f < 0.1)) {
					List<ItemStack> list = draw(itemStack, itemStack2, player);
					if (!level.isClientSide() && !list.isEmpty()) {
						this.shoot(level, player, player.getUsedItemHand(), itemStack, list, f * 3.0F, 1.0F, f == 1.0F, null);
					}

					level.playSound(
						null,
						player.getX(),
						player.getY(),
						player.getZ(),
						SoundEvents.ARROW_SHOOT,
						SoundSource.PLAYERS,
						1.0F,
						1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F
					);
					player.awardStat(Stats.ITEM_USED.get(this));
				}
			}
		}
	}

	@Override
	protected void shootProjectile(LivingEntity livingEntity, Projectile projectile, int i, float f, float g, float h, @Nullable LivingEntity livingEntity2) {
		projectile.shootFromRotation(livingEntity, livingEntity.getXRot(), livingEntity.getYRot() + h, 0.0F, f, g);
	}

	public static float getPowerForTime(int i) {
		float f = (float)i / 20.0F;
		f = (f * f + f * 2.0F) / 3.0F;
		if (f > 1.0F) {
			f = 1.0F;
		}

		return f;
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		return 72000;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.BOW;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		boolean bl = !player.getProjectile(itemStack).isEmpty();
		if (!player.hasInfiniteMaterials() && !bl) {
			return InteractionResultHolder.fail(itemStack);
		} else {
			player.startUsingItem(interactionHand);
			return InteractionResultHolder.consume(itemStack);
		}
	}

	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return ARROW_ONLY;
	}

	@Override
	public int getDefaultProjectileRange() {
		return 15;
	}
}
