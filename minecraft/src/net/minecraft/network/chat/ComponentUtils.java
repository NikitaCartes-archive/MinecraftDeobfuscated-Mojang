package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;

public class ComponentUtils {
	public static Component mergeStyles(Component component, Style style) {
		if (style.isEmpty()) {
			return component;
		} else {
			return component.getStyle().isEmpty() ? component.setStyle(style.copy()) : new TextComponent("").append(component).setStyle(style.copy());
		}
	}

	public static Component updateForEntity(@Nullable CommandSourceStack commandSourceStack, Component component, @Nullable Entity entity, int i) throws CommandSyntaxException {
		if (i > 100) {
			return component;
		} else {
			i++;
			Component component2 = component instanceof ContextAwareComponent
				? ((ContextAwareComponent)component).resolve(commandSourceStack, entity, i)
				: component.copy();

			for (Component component3 : component.getSiblings()) {
				component2.append(updateForEntity(commandSourceStack, component3, entity, i));
			}

			return mergeStyles(component2, component.getStyle());
		}
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
			return new TextComponent("");
		} else if (collection.size() == 1) {
			return (Component)function.apply(collection.iterator().next());
		} else {
			List<T> list = Lists.<T>newArrayList(collection);
			list.sort(Comparable::compareTo);
			return formatList(list, function);
		}
	}

	public static <T> Component formatList(Collection<T> collection, Function<T, Component> function) {
		if (collection.isEmpty()) {
			return new TextComponent("");
		} else if (collection.size() == 1) {
			return (Component)function.apply(collection.iterator().next());
		} else {
			Component component = new TextComponent("");
			boolean bl = true;

			for (T object : collection) {
				if (!bl) {
					component.append(new TextComponent(", ").withStyle(ChatFormatting.GRAY));
				}

				component.append((Component)function.apply(object));
				bl = false;
			}

			return component;
		}
	}

	public static Component wrapInSquareBrackets(Component component) {
		return new TextComponent("[").append(component).append("]");
	}

	public static Component fromMessage(Message message) {
		return (Component)(message instanceof Component ? (Component)message : new TextComponent(message.getString()));
	}
}
