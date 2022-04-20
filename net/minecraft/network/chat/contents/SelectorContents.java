/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SelectorContents
implements ComponentContents {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String pattern;
    @Nullable
    private final EntitySelector selector;
    protected final Optional<Component> separator;

    public SelectorContents(String string, Optional<Component> optional) {
        this.pattern = string;
        this.separator = optional;
        this.selector = SelectorContents.parseSelector(string);
    }

    @Nullable
    private static EntitySelector parseSelector(String string) {
        EntitySelector entitySelector = null;
        try {
            EntitySelectorParser entitySelectorParser = new EntitySelectorParser(new StringReader(string));
            entitySelector = entitySelectorParser.parse();
        } catch (CommandSyntaxException commandSyntaxException) {
            LOGGER.warn("Invalid selector component: {}: {}", (Object)string, (Object)commandSyntaxException.getMessage());
        }
        return entitySelector;
    }

    public String getPattern() {
        return this.pattern;
    }

    @Nullable
    public EntitySelector getSelector() {
        return this.selector;
    }

    public Optional<Component> getSeparator() {
        return this.separator;
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
        if (commandSourceStack == null || this.selector == null) {
            return Component.empty();
        }
        Optional<MutableComponent> optional = ComponentUtils.updateForEntity(commandSourceStack, this.separator, entity, i);
        return ComponentUtils.formatList(this.selector.findEntities(commandSourceStack), optional, Entity::getDisplayName);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        return styledContentConsumer.accept(style, this.pattern);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        return contentConsumer.accept(this.pattern);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof SelectorContents)) return false;
        SelectorContents selectorContents = (SelectorContents)object;
        if (!this.pattern.equals(selectorContents.pattern)) return false;
        if (!this.separator.equals(selectorContents.separator)) return false;
        return true;
    }

    public int hashCode() {
        int i = this.pattern.hashCode();
        i = 31 * i + this.separator.hashCode();
        return i;
    }

    public String toString() {
        return "pattern{" + this.pattern + "}";
    }
}

