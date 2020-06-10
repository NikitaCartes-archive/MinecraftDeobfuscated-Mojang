/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.ContextAwareComponent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableFormatException;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class TranslatableComponent
extends BaseComponent
implements ContextAwareComponent {
    private static final Object[] NO_ARGS = new Object[0];
    private static final FormattedText TEXT_PERCENT = FormattedText.of("%");
    private static final FormattedText TEXT_NULL = FormattedText.of("null");
    private final String key;
    private final Object[] args;
    @Nullable
    private Language decomposedWith;
    private final List<FormattedText> decomposedParts = Lists.newArrayList();
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    public TranslatableComponent(String string) {
        this.key = string;
        this.args = NO_ARGS;
    }

    public TranslatableComponent(String string, Object ... objects) {
        this.key = string;
        this.args = objects;
    }

    private void decompose() {
        Language language = Language.getInstance();
        if (language == this.decomposedWith) {
            return;
        }
        this.decomposedWith = language;
        this.decomposedParts.clear();
        String string = language.getOrDefault(this.key);
        try {
            this.decomposeTemplate(language.reorder(string, true), language);
        } catch (TranslatableFormatException translatableFormatException) {
            this.decomposedParts.clear();
            this.decomposedParts.add(FormattedText.of(string));
        }
    }

    private void decomposeTemplate(String string, Language language) {
        Matcher matcher = FORMAT_PATTERN.matcher(string);
        try {
            int i = 0;
            int j = 0;
            while (matcher.find(j)) {
                String string2;
                int k = matcher.start();
                int l = matcher.end();
                if (k > j) {
                    string2 = string.substring(j, k);
                    if (string2.indexOf(37) != -1) {
                        throw new IllegalArgumentException();
                    }
                    this.decomposedParts.add(FormattedText.of(string2));
                }
                string2 = matcher.group(2);
                String string3 = string.substring(k, l);
                if ("%".equals(string2) && "%%".equals(string3)) {
                    this.decomposedParts.add(TEXT_PERCENT);
                } else if ("s".equals(string2)) {
                    int m;
                    String string4 = matcher.group(1);
                    int n = m = string4 != null ? Integer.parseInt(string4) - 1 : i++;
                    if (m < this.args.length) {
                        this.decomposedParts.add(this.getArgument(m, language));
                    }
                } else {
                    throw new TranslatableFormatException(this, "Unsupported format: '" + string3 + "'");
                }
                j = l;
            }
            if (j < string.length()) {
                String string5 = string.substring(j);
                if (string5.indexOf(37) != -1) {
                    throw new IllegalArgumentException();
                }
                this.decomposedParts.add(FormattedText.of(string5));
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new TranslatableFormatException(this, (Throwable)illegalArgumentException);
        }
    }

    private FormattedText getArgument(int i, Language language) {
        if (i >= this.args.length) {
            throw new TranslatableFormatException(this, i);
        }
        Object object = this.args[i];
        if (object instanceof Component) {
            return (Component)object;
        }
        return object == null ? TEXT_NULL : FormattedText.of(language.reorder(object.toString(), false));
    }

    @Override
    public TranslatableComponent plainCopy() {
        return new TranslatableComponent(this.key, this.args);
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public <T> Optional<T> visitSelf(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        this.decompose();
        for (FormattedText formattedText : this.decomposedParts) {
            Optional<T> optional = formattedText.visit(styledContentConsumer, style);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> visitSelf(FormattedText.ContentConsumer<T> contentConsumer) {
        this.decompose();
        for (FormattedText formattedText : this.decomposedParts) {
            Optional<T> optional = formattedText.visit(contentConsumer);
            if (!optional.isPresent()) continue;
            return optional;
        }
        return Optional.empty();
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
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
    public /* synthetic */ BaseComponent plainCopy() {
        return this.plainCopy();
    }

    @Override
    public /* synthetic */ MutableComponent plainCopy() {
        return this.plainCopy();
    }
}

