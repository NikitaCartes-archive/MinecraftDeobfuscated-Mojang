/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.FrameType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DisplayInfo {
    private final Component title;
    private final Component description;
    private final ItemStack icon;
    @Nullable
    private final ResourceLocation background;
    private final FrameType frame;
    private final boolean showToast;
    private final boolean announceChat;
    private final boolean hidden;
    private float x;
    private float y;

    public DisplayInfo(ItemStack itemStack, Component component, Component component2, @Nullable ResourceLocation resourceLocation, FrameType frameType, boolean bl, boolean bl2, boolean bl3) {
        this.title = component;
        this.description = component2;
        this.icon = itemStack;
        this.background = resourceLocation;
        this.frame = frameType;
        this.showToast = bl;
        this.announceChat = bl2;
        this.hidden = bl3;
    }

    public void setLocation(float f, float g) {
        this.x = f;
        this.y = g;
    }

    public Component getTitle() {
        return this.title;
    }

    public Component getDescription() {
        return this.description;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    @Nullable
    public ResourceLocation getBackground() {
        return this.background;
    }

    public FrameType getFrame() {
        return this.frame;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public boolean shouldShowToast() {
        return this.showToast;
    }

    public boolean shouldAnnounceChat() {
        return this.announceChat;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public static DisplayInfo fromJson(JsonObject jsonObject) {
        MutableComponent component = Component.Serializer.fromJson(jsonObject.get("title"));
        MutableComponent component2 = Component.Serializer.fromJson(jsonObject.get("description"));
        if (component == null || component2 == null) {
            throw new JsonSyntaxException("Both title and description must be set");
        }
        ItemStack itemStack = DisplayInfo.getIcon(GsonHelper.getAsJsonObject(jsonObject, "icon"));
        ResourceLocation resourceLocation = jsonObject.has("background") ? new ResourceLocation(GsonHelper.getAsString(jsonObject, "background")) : null;
        FrameType frameType = jsonObject.has("frame") ? FrameType.byName(GsonHelper.getAsString(jsonObject, "frame")) : FrameType.TASK;
        boolean bl = GsonHelper.getAsBoolean(jsonObject, "show_toast", true);
        boolean bl2 = GsonHelper.getAsBoolean(jsonObject, "announce_to_chat", true);
        boolean bl3 = GsonHelper.getAsBoolean(jsonObject, "hidden", false);
        return new DisplayInfo(itemStack, component, component2, resourceLocation, frameType, bl, bl2, bl3);
    }

    private static ItemStack getIcon(JsonObject jsonObject) {
        if (!jsonObject.has("item")) {
            throw new JsonSyntaxException("Unsupported icon type, currently only items are supported (add 'item' key)");
        }
        Item item = GsonHelper.getAsItem(jsonObject, "item");
        if (jsonObject.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
        }
        ItemStack itemStack = new ItemStack(item);
        if (jsonObject.has("nbt")) {
            try {
                CompoundTag compoundTag = TagParser.parseTag(GsonHelper.convertToString(jsonObject.get("nbt"), "nbt"));
                itemStack.setTag(compoundTag);
            } catch (CommandSyntaxException commandSyntaxException) {
                throw new JsonSyntaxException("Invalid nbt tag: " + commandSyntaxException.getMessage());
            }
        }
        return itemStack;
    }

    public void serializeToNetwork(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeComponent(this.title);
        friendlyByteBuf.writeComponent(this.description);
        friendlyByteBuf.writeItem(this.icon);
        friendlyByteBuf.writeEnum(this.frame);
        int i = 0;
        if (this.background != null) {
            i |= 1;
        }
        if (this.showToast) {
            i |= 2;
        }
        if (this.hidden) {
            i |= 4;
        }
        friendlyByteBuf.writeInt(i);
        if (this.background != null) {
            friendlyByteBuf.writeResourceLocation(this.background);
        }
        friendlyByteBuf.writeFloat(this.x);
        friendlyByteBuf.writeFloat(this.y);
    }

    public static DisplayInfo fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        Component component = friendlyByteBuf.readComponent();
        Component component2 = friendlyByteBuf.readComponent();
        ItemStack itemStack = friendlyByteBuf.readItem();
        FrameType frameType = friendlyByteBuf.readEnum(FrameType.class);
        int i = friendlyByteBuf.readInt();
        ResourceLocation resourceLocation = (i & 1) != 0 ? friendlyByteBuf.readResourceLocation() : null;
        boolean bl = (i & 2) != 0;
        boolean bl2 = (i & 4) != 0;
        DisplayInfo displayInfo = new DisplayInfo(itemStack, component, component2, resourceLocation, frameType, bl, false, bl2);
        displayInfo.setLocation(friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
        return displayInfo;
    }

    public JsonElement serializeToJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("icon", this.serializeIcon());
        jsonObject.add("title", Component.Serializer.toJsonTree(this.title));
        jsonObject.add("description", Component.Serializer.toJsonTree(this.description));
        jsonObject.addProperty("frame", this.frame.getName());
        jsonObject.addProperty("show_toast", this.showToast);
        jsonObject.addProperty("announce_to_chat", this.announceChat);
        jsonObject.addProperty("hidden", this.hidden);
        if (this.background != null) {
            jsonObject.addProperty("background", this.background.toString());
        }
        return jsonObject;
    }

    private JsonObject serializeIcon() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("item", BuiltInRegistries.ITEM.getKey(this.icon.getItem()).toString());
        if (this.icon.hasTag()) {
            jsonObject.addProperty("nbt", this.icon.getTag().toString());
        }
        return jsonObject;
    }
}

