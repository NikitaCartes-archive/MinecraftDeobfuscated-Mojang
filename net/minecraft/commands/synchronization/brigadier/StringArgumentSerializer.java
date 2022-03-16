/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class StringArgumentSerializer
implements ArgumentTypeInfo<StringArgumentType, Template> {
    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeEnum(template.type);
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        StringArgumentType.StringType stringType = friendlyByteBuf.readEnum(StringArgumentType.StringType.class);
        return new Template(stringType);
    }

    @Override
    public void serializeToJson(Template template, JsonObject jsonObject) {
        jsonObject.addProperty("type", switch (template.type) {
            default -> throw new IncompatibleClassChangeError();
            case StringArgumentType.StringType.SINGLE_WORD -> "word";
            case StringArgumentType.StringType.QUOTABLE_PHRASE -> "phrase";
            case StringArgumentType.StringType.GREEDY_PHRASE -> "greedy";
        });
    }

    @Override
    public Template unpack(StringArgumentType stringArgumentType) {
        return new Template(stringArgumentType.getType());
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<StringArgumentType> {
        final StringArgumentType.StringType type;

        public Template(StringArgumentType.StringType stringType) {
            this.type = stringType;
        }

        @Override
        public StringArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return switch (this.type) {
                default -> throw new IncompatibleClassChangeError();
                case StringArgumentType.StringType.SINGLE_WORD -> StringArgumentType.word();
                case StringArgumentType.StringType.QUOTABLE_PHRASE -> StringArgumentType.string();
                case StringArgumentType.StringType.GREEDY_PHRASE -> StringArgumentType.greedyString();
            };
        }

        @Override
        public ArgumentTypeInfo<StringArgumentType, ?> type() {
            return StringArgumentSerializer.this;
        }

        @Override
        public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
            return this.instantiate(commandBuildContext);
        }
    }
}

