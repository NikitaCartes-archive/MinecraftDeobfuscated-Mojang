/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.sounds;

public enum SoundSource {
    MASTER("master"),
    MUSIC("music"),
    RECORDS("record"),
    WEATHER("weather"),
    BLOCKS("block"),
    HOSTILE("hostile"),
    NEUTRAL("neutral"),
    PLAYERS("player"),
    AMBIENT("ambient"),
    VOICE("voice");

    private final String name;

    private SoundSource(String string2) {
        this.name = string2;
    }

    public String getName() {
        return this.name;
    }
}

