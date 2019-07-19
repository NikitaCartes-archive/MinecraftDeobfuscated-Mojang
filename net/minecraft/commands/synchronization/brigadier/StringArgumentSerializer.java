/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;

public class StringArgumentSerializer
implements ArgumentSerializer<StringArgumentType> {
    @Override
    public void serializeToNetwork(StringArgumentType stringArgumentType, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeEnum(stringArgumentType.getType());
    }

    @Override
    public StringArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        StringArgumentType.StringType stringType = friendlyByteBuf.readEnum(StringArgumentType.StringType.class);
        switch (stringType) {
            case SINGLE_WORD: {
                return StringArgumentType.word();
            }
            case QUOTABLE_PHRASE: {
                return StringArgumentType.string();
            }
        }
        return StringArgumentType.greedyString();
    }

    @Override
    public void serializeToJson(StringArgumentType stringArgumentType, JsonObject jsonObject) {
        switch (stringArgumentType.getType()) {
            case SINGLE_WORD: {
                jsonObject.addProperty("type", "word");
                break;
            }
            case QUOTABLE_PHRASE: {
                jsonObject.addProperty("type", "phrase");
                break;
            }
            default: {
                jsonObject.addProperty("type", "greedy");
            }
        }
    }

    @Override
    public /* synthetic */ ArgumentType deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }
}

