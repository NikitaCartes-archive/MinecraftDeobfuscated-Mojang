package net.minecraft.world.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;

public class SplashBottleOfEntityItem extends Item {
	public SplashBottleOfEntityItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (!level.isClientSide) {
			ThrownPotion thrownPotion = new ThrownPotion(level, player);
			thrownPotion.setItem(itemStack);
			thrownPotion.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
			level.addFreshEntity(thrownPotion);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		if (!player.getAbilities().instabuild) {
			itemStack.shrink(1);
		}

		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}

	@Override
	public Component getName(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && compoundTag.contains("entityTag", 10)) {
			CompoundTag compoundTag2 = compoundTag.getCompound("entityTag");
			EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(compoundTag2.getString("id")));
			return Component.translatable("item.minecraft.splash_bottle_of_entity.specific", Component.translatable(entityType.getDescriptionId()));
		} else {
			return super.getName(itemStack);
		}
	}
}
