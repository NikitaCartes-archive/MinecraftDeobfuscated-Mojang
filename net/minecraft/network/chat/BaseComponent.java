/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public abstract class BaseComponent
implements MutableComponent {
    protected final List<Component> siblings = Lists.newArrayList();
    private Style style = Style.EMPTY;

    @Override
    public MutableComponent append(Component component) {
        this.siblings.add(component);
        return this;
    }

    @Override
    public String getContents() {
        return "";
    }

    @Override
    public List<Component> getSiblings() {
        return this.siblings;
    }

    @Override
    public MutableComponent setStyle(Style style) {
        this.style = style;
        return this;
    }

    @Override
    public Style getStyle() {
        return this.style;
    }

    @Override
    public abstract BaseComponent toMutable();

    @Override
    public final MutableComponent mutableCopy() {
        BaseComponent baseComponent = this.toMutable();
        baseComponent.siblings.addAll(this.siblings);
        baseComponent.setStyle(this.style);
        return baseComponent;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof BaseComponent) {
            BaseComponent baseComponent = (BaseComponent)object;
            return this.siblings.equals(baseComponent.siblings) && Objects.equals(this.getStyle(), baseComponent.getStyle());
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(this.getStyle(), this.siblings);
    }

    public String toString() {
        return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
    }

    @Override
    public /* synthetic */ MutableComponent toMutable() {
        return this.toMutable();
    }
}

