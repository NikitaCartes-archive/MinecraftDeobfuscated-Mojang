/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.damagesource;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public class CombatTracker {
    private final List<CombatEntry> entries = Lists.newArrayList();
    private final LivingEntity mob;
    private int lastDamageTime;
    private int combatStartTime;
    private int combatEndTime;
    private boolean inCombat;
    private boolean takingDamage;
    private String nextLocation;

    public CombatTracker(LivingEntity livingEntity) {
        this.mob = livingEntity;
    }

    public void prepareForDamage() {
        this.resetPreparedStatus();
        Optional<BlockPos> optional = this.mob.getLastClimbablePos();
        if (optional.isPresent()) {
            Block block = this.mob.level.getBlockState(optional.get()).getBlock();
            this.nextLocation = block == Blocks.LADDER || block.is(BlockTags.TRAPDOORS) ? "ladder" : (block == Blocks.VINE ? "vines" : (block == Blocks.WEEPING_VINES || block == Blocks.WEEPING_VINES_PLANT ? "weeping_vines" : (block == Blocks.TWISTING_VINES || block == Blocks.TWISTING_VINES_PLANT ? "twisting_vines" : (block == Blocks.SCAFFOLDING ? "scaffolding" : "other_climbable"))));
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
            return new TranslatableComponent("death.attack.generic", this.mob.getDisplayName());
        }
        CombatEntry combatEntry = this.getMostSignificantFall();
        CombatEntry combatEntry2 = this.entries.get(this.entries.size() - 1);
        Component component = combatEntry2.getAttackerName();
        Entity entity = combatEntry2.getSource().getEntity();
        if (combatEntry != null && combatEntry2.getSource() == DamageSource.FALL) {
            Component component2 = combatEntry.getAttackerName();
            if (combatEntry.getSource() == DamageSource.FALL || combatEntry.getSource() == DamageSource.OUT_OF_WORLD) {
                component3 = new TranslatableComponent("death.fell.accident." + this.getFallLocation(combatEntry), this.mob.getDisplayName());
            } else if (!(component2 == null || component != null && component2.equals(component))) {
                ItemStack itemStack;
                Entity entity2 = combatEntry.getSource().getEntity();
                ItemStack itemStack2 = itemStack = entity2 instanceof LivingEntity ? ((LivingEntity)entity2).getMainHandItem() : ItemStack.EMPTY;
                component3 = !itemStack.isEmpty() && itemStack.hasCustomHoverName() ? new TranslatableComponent("death.fell.assist.item", this.mob.getDisplayName(), component2, itemStack.getDisplayName()) : new TranslatableComponent("death.fell.assist", this.mob.getDisplayName(), component2);
            } else if (component != null) {
                ItemStack itemStack2;
                ItemStack itemStack = itemStack2 = entity instanceof LivingEntity ? ((LivingEntity)entity).getMainHandItem() : ItemStack.EMPTY;
                component3 = !itemStack2.isEmpty() && itemStack2.hasCustomHoverName() ? new TranslatableComponent("death.fell.finish.item", this.mob.getDisplayName(), component, itemStack2.getDisplayName()) : new TranslatableComponent("death.fell.finish", this.mob.getDisplayName(), component);
            } else {
                component3 = new TranslatableComponent("death.fell.killer", this.mob.getDisplayName());
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
            if (combatEntry.getSource().getEntity() instanceof Player && (player == null || combatEntry.getDamage() > g)) {
                g = combatEntry.getDamage();
                player = (Player)combatEntry.getSource().getEntity();
            }
            if (!(combatEntry.getSource().getEntity() instanceof LivingEntity) || livingEntity != null && !(combatEntry.getDamage() > f)) continue;
            f = combatEntry.getDamage();
            livingEntity = (LivingEntity)combatEntry.getSource().getEntity();
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
}

