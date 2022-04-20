/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.narration;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;

@Environment(value=EnvType.CLIENT)
public class NarrationThunk<T> {
    private final T contents;
    private final BiConsumer<Consumer<String>, T> converter;
    public static final NarrationThunk<?> EMPTY = new NarrationThunk<Unit>(Unit.INSTANCE, (consumer, unit) -> {});

    private NarrationThunk(T object, BiConsumer<Consumer<String>, T> biConsumer) {
        this.contents = object;
        this.converter = biConsumer;
    }

    public static NarrationThunk<?> from(String string) {
        return new NarrationThunk<String>(string, Consumer::accept);
    }

    public static NarrationThunk<?> from(Component component2) {
        return new NarrationThunk<Component>(component2, (consumer, component) -> consumer.accept(component.getString()));
    }

    public static NarrationThunk<?> from(List<Component> list) {
        return new NarrationThunk<List>(list, (consumer, list2) -> list.stream().map(Component::getString).forEach((Consumer<String>)consumer));
    }

    public void getText(Consumer<String> consumer) {
        this.converter.accept(consumer, (Consumer<String>)this.contents);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof NarrationThunk) {
            NarrationThunk narrationThunk = (NarrationThunk)object;
            return narrationThunk.converter == this.converter && narrationThunk.contents.equals(this.contents);
        }
        return false;
    }

    public int hashCode() {
        int i = this.contents.hashCode();
        i = 31 * i + this.converter.hashCode();
        return i;
    }
}

