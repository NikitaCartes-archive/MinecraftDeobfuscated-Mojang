/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.damagesource;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CombatTracker {
    public static final int RESET_DAMAGE_STATUS_TIME = 100;
    public static final int RESET_COMBAT_STATUS_TIME = 300;
    private final List<CombatEntry> entries = Lists.newArrayList();
    private final LivingEntity mob;
    private int lastDamageTime;
    private int combatStartTime;
    private int combatEndTime;
    private boolean inCombat;
    private boolean takingDamage;
    @Nullable
    private String nextLocation;

    public CombatTracker(LivingEntity livingEntity) {
        this.mob = livingEntity;
    }

    public void prepareForDamage() {
        this.resetPreparedStatus();
        Optional<BlockPos> optional = this.mob.getLastClimbablePos();
        if (optional.isPresent()) {
            BlockState blockState = this.mob.level.getBlockState(optional.get());
            this.nextLocation = blockState.is(Blocks.LADDER) || blockState.is(BlockTags.TRAPDOORS) ? "ladder" : (blockState.is(Blocks.VINE) ? "vines" : (blockState.is(Blocks.WEEPING_VINES) || blockState.is(Blocks.WEEPING_VINES_PLANT) ? "weeping_vines" : (blockState.is(Blocks.TWISTING_VINES) || blockState.is(Blocks.TWISTING_VINES_PLANT) ? "twisting_vines" : (blockState.is(Blocks.SCAFFOLDING) ? "scaffolding" : "other_climbable"))));
        } else if (this.mob.isInWater()) {
            this.nextLocation = "water";
        }
    }

    public void recordDamage(DamageSource damageSource, float f, float g) {
        this.recheckStatus();
        this.prepareForDamage();
        CombatEntry combatEntry = new CombatEntry(damageSource, this.mob.tickCount, f, g, this.nextLocation, this.mob.fallDistance);
        this.entries.add(combatEntry);
        this.lastDamageTime = this.mob.tickCount;
        this.takingDamage = true;
        if (combatEntry.isCombatRelated() && !this.inCombat && this.mob.isAlive()) {
            this.inCombat = true;
            this.combatEndTime = this.combatStartTime = this.mob.tickCount;
            this.mob.onEnterCombat();
        }
    }

    public Component getDeathMessage() {
        Component component3;
        if (this.entries.isEmpty()) {
            return Component.translatable("death.attack.generic", this.mob.getDisplayName());
        }
        CombatEntry combatEntry = this.getMostSignificantFall();
        CombatEntry combatEntry2 = this.entries.get(this.entries.size() - 1);
        Component component = combatEntry2.getAttackerName();
        Entity entity = combatEntry2.getSource().getEntity();
        if (combatEntry != null && combatEntry2.getSource() == DamageSource.FALL) {
            Component component2 = combatEntry.getAttackerName();
            if (combatEntry.getSource() == DamageSource.FALL || combatEntry.getSource() == DamageSource.OUT_OF_WORLD) {
                component3 = Component.translatable("death.fell.accident." + this.getFallLocation(combatEntry), this.mob.getDisplayName());
            } else if (component2 != null && !component2.equals(component)) {
                ItemStack itemStack;
                Entity entity2 = combatEntry.getSource().getEntity();
                if (entity2 instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity)entity2;
                    v0 = livingEntity.getMainHandItem();
                } else {
                    v0 = itemStack = ItemStack.EMPTY;
                }
                component3 = !itemStack.isEmpty() && itemStack.hasCustomHoverName() ? Component.translatable("death.fell.assist.item", this.mob.getDisplayName(), component2, itemStack.getDisplayName()) : Component.translatable("death.fell.assist", this.mob.getDisplayName(), component2);
            } else if (component != null) {
                ItemStack itemStack2;
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity2 = (LivingEntity)entity;
                    v1 = livingEntity2.getMainHandItem();
                } else {
                    v1 = itemStack2 = ItemStack.EMPTY;
                }
                component3 = !itemStack2.isEmpty() && itemStack2.hasCustomHoverName() ? Component.translatable("death.fell.finish.item", this.mob.getDisplayName(), component, itemStack2.getDisplayName()) : Component.translatable("death.fell.finish", this.mob.getDisplayName(), component);
            } else {
                component3 = Component.translatable("death.fell.killer", this.mob.getDisplayName());
            }
        } else {
            component3 = combatEntry2.getSource().getLocalizedDeathMessage(this.mob);
        }
        return component3;
    }

    @Nullable
    public LivingEntity getKiller() {
        LivingEntity livingEntity = null;
        Player player = null;
        float f = 0.0f;
        float g = 0.0f;
        for (CombatEntry combatEntry : this.entries) {
            Entity entity = combatEntry.getSource().getEntity();
            if (entity instanceof Player) {
                Player player2 = (Player)entity;
                if (player == null || combatEntry.getDamage() > g) {
                    g = combatEntry.getDamage();
                    player = player2;
                }
            }
            if (!((entity = combatEntry.getSource().getEntity()) instanceof LivingEntity)) continue;
            LivingEntity livingEntity2 = (LivingEntity)entity;
            if (livingEntity != null && !(combatEntry.getDamage() > f)) continue;
            f = combatEntry.getDamage();
            livingEntity = livingEntity2;
        }
        if (player != null && g >= f / 3.0f) {
            return player;
        }
        return livingEntity;
    }

    @Nullable
    private CombatEntry getMostSignificantFall() {
        CombatEntry combatEntry = null;
        CombatEntry combatEntry2 = null;
        float f = 0.0f;
        float g = 0.0f;
        for (int i = 0; i < this.entries.size(); ++i) {
            CombatEntry combatEntry4;
            CombatEntry combatEntry3 = this.entries.get(i);
            CombatEntry combatEntry5 = combatEntry4 = i > 0 ? this.entries.get(i - 1) : null;
            if ((combatEntry3.getSource() == DamageSource.FALL || combatEntry3.getSource() == DamageSource.OUT_OF_WORLD) && combatEntry3.getFallDistance() > 0.0f && (combatEntry == null || combatEntry3.getFallDistance() > g)) {
                combatEntry = i > 0 ? combatEntry4 : combatEntry3;
                g = combatEntry3.getFallDistance();
            }
            if (combatEntry3.getLocation() == null || combatEntry2 != null && !(combatEntry3.getDamage() > f)) continue;
            combatEntry2 = combatEntry3;
            f = combatEntry3.getDamage();
        }
        if (g > 5.0f && combatEntry != null) {
            return combatEntry;
        }
        if (f > 5.0f && combatEntry2 != null) {
            return combatEntry2;
        }
        return null;
    }

    private String getFallLocation(CombatEntry combatEntry) {
        return combatEntry.getLocation() == null ? "generic" : combatEntry.getLocation();
    }

    public boolean isTakingDamage() {
        this.recheckStatus();
        return this.takingDamage;
    }

    public boolean isInCombat() {
        this.recheckStatus();
        return this.inCombat;
    }

    public int getCombatDuration() {
        if (this.inCombat) {
            return this.mob.tickCount - this.combatStartTime;
        }
        return this.combatEndTime - this.combatStartTime;
    }

    private void resetPreparedStatus() {
        this.nextLocation = null;
    }

    public void recheckStatus() {
        int i;
        int n = i = this.inCombat ? 300 : 100;
        if (this.takingDamage && (!this.mob.isAlive() || this.mob.tickCount - this.lastDamageTime > i)) {
            boolean bl = this.inCombat;
            this.takingDamage = false;
            this.inCombat = false;
            this.combatEndTime = this.mob.tickCount;
            if (bl) {
                this.mob.onLeaveCombat();
            }
            this.entries.clear();
        }
    }

    public LivingEntity getMob() {
        return this.mob;
    }

    @Nullable
    public CombatEntry getLastEntry() {
        if (this.entries.isEmpty()) {
            return null;
        }
        return this.entries.get(this.entries.size() - 1);
    }

    public int getKillerId() {
        LivingEntity livingEntity = this.getKiller();
        return livingEntity == null ? -1 : livingEntity.getId();
    }
}

