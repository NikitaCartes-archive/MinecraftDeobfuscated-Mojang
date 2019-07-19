/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public interface DataAccessor {
    public void setData(CompoundTag var1) throws CommandSyntaxException;

    public CompoundTag getData() throws CommandSyntaxException;

    public Component getModifiedSuccess();

    public Component getPrintSuccess(Tag var1);

    public Component getPrintSuccess(NbtPathArgument.NbtPath var1, double var2, int var4);
}

