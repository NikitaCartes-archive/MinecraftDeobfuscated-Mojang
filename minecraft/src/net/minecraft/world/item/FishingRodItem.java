package net.minecraft.world.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class FishingRodItem extends Item implements Vanishable {
	public FishingRodItem(Item.Properties properties) {
		super(properties);
		this.addProperty(new ResourceLocation("cast"), (itemStack, level, livingEntity) -> {
			if (livingEntity == null) {
				return 0.0F;
			} else {
				boolean bl = livingEntity.getMainHandItem() == itemStack;
				boolean bl2 = livingEntity.getOffhandItem() == itemStack;
				if (livingEntity.getMainHandItem().getItem() instanceof FishingRodItem) {
					bl2 = false;
				}

				return (bl || bl2) && livingEntity instanceof Player && ((Player)livingEntity).fishing != null ? 1.0F : 0.0F;
			}
		});
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (player.fishing != null) {
			if (!level.isClientSide) {
				int i = player.fishing.retrieve(itemStack);
				itemStack.hurtAndBreak(i, player, playerx -> playerx.broadcastBreakEvent(interactionHand));
			}

			level.playSound(
				null,
				player.getX(),
				player.getY(),
				player.getZ(),
				SoundEvents.FISHING_BOBBER_RETRIEVE,
				SoundSource.NEUTRAL,
				1.0F,
				0.4F / (random.nextFloat() * 0.4F + 0.8F)
			);
		} else {
			level.playSound(
				null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F)
			);
			if (!level.isClientSide) {
				int i = EnchantmentHelper.getFishingSpeedBonus(itemStack);
				int j = EnchantmentHelper.getFishingLuckBonus(itemStack);
				level.addFreshEntity(new FishingHook(player, level, j, i));
			}

			player.awardStat(Stats.ITEM_USED.get(this));
		}

		return InteractionResultHolder.success(itemStack);
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
	}
}
