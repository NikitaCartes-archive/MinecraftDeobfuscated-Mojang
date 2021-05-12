/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockModelDefinition {
    private final Map<String, MultiVariant> variants = Maps.newLinkedHashMap();
    private MultiPart multiPart;

    public static BlockModelDefinition fromStream(Context context, Reader reader) {
        return GsonHelper.fromJson(context.gson, reader, BlockModelDefinition.class);
    }

    public BlockModelDefinition(Map<String, MultiVariant> map, MultiPart multiPart) {
        this.multiPart = multiPart;
        this.variants.putAll(map);
    }

    public BlockModelDefinition(List<BlockModelDefinition> list) {
        BlockModelDefinition blockModelDefinition = null;
        for (BlockModelDefinition blockModelDefinition2 : list) {
            if (blockModelDefinition2.isMultiPart()) {
                this.variants.clear();
                blockModelDefinition = blockModelDefinition2;
            }
            this.variants.putAll(blockModelDefinition2.variants);
        }
        if (blockModelDefinition != null) {
            this.multiPart = blockModelDefinition.multiPart;
        }
    }

    @VisibleForTesting
    public boolean hasVariant(String string) {
        return this.variants.get(string) != null;
    }

    @VisibleForTesting
    public MultiVariant getVariant(String string) {
        MultiVariant multiVariant = this.variants.get(string);
        if (multiVariant == null) {
            throw new MissingVariantException();
        }
        return multiVariant;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof BlockModelDefinition) {
            BlockModelDefinition blockModelDefinition = (BlockModelDefinition)object;
            if (this.variants.equals(blockModelDefinition.variants)) {
                return this.isMultiPart() ? this.multiPart.equals(blockModelDefinition.multiPart) : !blockModelDefinition.isMultiPart();
            }
        }
        return false;
    }

    public int hashCode() {
        return 31 * this.variants.hashCode() + (this.isMultiPart() ? this.multiPart.hashCode() : 0);
    }

    public Map<String, MultiVariant> getVariants() {
        return this.variants;
    }

    @VisibleForTesting
    public Set<MultiVariant> getMultiVariants() {
        HashSet<MultiVariant> set = Sets.newHashSet(this.variants.values());
        if (this.isMultiPart()) {
            set.addAll(this.multiPart.getMultiVariants());
        }
        return set;
    }

    public boolean isMultiPart() {
        return this.multiPart != null;
    }

    public MultiPart getMultiPart() {
        return this.multiPart;
    }

    @Environment(value=EnvType.CLIENT)
    public static final class Context {
        protected final Gson gson = new GsonBuilder().registerTypeAdapter((Type)((Object)BlockModelDefinition.class), new Deserializer()).registerTypeAdapter((Type)((Object)Variant.class), new Variant.Deserializer()).registerTypeAdapter((Type)((Object)MultiVariant.class), new MultiVariant.Deserializer()).registerTypeAdapter((Type)((Object)MultiPart.class), new MultiPart.Deserializer(this)).registerTypeAdapter((Type)((Object)Selector.class), new Selector.Deserializer()).create();
        private StateDefinition<Block, BlockState> definition;

        public StateDefinition<Block, BlockState> getDefinition() {
            return this.definition;
        }

        public void setDefinition(StateDefinition<Block, BlockState> stateDefinition) {
            this.definition = stateDefinition;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected class MissingVariantException
    extends RuntimeException {
        protected MissingVariantException() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Deserializer
    implements JsonDeserializer<BlockModelDefinition> {
        @Override
        public BlockModelDefinition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Map<String, MultiVariant> map = this.getVariants(jsonDeserializationContext, jsonObject);
            MultiPart multiPart = this.getMultiPart(jsonDeserializationContext, jsonObject);
            if (map.isEmpty() && (multiPart == null || multiPart.getMultiVariants().isEmpty())) {
                throw new JsonParseException("Neither 'variants' nor 'multipart' found");
            }
            return new BlockModelDefinition(map, multiPart);
        }

        protected Map<String, MultiVariant> getVariants(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
            HashMap<String, MultiVariant> map = Maps.newHashMap();
            if (jsonObject.has("variants")) {
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "variants");
                for (Map.Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
                    map.put(entry.getKey(), (MultiVariant)jsonDeserializationContext.deserialize(entry.getValue(), (Type)((Object)MultiVariant.class)));
                }
            }
            return map;
        }

        @Nullable
        protected MultiPart getMultiPart(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
            if (!jsonObject.has("multipart")) {
                return null;
            }
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "multipart");
            return (MultiPart)jsonDeserializationContext.deserialize(jsonArray, (Type)((Object)MultiPart.class));
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

