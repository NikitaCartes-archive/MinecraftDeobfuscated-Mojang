/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface ContextAwareComponent {
    public Component resolve(@Nullable CommandSourceStack var1, @Nullable Entity var2, int var3) throws CommandSyntaxException;
}

