/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

public class TextComponent
extends BaseComponent {
    public static final Component EMPTY = new TextComponent("");
    private final String text;
    @Nullable
    private Language decomposedWith;
    private String reorderedText;

    public TextComponent(String string) {
        this.text = string;
        this.reorderedText = string;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String getContents() {
        if (this.text.isEmpty()) {
            return this.text;
        }
        Language language = Language.getInstance();
        if (this.decomposedWith != language) {
            this.reorderedText = language.reorder(this.text, false);
            this.decomposedWith = language;
        }
        return this.reorderedText;
    }

    @Override
    public TextComponent toMutable() {
        TextComponent textComponent = new TextComponent(this.text);
        textComponent.setStyle(this.getStyle());
        return textComponent;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof TextComponent) {
            TextComponent textComponent = (TextComponent)object;
            return this.text.equals(textComponent.getText()) && super.equals(object);
        }
        return false;
    }

    @Override
    public String toString() {
        return "TextComponent{text='" + this.text + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }

    @Override
    public /* synthetic */ BaseComponent toMutable() {
        return this.toMutable();
    }

    @Override
    public /* synthetic */ MutableComponent toMutable() {
        return this.toMutable();
    }
}

