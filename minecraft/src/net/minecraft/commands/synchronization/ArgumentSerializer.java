package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.network.FriendlyByteBuf;

public interface ArgumentSerializer<T extends ArgumentType<?>> {
	void serializeToNetwork(T argumentType, FriendlyByteBuf friendlyByteBuf);

	T deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf);

	void serializeToJson(T argumentType, JsonObject jsonObject);
}
