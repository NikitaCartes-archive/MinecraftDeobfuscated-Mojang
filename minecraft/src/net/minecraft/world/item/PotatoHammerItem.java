package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class PotatoHammerItem extends Item {
	public PotatoHammerItem(Item.Properties properties) {
		super(properties);
	}

	public static ItemAttributeModifiers createAttributes() {
		return ItemAttributeModifiers.builder()
			.add(
				Attributes.ATTACK_DAMAGE,
				new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", 10.0, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.add(
				Attributes.ATTACK_SPEED,
				new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", 2.0, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.build();
	}

	public static ItemEnchantments createDefaultEnchantments() {
		ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
		mutable.set(Enchantments.KNOCKBACK, 10);
		return mutable.toImmutable();
	}

	@Override
	public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		return !player.isCreative();
	}

	@Override
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		Level level = livingEntity2.level();
		itemStack.hurtAndBreak(1, livingEntity2, EquipmentSlot.MAINHAND);
		if (level.getRandom().nextFloat() < 0.3F) {
			ThrownPotion thrownPotion = new ThrownPotion(level, livingEntity2);
			thrownPotion.setItem(PotionContents.createItemStack(Items.LINGERING_POTION, Potions.LONG_POISON));
			thrownPotion.shootFromRotation(livingEntity, livingEntity.getXRot(), livingEntity.getYRot(), -1.0F, 0.0F, 0.0F);
			level.addFreshEntity(thrownPotion);
		}

		return true;
	}
}
