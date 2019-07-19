/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.commands.synchronization.brigadier.BrigadierArgumentSerializers;
import net.minecraft.network.FriendlyByteBuf;

public class IntegerArgumentSerializer
implements ArgumentSerializer<IntegerArgumentType> {
    @Override
    public void serializeToNetwork(IntegerArgumentType integerArgumentType, FriendlyByteBuf friendlyByteBuf) {
        boolean bl = integerArgumentType.getMinimum() != Integer.MIN_VALUE;
        boolean bl2 = integerArgumentType.getMaximum() != Integer.MAX_VALUE;
        friendlyByteBuf.writeByte(BrigadierArgumentSerializers.createNumberFlags(bl, bl2));
        if (bl) {
            friendlyByteBuf.writeInt(integerArgumentType.getMinimum());
        }
        if (bl2) {
            friendlyByteBuf.writeInt(integerArgumentType.getMaximum());
        }
    }

    @Override
    public IntegerArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        byte b = friendlyByteBuf.readByte();
        int i = BrigadierArgumentSerializers.numberHasMin(b) ? friendlyByteBuf.readInt() : Integer.MIN_VALUE;
        int j = BrigadierArgumentSerializers.numberHasMax(b) ? friendlyByteBuf.readInt() : Integer.MAX_VALUE;
        return IntegerArgumentType.integer(i, j);
    }

    @Override
    public void serializeToJson(IntegerArgumentType integerArgumentType, JsonObject jsonObject) {
        if (integerArgumentType.getMinimum() != Integer.MIN_VALUE) {
            jsonObject.addProperty("min", integerArgumentType.getMinimum());
        }
        if (integerArgumentType.getMaximum() != Integer.MAX_VALUE) {
            jsonObject.addProperty("max", integerArgumentType.getMaximum());
        }
    }

    @Override
    public /* synthetic */ ArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }
}

