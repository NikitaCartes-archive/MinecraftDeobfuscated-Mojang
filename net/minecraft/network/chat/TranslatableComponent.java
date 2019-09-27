/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.ContextAwareComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableFormatException;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class TranslatableComponent
extends BaseComponent
implements ContextAwareComponent {
    private static final Language DEFAULT_LANGUAGE = new Language();
    private static final Language LANGUAGE = Language.getInstance();
    private final String key;
    private final Object[] args;
    private final Object decomposeLock = new Object();
    private long decomposedLanguageTime = -1L;
    protected final List<Component> decomposedParts = Lists.newArrayList();
    public static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    public TranslatableComponent(String string, Object ... objects) {
        this.key = string;
        this.args = objects;
        for (int i = 0; i < objects.length; ++i) {
            Object object = objects[i];
            if (object instanceof Component) {
                Component component = ((Component)object).deepCopy();
                this.args[i] = component;
                component.getStyle().inheritFrom(this.getStyle());
                continue;
            }
            if (object != null) continue;
            this.args[i] = "null";
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @VisibleForTesting
    synchronized void decompose() {
        Object object = this.decomposeLock;
        synchronized (object) {
            long l = LANGUAGE.getLastUpdateTime();
            if (l == this.decomposedLanguageTime) {
                return;
            }
            this.decomposedLanguageTime = l;
            this.decomposedParts.clear();
        }
        String string = LANGUAGE.getElement(this.key);
        try {
            this.decomposeTemplate(string);
        } catch (TranslatableFormatException translatableFormatException) {
            this.decomposedParts.clear();
            this.decomposedParts.add(new TextComponent(string));
        }
    }

    protected void decomposeTemplate(String string) {
        Matcher matcher = FORMAT_PATTERN.matcher(string);
        try {
            int i = 0;
            int j = 0;
            while (matcher.find(j)) {
                int k = matcher.start();
                int l = matcher.end();
                if (k > j) {
                    TextComponent component = new TextComponent(String.format(string.substring(j, k), new Object[0]));
                    component.getStyle().inheritFrom(this.getStyle());
                    this.decomposedParts.add(component);
                }
                String string2 = matcher.group(2);
                String string3 = string.substring(k, l);
                if ("%".equals(string2) && "%%".equals(string3)) {
                    TextComponent component2 = new TextComponent("%");
                    component2.getStyle().inheritFrom(this.getStyle());
                    this.decomposedParts.add(component2);
                } else if ("s".equals(string2)) {
                    int m;
                    String string4 = matcher.group(1);
                    int n = m = string4 != null ? Integer.parseInt(string4) - 1 : i++;
                    if (m < this.args.length) {
                        this.decomposedParts.add(this.getComponent(m));
                    }
                } else {
                    throw new TranslatableFormatException(this, "Unsupported format: '" + string3 + "'");
                }
                j = l;
            }
            if (j < string.length()) {
                TextComponent component3 = new TextComponent(String.format(string.substring(j), new Object[0]));
                component3.getStyle().inheritFrom(this.getStyle());
                this.decomposedParts.add(component3);
            }
        } catch (IllegalFormatException illegalFormatException) {
            throw new TranslatableFormatException(this, (Throwable)illegalFormatException);
        }
    }

    private Component getComponent(int i) {
        Component component;
        if (i >= this.args.length) {
            throw new TranslatableFormatException(this, i);
        }
        Object object = this.args[i];
        if (object instanceof Component) {
            component = (Component)object;
        } else {
            component = new TextComponent(object == null ? "null" : object.toString());
            component.getStyle().inheritFrom(this.getStyle());
        }
        return component;
    }

    @Override
    public Component setStyle(Style style) {
        super.setStyle(style);
        for (Object object : this.args) {
            if (!(object instanceof Component)) continue;
            ((Component)object).getStyle().inheritFrom(this.getStyle());
        }
        if (this.decomposedLanguageTime > -1L) {
            for (Component component : this.decomposedParts) {
                component.getStyle().inheritFrom(style);
            }
        }
        return this;
    }

    @Override
    public Stream<Component> stream() {
        this.decompose();
        return Streams.concat(this.decomposedParts.stream(), this.siblings.stream()).flatMap(Component::stream);
    }

    @Override
    public String getContents() {
        this.decompose();
        StringBuilder stringBuilder = new StringBuilder();
        for (Component component : this.decomposedParts) {
            stringBuilder.append(component.getContents());
        }
        return stringBuilder.toString();
    }

    @Override
    public TranslatableComponent copy() {
        Object[] objects = new Object[this.args.length];
        for (int i = 0; i < this.args.length; ++i) {
            objects[i] = this.args[i] instanceof Component ? ((Component)this.args[i]).deepCopy() : this.args[i];
        }
        return new TranslatableComponent(this.key, objects);
    }

    @Override
    public Component resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
        Object[] objects = new Object[this.args.length];
        for (int j = 0; j < objects.length; ++j) {
            Object object = this.args[j];
            objects[j] = object instanceof Component ? ComponentUtils.updateForEntity(commandSourceStack, (Component)object, entity, i) : object;
        }
        return new TranslatableComponent(this.key, objects);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof TranslatableComponent) {
            TranslatableComponent translatableComponent = (TranslatableComponent)object;
            return Arrays.equals(this.args, translatableComponent.args) && this.key.equals(translatableComponent.key) && super.equals(object);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int i = super.hashCode();
        i = 31 * i + this.key.hashCode();
        i = 31 * i + Arrays.hashCode(this.args);
        return i;
    }

    @Override
    public String toString() {
        return "TranslatableComponent{key='" + this.key + '\'' + ", args=" + Arrays.toString(this.args) + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }

    public String getKey() {
        return this.key;
    }

    public Object[] getArgs() {
        return this.args;
    }

    @Override
    public /* synthetic */ Component copy() {
        return this.copy();
    }
}

