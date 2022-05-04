package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

public record ChatDecoration(String translationKey, List<ChatDecoration.Parameter> parameters, Style style) {
	public static final Codec<ChatDecoration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.STRING.fieldOf("translation_key").forGetter(ChatDecoration::translationKey),
					ChatDecoration.Parameter.CODEC.listOf().fieldOf("parameters").forGetter(ChatDecoration::parameters),
					Style.FORMATTING_CODEC.fieldOf("style").forGetter(ChatDecoration::style)
				)
				.apply(instance, ChatDecoration::new)
	);

	public static ChatDecoration withSender(String string) {
		return new ChatDecoration(string, List.of(ChatDecoration.Parameter.SENDER, ChatDecoration.Parameter.CONTENT), Style.EMPTY);
	}

	public static ChatDecoration directMessage(String string) {
		Style style = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
		return new ChatDecoration(string, List.of(ChatDecoration.Parameter.SENDER, ChatDecoration.Parameter.CONTENT), style);
	}

	public static ChatDecoration teamMessage(String string) {
		return new ChatDecoration(string, List.of(ChatDecoration.Parameter.TEAM_NAME, ChatDecoration.Parameter.SENDER, ChatDecoration.Parameter.CONTENT), Style.EMPTY);
	}

	public Component decorate(Component component, @Nullable ChatSender chatSender) {
		Object[] objects = this.resolveParameters(component, chatSender);
		return Component.translatable(this.translationKey, objects).withStyle(this.style);
	}

	private Component[] resolveParameters(Component component, @Nullable ChatSender chatSender) {
		Component[] components = new Component[this.parameters.size()];

		for (int i = 0; i < components.length; i++) {
			ChatDecoration.Parameter parameter = (ChatDecoration.Parameter)this.parameters.get(i);
			components[i] = parameter.select(component, chatSender);
		}

		return components;
	}

	public static enum Parameter implements StringRepresentable {
		SENDER("sender", (component, chatSender) -> chatSender != null ? chatSender.name() : null),
		TEAM_NAME("team_name", (component, chatSender) -> chatSender != null ? chatSender.teamName() : null),
		CONTENT("content", (component, chatSender) -> component);

		public static final Codec<ChatDecoration.Parameter> CODEC = StringRepresentable.fromEnum(ChatDecoration.Parameter::values);
		private final String name;
		private final ChatDecoration.Parameter.Selector selector;

		private Parameter(String string2, ChatDecoration.Parameter.Selector selector) {
			this.name = string2;
			this.selector = selector;
		}

		public Component select(Component component, @Nullable ChatSender chatSender) {
			Component component2 = this.selector.select(component, chatSender);
			return (Component)Objects.requireNonNullElse(component2, CommonComponents.EMPTY);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public interface Selector {
			@Nullable
			Component select(Component component, @Nullable ChatSender chatSender);
		}
	}
}
