package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;

public class EmptyArgumentSerializer<T extends ArgumentType<?>> implements ArgumentSerializer<T> {
	private final Supplier<T> constructor;

	public EmptyArgumentSerializer(Supplier<T> supplier) {
		this.constructor = supplier;
	}

	@Override
	public void serializeToNetwork(T argumentType, FriendlyByteBuf friendlyByteBuf) {
	}

	@Override
	public T deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		return (T)this.constructor.get();
	}

	@Override
	public void serializeToJson(T argumentType, JsonObject jsonObject) {
	}
}
