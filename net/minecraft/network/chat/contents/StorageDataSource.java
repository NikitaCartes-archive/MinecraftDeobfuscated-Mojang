/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat.contents;

import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.DataSource;
import net.minecraft.resources.ResourceLocation;

public record StorageDataSource(ResourceLocation id) implements DataSource
{
    @Override
    public Stream<CompoundTag> getData(CommandSourceStack commandSourceStack) {
        CompoundTag compoundTag = commandSourceStack.getServer().getCommandStorage().get(this.id);
        return Stream.of(compoundTag);
    }

    @Override
    public String toString() {
        return "storage=" + this.id;
    }
}

