package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class BottleItem extends Item {
	public BottleItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		List<AreaEffectCloud> list = level.getEntitiesOfClass(
			AreaEffectCloud.class,
			player.getBoundingBox().inflate(2.0),
			areaEffectCloud -> areaEffectCloud != null && areaEffectCloud.isAlive() && areaEffectCloud.getOwner() instanceof EnderDragon
		);
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (!list.isEmpty()) {
			AreaEffectCloud areaEffectCloud = (AreaEffectCloud)list.get(0);
			areaEffectCloud.setRadius(areaEffectCloud.getRadius() - 0.5F);
			level.playSound(null, player.x, player.y, player.z, SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0F, 1.0F);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, this.turnBottleIntoItem(itemStack, player, new ItemStack(Items.DRAGON_BREATH)));
		} else {
			HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
			if (hitResult.getType() == HitResult.Type.MISS) {
				return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
			} else {
				if (hitResult.getType() == HitResult.Type.BLOCK) {
					BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
					if (!level.mayInteract(player, blockPos)) {
						return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
					}

					if (level.getFluidState(blockPos).is(FluidTags.WATER)) {
						level.playSound(player, player.x, player.y, player.z, SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
						return new InteractionResultHolder<>(
							InteractionResult.SUCCESS, this.turnBottleIntoItem(itemStack, player, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER))
						);
					}
				}

				return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
			}
		}
	}

	protected ItemStack turnBottleIntoItem(ItemStack itemStack, Player player, ItemStack itemStack2) {
		itemStack.shrink(1);
		player.awardStat(Stats.ITEM_USED.get(this));
		if (itemStack.isEmpty()) {
			return itemStack2;
		} else {
			if (!player.inventory.add(itemStack2)) {
				player.drop(itemStack2, false);
			}

			return itemStack;
		}
	}
}
