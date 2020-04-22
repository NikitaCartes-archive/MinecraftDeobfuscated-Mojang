/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

public interface MutableComponent
extends Component {
    public MutableComponent setStyle(Style var1);

    default public MutableComponent append(String string) {
        return this.append(new TextComponent(string));
    }

    public MutableComponent append(Component var1);

    default public MutableComponent withStyle(UnaryOperator<Style> unaryOperator) {
        this.setStyle((Style)unaryOperator.apply(this.getStyle()));
        return this;
    }

    default public MutableComponent withStyle(Style style) {
        this.setStyle(style.applyTo(this.getStyle()));
        return this;
    }

    default public MutableComponent withStyle(ChatFormatting ... chatFormattings) {
        this.setStyle(this.getStyle().applyFormats(chatFormattings));
        return this;
    }

    default public MutableComponent withStyle(ChatFormatting chatFormatting) {
        this.setStyle(this.getStyle().applyFormat(chatFormatting));
        return this;
    }
}

