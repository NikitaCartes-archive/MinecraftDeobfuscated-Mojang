/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import net.minecraft.CrashReportDetail;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CrashReportCategory {
    private final String title;
    private final List<Entry> entries = Lists.newArrayList();
    private StackTraceElement[] stackTrace = new StackTraceElement[0];

    public CrashReportCategory(String string) {
        this.title = string;
    }

    public static String formatLocation(LevelHeightAccessor levelHeightAccessor, double d, double e, double f) {
        return String.format(Locale.ROOT, "%.2f,%.2f,%.2f - %s", d, e, f, CrashReportCategory.formatLocation(levelHeightAccessor, new BlockPos(d, e, f)));
    }

    public static String formatLocation(LevelHeightAccessor levelHeightAccessor, BlockPos blockPos) {
        return CrashReportCategory.formatLocation(levelHeightAccessor, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static String formatLocation(LevelHeightAccessor levelHeightAccessor, int i, int j, int k) {
        int w;
        int v;
        int u;
        int t;
        int s;
        int r;
        int q;
        int p;
        int o;
        int n;
        int m;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append(String.format(Locale.ROOT, "World: (%d,%d,%d)", i, j, k));
        } catch (Throwable throwable) {
            stringBuilder.append("(Error finding world loc)");
        }
        stringBuilder.append(", ");
        try {
            int l = SectionPos.blockToSectionCoord(i);
            m = SectionPos.blockToSectionCoord(j);
            n = SectionPos.blockToSectionCoord(k);
            o = i & 0xF;
            p = j & 0xF;
            q = k & 0xF;
            r = SectionPos.sectionToBlockCoord(l);
            s = levelHeightAccessor.getMinBuildHeight();
            t = SectionPos.sectionToBlockCoord(n);
            u = SectionPos.sectionToBlockCoord(l + 1) - 1;
            v = levelHeightAccessor.getMaxBuildHeight() - 1;
            w = SectionPos.sectionToBlockCoord(n + 1) - 1;
            stringBuilder.append(String.format(Locale.ROOT, "Section: (at %d,%d,%d in %d,%d,%d; chunk contains blocks %d,%d,%d to %d,%d,%d)", o, p, q, l, m, n, r, s, t, u, v, w));
        } catch (Throwable throwable) {
            stringBuilder.append("(Error finding chunk loc)");
        }
        stringBuilder.append(", ");
        try {
            int l = i >> 9;
            m = k >> 9;
            n = l << 5;
            o = m << 5;
            p = (l + 1 << 5) - 1;
            q = (m + 1 << 5) - 1;
            r = l << 9;
            s = levelHeightAccessor.getMinBuildHeight();
            t = m << 9;
            u = (l + 1 << 9) - 1;
            v = levelHeightAccessor.getMaxBuildHeight() - 1;
            w = (m + 1 << 9) - 1;
            stringBuilder.append(String.format(Locale.ROOT, "Region: (%d,%d; contains chunks %d,%d to %d,%d, blocks %d,%d,%d to %d,%d,%d)", l, m, n, o, p, q, r, s, t, u, v, w));
        } catch (Throwable throwable) {
            stringBuilder.append("(Error finding world loc)");
        }
        return stringBuilder.toString();
    }

    public CrashReportCategory setDetail(String string, CrashReportDetail<String> crashReportDetail) {
        try {
            this.setDetail(string, crashReportDetail.call());
        } catch (Throwable throwable) {
            this.setDetailError(string, throwable);
        }
        return this;
    }

    public CrashReportCategory setDetail(String string, Object object) {
        this.entries.add(new Entry(string, object));
        return this;
    }

    public void setDetailError(String string, Throwable throwable) {
        this.setDetail(string, throwable);
    }

    public int fillInStackTrace(int i) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (stackTraceElements.length <= 0) {
            return 0;
        }
        this.stackTrace = new StackTraceElement[stackTraceElements.length - 3 - i];
        System.arraycopy(stackTraceElements, 3 + i, this.stackTrace, 0, this.stackTrace.length);
        return this.stackTrace.length;
    }

    public boolean validateStackTrace(StackTraceElement stackTraceElement, StackTraceElement stackTraceElement2) {
        if (this.stackTrace.length == 0 || stackTraceElement == null) {
            return false;
        }
        StackTraceElement stackTraceElement3 = this.stackTrace[0];
        if (!(stackTraceElement3.isNativeMethod() == stackTraceElement.isNativeMethod() && stackTraceElement3.getClassName().equals(stackTraceElement.getClassName()) && stackTraceElement3.getFileName().equals(stackTraceElement.getFileName()) && stackTraceElement3.getMethodName().equals(stackTraceElement.getMethodName()))) {
            return false;
        }
        if (stackTraceElement2 != null != this.stackTrace.length > 1) {
            return false;
        }
        if (stackTraceElement2 != null && !this.stackTrace[1].equals(stackTraceElement2)) {
            return false;
        }
        this.stackTrace[0] = stackTraceElement;
        return true;
    }

    public void trimStacktrace(int i) {
        StackTraceElement[] stackTraceElements = new StackTraceElement[this.stackTrace.length - i];
        System.arraycopy(this.stackTrace, 0, stackTraceElements, 0, stackTraceElements.length);
        this.stackTrace = stackTraceElements;
    }

    public void getDetails(StringBuilder stringBuilder) {
        stringBuilder.append("-- ").append(this.title).append(" --\n");
        stringBuilder.append("Details:");
        for (Entry entry : this.entries) {
            stringBuilder.append("\n\t");
            stringBuilder.append(entry.getKey());
            stringBuilder.append(": ");
            stringBuilder.append(entry.getValue());
        }
        if (this.stackTrace != null && this.stackTrace.length > 0) {
            stringBuilder.append("\nStacktrace:");
            for (StackTraceElement stackTraceElement : this.stackTrace) {
                stringBuilder.append("\n\tat ");
                stringBuilder.append(stackTraceElement);
            }
        }
    }

    public StackTraceElement[] getStacktrace() {
        return this.stackTrace;
    }

    public static void populateBlockDetails(CrashReportCategory crashReportCategory, LevelHeightAccessor levelHeightAccessor, BlockPos blockPos, @Nullable BlockState blockState) {
        if (blockState != null) {
            crashReportCategory.setDetail("Block", blockState::toString);
        }
        crashReportCategory.setDetail("Block location", () -> CrashReportCategory.formatLocation(levelHeightAccessor, blockPos));
    }

    static class Entry {
        private final String key;
        private final String value;

        public Entry(String string, @Nullable Object object) {
            this.key = string;
            if (object == null) {
                this.value = "~~NULL~~";
            } else if (object instanceof Throwable) {
                Throwable throwable = (Throwable)object;
                this.value = "~~ERROR~~ " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
            } else {
                this.value = object.toString();
            }
        }

        public String getKey() {
            return this.key;
        }

        public String getValue() {
            return this.value;
        }
    }
}

