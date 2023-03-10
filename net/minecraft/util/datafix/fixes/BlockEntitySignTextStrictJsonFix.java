/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.lang.reflect.Type;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.StringUtils;

public class BlockEntitySignTextStrictJsonFix
extends NamedEntityFix {
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter((Type)((Object)Component.class), new JsonDeserializer<Component>(){

        @Override
        public MutableComponent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (jsonElement.isJsonPrimitive()) {
                return Component.literal(jsonElement.getAsString());
            }
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                MutableComponent mutableComponent = null;
                for (JsonElement jsonElement2 : jsonArray) {
                    MutableComponent mutableComponent2 = this.deserialize(jsonElement2, jsonElement2.getClass(), jsonDeserializationContext);
                    if (mutableComponent == null) {
                        mutableComponent = mutableComponent2;
                        continue;
                    }
                    mutableComponent.append(mutableComponent2);
                }
                return mutableComponent;
            }
            throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }).create();

    public BlockEntitySignTextStrictJsonFix(Schema schema, boolean bl) {
        super(schema, bl, "BlockEntitySignTextStrictJsonFix", References.BLOCK_ENTITY, "Sign");
    }

    private Dynamic<?> updateLine(Dynamic<?> dynamic, String string) {
        String string2 = dynamic.get(string).asString("");
        Component component = null;
        if ("null".equals(string2) || StringUtils.isEmpty(string2)) {
            component = CommonComponents.EMPTY;
        } else if (string2.charAt(0) == '\"' && string2.charAt(string2.length() - 1) == '\"' || string2.charAt(0) == '{' && string2.charAt(string2.length() - 1) == '}') {
            try {
                component = GsonHelper.fromNullableJson(GSON, string2, Component.class, true);
                if (component == null) {
                    component = CommonComponents.EMPTY;
                }
            } catch (Exception exception) {
                // empty catch block
            }
            if (component == null) {
                try {
                    component = Component.Serializer.fromJson(string2);
                } catch (Exception exception) {
                    // empty catch block
                }
            }
            if (component == null) {
                try {
                    component = Component.Serializer.fromJsonLenient(string2);
                } catch (Exception exception) {
                    // empty catch block
                }
            }
            if (component == null) {
                component = Component.literal(string2);
            }
        } else {
            component = Component.literal(string2);
        }
        return dynamic.set(string, dynamic.createString(Component.Serializer.toJson(component)));
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic -> {
            dynamic = this.updateLine((Dynamic<?>)dynamic, "Text1");
            dynamic = this.updateLine((Dynamic<?>)dynamic, "Text2");
            dynamic = this.updateLine((Dynamic<?>)dynamic, "Text3");
            dynamic = this.updateLine((Dynamic<?>)dynamic, "Text4");
            return dynamic;
        });
    }
}

