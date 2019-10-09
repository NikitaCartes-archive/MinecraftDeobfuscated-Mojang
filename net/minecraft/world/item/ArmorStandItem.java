/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class ArmorStandItem
extends Item {
    public ArmorStandItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        double f;
        double e;
        Direction direction = useOnContext.getClickedFace();
        if (direction == Direction.DOWN) {
            return InteractionResult.FAIL;
        }
        Level level = useOnContext.getLevel();
        BlockPlaceContext blockPlaceContext = new BlockPlaceContext(useOnContext);
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        BlockPos blockPos2 = blockPos.above();
        if (!blockPlaceContext.canPlace() || !level.getBlockState(blockPos2).canBeReplaced(blockPlaceContext)) {
            return InteractionResult.FAIL;
        }
        double d = blockPos.getX();
        List<Entity> list = level.getEntities(null, new AABB(d, e = (double)blockPos.getY(), f = (double)blockPos.getZ(), d + 1.0, e + 2.0, f + 1.0));
        if (!list.isEmpty()) {
            return InteractionResult.FAIL;
        }
        ItemStack itemStack = useOnContext.getItemInHand();
        if (!level.isClientSide) {
            level.removeBlock(blockPos, false);
            level.removeBlock(blockPos2, false);
            ArmorStand armorStand = new ArmorStand(level, d + 0.5, e, f + 0.5);
            float g = (float)Mth.floor((Mth.wrapDegrees(useOnContext.getRotation() - 180.0f) + 22.5f) / 45.0f) * 45.0f;
            armorStand.moveTo(d + 0.5, e, f + 0.5, g, 0.0f);
            this.randomizePose(armorStand, level.random);
            EntityType.updateCustomEntityTag(level, useOnContext.getPlayer(), armorStand, itemStack.getTag());
            level.addFreshEntity(armorStand);
            level.playSound(null, armorStand.getX(), armorStand.getY(), armorStand.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75f, 0.8f);
        }
        itemStack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private void randomizePose(ArmorStand armorStand, Random random) {
        Rotations rotations = armorStand.getHeadPose();
        float f = random.nextFloat() * 5.0f;
        float g = random.nextFloat() * 20.0f - 10.0f;
        Rotations rotations2 = new Rotations(rotations.getX() + f, rotations.getY() + g, rotations.getZ());
        armorStand.setHeadPose(rotations2);
        rotations = armorStand.getBodyPose();
        f = random.nextFloat() * 10.0f - 5.0f;
        rotations2 = new Rotations(rotations.getX(), rotations.getY() + f, rotations.getZ());
        armorStand.setBodyPose(rotations2);
    }
}

