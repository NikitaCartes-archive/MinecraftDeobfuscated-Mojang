/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RewindableStream<T> {
    final List<T> cache = Lists.newArrayList();
    final Spliterator<T> source;

    public RewindableStream(Stream<T> stream) {
        this.source = stream.spliterator();
    }

    public Stream<T> getStream() {
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE, 0){
            private int index;

            @Override
            public boolean tryAdvance(Consumer<? super T> consumer) {
                while (this.index >= RewindableStream.this.cache.size()) {
                    if (RewindableStream.this.source.tryAdvance(RewindableStream.this.cache::add)) continue;
                    return false;
                }
                consumer.accept(RewindableStream.this.cache.get(this.index++));
                return true;
            }
        }, false);
    }
}

