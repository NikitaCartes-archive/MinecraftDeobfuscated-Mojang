/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

public class DataVersion {
    private final int version;
    private final String series;
    public static String MAIN_SERIES = "main";

    public DataVersion(int i) {
        this(i, MAIN_SERIES);
    }

    public DataVersion(int i, String string) {
        this.version = i;
        this.series = string;
    }

    public boolean isSideSeries() {
        return !this.series.equals(MAIN_SERIES);
    }

    public String getSeries() {
        return this.series;
    }

    public int getVersion() {
        return this.version;
    }

    public boolean isSameSeries(DataVersion dataVersion) {
        return this.getSeries().equals(dataVersion.getSeries()) && this.getVersion() == dataVersion.getVersion();
    }

    public boolean isCompatible(DataVersion dataVersion) {
        if (!this.isSameSeries(dataVersion)) {
            return false;
        }
        return this.isInExtendedWorldHeightSegment() == dataVersion.isInExtendedWorldHeightSegment();
    }

    public boolean isInExtendedWorldHeightSegment() {
        return this.version > 2692 && this.version <= 2706;
    }
}

