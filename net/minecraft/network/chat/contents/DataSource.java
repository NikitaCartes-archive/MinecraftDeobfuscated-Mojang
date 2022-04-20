/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;

@FunctionalInterface
public interface DataSource {
    public Stream<CompoundTag> getData(CommandSourceStack var1) throws CommandSyntaxException;
}

