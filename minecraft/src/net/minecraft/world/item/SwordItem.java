package net.minecraft.world.item;

import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class SwordItem extends TieredItem {
	private final float attackDamage;
	private final float attackSpeed;

	public SwordItem(Tier tier, int i, float f, Item.Properties properties) {
		super(tier, properties);
		this.attackSpeed = f;
		this.attackDamage = (float)i + tier.getAttackDamageBonus();
	}

	public float getDamage() {
		return this.attackDamage;
	}

	@Override
	public boolean canAttackBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
		return !player.isCreative();
	}

	@Override
	public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
		Block block = blockState.getBlock();
		if (block == Blocks.COBWEB) {
			return 15.0F;
		} else {
			Material material = blockState.getMaterial();
			return material != Material.PLANT
					&& material != Material.REPLACEABLE_PLANT
					&& material != Material.CORAL
					&& !blockState.is(BlockTags.LEAVES)
					&& material != Material.VEGETABLE
				? 1.0F
				: 1.5F;
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2) {
		itemStack.hurtAndBreak(1, livingEntity2, livingEntityx -> livingEntityx.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		return true;
	}

	@Override
	public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos blockPos, LivingEntity livingEntity) {
		if (blockState.getDestroySpeed(level, blockPos) != 0.0F) {
			itemStack.hurtAndBreak(2, livingEntity, livingEntityx -> livingEntityx.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		}

		return true;
	}

	@Override
	public boolean canDestroySpecial(BlockState blockState) {
		return blockState.getBlock() == Blocks.COBWEB;
	}

	@Override
	public Multimap<String, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getDefaultAttributeModifiers(equipmentSlot);
		if (equipmentSlot == EquipmentSlot.MAINHAND) {
			multimap.put(
				SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
				new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", (double)this.attackDamage, AttributeModifier.Operation.ADDITION)
			);
			multimap.put(
				SharedMonsterAttributes.ATTACK_SPEED.getName(),
				new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", (double)this.attackSpeed, AttributeModifier.Operation.ADDITION)
			);
		}

		return multimap;
	}
}
