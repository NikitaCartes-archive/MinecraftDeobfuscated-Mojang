/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class IntegerArgumentInfo
implements ArgumentTypeInfo<IntegerArgumentType, Template> {
    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
        boolean bl = template.min != Integer.MIN_VALUE;
        boolean bl2 = template.max != Integer.MAX_VALUE;
        friendlyByteBuf.writeByte(ArgumentUtils.createNumberFlags(bl, bl2));
        if (bl) {
            friendlyByteBuf.writeInt(template.min);
        }
        if (bl2) {
            friendlyByteBuf.writeInt(template.max);
        }
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        byte b = friendlyByteBuf.readByte();
        int i = ArgumentUtils.numberHasMin(b) ? friendlyByteBuf.readInt() : Integer.MIN_VALUE;
        int j = ArgumentUtils.numberHasMax(b) ? friendlyByteBuf.readInt() : Integer.MAX_VALUE;
        return new Template(i, j);
    }

    @Override
    public void serializeToJson(Template template, JsonObject jsonObject) {
        if (template.min != Integer.MIN_VALUE) {
            jsonObject.addProperty("min", template.min);
        }
        if (template.max != Integer.MAX_VALUE) {
            jsonObject.addProperty("max", template.max);
        }
    }

    @Override
    public Template unpack(IntegerArgumentType integerArgumentType) {
        return new Template(integerArgumentType.getMinimum(), integerArgumentType.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<IntegerArgumentType> {
        final int min;
        final int max;

        Template(int i, int j) {
            this.min = i;
            this.max = j;
        }

        @Override
        public IntegerArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return IntegerArgumentType.integer(this.min, this.max);
        }

        @Override
        public ArgumentTypeInfo<IntegerArgumentType, ?> type() {
            return IntegerArgumentInfo.this;
        }

        @Override
        public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return this.instantiate(commandBuildContext);
        }
    }
}

