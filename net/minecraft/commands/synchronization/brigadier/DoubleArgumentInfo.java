/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleArgumentInfo
implements ArgumentTypeInfo<DoubleArgumentType, Template> {
    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
        boolean bl = template.min != -1.7976931348623157E308;
        boolean bl2 = template.max != Double.MAX_VALUE;
        friendlyByteBuf.writeByte(ArgumentUtils.createNumberFlags(bl, bl2));
        if (bl) {
            friendlyByteBuf.writeDouble(template.min);
        }
        if (bl2) {
            friendlyByteBuf.writeDouble(template.max);
        }
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        byte b = friendlyByteBuf.readByte();
        double d = ArgumentUtils.numberHasMin(b) ? friendlyByteBuf.readDouble() : -1.7976931348623157E308;
        double e = ArgumentUtils.numberHasMax(b) ? friendlyByteBuf.readDouble() : Double.MAX_VALUE;
        return new Template(d, e);
    }

    @Override
    public void serializeToJson(Template template, JsonObject jsonObject) {
        if (template.min != -1.7976931348623157E308) {
            jsonObject.addProperty("min", template.min);
        }
        if (template.max != Double.MAX_VALUE) {
            jsonObject.addProperty("max", template.max);
        }
    }

    @Override
    public Template unpack(DoubleArgumentType doubleArgumentType) {
        return new Template(doubleArgumentType.getMinimum(), doubleArgumentType.getMaximum());
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<DoubleArgumentType> {
        final double min;
        final double max;

        Template(double d, double e) {
            this.min = d;
            this.max = e;
        }

        @Override
        public DoubleArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return DoubleArgumentType.doubleArg(this.min, this.max);
        }

        @Override
        public ArgumentTypeInfo<DoubleArgumentType, ?> type() {
            return DoubleArgumentInfo.this;
        }

        @Override
        public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return this.instantiate(commandBuildContext);
        }
    }
}

