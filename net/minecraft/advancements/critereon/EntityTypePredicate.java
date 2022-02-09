/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public abstract class EntityTypePredicate {
    public static final EntityTypePredicate ANY = new EntityTypePredicate(){

        @Override
        public boolean matches(EntityType<?> entityType) {
            return true;
        }

        @Override
        public JsonElement serializeToJson() {
            return JsonNull.INSTANCE;
        }
    };
    private static final Joiner COMMA_JOINER = Joiner.on(", ");

    public abstract boolean matches(EntityType<?> var1);

    public abstract JsonElement serializeToJson();

    public static EntityTypePredicate fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        String string = GsonHelper.convertToString(jsonElement, "type");
        if (string.startsWith("#")) {
            ResourceLocation resourceLocation = new ResourceLocation(string.substring(1));
            return new TagPredicate(TagKey.create(Registry.ENTITY_TYPE_REGISTRY, resourceLocation));
        }
        ResourceLocation resourceLocation = new ResourceLocation(string);
        EntityType<?> entityType = Registry.ENTITY_TYPE.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown entity type '" + resourceLocation + "', valid types are: " + COMMA_JOINER.join(Registry.ENTITY_TYPE.keySet())));
        return new TypePredicate(entityType);
    }

    public static EntityTypePredicate of(EntityType<?> entityType) {
        return new TypePredicate(entityType);
    }

    public static EntityTypePredicate of(TagKey<EntityType<?>> tagKey) {
        return new TagPredicate(tagKey);
    }

    static class TagPredicate
    extends EntityTypePredicate {
        private final TagKey<EntityType<?>> tag;

        public TagPredicate(TagKey<EntityType<?>> tagKey) {
            this.tag = tagKey;
        }

        @Override
        public boolean matches(EntityType<?> entityType) {
            return entityType.is(this.tag);
        }

        @Override
        public JsonElement serializeToJson() {
            return new JsonPrimitive("#" + this.tag.location());
        }
    }

    static class TypePredicate
    extends EntityTypePredicate {
        private final EntityType<?> type;

        public TypePredicate(EntityType<?> entityType) {
            this.type = entityType;
        }

        @Override
        public boolean matches(EntityType<?> entityType) {
            return this.type == entityType;
        }

        @Override
        public JsonElement serializeToJson() {
            return new JsonPrimitive(Registry.ENTITY_TYPE.getKey(this.type).toString());
        }
    }
}

