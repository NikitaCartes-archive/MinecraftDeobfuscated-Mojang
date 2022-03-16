/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class LongArgumentInfo
implements ArgumentTypeInfo<LongArgumentType, Template> {
    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
        boolean bl = template.min != Long.MIN_VALUE;
        boolean bl2 = template.max != Long.MAX_VALUE;
        friendlyByteBuf.writeByte(ArgumentUtils.createNumberFlags(bl, bl2));
        if (bl) {
            friendlyByteBuf.writeLong(template.min);
        }
        if (bl2) {
            friendlyByteBuf.writeLong(template.max);
        }
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        byte b = friendlyByteBuf.readByte();
        long l = ArgumentUtils.numberHasMin(b) ? friendlyByteBuf.readLong() : Long.MIN_VALUE;
        long m = ArgumentUtils.numberHasMax(b) ? friendlyByteBuf.readLong() : Long.MAX_VALUE;
        return new Template(l, m);
    }

    @Override
    public void serializeToJson(Template template, JsonObject jsonObject) {
        if (template.min != Long.MIN_VALUE) {
            jsonObject.addProperty("min", template.min);
        }
        if (template.max != Long.MAX_VALUE) {
            jsonObject.addProperty("max", template.max);
        }
    }

    @Override
    public Template unpack(LongArgumentType longArgumentType) {
        return new Template(longArgumentType.getMinimum(), longArgumentType.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<LongArgumentType> {
        final long min;
        final long max;

        Template(long l, long m) {
            this.min = l;
            this.max = m;
        }

        @Override
        public LongArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return LongArgumentType.longArg(this.min, this.max);
        }

        @Override
        public ArgumentTypeInfo<LongArgumentType, ?> type() {
            return LongArgumentInfo.this;
        }

        @Override
        public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return this.instantiate(commandBuildContext);
        }
    }
}

