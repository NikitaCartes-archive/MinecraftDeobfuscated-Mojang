/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.tags;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.StaticTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventTags {
    private static final StaticTagHelper<GameEvent> HELPER = StaticTags.create(new ResourceLocation("game_event"), TagContainer::getGameEvents);
    public static final Tag.Named<GameEvent> VIBRATIONS = GameEventTags.bind("vibrations");
    public static final Tag.Named<GameEvent> IGNORE_VIBRATIONS_STEPPING_CAREFULLY = GameEventTags.bind("ignore_vibrations_stepping_carefully");

    private static Tag.Named<GameEvent> bind(String string) {
        return HELPER.bind(string);
    }

    public static List<? extends Tag.Named<GameEvent>> getWrappers() {
        return HELPER.getWrappers();
    }
}

