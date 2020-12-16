/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.StaticTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventTags {
    protected static final StaticTagHelper<GameEvent> HELPER = StaticTags.create(Registry.GAME_EVENT_REGISTRY, "tags/game_events");
    public static final Tag.Named<GameEvent> VIBRATIONS = GameEventTags.bind("vibrations");
    public static final Tag.Named<GameEvent> IGNORE_VIBRATIONS_STEPPING_CAREFULLY = GameEventTags.bind("ignore_vibrations_stepping_carefully");

    private static Tag.Named<GameEvent> bind(String string) {
        return HELPER.bind(string);
    }
}

