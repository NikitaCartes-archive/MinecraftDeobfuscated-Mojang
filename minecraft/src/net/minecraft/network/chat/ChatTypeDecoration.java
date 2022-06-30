package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

public record ChatTypeDecoration(String translationKey, List<ChatTypeDecoration.Parameter> parameters, Style style) {
	public static final Codec<ChatTypeDecoration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.STRING.fieldOf("translation_key").forGetter(ChatTypeDecoration::translationKey),
					ChatTypeDecoration.Parameter.CODEC.listOf().fieldOf("parameters").forGetter(ChatTypeDecoration::parameters),
					Style.FORMATTING_CODEC.optionalFieldOf("style", Style.EMPTY).forGetter(ChatTypeDecoration::style)
				)
				.apply(instance, ChatTypeDecoration::new)
	);

	public static ChatTypeDecoration withSender(String string) {
		return new ChatTypeDecoration(string, List.of(ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY);
	}

	public static ChatTypeDecoration directMessage(String string) {
		Style style = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
		return new ChatTypeDecoration(string, List.of(ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), style);
	}

	public static ChatTypeDecoration teamMessage(String string) {
		return new ChatTypeDecoration(
			string, List.of(ChatTypeDecoration.Parameter.TEAM_NAME, ChatTypeDecoration.Parameter.SENDER, ChatTypeDecoration.Parameter.CONTENT), Style.EMPTY
		);
	}

	public Component decorate(Component component, ChatSender chatSender) {
		Object[] objects = this.resolveParameters(component, chatSender);
		return Component.translatable(this.translationKey, objects).withStyle(this.style);
	}

	private Component[] resolveParameters(Component component, ChatSender chatSender) {
		Component[] components = new Component[this.parameters.size()];

		for (int i = 0; i < components.length; i++) {
			ChatTypeDecoration.Parameter parameter = (ChatTypeDecoration.Parameter)this.parameters.get(i);
			components[i] = parameter.select(component, chatSender);
		}

		return components;
	}

	public static enum Parameter implements StringRepresentable {
		SENDER("sender", (component, chatSender) -> chatSender.name()),
		TEAM_NAME("team_name", (component, chatSender) -> chatSender.teamName()),
		CONTENT("content", (component, chatSender) -> component);

		public static final Codec<ChatTypeDecoration.Parameter> CODEC = StringRepresentable.fromEnum(ChatTypeDecoration.Parameter::values);
		private final String name;
		private final ChatTypeDecoration.Parameter.Selector selector;

		private Parameter(String string2, ChatTypeDecoration.Parameter.Selector selector) {
			this.name = string2;
			this.selector = selector;
		}

		public Component select(Component component, ChatSender chatSender) {
			Component component2 = this.selector.select(component, chatSender);
			return (Component)Objects.requireNonNullElse(component2, CommonComponents.EMPTY);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public interface Selector {
			@Nullable
			Component select(Component component, ChatSender chatSender);
		}
	}
}
