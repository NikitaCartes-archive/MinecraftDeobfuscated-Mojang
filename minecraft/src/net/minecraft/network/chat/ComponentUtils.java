package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;

public class ComponentUtils {
	public static final String DEFAULT_SEPARATOR_TEXT = ", ";
	public static final Component DEFAULT_SEPARATOR = new TextComponent(", ").withStyle(ChatFormatting.GRAY);
	public static final Component DEFAULT_NO_STYLE_SEPARATOR = new TextComponent(", ");

	public static MutableComponent mergeStyles(MutableComponent mutableComponent, Style style) {
		if (style.isEmpty()) {
			return mutableComponent;
		} else {
			Style style2 = mutableComponent.getStyle();
			if (style2.isEmpty()) {
				return mutableComponent.setStyle(style);
			} else {
				return style2.equals(style) ? mutableComponent : mutableComponent.setStyle(style2.applyTo(style));
			}
		}
	}

	public static Optional<MutableComponent> updateForEntity(
		@Nullable CommandSourceStack commandSourceStack, Optional<Component> optional, @Nullable Entity entity, int i
	) throws CommandSyntaxException {
		return optional.isPresent() ? Optional.of(updateForEntity(commandSourceStack, (Component)optional.get(), entity, i)) : Optional.empty();
	}

	public static MutableComponent updateForEntity(@Nullable CommandSourceStack commandSourceStack, Component component, @Nullable Entity entity, int i) throws CommandSyntaxException {
		if (i > 100) {
			return component.copy();
		} else {
			MutableComponent mutableComponent = component instanceof ContextAwareComponent
				? ((ContextAwareComponent)component).resolve(commandSourceStack, entity, i + 1)
				: component.plainCopy();

			for (Component component2 : component.getSiblings()) {
				mutableComponent.append(updateForEntity(commandSourceStack, component2, entity, i + 1));
			}

			return mutableComponent.withStyle(resolveStyle(commandSourceStack, component.getStyle(), entity, i));
		}
	}

	private static Style resolveStyle(@Nullable CommandSourceStack commandSourceStack, Style style, @Nullable Entity entity, int i) throws CommandSyntaxException {
		HoverEvent hoverEvent = style.getHoverEvent();
		if (hoverEvent != null) {
			Component component = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
			if (component != null) {
				HoverEvent hoverEvent2 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, updateForEntity(commandSourceStack, component, entity, i + 1));
				return style.withHoverEvent(hoverEvent2);
			}
		}

		return style;
	}

	public static Component getDisplayName(GameProfile gameProfile) {
		if (gameProfile.getName() != null) {
			return new TextComponent(gameProfile.getName());
		} else {
			return gameProfile.getId() != null ? new TextComponent(gameProfile.getId().toString()) : new TextComponent("(unknown)");
		}
	}

	public static Component formatList(Collection<String> collection) {
		return formatAndSortList(collection, string -> new TextComponent(string).withStyle(ChatFormatting.GREEN));
	}

	public static <T extends Comparable<T>> Component formatAndSortList(Collection<T> collection, Function<T, Component> function) {
		if (collection.isEmpty()) {
			return TextComponent.EMPTY;
		} else if (collection.size() == 1) {
			return (Component)function.apply((Comparable)collection.iterator().next());
		} else {
			List<T> list = Lists.<T>newArrayList(collection);
			list.sort(Comparable::compareTo);
			return formatList(list, function);
		}
	}

	public static <T> Component formatList(Collection<T> collection, Function<T, Component> function) {
		return formatList(collection, DEFAULT_SEPARATOR, function);
	}

	public static <T> MutableComponent formatList(Collection<T> collection, Optional<? extends Component> optional, Function<T, Component> function) {
		return formatList(collection, DataFixUtils.orElse(optional, DEFAULT_SEPARATOR), function);
	}

	public static <T> MutableComponent formatList(Collection<T> collection, Component component, Function<T, Component> function) {
		if (collection.isEmpty()) {
			return new TextComponent("");
		} else if (collection.size() == 1) {
			return ((Component)function.apply(collection.iterator().next())).copy();
		} else {
			MutableComponent mutableComponent = new TextComponent("");
			boolean bl = true;

			for (T object : collection) {
				if (!bl) {
					mutableComponent.append(component);
				}

				mutableComponent.append((Component)function.apply(object));
				bl = false;
			}

			return mutableComponent;
		}
	}

	public static MutableComponent wrapInSquareBrackets(Component component) {
		return new TranslatableComponent("chat.square_brackets", component);
	}

	public static Component fromMessage(Message message) {
		return (Component)(message instanceof Component ? (Component)message : new TextComponent(message.getString()));
	}
}
