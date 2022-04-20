/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public class Style {
    public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null);
    public static final ResourceLocation DEFAULT_FONT = new ResourceLocation("minecraft", "default");
    @Nullable
    final TextColor color;
    @Nullable
    final Boolean bold;
    @Nullable
    final Boolean italic;
    @Nullable
    final Boolean underlined;
    @Nullable
    final Boolean strikethrough;
    @Nullable
    final Boolean obfuscated;
    @Nullable
    final ClickEvent clickEvent;
    @Nullable
    final HoverEvent hoverEvent;
    @Nullable
    final String insertion;
    @Nullable
    final ResourceLocation font;

    Style(@Nullable TextColor textColor, @Nullable Boolean boolean_, @Nullable Boolean boolean2, @Nullable Boolean boolean3, @Nullable Boolean boolean4, @Nullable Boolean boolean5, @Nullable ClickEvent clickEvent, @Nullable HoverEvent hoverEvent, @Nullable String string, @Nullable ResourceLocation resourceLocation) {
        this.color = textColor;
        this.bold = boolean_;
        this.italic = boolean2;
        this.underlined = boolean3;
        this.strikethrough = boolean4;
        this.obfuscated = boolean5;
        this.clickEvent = clickEvent;
        this.hoverEvent = hoverEvent;
        this.insertion = string;
        this.font = resourceLocation;
    }

    @Nullable
    public TextColor getColor() {
        return this.color;
    }

    public boolean isBold() {
        return this.bold == Boolean.TRUE;
    }

    public boolean isItalic() {
        return this.italic == Boolean.TRUE;
    }

    public boolean isStrikethrough() {
        return this.strikethrough == Boolean.TRUE;
    }

    public boolean isUnderlined() {
        return this.underlined == Boolean.TRUE;
    }

    public boolean isObfuscated() {
        return this.obfuscated == Boolean.TRUE;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    @Nullable
    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    @Nullable
    public HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    @Nullable
    public String getInsertion() {
        return this.insertion;
    }

    public ResourceLocation getFont() {
        return this.font != null ? this.font : DEFAULT_FONT;
    }

    public Style withColor(@Nullable TextColor textColor) {
        return new Style(textColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withColor(@Nullable ChatFormatting chatFormatting) {
        return this.withColor(chatFormatting != null ? TextColor.fromLegacyFormat(chatFormatting) : null);
    }

    public Style withColor(int i) {
        return this.withColor(TextColor.fromRgb(i));
    }

    public Style withBold(@Nullable Boolean boolean_) {
        return new Style(this.color, boolean_, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withItalic(@Nullable Boolean boolean_) {
        return new Style(this.color, this.bold, boolean_, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withUnderlined(@Nullable Boolean boolean_) {
        return new Style(this.color, this.bold, this.italic, boolean_, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withStrikethrough(@Nullable Boolean boolean_) {
        return new Style(this.color, this.bold, this.italic, this.underlined, boolean_, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withObfuscated(@Nullable Boolean boolean_) {
        return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, boolean_, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withClickEvent(@Nullable ClickEvent clickEvent) {
        return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style withHoverEvent(@Nullable HoverEvent hoverEvent) {
        return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, hoverEvent, this.insertion, this.font);
    }

    public Style withInsertion(@Nullable String string) {
        return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, string, this.font);
    }

    public Style withFont(@Nullable ResourceLocation resourceLocation) {
        return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, resourceLocation);
    }

    public Style applyFormat(ChatFormatting chatFormatting) {
        TextColor textColor = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        switch (chatFormatting) {
            case OBFUSCATED: {
                boolean5 = true;
                break;
            }
            case BOLD: {
                boolean_ = true;
                break;
            }
            case STRIKETHROUGH: {
                boolean3 = true;
                break;
            }
            case UNDERLINE: {
                boolean4 = true;
                break;
            }
            case ITALIC: {
                boolean2 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                textColor = TextColor.fromLegacyFormat(chatFormatting);
            }
        }
        return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style applyLegacyFormat(ChatFormatting chatFormatting) {
        TextColor textColor = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        switch (chatFormatting) {
            case OBFUSCATED: {
                boolean5 = true;
                break;
            }
            case BOLD: {
                boolean_ = true;
                break;
            }
            case STRIKETHROUGH: {
                boolean3 = true;
                break;
            }
            case UNDERLINE: {
                boolean4 = true;
                break;
            }
            case ITALIC: {
                boolean2 = true;
                break;
            }
            case RESET: {
                return EMPTY;
            }
            default: {
                boolean5 = false;
                boolean_ = false;
                boolean3 = false;
                boolean4 = false;
                boolean2 = false;
                textColor = TextColor.fromLegacyFormat(chatFormatting);
            }
        }
        return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style applyFormats(ChatFormatting ... chatFormattings) {
        TextColor textColor = this.color;
        Boolean boolean_ = this.bold;
        Boolean boolean2 = this.italic;
        Boolean boolean3 = this.strikethrough;
        Boolean boolean4 = this.underlined;
        Boolean boolean5 = this.obfuscated;
        block8: for (ChatFormatting chatFormatting : chatFormattings) {
            switch (chatFormatting) {
                case OBFUSCATED: {
                    boolean5 = true;
                    continue block8;
                }
                case BOLD: {
                    boolean_ = true;
                    continue block8;
                }
                case STRIKETHROUGH: {
                    boolean3 = true;
                    continue block8;
                }
                case UNDERLINE: {
                    boolean4 = true;
                    continue block8;
                }
                case ITALIC: {
                    boolean2 = true;
                    continue block8;
                }
                case RESET: {
                    return EMPTY;
                }
                default: {
                    textColor = TextColor.fromLegacyFormat(chatFormatting);
                }
            }
        }
        return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
    }

    public Style applyTo(Style style) {
        if (this == EMPTY) {
            return style;
        }
        if (style == EMPTY) {
            return this;
        }
        return new Style(this.color != null ? this.color : style.color, this.bold != null ? this.bold : style.bold, this.italic != null ? this.italic : style.italic, this.underlined != null ? this.underlined : style.underlined, this.strikethrough != null ? this.strikethrough : style.strikethrough, this.obfuscated != null ? this.obfuscated : style.obfuscated, this.clickEvent != null ? this.clickEvent : style.clickEvent, this.hoverEvent != null ? this.hoverEvent : style.hoverEvent, this.insertion != null ? this.insertion : style.insertion, this.font != null ? this.font : style.font);
    }

    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("{");
        class Collector {
            private boolean isNotFirst;

            Collector() {
            }

            private void prependSeparator() {
                if (this.isNotFirst) {
                    stringBuilder.append(',');
                }
                this.isNotFirst = true;
            }

            void addFlagString(String string, @Nullable Boolean boolean_) {
                if (boolean_ != null) {
                    this.prependSeparator();
                    if (!boolean_.booleanValue()) {
                        stringBuilder.append('!');
                    }
                    stringBuilder.append(string);
                }
            }

            void addValueString(String string, @Nullable Object object) {
                if (object != null) {
                    this.prependSeparator();
                    stringBuilder.append(string);
                    stringBuilder.append('=');
                    stringBuilder.append(object);
                }
            }
        }
        Collector lv = new Collector();
        lv.addValueString("color", this.color);
        lv.addFlagString("bold", this.bold);
        lv.addFlagString("italic", this.italic);
        lv.addFlagString("underlined", this.underlined);
        lv.addFlagString("strikethrough", this.strikethrough);
        lv.addFlagString("obfuscated", this.obfuscated);
        lv.addValueString("clickEvent", this.clickEvent);
        lv.addValueString("hoverEvent", this.hoverEvent);
        lv.addValueString("insertion", this.insertion);
        lv.addValueString("font", this.font);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Style) {
            Style style = (Style)object;
            return this.isBold() == style.isBold() && Objects.equals(this.getColor(), style.getColor()) && this.isItalic() == style.isItalic() && this.isObfuscated() == style.isObfuscated() && this.isStrikethrough() == style.isStrikethrough() && this.isUnderlined() == style.isUnderlined() && Objects.equals(this.getClickEvent(), style.getClickEvent()) && Objects.equals(this.getHoverEvent(), style.getHoverEvent()) && Objects.equals(this.getInsertion(), style.getInsertion()) && Objects.equals(this.getFont(), style.getFont());
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion);
    }

    public static class Serializer
    implements JsonDeserializer<Style>,
    JsonSerializer<Style> {
        @Override
        @Nullable
        public Style deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject == null) {
                    return null;
                }
                Boolean boolean_ = Serializer.getOptionalFlag(jsonObject, "bold");
                Boolean boolean2 = Serializer.getOptionalFlag(jsonObject, "italic");
                Boolean boolean3 = Serializer.getOptionalFlag(jsonObject, "underlined");
                Boolean boolean4 = Serializer.getOptionalFlag(jsonObject, "strikethrough");
                Boolean boolean5 = Serializer.getOptionalFlag(jsonObject, "obfuscated");
                TextColor textColor = Serializer.getTextColor(jsonObject);
                String string = Serializer.getInsertion(jsonObject);
                ClickEvent clickEvent = Serializer.getClickEvent(jsonObject);
                HoverEvent hoverEvent = Serializer.getHoverEvent(jsonObject);
                ResourceLocation resourceLocation = Serializer.getFont(jsonObject);
                return new Style(textColor, boolean_, boolean2, boolean3, boolean4, boolean5, clickEvent, hoverEvent, string, resourceLocation);
            }
            return null;
        }

        @Nullable
        private static ResourceLocation getFont(JsonObject jsonObject) {
            if (jsonObject.has("font")) {
                String string = GsonHelper.getAsString(jsonObject, "font");
                try {
                    return new ResourceLocation(string);
                } catch (ResourceLocationException resourceLocationException) {
                    throw new JsonSyntaxException("Invalid font name: " + string);
                }
            }
            return null;
        }

        @Nullable
        private static HoverEvent getHoverEvent(JsonObject jsonObject) {
            JsonObject jsonObject2;
            HoverEvent hoverEvent;
            if (jsonObject.has("hoverEvent") && (hoverEvent = HoverEvent.deserialize(jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "hoverEvent"))) != null && hoverEvent.getAction().isAllowedFromServer()) {
                return hoverEvent;
            }
            return null;
        }

        @Nullable
        private static ClickEvent getClickEvent(JsonObject jsonObject) {
            if (jsonObject.has("clickEvent")) {
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "clickEvent");
                String string = GsonHelper.getAsString(jsonObject2, "action", null);
                ClickEvent.Action action = string == null ? null : ClickEvent.Action.getByName(string);
                String string2 = GsonHelper.getAsString(jsonObject2, "value", null);
                if (action != null && string2 != null && action.isAllowedFromServer()) {
                    return new ClickEvent(action, string2);
                }
            }
            return null;
        }

        @Nullable
        private static String getInsertion(JsonObject jsonObject) {
            return GsonHelper.getAsString(jsonObject, "insertion", null);
        }

        @Nullable
        private static TextColor getTextColor(JsonObject jsonObject) {
            if (jsonObject.has("color")) {
                String string = GsonHelper.getAsString(jsonObject, "color");
                return TextColor.parseColor(string);
            }
            return null;
        }

        @Nullable
        private static Boolean getOptionalFlag(JsonObject jsonObject, String string) {
            if (jsonObject.has(string)) {
                return jsonObject.get(string).getAsBoolean();
            }
            return null;
        }

        @Override
        @Nullable
        public JsonElement serialize(Style style, Type type, JsonSerializationContext jsonSerializationContext) {
            if (style.isEmpty()) {
                return null;
            }
            JsonObject jsonObject = new JsonObject();
            if (style.bold != null) {
                jsonObject.addProperty("bold", style.bold);
            }
            if (style.italic != null) {
                jsonObject.addProperty("italic", style.italic);
            }
            if (style.underlined != null) {
                jsonObject.addProperty("underlined", style.underlined);
            }
            if (style.strikethrough != null) {
                jsonObject.addProperty("strikethrough", style.strikethrough);
            }
            if (style.obfuscated != null) {
                jsonObject.addProperty("obfuscated", style.obfuscated);
            }
            if (style.color != null) {
                jsonObject.addProperty("color", style.color.serialize());
            }
            if (style.insertion != null) {
                jsonObject.add("insertion", jsonSerializationContext.serialize(style.insertion));
            }
            if (style.clickEvent != null) {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("action", style.clickEvent.getAction().getName());
                jsonObject2.addProperty("value", style.clickEvent.getValue());
                jsonObject.add("clickEvent", jsonObject2);
            }
            if (style.hoverEvent != null) {
                jsonObject.add("hoverEvent", style.hoverEvent.serialize());
            }
            if (style.font != null) {
                jsonObject.addProperty("font", style.font.toString());
            }
            return jsonObject;
        }

        @Override
        @Nullable
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((Style)object, type, jsonSerializationContext);
        }

        @Override
        @Nullable
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

