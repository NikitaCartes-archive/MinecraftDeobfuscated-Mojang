/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.commands.synchronization.brigadier.BrigadierArgumentSerializers;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleArgumentSerializer
implements ArgumentSerializer<DoubleArgumentType> {
    @Override
    public void serializeToNetwork(DoubleArgumentType doubleArgumentType, FriendlyByteBuf friendlyByteBuf) {
        boolean bl = doubleArgumentType.getMinimum() != -1.7976931348623157E308;
        boolean bl2 = doubleArgumentType.getMaximum() != Double.MAX_VALUE;
        friendlyByteBuf.writeByte(BrigadierArgumentSerializers.createNumberFlags(bl, bl2));
        if (bl) {
            friendlyByteBuf.writeDouble(doubleArgumentType.getMinimum());
        }
        if (bl2) {
            friendlyByteBuf.writeDouble(doubleArgumentType.getMaximum());
        }
    }

    @Override
    public DoubleArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        byte b = friendlyByteBuf.readByte();
        double d = BrigadierArgumentSerializers.numberHasMin(b) ? friendlyByteBuf.readDouble() : -1.7976931348623157E308;
        double e = BrigadierArgumentSerializers.numberHasMax(b) ? friendlyByteBuf.readDouble() : Double.MAX_VALUE;
        return DoubleArgumentType.doubleArg(d, e);
    }

    @Override
    public void serializeToJson(DoubleArgumentType doubleArgumentType, JsonObject jsonObject) {
        if (doubleArgumentType.getMinimum() != -1.7976931348623157E308) {
            jsonObject.addProperty("min", doubleArgumentType.getMinimum());
        }
        if (doubleArgumentType.getMaximum() != Double.MAX_VALUE) {
            jsonObject.addProperty("max", doubleArgumentType.getMaximum());
        }
    }

    @Override
    public /* synthetic */ ArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }
}

