/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class FloatArgumentInfo
implements ArgumentTypeInfo<FloatArgumentType, Template> {
    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
        boolean bl = template.min != -3.4028235E38f;
        boolean bl2 = template.max != Float.MAX_VALUE;
        friendlyByteBuf.writeByte(ArgumentUtils.createNumberFlags(bl, bl2));
        if (bl) {
            friendlyByteBuf.writeFloat(template.min);
        }
        if (bl2) {
            friendlyByteBuf.writeFloat(template.max);
        }
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        byte b = friendlyByteBuf.readByte();
        float f = ArgumentUtils.numberHasMin(b) ? friendlyByteBuf.readFloat() : -3.4028235E38f;
        float g = ArgumentUtils.numberHasMax(b) ? friendlyByteBuf.readFloat() : Float.MAX_VALUE;
        return new Template(f, g);
    }

    @Override
    public void serializeToJson(Template template, JsonObject jsonObject) {
        if (template.min != -3.4028235E38f) {
            jsonObject.addProperty("min", Float.valueOf(template.min));
        }
        if (template.max != Float.MAX_VALUE) {
            jsonObject.addProperty("max", Float.valueOf(template.max));
        }
    }

    @Override
    public Template unpack(FloatArgumentType floatArgumentType) {
        return new Template(floatArgumentType.getMinimum(), floatArgumentType.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<FloatArgumentType> {
        final float min;
        final float max;

        Template(float f, float g) {
            this.min = f;
            this.max = g;
        }

        @Override
        public FloatArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return FloatArgumentType.floatArg(this.min, this.max);
        }

        @Override
        public ArgumentTypeInfo<FloatArgumentType, ?> type() {
            return FloatArgumentInfo.this;
        }

        @Override
        public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return this.instantiate(commandBuildContext);
        }
    }
}

