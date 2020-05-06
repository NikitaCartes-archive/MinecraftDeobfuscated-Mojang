/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.thread;

import com.mojang.datafixers.util.Either;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ProcessorHandle<Msg>
extends AutoCloseable {
    public String name();

    public void tell(Msg var1);

    @Override
    default public void close() {
    }

    default public <Source> CompletableFuture<Source> ask(Function<? super ProcessorHandle<Source>, ? extends Msg> function) {
        CompletableFuture completableFuture = new CompletableFuture();
        Msg object = function.apply(ProcessorHandle.of("ask future procesor handle", completableFuture::complete));
        this.tell(object);
        return completableFuture;
    }

    default public <Source> CompletableFuture<Source> askEither(Function<? super ProcessorHandle<Either<Source, Exception>>, ? extends Msg> function) {
        CompletableFuture completableFuture = new CompletableFuture();
        Msg object = function.apply(ProcessorHandle.of("ask future procesor handle", either -> {
            either.ifLeft(completableFuture::complete);
            either.ifRight(completableFuture::completeExceptionally);
        }));
        this.tell(object);
        return completableFuture;
    }

    public static <Msg> ProcessorHandle<Msg> of(final String string, final Consumer<Msg> consumer) {
        return new ProcessorHandle<Msg>(){

            @Override
            public String name() {
                return string;
            }

            @Override
            public void tell(Msg object) {
                consumer.accept(object);
            }

            public String toString() {
                return string;
            }
        };
    }
}

