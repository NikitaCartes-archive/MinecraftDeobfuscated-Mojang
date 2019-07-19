/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;

public class TextComponent
extends BaseComponent {
    private final String text;

    public TextComponent(String string) {
        this.text = string;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String getContents() {
        return this.text;
    }

    @Override
    public TextComponent copy() {
        return new TextComponent(this.text);
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
    public /* synthetic */ Component copy() {
        return this.copy();
    }
}

