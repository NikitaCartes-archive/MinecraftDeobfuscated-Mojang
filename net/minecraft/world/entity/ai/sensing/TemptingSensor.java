/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class TemptingSensor
extends Sensor<PathfinderMob> {
    public static final int TEMPTATION_RANGE = 10;
    private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().range(10.0).ignoreLineOfSight();
    private final Ingredient temptations;

    public TemptingSensor(Ingredient ingredient) {
        this.temptations = ingredient;
    }

    @Override
    protected void doTick(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        Brain<?> brain = pathfinderMob.getBrain();
        List list = serverLevel.players().stream().filter(EntitySelector.NO_SPECTATORS).filter(serverPlayer -> TEMPT_TARGETING.test(pathfinderMob, (LivingEntity)serverPlayer)).filter(serverPlayer -> pathfinderMob.closerThan((Entity)serverPlayer, 10.0)).filter(this::playerHoldingTemptation).sorted(Comparator.comparingDouble(pathfinderMob::distanceToSqr)).collect(Collectors.toList());
        if (!list.isEmpty()) {
            Player player = (Player)list.get(0);
            brain.setMemory(MemoryModuleType.TEMPTING_PLAYER, player);
        } else {
            brain.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
        }
    }

    private boolean playerHoldingTemptation(Player player) {
        return this.isTemptation(player.getMainHandItem()) || this.isTemptation(player.getOffhandItem());
    }

    private boolean isTemptation(ItemStack itemStack) {
        return this.temptations.test(itemStack);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
    }
}

