/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class ItemTransform {
    public static final ItemTransform NO_TRANSFORM = new ItemTransform(new Vector3f(), new Vector3f(), new Vector3f(1.0f, 1.0f, 1.0f));
    public final Vector3f rotation;
    public final Vector3f translation;
    public final Vector3f scale;

    public ItemTransform(Vector3f vector3f, Vector3f vector3f2, Vector3f vector3f3) {
        this.rotation = new Vector3f(vector3f);
        this.translation = new Vector3f(vector3f2);
        this.scale = new Vector3f(vector3f3);
    }

    public void apply(boolean bl, PoseStack poseStack) {
        if (this == NO_TRANSFORM) {
            return;
        }
        float f = this.rotation.x();
        float g = this.rotation.y();
        float h = this.rotation.z();
        if (bl) {
            g = -g;
            h = -h;
        }
        int i = bl ? -1 : 1;
        poseStack.translate((float)i * this.translation.x(), this.translation.y(), this.translation.z());
        poseStack.mulPose(new Quaternionf().rotationXYZ(f * ((float)Math.PI / 180), g * ((float)Math.PI / 180), h * ((float)Math.PI / 180)));
        poseStack.scale(this.scale.x(), this.scale.y(), this.scale.z());
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (this.getClass() == object.getClass()) {
            ItemTransform itemTransform = (ItemTransform)object;
            return this.rotation.equals(itemTransform.rotation) && this.scale.equals(itemTransform.scale) && this.translation.equals(itemTransform.translation);
        }
        return false;
    }

    public int hashCode() {
        int i = this.rotation.hashCode();
        i = 31 * i + this.translation.hashCode();
        i = 31 * i + this.scale.hashCode();
        return i;
    }

    @Environment(value=EnvType.CLIENT)
    protected static class Deserializer
    implements JsonDeserializer<ItemTransform> {
        private static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0f, 0.0f, 0.0f);
        private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0f, 0.0f, 0.0f);
        private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0f, 1.0f, 1.0f);
        public static final float MAX_TRANSLATION = 5.0f;
        public static final float MAX_SCALE = 4.0f;

        protected Deserializer() {
        }

        @Override
        public ItemTransform deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Vector3f vector3f = this.getVector3f(jsonObject, "rotation", DEFAULT_ROTATION);
            Vector3f vector3f2 = this.getVector3f(jsonObject, "translation", DEFAULT_TRANSLATION);
            vector3f2.mul(0.0625f);
            vector3f2.set(Mth.clamp(vector3f2.x, -5.0f, 5.0f), Mth.clamp(vector3f2.y, -5.0f, 5.0f), Mth.clamp(vector3f2.z, -5.0f, 5.0f));
            Vector3f vector3f3 = this.getVector3f(jsonObject, "scale", DEFAULT_SCALE);
            vector3f3.set(Mth.clamp(vector3f3.x, -4.0f, 4.0f), Mth.clamp(vector3f3.y, -4.0f, 4.0f), Mth.clamp(vector3f3.z, -4.0f, 4.0f));
            return new ItemTransform(vector3f, vector3f2, vector3f3);
        }

        private Vector3f getVector3f(JsonObject jsonObject, String string, Vector3f vector3f) {
            if (!jsonObject.has(string)) {
                return vector3f;
            }
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, string);
            if (jsonArray.size() != 3) {
                throw new JsonParseException("Expected 3 " + string + " values, found: " + jsonArray.size());
            }
            float[] fs = new float[3];
            for (int i = 0; i < fs.length; ++i) {
                fs[i] = GsonHelper.convertToFloat(jsonArray.get(i), string + "[" + i + "]");
            }
            return new Vector3f(fs[0], fs[1], fs[2]);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

