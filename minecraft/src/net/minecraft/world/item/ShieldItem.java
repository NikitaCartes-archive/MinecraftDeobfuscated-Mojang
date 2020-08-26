package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class ShieldItem extends Item {
	public ShieldItem(Item.Properties properties) {
		super(properties);
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
		float f = getShieldBlockDamageValue(itemStack);
		float g = getShieldKnockbackResistanceValue(itemStack);
		list.add(
			new TextComponent(" ")
				.append(
					new TranslatableComponent(
						"attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(),
						ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double)f),
						new TranslatableComponent("attribute.name.generic.shield_strength")
					)
				)
				.withStyle(ChatFormatting.DARK_GREEN)
		);
		list.add(
			new TextComponent(" ")
				.append(
					new TranslatableComponent(
						"attribute.modifier.equals." + AttributeModifier.Operation.ADDITION.toValue(),
						ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format((double)(g * 10.0F)),
						new TranslatableComponent("attribute.name.generic.knockback_resistance")
					)
				)
				.withStyle(ChatFormatting.DARK_GREEN)
		);
	}

	public static float getShieldBlockDamageValue(ItemStack itemStack) {
		return itemStack.getTagElement("BlockEntityTag") != null ? 10.0F : 5.0F;
	}

	public static float getShieldKnockbackResistanceValue(ItemStack itemStack) {
		return itemStack.getTagElement("BlockEntityTag") != null ? 0.8F : 0.5F;
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
