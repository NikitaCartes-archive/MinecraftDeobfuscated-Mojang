/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ContextAwareComponent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class ComponentUtils {
    public static final String DEFAULT_SEPARATOR_TEXT = ", ";
    public static final Component DEFAULT_SEPARATOR = new TextComponent(", ").withStyle(ChatFormatting.GRAY);
    public static final Component DEFAULT_NO_STYLE_SEPARATOR = new TextComponent(", ");

    public static MutableComponent mergeStyles(MutableComponent mutableComponent, Style style) {
        if (style.isEmpty()) {
            return mutableComponent;
        }
        Style style2 = mutableComponent.getStyle();
        if (style2.isEmpty()) {
            return mutableComponent.setStyle(style);
        }
        if (style2.equals(style)) {
            return mutableComponent;
        }
        return mutableComponent.setStyle(style2.applyTo(style));
    }

    public static Optional<MutableComponent> updateForEntity(@Nullable CommandSourceStack commandSourceStack, Optional<Component> optional, @Nullable Entity entity, int i) throws CommandSyntaxException {
        return optional.isPresent() ? Optional.of(ComponentUtils.updateForEntity(commandSourceStack, optional.get(), entity, i)) : Optional.empty();
    }

    public static MutableComponent updateForEntity(@Nullable CommandSourceStack commandSourceStack, Component component, @Nullable Entity entity, int i) throws CommandSyntaxException {
        if (i > 100) {
            return component.copy();
        }
        MutableComponent mutableComponent = component instanceof ContextAwareComponent ? ((ContextAwareComponent)((Object)component)).resolve(commandSourceStack, entity, i + 1) : component.plainCopy();
        for (Component component2 : component.getSiblings()) {
            mutableComponent.append(ComponentUtils.updateForEntity(commandSourceStack, component2, entity, i + 1));
        }
        return mutableComponent.withStyle(ComponentUtils.resolveStyle(commandSourceStack, component.getStyle(), entity, i));
    }

    private static Style resolveStyle(@Nullable CommandSourceStack commandSourceStack, Style style, @Nullable Entity entity, int i) throws CommandSyntaxException {
        Component component;
        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent != null && (component = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT)) != null) {
            HoverEvent hoverEvent2 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentUtils.updateForEntity(commandSourceStack, component, entity, i + 1));
            return style.withHoverEvent(hoverEvent2);
        }
        return style;
    }

    public static Component getDisplayName(GameProfile gameProfile) {
        if (gameProfile.getName() != null) {
            return new TextComponent(gameProfile.getName());
        }
        if (gameProfile.getId() != null) {
            return new TextComponent(gameProfile.getId().toString());
        }
        return new TextComponent("(unknown)");
    }

    public static Component formatList(Collection<String> collection) {
        return ComponentUtils.formatAndSortList(collection, string -> new TextComponent((String)string).withStyle(ChatFormatting.GREEN));
    }

    public static <T extends Comparable<T>> Component formatAndSortList(Collection<T> collection, Function<T, Component> function) {
        if (collection.isEmpty()) {
            return TextComponent.EMPTY;
        }
        if (collection.size() == 1) {
            return function.apply((Comparable)collection.iterator().next());
        }
        ArrayList<T> list = Lists.newArrayList(collection);
        list.sort(Comparable::compareTo);
        return ComponentUtils.formatList(list, function);
    }

    public static <T> Component formatList(Collection<? extends T> collection, Function<T, Component> function) {
        return ComponentUtils.formatList(collection, DEFAULT_SEPARATOR, function);
    }

    public static <T> MutableComponent formatList(Collection<? extends T> collection, Optional<? extends Component> optional, Function<T, Component> function) {
        return ComponentUtils.formatList(collection, DataFixUtils.orElse(optional, DEFAULT_SEPARATOR), function);
    }

    public static Component formatList(Collection<? extends Component> collection, Component component) {
        return ComponentUtils.formatList(collection, component, Function.identity());
    }

    public static <T> MutableComponent formatList(Collection<? extends T> collection, Component component, Function<T, Component> function) {
        if (collection.isEmpty()) {
            return new TextComponent("");
        }
        if (collection.size() == 1) {
            return function.apply(collection.iterator().next()).copy();
        }
        TextComponent mutableComponent = new TextComponent("");
        boolean bl = true;
        for (T object : collection) {
            if (!bl) {
                mutableComponent.append(component);
            }
            mutableComponent.append(function.apply(object));
            bl = false;
        }
        return mutableComponent;
    }

    public static MutableComponent wrapInSquareBrackets(Component component) {
        return new TranslatableComponent("chat.square_brackets", component);
    }

    public static Component fromMessage(Message message) {
        if (message instanceof Component) {
            return (Component)message;
        }
        return new TextComponent(message.getString());
    }
}

