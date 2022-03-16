package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.FriendlyByteBuf;

public interface ArgumentTypeInfo<A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> {
	void serializeToNetwork(T template, FriendlyByteBuf friendlyByteBuf);

	T deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf);

	void serializeToJson(T template, JsonObject jsonObject);

	T unpack(A argumentType);

	public interface Template<A extends ArgumentType<?>> {
		A instantiate(CommandBuildContext commandBuildContext);

		ArgumentTypeInfo<A, ?> type();
	}
}
