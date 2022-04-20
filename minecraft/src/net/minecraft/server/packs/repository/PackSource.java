package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

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
		Component component = Component.translatable(string);
		return component2 -> Component.translatable("pack.nameAndSource", component2, component).withStyle(ChatFormatting.GRAY);
	}
}
