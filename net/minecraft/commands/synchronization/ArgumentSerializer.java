/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.network.FriendlyByteBuf;

public interface ArgumentSerializer<T extends ArgumentType<?>> {
    public void serializeToNetwork(T var1, FriendlyByteBuf var2);

    public T deserializeFromNetwork(FriendlyByteBuf var1);

    public void serializeToJson(T var1, JsonObject var2);
}

