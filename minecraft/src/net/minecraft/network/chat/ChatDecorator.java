package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface ChatDecorator {
	ChatDecorator PLAIN = (serverPlayer, component) -> component;

	Component decorate(@Nullable ServerPlayer serverPlayer, Component component);
}
