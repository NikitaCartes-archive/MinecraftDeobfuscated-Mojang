/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ContextAwareComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class ComponentUtils {
    public static Component mergeStyles(Component component, Style style) {
        if (style.isEmpty()) {
            return component;
        }
        if (component.getStyle().isEmpty()) {
            return component.setStyle(style.copy());
        }
        return new TextComponent("").append(component).setStyle(style.copy());
    }

    public static Component updateForEntity(@Nullable CommandSourceStack commandSourceStack, Component component, @Nullable Entity entity, int i) throws CommandSyntaxException {
        if (i > 100) {
            return component;
        }
        Component component2 = component instanceof ContextAwareComponent ? ((ContextAwareComponent)((Object)component)).resolve(commandSourceStack, entity, ++i) : component.copy();
        for (Component component3 : component.getSiblings()) {
            component2.append(ComponentUtils.updateForEntity(commandSourceStack, component3, entity, i));
        }
        return ComponentUtils.mergeStyles(component2, component.getStyle());
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
            return new TextComponent("");
        }
        if (collection.size() == 1) {
            return function.apply(collection.iterator().next());
        }
        ArrayList<T> list = Lists.newArrayList(collection);
        list.sort(Comparable::compareTo);
        return ComponentUtils.formatList(collection, function);
    }

    public static <T> Component formatList(Collection<T> collection, Function<T, Component> function) {
        if (collection.isEmpty()) {
            return new TextComponent("");
        }
        if (collection.size() == 1) {
            return function.apply(collection.iterator().next());
        }
        TextComponent component = new TextComponent("");
        boolean bl = true;
        for (T object : collection) {
            if (!bl) {
                component.append(new TextComponent(", ").withStyle(ChatFormatting.GRAY));
            }
            component.append(function.apply(object));
            bl = false;
        }
        return component;
    }

    public static Component wrapInSquareBrackets(Component component) {
        return new TextComponent("[").append(component).append("]");
    }

    public static Component fromMessage(Message message) {
        if (message instanceof Component) {
            return (Component)message;
        }
        return new TextComponent(message.getString());
    }
}

