/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat.contents;

import java.util.Locale;
import net.minecraft.network.chat.contents.TranslatableContents;

public class TranslatableFormatException
extends IllegalArgumentException {
    public TranslatableFormatException(TranslatableContents translatableContents, String string) {
        super(String.format(Locale.ROOT, "Error parsing: %s: %s", translatableContents, string));
    }

    public TranslatableFormatException(TranslatableContents translatableContents, int i) {
        super(String.format(Locale.ROOT, "Invalid index %d requested for %s", i, translatableContents));
    }

    public TranslatableFormatException(TranslatableContents translatableContents, Throwable throwable) {
        super(String.format(Locale.ROOT, "Error while parsing: %s", translatableContents), throwable);
    }
}

