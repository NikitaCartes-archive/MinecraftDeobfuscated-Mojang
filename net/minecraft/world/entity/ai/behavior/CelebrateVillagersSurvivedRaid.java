/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.MoveToSkySeeingSpot;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class CelebrateVillagersSurvivedRaid
extends Behavior<Villager> {
    @Nullable
    private Raid currentRaid;

    public CelebrateVillagersSurvivedRaid(int i, int j) {
        super(ImmutableMap.of(), i, j);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Villager villager) {
        BlockPos blockPos = villager.blockPosition();
        this.currentRaid = serverLevel.getRaidAt(blockPos);
        return this.currentRaid != null && this.currentRaid.isVictory() && MoveToSkySeeingSpot.hasNoBlocksAbove(serverLevel, villager, blockPos);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Villager villager, long l) {
        return this.currentRaid != null && !this.currentRaid.isStopped();
    }

    @Override
    protected void stop(ServerLevel serverLevel, Villager villager, long l) {
        this.currentRaid = null;
        villager.getBrain().updateActivityFromSchedule(serverLevel.getDayTime(), serverLevel.getGameTime());
    }

    @Override
    protected void tick(ServerLevel serverLevel, Villager villager, long l) {
        RandomSource randomSource = villager.getRandom();
        if (randomSource.nextInt(100) == 0) {
            villager.playCelebrateSound();
        }
        if (randomSource.nextInt(200) == 0 && MoveToSkySeeingSpot.hasNoBlocksAbove(serverLevel, villager, villager.blockPosition())) {
            DyeColor dyeColor = Util.getRandom(DyeColor.values(), randomSource);
            int i = randomSource.nextInt(3);
            ItemStack itemStack = this.getFirework(dyeColor, i);
            FireworkRocketEntity fireworkRocketEntity = new FireworkRocketEntity(villager.level, villager, villager.getX(), villager.getEyeY(), villager.getZ(), itemStack);
            villager.level.addFreshEntity(fireworkRocketEntity);
        }
    }

    private ItemStack getFirework(DyeColor dyeColor, int i) {
        ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET, 1);
        ItemStack itemStack2 = new ItemStack(Items.FIREWORK_STAR);
        CompoundTag compoundTag = itemStack2.getOrCreateTagElement("Explosion");
        ArrayList<Integer> list = Lists.newArrayList();
        list.add(dyeColor.getFireworkColor());
        compoundTag.putIntArray("Colors", list);
        compoundTag.putByte("Type", (byte)FireworkRocketItem.Shape.BURST.getId());
        CompoundTag compoundTag2 = itemStack.getOrCreateTagElement("Fireworks");
        ListTag listTag = new ListTag();
        CompoundTag compoundTag3 = itemStack2.getTagElement("Explosion");
        if (compoundTag3 != null) {
            listTag.add(compoundTag3);
        }
        compoundTag2.putByte("Flight", (byte)i);
        if (!listTag.isEmpty()) {
            compoundTag2.put("Explosions", listTag);
        }
        return itemStack;
    }
}

