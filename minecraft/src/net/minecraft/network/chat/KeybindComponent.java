package net.minecraft.network.chat;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class KeybindComponent extends BaseComponent {
	private static Function<String, Supplier<Component>> keyResolver = string -> () -> new TextComponent(string);
	private final String name;
	private Supplier<Component> nameResolver;

	public KeybindComponent(String string) {
		this.name = string;
	}

	@Environment(EnvType.CLIENT)
	public static void setKeyResolver(Function<String, Supplier<Component>> function) {
		keyResolver = function;
	}

	private Component getNestedComponent() {
		if (this.nameResolver == null) {
			this.nameResolver = (Supplier<Component>)keyResolver.apply(this.name);
		}

		return (Component)this.nameResolver.get();
	}

	@Override
	public <T> Optional<T> visitSelf(FormattedText.ContentConsumer<T> contentConsumer) {
		return this.getNestedComponent().visit(contentConsumer);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public <T> Optional<T> visitSelf(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
		return this.getNestedComponent().visit(styledContentConsumer, style);
	}

	public KeybindComponent plainCopy() {
		return new KeybindComponent(this.name);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof KeybindComponent)) {
			return false;
		} else {
			KeybindComponent keybindComponent = (KeybindComponent)object;
			return this.name.equals(keybindComponent.name) && super.equals(object);
		}
	}

	@Override
	public String toString() {
		return "KeybindComponent{keybind='" + this.name + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
	}

	public String getName() {
		return this.name;
	}
}
