package net.minecraft.network.chat;

import java.util.function.Function;
import java.util.function.Supplier;

public class KeybindComponent extends BaseComponent {
	public static Function<String, Supplier<String>> keyResolver = string -> () -> string;
	private final String name;
	private Supplier<String> nameResolver;

	public KeybindComponent(String string) {
		this.name = string;
	}

	@Override
	public String getContents() {
		if (this.nameResolver == null) {
			this.nameResolver = (Supplier<String>)keyResolver.apply(this.name);
		}

		return (String)this.nameResolver.get();
	}

	public KeybindComponent copy() {
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
