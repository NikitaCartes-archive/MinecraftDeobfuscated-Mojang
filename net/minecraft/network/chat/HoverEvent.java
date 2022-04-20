/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class HoverEvent {
    static final Logger LOGGER = LogUtils.getLogger();
    private final Action<?> action;
    private final Object value;

    public <T> HoverEvent(Action<T> action, T object) {
        this.action = action;
        this.value = object;
    }

    public Action<?> getAction() {
        return this.action;
    }

    @Nullable
    public <T> T getValue(Action<T> action) {
        if (this.action == action) {
            return action.cast(this.value);
        }
        return null;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        HoverEvent hoverEvent = (HoverEvent)object;
        return this.action == hoverEvent.action && Objects.equals(this.value, hoverEvent.value);
    }

    public String toString() {
        return "HoverEvent{action=" + this.action + ", value='" + this.value + "'}";
    }

    public int hashCode() {
        int i = this.action.hashCode();
        i = 31 * i + (this.value != null ? this.value.hashCode() : 0);
        return i;
    }

    @Nullable
    public static HoverEvent deserialize(JsonObject jsonObject) {
        String string = GsonHelper.getAsString(jsonObject, "action", null);
        if (string == null) {
            return null;
        }
        Action<?> action = Action.getByName(string);
        if (action == null) {
            return null;
        }
        JsonElement jsonElement = jsonObject.get("contents");
        if (jsonElement != null) {
            return action.deserialize(jsonElement);
        }
        MutableComponent component = Component.Serializer.fromJson(jsonObject.get("value"));
        if (component != null) {
            return action.deserializeFromLegacy(component);
        }
        return null;
    }

    public JsonObject serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("action", this.action.getName());
        jsonObject.add("contents", this.action.serializeArg(this.value));
        return jsonObject;
    }

    public static class Action<T> {
        public static final Action<Component> SHOW_TEXT = new Action<Component>("show_text", true, Component.Serializer::fromJson, Component.Serializer::toJsonTree, Function.identity());
        public static final Action<ItemStackInfo> SHOW_ITEM = new Action<ItemStackInfo>("show_item", true, ItemStackInfo::create, ItemStackInfo::serialize, ItemStackInfo::create);
        public static final Action<EntityTooltipInfo> SHOW_ENTITY = new Action<EntityTooltipInfo>("show_entity", true, EntityTooltipInfo::create, EntityTooltipInfo::serialize, EntityTooltipInfo::create);
        private static final Map<String, Action<?>> LOOKUP = Stream.of(SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY).collect(ImmutableMap.toImmutableMap(Action::getName, action -> action));
        private final String name;
        private final boolean allowFromServer;
        private final Function<JsonElement, T> argDeserializer;
        private final Function<T, JsonElement> argSerializer;
        private final Function<Component, T> legacyArgDeserializer;

        public Action(String string, boolean bl, Function<JsonElement, T> function, Function<T, JsonElement> function2, Function<Component, T> function3) {
            this.name = string;
            this.allowFromServer = bl;
            this.argDeserializer = function;
            this.argSerializer = function2;
            this.legacyArgDeserializer = function3;
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        public String getName() {
            return this.name;
        }

        @Nullable
        public static Action<?> getByName(String string) {
            return LOOKUP.get(string);
        }

        T cast(Object object) {
            return (T)object;
        }

        @Nullable
        public HoverEvent deserialize(JsonElement jsonElement) {
            T object = this.argDeserializer.apply(jsonElement);
            if (object == null) {
                return null;
            }
            return new HoverEvent(this, object);
        }

        @Nullable
        public HoverEvent deserializeFromLegacy(Component component) {
            T object = this.legacyArgDeserializer.apply(component);
            if (object == null) {
                return null;
            }
            return new HoverEvent(this, object);
        }

        public JsonElement serializeArg(Object object) {
            return this.argSerializer.apply(this.cast(object));
        }

        public String toString() {
            return "<action " + this.name + ">";
        }
    }

    public static class ItemStackInfo {
        private final Item item;
        private final int count;
        @Nullable
        private final CompoundTag tag;
        @Nullable
        private ItemStack itemStack;

        ItemStackInfo(Item item, int i, @Nullable CompoundTag compoundTag) {
            this.item = item;
            this.count = i;
            this.tag = compoundTag;
        }

        public ItemStackInfo(ItemStack itemStack) {
            this(itemStack.getItem(), itemStack.getCount(), itemStack.getTag() != null ? itemStack.getTag().copy() : null);
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            ItemStackInfo itemStackInfo = (ItemStackInfo)object;
            return this.count == itemStackInfo.count && this.item.equals(itemStackInfo.item) && Objects.equals(this.tag, itemStackInfo.tag);
        }

        public int hashCode() {
            int i = this.item.hashCode();
            i = 31 * i + this.count;
            i = 31 * i + (this.tag != null ? this.tag.hashCode() : 0);
            return i;
        }

        public ItemStack getItemStack() {
            if (this.itemStack == null) {
                this.itemStack = new ItemStack(this.item, this.count);
                if (this.tag != null) {
                    this.itemStack.setTag(this.tag);
                }
            }
            return this.itemStack;
        }

        private static ItemStackInfo create(JsonElement jsonElement) {
            if (jsonElement.isJsonPrimitive()) {
                return new ItemStackInfo(Registry.ITEM.get(new ResourceLocation(jsonElement.getAsString())), 1, null);
            }
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "item");
            Item item = Registry.ITEM.get(new ResourceLocation(GsonHelper.getAsString(jsonObject, "id")));
            int i = GsonHelper.getAsInt(jsonObject, "count", 1);
            if (jsonObject.has("tag")) {
                String string = GsonHelper.getAsString(jsonObject, "tag");
                try {
                    CompoundTag compoundTag = TagParser.parseTag(string);
                    return new ItemStackInfo(item, i, compoundTag);
                } catch (CommandSyntaxException commandSyntaxException) {
                    LOGGER.warn("Failed to parse tag: {}", (Object)string, (Object)commandSyntaxException);
                }
            }
            return new ItemStackInfo(item, i, null);
        }

        @Nullable
        private static ItemStackInfo create(Component component) {
            try {
                CompoundTag compoundTag = TagParser.parseTag(component.getString());
                return new ItemStackInfo(ItemStack.of(compoundTag));
            } catch (CommandSyntaxException commandSyntaxException) {
                LOGGER.warn("Failed to parse item tag: {}", (Object)component, (Object)commandSyntaxException);
                return null;
            }
        }

        private JsonElement serialize() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", Registry.ITEM.getKey(this.item).toString());
            if (this.count != 1) {
                jsonObject.addProperty("count", this.count);
            }
            if (this.tag != null) {
                jsonObject.addProperty("tag", this.tag.toString());
            }
            return jsonObject;
        }
    }

    public static class EntityTooltipInfo {
        public final EntityType<?> type;
        public final UUID id;
        @Nullable
        public final Component name;
        @Nullable
        private List<Component> linesCache;

        public EntityTooltipInfo(EntityType<?> entityType, UUID uUID, @Nullable Component component) {
            this.type = entityType;
            this.id = uUID;
            this.name = component;
        }

        @Nullable
        public static EntityTooltipInfo create(JsonElement jsonElement) {
            if (!jsonElement.isJsonObject()) {
                return null;
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            EntityType<?> entityType = Registry.ENTITY_TYPE.get(new ResourceLocation(GsonHelper.getAsString(jsonObject, "type")));
            UUID uUID = UUID.fromString(GsonHelper.getAsString(jsonObject, "id"));
            MutableComponent component = Component.Serializer.fromJson(jsonObject.get("name"));
            return new EntityTooltipInfo(entityType, uUID, component);
        }

        @Nullable
        public static EntityTooltipInfo create(Component component) {
            try {
                CompoundTag compoundTag = TagParser.parseTag(component.getString());
                MutableComponent component2 = Component.Serializer.fromJson(compoundTag.getString("name"));
                EntityType<?> entityType = Registry.ENTITY_TYPE.get(new ResourceLocation(compoundTag.getString("type")));
                UUID uUID = UUID.fromString(compoundTag.getString("id"));
                return new EntityTooltipInfo(entityType, uUID, component2);
            } catch (Exception exception) {
                return null;
            }
        }

        public JsonElement serialize() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", Registry.ENTITY_TYPE.getKey(this.type).toString());
            jsonObject.addProperty("id", this.id.toString());
            if (this.name != null) {
                jsonObject.add("name", Component.Serializer.toJsonTree(this.name));
            }
            return jsonObject;
        }

        public List<Component> getTooltipLines() {
            if (this.linesCache == null) {
                this.linesCache = Lists.newArrayList();
                if (this.name != null) {
                    this.linesCache.add(this.name);
                }
                this.linesCache.add(Component.translatable("gui.entity_tooltip.type", this.type.getDescription()));
                this.linesCache.add(Component.literal(this.id.toString()));
            }
            return this.linesCache;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            EntityTooltipInfo entityTooltipInfo = (EntityTooltipInfo)object;
            return this.type.equals(entityTooltipInfo.type) && this.id.equals(entityTooltipInfo.id) && Objects.equals(this.name, entityTooltipInfo.name);
        }

        public int hashCode() {
            int i = this.type.hashCode();
            i = 31 * i + this.id.hashCode();
            i = 31 * i + (this.name != null ? this.name.hashCode() : 0);
            return i;
        }
    }
}

