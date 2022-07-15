/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import net.minecraft.commands.arguments.PreviewedArgument;

public interface SignedArgument<T>
extends PreviewedArgument<T> {
    public String getSignableText(T var1);
}

