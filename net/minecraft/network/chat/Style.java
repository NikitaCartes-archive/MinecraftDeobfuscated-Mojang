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
import java.lang.reflect.Type;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public class Style {
    private Style parent;
    private ChatFormatting color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underlined;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private ClickEvent clickEvent;
    private HoverEvent hoverEvent;
    private String insertion;
    private static final Style ROOT = new Style(){

        @Override
        @Nullable
        public ChatFormatting getColor() {
            return null;
        }

        @Override
        public boolean isBold() {
            return false;
        }

        @Override
        public boolean isItalic() {
            return false;
        }

        @Override
        public boolean isStrikethrough() {
            return false;
        }

        @Override
        public boolean isUnderlined() {
            return false;
        }

        @Override
        public boolean isObfuscated() {
            return false;
        }

        @Override
        @Nullable
        public ClickEvent getClickEvent() {
            return null;
        }

        @Override
        @Nullable
        public HoverEvent getHoverEvent() {
            return null;
        }

        @Override
        @Nullable
        public String getInsertion() {
            return null;
        }

        @Override
        public Style setColor(ChatFormatting chatFormatting) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setBold(Boolean boolean_) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setItalic(Boolean boolean_) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setStrikethrough(Boolean boolean_) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setUnderlined(Boolean boolean_) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setObfuscated(Boolean boolean_) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setClickEvent(ClickEvent clickEvent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style setHoverEvent(HoverEvent hoverEvent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Style inheritFrom(Style style) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "Style.ROOT";
        }

        @Override
        public Style copy() {
            return this;
        }

        @Override
        public Style flatCopy() {
            return this;
        }

        @Override
        public String getLegacyFormatCodes() {
            return "";
        }
    };

    @Nullable
    public ChatFormatting getColor() {
        return this.color == null ? this.getParent().getColor() : this.color;
    }

    public boolean isBold() {
        return this.bold == null ? this.getParent().isBold() : this.bold.booleanValue();
    }

    public boolean isItalic() {
        return this.italic == null ? this.getParent().isItalic() : this.italic.booleanValue();
    }

    public boolean isStrikethrough() {
        return this.strikethrough == null ? this.getParent().isStrikethrough() : this.strikethrough.booleanValue();
    }

    public boolean isUnderlined() {
        return this.underlined == null ? this.getParent().isUnderlined() : this.underlined.booleanValue();
    }

    public boolean isObfuscated() {
        return this.obfuscated == null ? this.getParent().isObfuscated() : this.obfuscated.booleanValue();
    }

    public boolean isEmpty() {
        return this.bold == null && this.italic == null && this.strikethrough == null && this.underlined == null && this.obfuscated == null && this.color == null && this.clickEvent == null && this.hoverEvent == null && this.insertion == null;
    }

    @Nullable
    public ClickEvent getClickEvent() {
        return this.clickEvent == null ? this.getParent().getClickEvent() : this.clickEvent;
    }

    @Nullable
    public HoverEvent getHoverEvent() {
        return this.hoverEvent == null ? this.getParent().getHoverEvent() : this.hoverEvent;
    }

    @Nullable
    public String getInsertion() {
        return this.insertion == null ? this.getParent().getInsertion() : this.insertion;
    }

    public Style setColor(ChatFormatting chatFormatting) {
        this.color = chatFormatting;
        return this;
    }

    public Style setBold(Boolean boolean_) {
        this.bold = boolean_;
        return this;
    }

    public Style setItalic(Boolean boolean_) {
        this.italic = boolean_;
        return this;
    }

    public Style setStrikethrough(Boolean boolean_) {
        this.strikethrough = boolean_;
        return this;
    }

    public Style setUnderlined(Boolean boolean_) {
        this.underlined = boolean_;
        return this;
    }

    public Style setObfuscated(Boolean boolean_) {
        this.obfuscated = boolean_;
        return this;
    }

    public Style setClickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    public Style setHoverEvent(HoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
        return this;
    }

    public Style setInsertion(String string) {
        this.insertion = string;
        return this;
    }

    public Style inheritFrom(Style style) {
        this.parent = style;
        return this;
    }

    public String getLegacyFormatCodes() {
        if (this.isEmpty()) {
            if (this.parent != null) {
                return this.parent.getLegacyFormatCodes();
            }
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (this.getColor() != null) {
            stringBuilder.append((Object)this.getColor());
        }
        if (this.isBold()) {
            stringBuilder.append((Object)ChatFormatting.BOLD);
        }
        if (this.isItalic()) {
            stringBuilder.append((Object)ChatFormatting.ITALIC);
        }
        if (this.isUnderlined()) {
            stringBuilder.append((Object)ChatFormatting.UNDERLINE);
        }
        if (this.isObfuscated()) {
            stringBuilder.append((Object)ChatFormatting.OBFUSCATED);
        }
        if (this.isStrikethrough()) {
            stringBuilder.append((Object)ChatFormatting.STRIKETHROUGH);
        }
        return stringBuilder.toString();
    }

    private Style getParent() {
        return this.parent == null ? ROOT : this.parent;
    }

    public String toString() {
        return "Style{hasParent=" + (this.parent != null) + ", color=" + (Object)((Object)this.color) + ", bold=" + this.bold + ", italic=" + this.italic + ", underlined=" + this.underlined + ", obfuscated=" + this.obfuscated + ", clickEvent=" + this.getClickEvent() + ", hoverEvent=" + this.getHoverEvent() + ", insertion=" + this.getInsertion() + '}';
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Style) {
            Style style = (Style)object;
            return this.isBold() == style.isBold() && this.getColor() == style.getColor() && this.isItalic() == style.isItalic() && this.isObfuscated() == style.isObfuscated() && this.isStrikethrough() == style.isStrikethrough() && this.isUnderlined() == style.isUnderlined() && (this.getClickEvent() != null ? this.getClickEvent().equals(style.getClickEvent()) : style.getClickEvent() == null) && (this.getHoverEvent() != null ? this.getHoverEvent().equals(style.getHoverEvent()) : style.getHoverEvent() == null) && (this.getInsertion() != null ? this.getInsertion().equals(style.getInsertion()) : style.getInsertion() == null);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion});
    }

    public Style copy() {
        Style style = new Style();
        style.bold = this.bold;
        style.italic = this.italic;
        style.strikethrough = this.strikethrough;
        style.underlined = this.underlined;
        style.obfuscated = this.obfuscated;
        style.color = this.color;
        style.clickEvent = this.clickEvent;
        style.hoverEvent = this.hoverEvent;
        style.parent = this.parent;
        style.insertion = this.insertion;
        return style;
    }

    public Style flatCopy() {
        Style style = new Style();
        style.setBold(this.isBold());
        style.setItalic(this.isItalic());
        style.setStrikethrough(this.isStrikethrough());
        style.setUnderlined(this.isUnderlined());
        style.setObfuscated(this.isObfuscated());
        style.setColor(this.getColor());
        style.setClickEvent(this.getClickEvent());
        style.setHoverEvent(this.getHoverEvent());
        style.setInsertion(this.getInsertion());
        return style;
    }

    public static class Serializer
    implements JsonDeserializer<Style>,
    JsonSerializer<Style> {
        @Override
        @Nullable
        public Style deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonObject()) {
                String string;
                JsonObject jsonObject2;
                Style style = new Style();
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject == null) {
                    return null;
                }
                if (jsonObject.has("bold")) {
                    style.bold = jsonObject.get("bold").getAsBoolean();
                }
                if (jsonObject.has("italic")) {
                    style.italic = jsonObject.get("italic").getAsBoolean();
                }
                if (jsonObject.has("underlined")) {
                    style.underlined = jsonObject.get("underlined").getAsBoolean();
                }
                if (jsonObject.has("strikethrough")) {
                    style.strikethrough = jsonObject.get("strikethrough").getAsBoolean();
                }
                if (jsonObject.has("obfuscated")) {
                    style.obfuscated = jsonObject.get("obfuscated").getAsBoolean();
                }
                if (jsonObject.has("color")) {
                    style.color = (ChatFormatting)((Object)jsonDeserializationContext.deserialize(jsonObject.get("color"), (Type)((Object)ChatFormatting.class)));
                }
                if (jsonObject.has("insertion")) {
                    style.insertion = jsonObject.get("insertion").getAsString();
                }
                if (jsonObject.has("clickEvent")) {
                    jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "clickEvent");
                    string = GsonHelper.getAsString(jsonObject2, "action", null);
                    ClickEvent.Action action = string == null ? null : ClickEvent.Action.getByName(string);
                    String string2 = GsonHelper.getAsString(jsonObject2, "value", null);
                    if (action != null && string2 != null && action.isAllowedFromServer()) {
                        style.clickEvent = new ClickEvent(action, string2);
                    }
                }
                if (jsonObject.has("hoverEvent")) {
                    jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "hoverEvent");
                    string = GsonHelper.getAsString(jsonObject2, "action", null);
                    HoverEvent.Action action2 = string == null ? null : HoverEvent.Action.getByName(string);
                    Component component = (Component)jsonDeserializationContext.deserialize(jsonObject2.get("value"), (Type)((Object)Component.class));
                    if (action2 != null && component != null && action2.isAllowedFromServer()) {
                        style.hoverEvent = new HoverEvent(action2, component);
                    }
                }
                return style;
            }
            return null;
        }

        @Override
        @Nullable
        public JsonElement serialize(Style style, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject2;
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
                jsonObject.add("color", jsonSerializationContext.serialize((Object)style.color));
            }
            if (style.insertion != null) {
                jsonObject.add("insertion", jsonSerializationContext.serialize(style.insertion));
            }
            if (style.clickEvent != null) {
                jsonObject2 = new JsonObject();
                jsonObject2.addProperty("action", style.clickEvent.getAction().getName());
                jsonObject2.addProperty("value", style.clickEvent.getValue());
                jsonObject.add("clickEvent", jsonObject2);
            }
            if (style.hoverEvent != null) {
                jsonObject2 = new JsonObject();
                jsonObject2.addProperty("action", style.hoverEvent.getAction().getName());
                jsonObject2.add("value", jsonSerializationContext.serialize(style.hoverEvent.getValue()));
                jsonObject.add("hoverEvent", jsonObject2);
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

