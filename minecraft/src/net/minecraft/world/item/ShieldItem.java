package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ShieldItem extends Item {
	public ShieldItem(Item.Properties properties) {
		super(properties);
		this.addProperty(new ResourceLocation("blocking"), (itemStack, level, livingEntity) -> {
			if (livingEntity != null && livingEntity.isBlocking()) {
				if (livingEntity.getUseItem() == itemStack) {
					return 1.0F;
				} else {
					return !livingEntity.isUsingItem() && livingEntity.getItemInHand(InteractionHand.OFF_HAND) == itemStack ? 1.0F : 0.0F;
				}
			} else {
				return 0.0F;
			}
		});
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return itemStack.getTagElement("BlockEntityTag") != null ? this.getDescriptionId() + '.' + getColor(itemStack).getName() : super.getDescriptionId(itemStack);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
		BannerItem.appendHoverTextFromBannerBlockEntityTag(itemStack, list);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack itemStack) {
		return UseAnim.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack itemStack) {
		return 72000;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		player.startUsingItem(interactionHand);
		return InteractionResultHolder.consume(itemStack);
	}

	@Override
	public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
		return ItemTags.PLANKS.contains(itemStack2.getItem()) || super.isValidRepairItem(itemStack, itemStack2);
	}

	public static DyeColor getColor(ItemStack itemStack) {
		return DyeColor.byId(itemStack.getOrCreateTagElement("BlockEntityTag").getInt("Base"));
	}
}
