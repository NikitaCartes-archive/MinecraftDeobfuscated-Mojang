/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.damagesource;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;

public interface DamageTypes {
    public static final ResourceKey<DamageType> IN_FIRE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("in_fire"));
    public static final ResourceKey<DamageType> LIGHTNING_BOLT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("lightning_bolt"));
    public static final ResourceKey<DamageType> ON_FIRE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("on_fire"));
    public static final ResourceKey<DamageType> LAVA = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("lava"));
    public static final ResourceKey<DamageType> HOT_FLOOR = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("hot_floor"));
    public static final ResourceKey<DamageType> IN_WALL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("in_wall"));
    public static final ResourceKey<DamageType> CRAMMING = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("cramming"));
    public static final ResourceKey<DamageType> DROWN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("drown"));
    public static final ResourceKey<DamageType> STARVE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("starve"));
    public static final ResourceKey<DamageType> CACTUS = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("cactus"));
    public static final ResourceKey<DamageType> FALL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("fall"));
    public static final ResourceKey<DamageType> FLY_INTO_WALL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("fly_into_wall"));
    public static final ResourceKey<DamageType> OUT_OF_WORLD = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("out_of_world"));
    public static final ResourceKey<DamageType> GENERIC = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("generic"));
    public static final ResourceKey<DamageType> MAGIC = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("magic"));
    public static final ResourceKey<DamageType> WITHER = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("wither"));
    public static final ResourceKey<DamageType> DRAGON_BREATH = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("dragon_breath"));
    public static final ResourceKey<DamageType> DRY_OUT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("dry_out"));
    public static final ResourceKey<DamageType> SWEET_BERRY_BUSH = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sweet_berry_bush"));
    public static final ResourceKey<DamageType> FREEZE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("freeze"));
    public static final ResourceKey<DamageType> STALAGMITE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("stalagmite"));
    public static final ResourceKey<DamageType> FALLING_BLOCK = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("falling_block"));
    public static final ResourceKey<DamageType> FALLING_ANVIL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("falling_anvil"));
    public static final ResourceKey<DamageType> FALLING_STALACTITE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("falling_stalactite"));
    public static final ResourceKey<DamageType> STING = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sting"));
    public static final ResourceKey<DamageType> MOB_ATTACK = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("mob_attack"));
    public static final ResourceKey<DamageType> MOB_ATTACK_NO_AGGRO = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("mob_attack_no_aggro"));
    public static final ResourceKey<DamageType> PLAYER_ATTACK = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("player_attack"));
    public static final ResourceKey<DamageType> ARROW = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("arrow"));
    public static final ResourceKey<DamageType> TRIDENT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("trident"));
    public static final ResourceKey<DamageType> MOB_PROJECTILE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("mob_projectile"));
    public static final ResourceKey<DamageType> FIREWORKS = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("fireworks"));
    public static final ResourceKey<DamageType> FIREBALL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("fireball"));
    public static final ResourceKey<DamageType> UNATTRIBUTED_FIREBALL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("unattributed_fireball"));
    public static final ResourceKey<DamageType> WITHER_SKULL = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("wither_skull"));
    public static final ResourceKey<DamageType> THROWN = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("thrown"));
    public static final ResourceKey<DamageType> INDIRECT_MAGIC = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("indirect_magic"));
    public static final ResourceKey<DamageType> THORNS = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("thorns"));
    public static final ResourceKey<DamageType> EXPLOSION = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("explosion"));
    public static final ResourceKey<DamageType> PLAYER_EXPLOSION = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("player_explosion"));
    public static final ResourceKey<DamageType> SONIC_BOOM = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("sonic_boom"));
    public static final ResourceKey<DamageType> BAD_RESPAWN_POINT = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("bad_respawn_point"));

    public static void bootstrap(BootstapContext<DamageType> bootstapContext) {
        bootstapContext.register(IN_FIRE, new DamageType("inFire", 0.1f, DamageEffects.BURNING));
        bootstapContext.register(LIGHTNING_BOLT, new DamageType("lightningBolt", 0.1f));
        bootstapContext.register(ON_FIRE, new DamageType("onFire", 0.0f, DamageEffects.BURNING));
        bootstapContext.register(LAVA, new DamageType("lava", 0.1f, DamageEffects.BURNING));
        bootstapContext.register(HOT_FLOOR, new DamageType("hotFloor", 0.1f, DamageEffects.BURNING));
        bootstapContext.register(IN_WALL, new DamageType("inWall", 0.0f));
        bootstapContext.register(CRAMMING, new DamageType("cramming", 0.0f));
        bootstapContext.register(DROWN, new DamageType("drown", 0.0f, DamageEffects.DROWNING));
        bootstapContext.register(STARVE, new DamageType("starve", 0.0f));
        bootstapContext.register(CACTUS, new DamageType("cactus", 0.1f));
        bootstapContext.register(FALL, new DamageType("fall", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.0f, DamageEffects.HURT, DeathMessageType.FALL_VARIANTS));
        bootstapContext.register(FLY_INTO_WALL, new DamageType("flyIntoWall", 0.0f));
        bootstapContext.register(OUT_OF_WORLD, new DamageType("outOfWorld", 0.0f));
        bootstapContext.register(GENERIC, new DamageType("generic", 0.0f));
        bootstapContext.register(MAGIC, new DamageType("magic", 0.0f));
        bootstapContext.register(WITHER, new DamageType("wither", 0.0f));
        bootstapContext.register(DRAGON_BREATH, new DamageType("dragonBreath", 0.0f));
        bootstapContext.register(DRY_OUT, new DamageType("dryout", 0.1f));
        bootstapContext.register(SWEET_BERRY_BUSH, new DamageType("sweetBerryBush", 0.1f, DamageEffects.POKING));
        bootstapContext.register(FREEZE, new DamageType("freeze", 0.0f, DamageEffects.FREEZING));
        bootstapContext.register(STALAGMITE, new DamageType("stalagmite", 0.0f));
        bootstapContext.register(FALLING_BLOCK, new DamageType("fallingBlock", 0.1f));
        bootstapContext.register(FALLING_ANVIL, new DamageType("anvil", 0.1f));
        bootstapContext.register(FALLING_STALACTITE, new DamageType("fallingStalactite", 0.1f));
        bootstapContext.register(STING, new DamageType("sting", 0.1f));
        bootstapContext.register(MOB_ATTACK, new DamageType("mob", 0.1f));
        bootstapContext.register(MOB_ATTACK_NO_AGGRO, new DamageType("mob", 0.1f));
        bootstapContext.register(PLAYER_ATTACK, new DamageType("player", 0.1f));
        bootstapContext.register(ARROW, new DamageType("arrow", 0.1f));
        bootstapContext.register(TRIDENT, new DamageType("trident", 0.1f));
        bootstapContext.register(MOB_PROJECTILE, new DamageType("mob", 0.1f));
        bootstapContext.register(FIREWORKS, new DamageType("fireworks", 0.1f));
        bootstapContext.register(UNATTRIBUTED_FIREBALL, new DamageType("onFire", 0.1f, DamageEffects.BURNING));
        bootstapContext.register(FIREBALL, new DamageType("fireball", 0.1f, DamageEffects.BURNING));
        bootstapContext.register(WITHER_SKULL, new DamageType("witherSkull", 0.1f));
        bootstapContext.register(THROWN, new DamageType("thrown", 0.1f));
        bootstapContext.register(INDIRECT_MAGIC, new DamageType("indirectMagic", 0.0f));
        bootstapContext.register(THORNS, new DamageType("thorns", 0.1f, DamageEffects.THORNS));
        bootstapContext.register(EXPLOSION, new DamageType("explosion", DamageScaling.ALWAYS, 0.1f));
        bootstapContext.register(PLAYER_EXPLOSION, new DamageType("explosion.player", DamageScaling.ALWAYS, 0.1f));
        bootstapContext.register(SONIC_BOOM, new DamageType("sonic_boom", DamageScaling.ALWAYS, 0.0f));
        bootstapContext.register(BAD_RESPAWN_POINT, new DamageType("badRespawnPoint", DamageScaling.ALWAYS, 0.1f, DamageEffects.HURT, DeathMessageType.INTENTIONAL_GAME_DESIGN));
    }
}

