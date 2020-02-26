package net.minecraft.world.item.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;

public class FrostWalkerEnchantment extends Enchantment {
	public FrostWalkerEnchantment(Enchantment.Rarity rarity, EquipmentSlot... equipmentSlots) {
		super(rarity, EnchantmentCategory.ARMOR_FEET, equipmentSlots);
	}

	@Override
	public int getMinCost(int i) {
		return i * 10;
	}

	@Override
	public int getMaxCost(int i) {
		return this.getMinCost(i) + 15;
	}

	@Override
	public boolean isTreasureOnly() {
		return true;
	}

	@Override
	public int getMaxLevel() {
		return 2;
	}

	public static void onEntityMoved(LivingEntity livingEntity, Level level, BlockPos blockPos, int i) {
		if (livingEntity.isOnGround()) {
			BlockState blockState = Blocks.FROSTED_ICE.defaultBlockState();
			float f = (float)Math.min(16, 2 + i);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos.offset((double)(-f), -1.0, (double)(-f)), blockPos.offset((double)f, -1.0, (double)f))) {
				if (blockPos2.closerThan(livingEntity.position(), (double)f)) {
					mutableBlockPos.set(blockPos2.getX(), blockPos2.getY() + 1, blockPos2.getZ());
					BlockState blockState2 = level.getBlockState(mutableBlockPos);
					if (blockState2.isAir()) {
						BlockState blockState3 = level.getBlockState(blockPos2);
						if (blockState3.getMaterial() == Material.WATER
							&& (Integer)blockState3.getValue(LiquidBlock.LEVEL) == 0
							&& blockState.canSurvive(level, blockPos2)
							&& level.isUnobstructed(blockState, blockPos2, CollisionContext.empty())) {
							level.setBlockAndUpdate(blockPos2, blockState);
							level.getBlockTicks().scheduleTick(blockPos2, Blocks.FROSTED_ICE, Mth.nextInt(livingEntity.getRandom(), 60, 120));
						}
					}
				}
			}
		}
	}

	@Override
	public boolean checkCompatibility(Enchantment enchantment) {
		return super.checkCompatibility(enchantment) && enchantment != Enchantments.DEPTH_STRIDER;
	}
}
