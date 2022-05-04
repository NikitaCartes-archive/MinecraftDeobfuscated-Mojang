/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public enum NarratorStatus {
    OFF(0, "options.narrator.off"),
    ALL(1, "options.narrator.all"),
    CHAT(2, "options.narrator.chat"),
    SYSTEM(3, "options.narrator.system");

    private static final NarratorStatus[] BY_ID;
    private final int id;
    private final Component name;

    private NarratorStatus(int j, String string2) {
        this.id = j;
        this.name = Component.translatable(string2);
    }

    public int getId() {
        return this.id;
    }

    public Component getName() {
        return this.name;
    }

    public static NarratorStatus byId(int i) {
        return BY_ID[Mth.positiveModulo(i, BY_ID.length)];
    }

    public boolean shouldNarrate(ChatType.Narration.Priority priority) {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case OFF -> false;
            case ALL -> true;
            case CHAT -> {
                if (priority == ChatType.Narration.Priority.CHAT) {
                    yield true;
                }
                yield false;
            }
            case SYSTEM -> priority == ChatType.Narration.Priority.SYSTEM;
        };
    }

    static {
        BY_ID = (NarratorStatus[])Arrays.stream(NarratorStatus.values()).sorted(Comparator.comparingInt(NarratorStatus::getId)).toArray(NarratorStatus[]::new);
    }
}

