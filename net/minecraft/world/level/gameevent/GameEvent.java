/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;

public class GameEvent {
    public static final GameEvent BLOCK_ATTACH = GameEvent.register("block_attach");
    public static final GameEvent BLOCK_CHANGE = GameEvent.register("block_change");
    public static final GameEvent BLOCK_CLOSE = GameEvent.register("block_close");
    public static final GameEvent BLOCK_DESTROY = GameEvent.register("block_destroy");
    public static final GameEvent BLOCK_DETACH = GameEvent.register("block_detach");
    public static final GameEvent BLOCK_OPEN = GameEvent.register("block_open");
    public static final GameEvent BLOCK_PLACE = GameEvent.register("block_place");
    public static final GameEvent BLOCK_PRESS = GameEvent.register("block_press");
    public static final GameEvent BLOCK_SWITCH = GameEvent.register("block_switch");
    public static final GameEvent BLOCK_UNPRESS = GameEvent.register("block_unpress");
    public static final GameEvent BLOCK_UNSWITCH = GameEvent.register("block_unswitch");
    public static final GameEvent CONTAINER_CLOSE = GameEvent.register("container_close");
    public static final GameEvent CONTAINER_OPEN = GameEvent.register("container_open");
    public static final GameEvent DISPENSE_FAIL = GameEvent.register("dispense_fail");
    public static final GameEvent DRINKING_FINISH = GameEvent.register("drinking_finish");
    public static final GameEvent EAT = GameEvent.register("eat");
    public static final GameEvent ELYTRA_FREE_FALL = GameEvent.register("elytra_free_fall");
    public static final GameEvent ENTITY_DAMAGED = GameEvent.register("entity_damaged");
    public static final GameEvent ENTITY_KILLED = GameEvent.register("entity_killed");
    public static final GameEvent ENTITY_PLACE = GameEvent.register("entity_place");
    public static final GameEvent EQUIP = GameEvent.register("equip");
    public static final GameEvent EXPLODE = GameEvent.register("explode");
    public static final GameEvent FISHING_ROD_CAST = GameEvent.register("fishing_rod_cast");
    public static final GameEvent FISHING_ROD_REEL_IN = GameEvent.register("fishing_rod_reel_in");
    public static final GameEvent FLAP = GameEvent.register("flap");
    public static final GameEvent FLUID_PICKUP = GameEvent.register("fluid_pickup");
    public static final GameEvent FLUID_PLACE = GameEvent.register("fluid_place");
    public static final GameEvent HIT_GROUND = GameEvent.register("hit_ground");
    public static final GameEvent MOB_INTERACT = GameEvent.register("mob_interact");
    public static final GameEvent LIGHTNING_STRIKE = GameEvent.register("lightning_strike");
    public static final GameEvent MINECART_MOVING = GameEvent.register("minecart_moving");
    public static final GameEvent PISTON_CONTRACT = GameEvent.register("piston_contract");
    public static final GameEvent PISTON_EXTEND = GameEvent.register("piston_extend");
    public static final GameEvent PRIME_FUSE = GameEvent.register("prime_fuse");
    public static final GameEvent PROJECTILE_LAND = GameEvent.register("projectile_land");
    public static final GameEvent PROJECTILE_SHOOT = GameEvent.register("projectile_shoot");
    public static final GameEvent RAVAGER_ROAR = GameEvent.register("ravager_roar");
    public static final GameEvent RING_BELL = GameEvent.register("ring_bell");
    public static final GameEvent SHEAR = GameEvent.register("shear");
    public static final GameEvent SHULKER_CLOSE = GameEvent.register("shulker_close");
    public static final GameEvent SHULKER_OPEN = GameEvent.register("shulker_open");
    public static final GameEvent SPLASH = GameEvent.register("splash");
    public static final GameEvent STEP = GameEvent.register("step");
    public static final GameEvent SWIM = GameEvent.register("swim");
    public static final GameEvent WOLF_SHAKING = GameEvent.register("wolf_shaking");
    public static final int DEFAULT_NOTIFICATION_RADIUS = 16;
    private final String name;
    private final int notificationRadius;
    private final Holder.Reference<GameEvent> builtInRegistryHolder = Registry.GAME_EVENT.createIntrusiveHolder(this);

    public GameEvent(String string, int i) {
        this.name = string;
        this.notificationRadius = i;
    }

    public String getName() {
        return this.name;
    }

    public int getNotificationRadius() {
        return this.notificationRadius;
    }

    private static GameEvent register(String string) {
        return GameEvent.register(string, 16);
    }

    private static GameEvent register(String string, int i) {
        return Registry.register(Registry.GAME_EVENT, string, new GameEvent(string, i));
    }

    public String toString() {
        return "Game Event{ " + this.name + " , " + this.notificationRadius + "}";
    }

    @Deprecated
    public Holder.Reference<GameEvent> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    public boolean is(TagKey<GameEvent> tagKey) {
        return this.builtInRegistryHolder.is(tagKey);
    }
}

