package net.minecraft.server.packs.repository;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public interface PackSource {
	UnaryOperator<Component> NO_DECORATION = UnaryOperator.identity();
	PackSource DEFAULT = create(NO_DECORATION, true);
	PackSource BUILT_IN = create(decorateWithSource("pack.source.builtin"), true);
	PackSource FEATURE = create(decorateWithSource("pack.source.feature"), false);
	PackSource WORLD = create(decorateWithSource("pack.source.world"), true);
	PackSource SERVER = create(decorateWithSource("pack.source.server"), true);

	Component decorate(Component component);

	boolean shouldAddAutomatically();

	static PackSource create(UnaryOperator<Component> unaryOperator, boolean bl) {
		return new PackSource() {
			@Override
			public Component decorate(Component component) {
				return (Component)unaryOperator.apply(component);
			}

			@Override
			public boolean shouldAddAutomatically() {
				return bl;
			}
		};
	}

	private static UnaryOperator<Component> decorateWithSource(String string) {
		Component component = Component.translatable(string);
		return component2 -> Component.translatable("pack.nameAndSource", component2, component).withStyle(ChatFormatting.GRAY);
	}
}
