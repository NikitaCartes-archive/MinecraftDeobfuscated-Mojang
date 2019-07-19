/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.AbsoptionMobEffect;
import net.minecraft.world.effect.AttackDamageMobEffect;
import net.minecraft.world.effect.HealthBoostMobEffect;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;

public class MobEffects {
    public static final MobEffect MOVEMENT_SPEED = MobEffects.register(1, "speed", new MobEffect(MobEffectCategory.BENEFICIAL, 8171462).addAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "91AEAA56-376B-4498-935B-2F7F68070635", 0.2f, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final MobEffect MOVEMENT_SLOWDOWN = MobEffects.register(2, "slowness", new MobEffect(MobEffectCategory.HARMFUL, 5926017).addAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-514C1F160890", -0.15f, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final MobEffect DIG_SPEED = MobEffects.register(3, "haste", new MobEffect(MobEffectCategory.BENEFICIAL, 14270531).addAttributeModifier(SharedMonsterAttributes.ATTACK_SPEED, "AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3", 0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final MobEffect DIG_SLOWDOWN = MobEffects.register(4, "mining_fatigue", new MobEffect(MobEffectCategory.HARMFUL, 4866583).addAttributeModifier(SharedMonsterAttributes.ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.1f, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static final MobEffect DAMAGE_BOOST = MobEffects.register(5, "strength", new AttackDamageMobEffect(MobEffectCategory.BENEFICIAL, 9643043, 3.0).addAttributeModifier(SharedMonsterAttributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 0.0, AttributeModifier.Operation.ADDITION));
    public static final MobEffect HEAL = MobEffects.register(6, "instant_health", new InstantenousMobEffect(MobEffectCategory.BENEFICIAL, 16262179));
    public static final MobEffect HARM = MobEffects.register(7, "instant_damage", new InstantenousMobEffect(MobEffectCategory.HARMFUL, 4393481));
    public static final MobEffect JUMP = MobEffects.register(8, "jump_boost", new MobEffect(MobEffectCategory.BENEFICIAL, 2293580));
    public static final MobEffect CONFUSION = MobEffects.register(9, "nausea", new MobEffect(MobEffectCategory.HARMFUL, 5578058));
    public static final MobEffect REGENERATION = MobEffects.register(10, "regeneration", new MobEffect(MobEffectCategory.BENEFICIAL, 13458603));
    public static final MobEffect DAMAGE_RESISTANCE = MobEffects.register(11, "resistance", new MobEffect(MobEffectCategory.BENEFICIAL, 10044730));
    public static final MobEffect FIRE_RESISTANCE = MobEffects.register(12, "fire_resistance", new MobEffect(MobEffectCategory.BENEFICIAL, 14981690));
    public static final MobEffect WATER_BREATHING = MobEffects.register(13, "water_breathing", new MobEffect(MobEffectCategory.BENEFICIAL, 3035801));
    public static final MobEffect INVISIBILITY = MobEffects.register(14, "invisibility", new MobEffect(MobEffectCategory.BENEFICIAL, 8356754));
    public static final MobEffect BLINDNESS = MobEffects.register(15, "blindness", new MobEffect(MobEffectCategory.HARMFUL, 2039587));
    public static final MobEffect NIGHT_VISION = MobEffects.register(16, "night_vision", new MobEffect(MobEffectCategory.BENEFICIAL, 0x1F1FA1));
    public static final MobEffect HUNGER = MobEffects.register(17, "hunger", new MobEffect(MobEffectCategory.HARMFUL, 5797459));
    public static final MobEffect WEAKNESS = MobEffects.register(18, "weakness", new AttackDamageMobEffect(MobEffectCategory.HARMFUL, 0x484D48, -4.0).addAttributeModifier(SharedMonsterAttributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", 0.0, AttributeModifier.Operation.ADDITION));
    public static final MobEffect POISON = MobEffects.register(19, "poison", new MobEffect(MobEffectCategory.HARMFUL, 5149489));
    public static final MobEffect WITHER = MobEffects.register(20, "wither", new MobEffect(MobEffectCategory.HARMFUL, 3484199));
    public static final MobEffect HEALTH_BOOST = MobEffects.register(21, "health_boost", new HealthBoostMobEffect(MobEffectCategory.BENEFICIAL, 16284963).addAttributeModifier(SharedMonsterAttributes.MAX_HEALTH, "5D6F0BA2-1186-46AC-B896-C61C5CEE99CC", 4.0, AttributeModifier.Operation.ADDITION));
    public static final MobEffect ABSORPTION = MobEffects.register(22, "absorption", new AbsoptionMobEffect(MobEffectCategory.BENEFICIAL, 0x2552A5));
    public static final MobEffect SATURATION = MobEffects.register(23, "saturation", new InstantenousMobEffect(MobEffectCategory.BENEFICIAL, 16262179));
    public static final MobEffect GLOWING = MobEffects.register(24, "glowing", new MobEffect(MobEffectCategory.NEUTRAL, 9740385));
    public static final MobEffect LEVITATION = MobEffects.register(25, "levitation", new MobEffect(MobEffectCategory.HARMFUL, 0xCEFFFF));
    public static final MobEffect LUCK = MobEffects.register(26, "luck", new MobEffect(MobEffectCategory.BENEFICIAL, 0x339900).addAttributeModifier(SharedMonsterAttributes.LUCK, "03C3C89D-7037-4B42-869F-B146BCB64D2E", 1.0, AttributeModifier.Operation.ADDITION));
    public static final MobEffect UNLUCK = MobEffects.register(27, "unluck", new MobEffect(MobEffectCategory.HARMFUL, 12624973).addAttributeModifier(SharedMonsterAttributes.LUCK, "CC5AF142-2BD2-4215-B636-2605AED11727", -1.0, AttributeModifier.Operation.ADDITION));
    public static final MobEffect SLOW_FALLING = MobEffects.register(28, "slow_falling", new MobEffect(MobEffectCategory.BENEFICIAL, 16773073));
    public static final MobEffect CONDUIT_POWER = MobEffects.register(29, "conduit_power", new MobEffect(MobEffectCategory.BENEFICIAL, 1950417));
    public static final MobEffect DOLPHINS_GRACE = MobEffects.register(30, "dolphins_grace", new MobEffect(MobEffectCategory.BENEFICIAL, 8954814));
    public static final MobEffect BAD_OMEN = MobEffects.register(31, "bad_omen", new MobEffect(MobEffectCategory.NEUTRAL, 745784){

        @Override
        public boolean isDurationEffectTick(int i, int j) {
            return true;
        }

        @Override
        public void applyEffectTick(LivingEntity livingEntity, int i) {
            if (livingEntity instanceof ServerPlayer && !livingEntity.isSpectator()) {
                ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
                ServerLevel serverLevel = serverPlayer.getLevel();
                if (serverLevel.getDifficulty() == Difficulty.PEACEFUL) {
                    return;
                }
                if (serverLevel.isVillage(new BlockPos(livingEntity))) {
                    serverLevel.getRaids().createOrExtendRaid(serverPlayer);
                }
            }
        }
    });
    public static final MobEffect HERO_OF_THE_VILLAGE = MobEffects.register(32, "hero_of_the_village", new MobEffect(MobEffectCategory.BENEFICIAL, 0x44FF44));

    private static MobEffect register(int i, String string, MobEffect mobEffect) {
        return Registry.registerMapping(Registry.MOB_EFFECT, i, string, mobEffect);
    }
}

