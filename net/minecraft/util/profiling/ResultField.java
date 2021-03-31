/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.profiling;

public final class ResultField
implements Comparable<ResultField> {
    public final double percentage;
    public final double globalPercentage;
    public final long count;
    public final String name;

    public ResultField(String string, double d, double e, long l) {
        this.name = string;
        this.percentage = d;
        this.globalPercentage = e;
        this.count = l;
    }

    @Override
    public int compareTo(ResultField resultField) {
        if (resultField.percentage < this.percentage) {
            return -1;
        }
        if (resultField.percentage > this.percentage) {
            return 1;
        }
        return resultField.name.compareTo(this.name);
    }

    public int getColor() {
        return (this.name.hashCode() & 0xAAAAAA) + 0x444444;
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((ResultField)object);
    }
}

