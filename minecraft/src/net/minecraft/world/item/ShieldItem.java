package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ShieldItem extends Item {
	public static final int EFFECTIVE_BLOCK_DELAY = 5;
	public static final float MINIMUM_DURABILITY_DAMAGE = 3.0F;

	public ShieldItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public Component getName(ItemStack itemStack) {
		DyeColor dyeColor = itemStack.get(DataComponents.BASE_COLOR);
		return (Component)(dyeColor != null ? Component.translatable(this.descriptionId + "." + dyeColor.getName()) : super.getName(itemStack));
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
		BannerItem.appendHoverTextFromBannerBlockEntityTag(itemStack, list);
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
		return ItemUseAnimation.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
		return 72000;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
		player.startUsingItem(interactionHand);
		return InteractionResult.CONSUME;
	}
}
