package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

@FunctionalInterface
public interface ChatDecorator {
	ChatDecorator PLAIN = (serverPlayer, component) -> component;

	@Deprecated
	static ChatDecorator testRainbowChat() {
		return (serverPlayer, component) -> {
			String string = component.getString().trim();
			int i = string.length();
			float f = Math.nextDown(1.0F) * (float)i;
			MutableComponent mutableComponent = Component.literal(String.valueOf(string.charAt(0)))
				.withStyle(Style.EMPTY.withColor(Mth.hsvToRgb(Math.nextDown(1.0F), 1.0F, 1.0F)));

			for (int j = 1; j < i; j++) {
				mutableComponent.append(Component.literal(String.valueOf(string.charAt(j))).withStyle(Style.EMPTY.withColor(Mth.hsvToRgb((float)j / f, 1.0F, 1.0F))));
			}

			return mutableComponent;
		};
	}

	Component decorate(@Nullable ServerPlayer serverPlayer, Component component);

	default PlayerChatMessage decorate(@Nullable ServerPlayer serverPlayer, Component component, MessageSignature messageSignature, boolean bl) {
		Component component2 = this.decorate(serverPlayer, component);
		if (component.equals(component2)) {
			return PlayerChatMessage.signed(component, messageSignature);
		} else {
			return !bl ? PlayerChatMessage.signed(component, messageSignature).withUnsignedContent(component2) : PlayerChatMessage.signed(component2, messageSignature);
		}
	}

	default PlayerChatMessage decorate(@Nullable ServerPlayer serverPlayer, PlayerChatMessage playerChatMessage) {
		return this.decorate(serverPlayer, playerChatMessage.signedContent(), playerChatMessage.signature(), false);
	}
}
