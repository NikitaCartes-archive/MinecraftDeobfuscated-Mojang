/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum PackCompatibility {
    TOO_OLD("old"),
    TOO_NEW("new"),
    COMPATIBLE("compatible");

    private final Component description;
    private final Component confirmation;

    private PackCompatibility(String string2) {
        this.description = new TranslatableComponent("resourcePack.incompatible." + string2);
        this.confirmation = new TranslatableComponent("resourcePack.incompatible.confirm." + string2);
    }

    public boolean isCompatible() {
        return this == COMPATIBLE;
    }

    public static PackCompatibility forFormat(int i) {
        if (i < SharedConstants.getCurrentVersion().getPackVersion()) {
            return TOO_OLD;
        }
        if (i > SharedConstants.getCurrentVersion().getPackVersion()) {
            return TOO_NEW;
        }
        return COMPATIBLE;
    }

    @Environment(value=EnvType.CLIENT)
    public Component getDescription() {
        return this.description;
    }

    @Environment(value=EnvType.CLIENT)
    public Component getConfirmation() {
        return this.confirmation;
    }
}

