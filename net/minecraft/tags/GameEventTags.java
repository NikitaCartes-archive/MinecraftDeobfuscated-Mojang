/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventTags {
    public static final TagKey<GameEvent> VIBRATIONS = GameEventTags.create("vibrations");
    public static final TagKey<GameEvent> WARDEN_EVENTS_CAN_LISTEN = GameEventTags.create("warden_events_can_listen");
    public static final TagKey<GameEvent> IGNORE_VIBRATIONS_SNEAKING = GameEventTags.create("ignore_vibrations_sneaking");
    public static final TagKey<GameEvent> IGNORE_VIBRATIONS_ON_OCCLUDING_BLOCK = GameEventTags.create("ignore_vibrations_on_occluding_block");

    private static TagKey<GameEvent> create(String string) {
        return TagKey.create(Registry.GAME_EVENT_REGISTRY, new ResourceLocation(string));
    }
}

