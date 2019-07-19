/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import net.minecraft.CrashReport;

public class ReportedException
extends RuntimeException {
    private final CrashReport report;

    public ReportedException(CrashReport crashReport) {
        this.report = crashReport;
    }

    public CrashReport getReport() {
        return this.report;
    }

    @Override
    public Throwable getCause() {
        return this.report.getException();
    }

    @Override
    public String getMessage() {
        return this.report.getTitle();
    }
}

