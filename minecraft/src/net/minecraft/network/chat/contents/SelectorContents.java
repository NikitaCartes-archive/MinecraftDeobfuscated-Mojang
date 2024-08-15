package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;

public record SelectorContents(SelectorPattern selector, Optional<Component> separator) implements ComponentContents {
	public static final MapCodec<SelectorContents> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					SelectorPattern.CODEC.fieldOf("selector").forGetter(SelectorContents::selector),
					ComponentSerialization.CODEC.optionalFieldOf("separator").forGetter(SelectorContents::separator)
				)
				.apply(instance, SelectorContents::new)
	);
	public static final ComponentContents.Type<SelectorContents> TYPE = new ComponentContents.Type<>(CODEC, "selector");

	@Override
	public ComponentContents.Type<?> type() {
		return TYPE;
	}

	@Override
	public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
		if (commandSourceStack == null) {
			return Component.empty();
		} else {
			Optional<? extends Component> optional = ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, i);
			return ComponentUtils.formatList(this.selector.resolved().findEntities(commandSourceStack), optional, Entity::getDisplayName);
		}
	}

	@Override
	public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
		return styledContentConsumer.accept(style, this.selector.pattern());
	}

	@Override
	public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
		return contentConsumer.accept(this.selector.pattern());
	}

	public String toString() {
		return "pattern{" + this.selector + "}";
	}
}
