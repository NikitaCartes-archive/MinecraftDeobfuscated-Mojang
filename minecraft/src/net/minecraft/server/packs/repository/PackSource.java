package net.minecraft.server.packs.repository;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public interface PackSource {
	PackSource DEFAULT = passThrough();
	PackSource BUILT_IN = decorating("pack.source.builtin");
	PackSource WORLD = decorating("pack.source.world");
	PackSource SERVER = decorating("pack.source.server");

	Component decorate(Component component);

	static PackSource passThrough() {
		return component -> component;
	}

	static PackSource decorating(String string) {
		Component component = new TranslatableComponent(string);
		return component2 -> new TranslatableComponent("pack.nameAndSource", component2, component);
	}
}
