/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.commands.synchronization.brigadier.BrigadierArgumentSerializers;
import net.minecraft.network.FriendlyByteBuf;

public class FloatArgumentSerializer
implements ArgumentSerializer<FloatArgumentType> {
    @Override
    public void serializeToNetwork(FloatArgumentType floatArgumentType, FriendlyByteBuf friendlyByteBuf) {
        boolean bl = floatArgumentType.getMinimum() != -3.4028235E38f;
        boolean bl2 = floatArgumentType.getMaximum() != Float.MAX_VALUE;
        friendlyByteBuf.writeByte(BrigadierArgumentSerializers.createNumberFlags(bl, bl2));
        if (bl) {
            friendlyByteBuf.writeFloat(floatArgumentType.getMinimum());
        }
        if (bl2) {
            friendlyByteBuf.writeFloat(floatArgumentType.getMaximum());
        }
    }

    @Override
    public FloatArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        byte b = friendlyByteBuf.readByte();
        float f = BrigadierArgumentSerializers.numberHasMin(b) ? friendlyByteBuf.readFloat() : -3.4028235E38f;
        float g = BrigadierArgumentSerializers.numberHasMax(b) ? friendlyByteBuf.readFloat() : Float.MAX_VALUE;
        return FloatArgumentType.floatArg(f, g);
    }

    @Override
    public void serializeToJson(FloatArgumentType floatArgumentType, JsonObject jsonObject) {
        if (floatArgumentType.getMinimum() != -3.4028235E38f) {
            jsonObject.addProperty("min", Float.valueOf(floatArgumentType.getMinimum()));
        }
        if (floatArgumentType.getMaximum() != Float.MAX_VALUE) {
            jsonObject.addProperty("max", Float.valueOf(floatArgumentType.getMaximum()));
        }
    }

    @Override
    public /* synthetic */ ArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }
}

