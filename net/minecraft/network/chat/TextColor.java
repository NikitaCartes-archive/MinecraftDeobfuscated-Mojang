/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

public final class TextColor {
    private static final Map<ChatFormatting, TextColor> LEGACY_FORMAT_TO_COLOR = Stream.of(ChatFormatting.values()).filter(ChatFormatting::isColor).collect(ImmutableMap.toImmutableMap(Function.identity(), chatFormatting -> new TextColor(chatFormatting.getColor(), chatFormatting.getName())));
    private static final Map<String, TextColor> NAMED_COLORS = LEGACY_FORMAT_TO_COLOR.values().stream().collect(ImmutableMap.toImmutableMap(textColor -> textColor.name, Function.identity()));
    private final int value;
    @Nullable
    private final String name;

    private TextColor(int i, String string) {
        this.value = i;
        this.name = string;
    }

    private TextColor(int i) {
        this.value = i;
        this.name = null;
    }

    @Environment(value=EnvType.CLIENT)
    public int getValue() {
        return this.value;
    }

    public String serialize() {
        if (this.name != null) {
            return this.name;
        }
        return this.formatValue();
    }

    private String formatValue() {
        return String.format("#%06X", this.value);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        TextColor textColor = (TextColor)object;
        return this.value == textColor.value;
    }

    public int hashCode() {
        return Objects.hash(this.value, this.name);
    }

    public String toString() {
        return this.name != null ? this.name : this.formatValue();
    }

    @Nullable
    public static TextColor fromLegacyFormat(ChatFormatting chatFormatting) {
        return LEGACY_FORMAT_TO_COLOR.get((Object)chatFormatting);
    }

    public static TextColor fromRgb(int i) {
        return new TextColor(i);
    }

    @Nullable
    public static TextColor parseColor(String string) {
        if (string.startsWith("#")) {
            int i = Integer.parseInt(string.substring(1), 16);
            return TextColor.fromRgb(i);
        }
        return NAMED_COLORS.get(string);
    }
}

