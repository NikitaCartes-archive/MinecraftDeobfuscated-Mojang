package net.minecraft.world.item;

import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ArmorStandItem extends Item {
	public ArmorStandItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		Direction direction = useOnContext.getClickedFace();
		if (direction == Direction.DOWN) {
			return InteractionResult.FAIL;
		} else {
			Level level = useOnContext.getLevel();
			BlockPlaceContext blockPlaceContext = new BlockPlaceContext(useOnContext);
			BlockPos blockPos = blockPlaceContext.getClickedPos();
			BlockPos blockPos2 = blockPos.above();
			if (blockPlaceContext.canPlace() && level.getBlockState(blockPos2).canBeReplaced(blockPlaceContext)) {
				double d = (double)blockPos.getX();
				double e = (double)blockPos.getY();
				double f = (double)blockPos.getZ();
				List<Entity> list = level.getEntities(null, new AABB(d, e, f, d + 1.0, e + 2.0, f + 1.0));
				if (!list.isEmpty()) {
					return InteractionResult.FAIL;
				} else {
					ItemStack itemStack = useOnContext.getItemInHand();
					if (!level.isClientSide) {
						level.removeBlock(blockPos, false);
						level.removeBlock(blockPos2, false);
						ArmorStand armorStand = new ArmorStand(level, d + 0.5, e, f + 0.5);
						float g = (float)Mth.floor((Mth.wrapDegrees(useOnContext.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
						armorStand.moveTo(d + 0.5, e, f + 0.5, g, 0.0F);
						this.randomizePose(armorStand, level.random);
						EntityType.updateCustomEntityTag(level, useOnContext.getPlayer(), armorStand, itemStack.getTag());
						level.addFreshEntity(armorStand);
						level.playSound(null, armorStand.x, armorStand.y, armorStand.z, SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
					}

					itemStack.shrink(1);
					return InteractionResult.SUCCESS;
				}
			} else {
				return InteractionResult.FAIL;
			}
		}
	}

	private void randomizePose(ArmorStand armorStand, Random random) {
		Rotations rotations = armorStand.getHeadPose();
		float f = random.nextFloat() * 5.0F;
		float g = random.nextFloat() * 20.0F - 10.0F;
		Rotations rotations2 = new Rotations(rotations.getX() + f, rotations.getY() + g, rotations.getZ());
		armorStand.setHeadPose(rotations2);
		rotations = armorStand.getBodyPose();
		f = random.nextFloat() * 10.0F - 5.0F;
		rotations2 = new Rotations(rotations.getX(), rotations.getY() + f, rotations.getZ());
		armorStand.setBodyPose(rotations2);
	}
}
