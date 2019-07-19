/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.dimension.end.TheEndDimension;
import net.minecraft.world.phys.AABB;

public class EndCrystalItem
extends Item {
    public EndCrystalItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        double f;
        double e;
        BlockPos blockPos;
        Level level = useOnContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos = useOnContext.getClickedPos());
        if (blockState.getBlock() != Blocks.OBSIDIAN && blockState.getBlock() != Blocks.BEDROCK) {
            return InteractionResult.FAIL;
        }
        BlockPos blockPos2 = blockPos.above();
        if (!level.isEmptyBlock(blockPos2)) {
            return InteractionResult.FAIL;
        }
        double d = blockPos2.getX();
        List<Entity> list = level.getEntities(null, new AABB(d, e = (double)blockPos2.getY(), f = (double)blockPos2.getZ(), d + 1.0, e + 2.0, f + 1.0));
        if (!list.isEmpty()) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            EndCrystal endCrystal = new EndCrystal(level, d + 0.5, e, f + 0.5);
            endCrystal.setShowBottom(false);
            level.addFreshEntity(endCrystal);
            if (level.dimension instanceof TheEndDimension) {
                EndDragonFight endDragonFight = ((TheEndDimension)level.dimension).getDragonFight();
                endDragonFight.tryRespawn();
            }
        }
        useOnContext.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public boolean isFoil(ItemStack itemStack) {
        return true;
    }
}

