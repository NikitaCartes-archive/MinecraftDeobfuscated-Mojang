package net.minecraft.client.gui.narration;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;

@Environment(EnvType.CLIENT)
public class NarrationThunk<T> {
	private final T contents;
	private final BiConsumer<Consumer<String>, T> converter;
	public static final NarrationThunk<?> EMPTY = new NarrationThunk<>(Unit.INSTANCE, (consumer, unit) -> {
	});

	private NarrationThunk(T object, BiConsumer<Consumer<String>, T> biConsumer) {
		this.contents = object;
		this.converter = biConsumer;
	}

	public static NarrationThunk<?> from(String string) {
		return new NarrationThunk<>(string, Consumer::accept);
	}

	public static NarrationThunk<?> from(Component component) {
		return new NarrationThunk<>(component, (consumer, componentx) -> consumer.accept(componentx.getContents()));
	}

	public static NarrationThunk<?> from(List<Component> list) {
		return new NarrationThunk<>(list, (consumer, list2) -> list.stream().map(Component::getString).forEach(consumer));
	}

	public void getText(Consumer<String> consumer) {
		this.converter.accept(consumer, this.contents);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof NarrationThunk<?> narrationThunk)
				? false
				: narrationThunk.converter == this.converter && narrationThunk.contents.equals(this.contents);
		}
	}

	public int hashCode() {
		int i = this.contents.hashCode();
		return 31 * i + this.converter.hashCode();
	}
}
