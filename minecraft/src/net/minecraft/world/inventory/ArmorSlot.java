package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

class ArmorSlot extends Slot {
	private final LivingEntity owner;
	private final EquipmentSlot slot;
	@Nullable
	private final ResourceLocation emptyIcon;

	public ArmorSlot(Container container, LivingEntity livingEntity, EquipmentSlot equipmentSlot, int i, int j, int k, @Nullable ResourceLocation resourceLocation) {
		super(container, i, j, k);
		this.owner = livingEntity;
		this.slot = equipmentSlot;
		this.emptyIcon = resourceLocation;
	}

	@Override
	public void setByPlayer(ItemStack itemStack, ItemStack itemStack2) {
		this.owner.onEquipItem(this.slot, itemStack2, itemStack);
		super.setByPlayer(itemStack, itemStack2);
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean mayPlace(ItemStack itemStack) {
		return this.slot == Mob.getEquipmentSlotForItem(itemStack);
	}

	@Override
	public boolean mayPickup(Player player) {
		ItemStack itemStack = this.getItem();
		return !itemStack.isEmpty() && !player.isCreative() && EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
			? false
			: super.mayPickup(player);
	}

	@Override
	public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
		return this.emptyIcon != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, this.emptyIcon) : super.getNoItemIcon();
	}
}
