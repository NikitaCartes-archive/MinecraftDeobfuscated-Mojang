/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.providers.nbt;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import org.jetbrains.annotations.Nullable;

public class ContextNbtProvider
implements NbtProvider {
    private static final String BLOCK_ENTITY_ID = "block_entity";
    private static final Getter BLOCK_ENTITY_PROVIDER = new Getter(){

        @Override
        public Tag get(LootContext lootContext) {
            BlockEntity blockEntity = lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
            return blockEntity != null ? blockEntity.saveWithFullMetadata() : null;
        }

        @Override
        public String getId() {
            return ContextNbtProvider.BLOCK_ENTITY_ID;
        }

        @Override
        public Set<LootContextParam<?>> getReferencedContextParams() {
            return ImmutableSet.of(LootContextParams.BLOCK_ENTITY);
        }
    };
    public static final ContextNbtProvider BLOCK_ENTITY = new ContextNbtProvider(BLOCK_ENTITY_PROVIDER);
    final Getter getter;

    private static Getter forEntity(final LootContext.EntityTarget entityTarget) {
        return new Getter(){

            @Override
            @Nullable
            public Tag get(LootContext lootContext) {
                Entity entity = lootContext.getParamOrNull(entityTarget.getParam());
                return entity != null ? NbtPredicate.getEntityTagToCompare(entity) : null;
            }

            @Override
            public String getId() {
                return entityTarget.name();
            }

            @Override
            public Set<LootContextParam<?>> getReferencedContextParams() {
                return ImmutableSet.of(entityTarget.getParam());
            }
        };
    }

    private ContextNbtProvider(Getter getter) {
        this.getter = getter;
    }

    @Override
    public LootNbtProviderType getType() {
        return NbtProviders.CONTEXT;
    }

    @Override
    @Nullable
    public Tag get(LootContext lootContext) {
        return this.getter.get(lootContext);
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.getter.getReferencedContextParams();
    }

    public static NbtProvider forContextEntity(LootContext.EntityTarget entityTarget) {
        return new ContextNbtProvider(ContextNbtProvider.forEntity(entityTarget));
    }

    static ContextNbtProvider createFromContext(String string) {
        if (string.equals(BLOCK_ENTITY_ID)) {
            return new ContextNbtProvider(BLOCK_ENTITY_PROVIDER);
        }
        LootContext.EntityTarget entityTarget = LootContext.EntityTarget.getByName(string);
        return new ContextNbtProvider(ContextNbtProvider.forEntity(entityTarget));
    }

    static interface Getter {
        @Nullable
        public Tag get(LootContext var1);

        public String getId();

        public Set<LootContextParam<?>> getReferencedContextParams();
    }

    public static class InlineSerializer
    implements GsonAdapterFactory.InlineSerializer<ContextNbtProvider> {
        @Override
        public JsonElement serialize(ContextNbtProvider contextNbtProvider, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(contextNbtProvider.getter.getId());
        }

        @Override
        public ContextNbtProvider deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
            String string = jsonElement.getAsString();
            return ContextNbtProvider.createFromContext(string);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonElement, jsonDeserializationContext);
        }
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<ContextNbtProvider> {
        @Override
        public void serialize(JsonObject jsonObject, ContextNbtProvider contextNbtProvider, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("target", contextNbtProvider.getter.getId());
        }

        @Override
        public ContextNbtProvider deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            String string = GsonHelper.getAsString(jsonObject, "target");
            return ContextNbtProvider.createFromContext(string);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

