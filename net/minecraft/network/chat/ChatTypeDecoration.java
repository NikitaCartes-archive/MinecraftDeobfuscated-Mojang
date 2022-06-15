/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public record ChatTypeDecoration(String translationKey, List<Parameter> parameters, Style style) {
    public static final Codec<ChatTypeDecoration> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("translation_key")).forGetter(ChatTypeDecoration::translationKey), ((MapCodec)Parameter.CODEC.listOf().fieldOf("parameters")).forGetter(ChatTypeDecoration::parameters), Style.FORMATTING_CODEC.optionalFieldOf("style", Style.EMPTY).forGetter(ChatTypeDecoration::style)).apply((Applicative<ChatTypeDecoration, ?>)instance, ChatTypeDecoration::new));

    public static ChatTypeDecoration withSender(String string) {
        return new ChatTypeDecoration(string, List.of(Parameter.SENDER, Parameter.CONTENT), Style.EMPTY);
    }

    public static ChatTypeDecoration directMessage(String string) {
        Style style = Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true);
        return new ChatTypeDecoration(string, List.of(Parameter.SENDER, Parameter.CONTENT), style);
    }

    public static ChatTypeDecoration teamMessage(String string) {
        return new ChatTypeDecoration(string, List.of(Parameter.TEAM_NAME, Parameter.SENDER, Parameter.CONTENT), Style.EMPTY);
    }

    public Component decorate(Component component, @Nullable ChatSender chatSender) {
        Object[] objects = this.resolveParameters(component, chatSender);
        return Component.translatable(this.translationKey, objects).withStyle(this.style);
    }

    private Component[] resolveParameters(Component component, @Nullable ChatSender chatSender) {
        Component[] components = new Component[this.parameters.size()];
        for (int i = 0; i < components.length; ++i) {
            Parameter parameter = this.parameters.get(i);
            components[i] = parameter.select(component, chatSender);
        }
        return components;
    }

    public static enum Parameter implements StringRepresentable
    {
        SENDER("sender", (component, chatSender) -> chatSender != null ? chatSender.name() : null),
        TEAM_NAME("team_name", (component, chatSender) -> chatSender != null ? chatSender.teamName() : null),
        CONTENT("content", (component, chatSender) -> component);

        public static final Codec<Parameter> CODEC;
        private final String name;
        private final Selector selector;

        private Parameter(String string2, Selector selector) {
            this.name = string2;
            this.selector = selector;
        }

        public Component select(Component component, @Nullable ChatSender chatSender) {
            Component component2 = this.selector.select(component, chatSender);
            return Objects.requireNonNullElse(component2, CommonComponents.EMPTY);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Parameter::values);
        }

        public static interface Selector {
            @Nullable
            public Component select(Component var1, @Nullable ChatSender var2);
        }
    }
}

