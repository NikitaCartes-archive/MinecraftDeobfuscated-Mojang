/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.synchronization;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.ArgumentType;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class SingletonArgumentInfo<A extends ArgumentType<?>>
implements ArgumentTypeInfo<A, Template> {
    private final Template template;

    private SingletonArgumentInfo(Function<CommandBuildContext, A> function) {
        this.template = new Template(function);
    }

    public static <T extends ArgumentType<?>> SingletonArgumentInfo<T> contextFree(Supplier<T> supplier) {
        return new SingletonArgumentInfo<ArgumentType>(commandBuildContext -> (ArgumentType)supplier.get());
    }

    public static <T extends ArgumentType<?>> SingletonArgumentInfo<T> contextAware(Function<CommandBuildContext, T> function) {
        return new SingletonArgumentInfo<T>(function);
    }

    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void serializeToJson(Template template, JsonObject jsonObject) {
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.template;
    }

    @Override
    public Template unpack(A argumentType) {
        return this.template;
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template unpack(ArgumentType argumentType) {
        return this.unpack(argumentType);
    }

    @Override
    public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
        return this.deserializeFromNetwork(friendlyByteBuf);
    }

    public final class Template
    implements ArgumentTypeInfo.Template<A> {
        private final Function<CommandBuildContext, A> constructor;

        public Template(Function<CommandBuildContext, A> function) {
            this.constructor = function;
        }

        @Override
        public A instantiate(CommandBuildContext commandBuildContext) {
            return (ArgumentType)this.constructor.apply(commandBuildContext);
        }

        @Override
        public ArgumentTypeInfo<A, ?> type() {
            return SingletonArgumentInfo.this;
        }
    }
}

