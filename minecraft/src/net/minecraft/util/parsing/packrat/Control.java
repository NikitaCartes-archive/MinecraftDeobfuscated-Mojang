package net.minecraft.util.parsing.packrat;

public interface Control {
	Control UNBOUND = () -> {
	};

	void cut();
}
