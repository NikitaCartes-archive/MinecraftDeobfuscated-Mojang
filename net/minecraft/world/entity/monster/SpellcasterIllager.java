/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class SpellcasterIllager
extends AbstractIllager {
    private static final EntityDataAccessor<Byte> DATA_SPELL_CASTING_ID = SynchedEntityData.defineId(SpellcasterIllager.class, EntityDataSerializers.BYTE);
    protected int spellCastingTickCount;
    private IllagerSpell currentSpell = IllagerSpell.NONE;

    protected SpellcasterIllager(EntityType<? extends SpellcasterIllager> entityType, Level level) {
        super((EntityType<? extends AbstractIllager>)entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SPELL_CASTING_ID, (byte)0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.spellCastingTickCount = compoundTag.getInt("SpellTicks");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("SpellTicks", this.spellCastingTickCount);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isCastingSpell()) {
            return AbstractIllager.IllagerArmPose.SPELLCASTING;
        }
        if (this.isCelebrating()) {
            return AbstractIllager.IllagerArmPose.CELEBRATING;
        }
        return AbstractIllager.IllagerArmPose.CROSSED;
    }

    public boolean isCastingSpell() {
        if (this.level.isClientSide) {
            return this.entityData.get(DATA_SPELL_CASTING_ID) > 0;
        }
        return this.spellCastingTickCount > 0;
    }

    public void setIsCastingSpell(IllagerSpell illagerSpell) {
        this.currentSpell = illagerSpell;
        this.entityData.set(DATA_SPELL_CASTING_ID, (byte)illagerSpell.id);
    }

    protected IllagerSpell getCurrentSpell() {
        if (!this.level.isClientSide) {
            return this.currentSpell;
        }
        return IllagerSpell.byId(this.entityData.get(DATA_SPELL_CASTING_ID).byteValue());
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.spellCastingTickCount > 0) {
            --this.spellCastingTickCount;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide && this.isCastingSpell()) {
            IllagerSpell illagerSpell = this.getCurrentSpell();
            double d = illagerSpell.spellColor[0];
            double e = illagerSpell.spellColor[1];
            double f = illagerSpell.spellColor[2];
            float g = this.yBodyRot * ((float)Math.PI / 180) + Mth.cos((float)this.tickCount * 0.6662f) * 0.25f;
            float h = Mth.cos(g);
            float i = Mth.sin(g);
            this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + (double)h * 0.6, this.getY() + 1.8, this.getZ() + (double)i * 0.6, d, e, f);
            this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() - (double)h * 0.6, this.getY() + 1.8, this.getZ() - (double)i * 0.6, d, e, f);
        }
    }

    protected int getSpellCastingTime() {
        return this.spellCastingTickCount;
    }

    protected abstract SoundEvent getCastingSoundEvent();

    public static enum IllagerSpell {
        NONE(0, 0.0, 0.0, 0.0),
        SUMMON_VEX(1, 0.7, 0.7, 0.8),
        FANGS(2, 0.4, 0.3, 0.35),
        WOLOLO(3, 0.7, 0.5, 0.2),
        DISAPPEAR(4, 0.3, 0.3, 0.8),
        BLINDNESS(5, 0.1, 0.1, 0.2);

        private final int id;
        private final double[] spellColor;

        private IllagerSpell(int j, double d, double e, double f) {
            this.id = j;
            this.spellColor = new double[]{d, e, f};
        }

        public static IllagerSpell byId(int i) {
            for (IllagerSpell illagerSpell : IllagerSpell.values()) {
                if (i != illagerSpell.id) continue;
                return illagerSpell;
            }
            return NONE;
        }
    }

    public abstract class SpellcasterUseSpellGoal
    extends Goal {
        protected int attackWarmupDelay;
        protected int nextAttackTickCount;

        protected SpellcasterUseSpellGoal() {
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = SpellcasterIllager.this.getTarget();
            if (livingEntity == null || !livingEntity.isAlive()) {
                return false;
            }
            if (SpellcasterIllager.this.isCastingSpell()) {
                return false;
            }
            return SpellcasterIllager.this.tickCount >= this.nextAttackTickCount;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity livingEntity = SpellcasterIllager.this.getTarget();
            return livingEntity != null && livingEntity.isAlive() && this.attackWarmupDelay > 0;
        }

        @Override
        public void start() {
            this.attackWarmupDelay = this.getCastWarmupTime();
            SpellcasterIllager.this.spellCastingTickCount = this.getCastingTime();
            this.nextAttackTickCount = SpellcasterIllager.this.tickCount + this.getCastingInterval();
            SoundEvent soundEvent = this.getSpellPrepareSound();
            if (soundEvent != null) {
                SpellcasterIllager.this.playSound(soundEvent, 1.0f, 1.0f);
            }
            SpellcasterIllager.this.setIsCastingSpell(this.getSpell());
        }

        @Override
        public void tick() {
            --this.attackWarmupDelay;
            if (this.attackWarmupDelay == 0) {
                this.performSpellCasting();
                SpellcasterIllager.this.playSound(SpellcasterIllager.this.getCastingSoundEvent(), 1.0f, 1.0f);
            }
        }

        protected abstract void performSpellCasting();

        protected int getCastWarmupTime() {
            return 20;
        }

        protected abstract int getCastingTime();

        protected abstract int getCastingInterval();

        @Nullable
        protected abstract SoundEvent getSpellPrepareSound();

        protected abstract IllagerSpell getSpell();
    }

    public class SpellcasterCastingSpellGoal
    extends Goal {
        public SpellcasterCastingSpellGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return SpellcasterIllager.this.getSpellCastingTime() > 0;
        }

        @Override
        public void start() {
            super.start();
            SpellcasterIllager.this.navigation.stop();
        }

        @Override
        public void stop() {
            super.stop();
            SpellcasterIllager.this.setIsCastingSpell(IllagerSpell.NONE);
        }

        @Override
        public void tick() {
            if (SpellcasterIllager.this.getTarget() != null) {
                SpellcasterIllager.this.getLookControl().setLookAt(SpellcasterIllager.this.getTarget(), SpellcasterIllager.this.getMaxHeadYRot(), SpellcasterIllager.this.getMaxHeadXRot());
            }
        }
    }
}

