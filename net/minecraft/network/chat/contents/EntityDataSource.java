/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public record EntityDataSource(String selectorPattern, @Nullable EntitySelector compiledSelector) implements DataSource
{
    public EntityDataSource(String string) {
        this(string, EntityDataSource.compileSelector(string));
    }

    @Nullable
    private static EntitySelector compileSelector(String string) {
        try {
            EntitySelectorParser entitySelectorParser = new EntitySelectorParser(new StringReader(string));
            return entitySelectorParser.parse();
        } catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    @Override
    public Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        if (this.compiledSelector != null) {
            List<? extends Entity> list = this.compiledSelector.findEntities(commandSourceStack);
            return list.stream().map(NbtPredicate::getEntityTagToCompare);
        }
        return Stream.empty();
    }

    @Override
    public String toString() {
        return "entity=" + this.selectorPattern;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof EntityDataSource)) return false;
        EntityDataSource entityDataSource = (EntityDataSource)object;
        if (!this.selectorPattern.equals(entityDataSource.selectorPattern)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.selectorPattern);
    }

    @Nullable
    public EntitySelector compiledSelector() {
        return this.compiledSelector;
    }
}

