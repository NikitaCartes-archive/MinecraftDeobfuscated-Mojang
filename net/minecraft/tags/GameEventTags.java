/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import net.minecraft.core.Registry;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.StaticTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventTags {
    protected static final StaticTagHelper<GameEvent> HELPER = StaticTags.create(Registry.GAME_EVENT_REGISTRY, "tags/game_events");
    public static final Tag.Named<GameEvent> VIBRATIONS = GameEventTags.bind("vibrations");
    public static final Tag.Named<GameEvent> IGNORE_VIBRATIONS_SNEAKING = GameEventTags.bind("ignore_vibrations_sneaking");

    private static Tag.Named<GameEvent> bind(String string) {
        return HELPER.bind(string);
    }

    public static TagCollection<GameEvent> getAllTags() {
        return HELPER.getAllTags();
    }
}

