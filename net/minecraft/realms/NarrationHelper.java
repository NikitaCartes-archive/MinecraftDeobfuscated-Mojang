/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.realms;

import java.time.Duration;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.realms.RepeatedNarrator;

@Environment(value=EnvType.CLIENT)
public class NarrationHelper {
    private static final RepeatedNarrator REPEATED_NARRATOR = new RepeatedNarrator(Duration.ofSeconds(5L));

    public static void now(String string) {
        NarratorChatListener narratorChatListener = NarratorChatListener.INSTANCE;
        narratorChatListener.clear();
        narratorChatListener.handle(ChatType.SYSTEM, new TextComponent(NarrationHelper.fixNarrationNewlines(string)));
    }

    private static String fixNarrationNewlines(String string) {
        return string.replace("\\n", System.lineSeparator());
    }

    public static void now(String ... strings) {
        NarrationHelper.now(Arrays.asList(strings));
    }

    public static void now(Iterable<String> iterable) {
        NarrationHelper.now(NarrationHelper.join(iterable));
    }

    public static String join(Iterable<String> iterable) {
        return String.join((CharSequence)System.lineSeparator(), iterable);
    }

    public static void repeatedly(String string) {
        REPEATED_NARRATOR.narrate(NarrationHelper.fixNarrationNewlines(string));
    }
}

