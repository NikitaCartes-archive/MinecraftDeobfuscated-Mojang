package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.FriendlyByteBuf;

public class SingletonArgumentInfo<A extends ArgumentType<?>> implements ArgumentTypeInfo<A, SingletonArgumentInfo<A>.Template> {
	private final SingletonArgumentInfo<A>.Template template;

	private SingletonArgumentInfo(Function<CommandBuildContext, A> function) {
		this.template = new SingletonArgumentInfo.Template(function);
	}

	public static <T extends ArgumentType<?>> SingletonArgumentInfo<T> contextFree(Supplier<T> supplier) {
		return new SingletonArgumentInfo<>(commandBuildContext -> (ArgumentType)supplier.get());
	}

	public static <T extends ArgumentType<?>> SingletonArgumentInfo<T> contextAware(Function<CommandBuildContext, T> function) {
		return new SingletonArgumentInfo<>(function);
	}

	public void serializeToNetwork(SingletonArgumentInfo<A>.Template template, FriendlyByteBuf friendlyByteBuf) {
	}

	public void serializeToJson(SingletonArgumentInfo<A>.Template template, JsonObject jsonObject) {
	}

	public SingletonArgumentInfo<A>.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
		return this.template;
	}

	public SingletonArgumentInfo<A>.Template unpack(A argumentType) {
		return this.template;
	}

	public final class Template implements ArgumentTypeInfo.Template<A> {
		private final Function<CommandBuildContext, A> constructor;

		public Template(final Function<CommandBuildContext, A> function) {
			this.constructor = function;
		}

		@Override
		public A instantiate(CommandBuildContext commandBuildContext) {
			return (A)this.constructor.apply(commandBuildContext);
		}

		@Override
		public ArgumentTypeInfo<A, ?> type() {
			return SingletonArgumentInfo.this;
		}
	}
}
