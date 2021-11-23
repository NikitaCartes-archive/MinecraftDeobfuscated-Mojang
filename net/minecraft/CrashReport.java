/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionException;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.util.MemoryReserve;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrashReport {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String title;
    private final Throwable exception;
    private final List<CrashReportCategory> details = Lists.newArrayList();
    private File saveFile;
    private boolean trackingStackTrace = true;
    private StackTraceElement[] uncategorizedStackTrace = new StackTraceElement[0];
    private final SystemReport systemReport = new SystemReport();

    public CrashReport(String string, Throwable throwable) {
        this.title = string;
        this.exception = throwable;
    }

    public String getTitle() {
        return this.title;
    }

    public Throwable getException() {
        return this.exception;
    }

    public String getDetails() {
        StringBuilder stringBuilder = new StringBuilder();
        this.getDetails(stringBuilder);
        return stringBuilder.toString();
    }

    public void getDetails(StringBuilder stringBuilder) {
        if (!(this.uncategorizedStackTrace != null && this.uncategorizedStackTrace.length > 0 || this.details.isEmpty())) {
            this.uncategorizedStackTrace = ArrayUtils.subarray(this.details.get(0).getStacktrace(), 0, 1);
        }
        if (this.uncategorizedStackTrace != null && this.uncategorizedStackTrace.length > 0) {
            stringBuilder.append("-- Head --\n");
            stringBuilder.append("Thread: ").append(Thread.currentThread().getName()).append("\n");
            stringBuilder.append("Stacktrace:\n");
            for (StackTraceElement stackTraceElement : this.uncategorizedStackTrace) {
                stringBuilder.append("\t").append("at ").append(stackTraceElement);
                stringBuilder.append("\n");
            }
            stringBuilder.append("\n");
        }
        for (CrashReportCategory crashReportCategory : this.details) {
            crashReportCategory.getDetails(stringBuilder);
            stringBuilder.append("\n\n");
        }
        this.systemReport.appendToCrashReportString(stringBuilder);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String getExceptionMessage() {
        String string;
        StringWriter stringWriter = null;
        PrintWriter printWriter = null;
        Throwable throwable = this.exception;
        if (throwable.getMessage() == null) {
            if (throwable instanceof NullPointerException) {
                throwable = new NullPointerException(this.title);
            } else if (throwable instanceof StackOverflowError) {
                throwable = new StackOverflowError(this.title);
            } else if (throwable instanceof OutOfMemoryError) {
                throwable = new OutOfMemoryError(this.title);
            }
            throwable.setStackTrace(this.exception.getStackTrace());
        }
        try {
            stringWriter = new StringWriter();
            printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            string = stringWriter.toString();
        } catch (Throwable throwable2) {
            IOUtils.closeQuietly(stringWriter);
            IOUtils.closeQuietly(printWriter);
            throw throwable2;
        }
        IOUtils.closeQuietly(stringWriter);
        IOUtils.closeQuietly(printWriter);
        return string;
    }

    public String getFriendlyReport() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("---- Minecraft Crash Report ----\n");
        stringBuilder.append("// ");
        stringBuilder.append(CrashReport.getErrorComment());
        stringBuilder.append("\n\n");
        stringBuilder.append("Time: ");
        stringBuilder.append(new SimpleDateFormat().format(new Date()));
        stringBuilder.append("\n");
        stringBuilder.append("Description: ");
        stringBuilder.append(this.title);
        stringBuilder.append("\n\n");
        stringBuilder.append(this.getExceptionMessage());
        stringBuilder.append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");
        for (int i = 0; i < 87; ++i) {
            stringBuilder.append("-");
        }
        stringBuilder.append("\n\n");
        this.getDetails(stringBuilder);
        return stringBuilder.toString();
    }

    public File getSaveFile() {
        return this.saveFile;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean saveToFile(File file) {
        boolean bl;
        if (this.saveFile != null) {
            return false;
        }
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter((OutputStream)new FileOutputStream(file), StandardCharsets.UTF_8);
            writer.write(this.getFriendlyReport());
            this.saveFile = file;
            bl = true;
        } catch (Throwable throwable) {
            boolean bl2;
            try {
                LOGGER.error("Could not save crash report to {}", (Object)file, (Object)throwable);
                bl2 = false;
            } catch (Throwable throwable2) {
                IOUtils.closeQuietly(writer);
                throw throwable2;
            }
            IOUtils.closeQuietly(writer);
            return bl2;
        }
        IOUtils.closeQuietly(writer);
        return bl;
    }

    public SystemReport getSystemReport() {
        return this.systemReport;
    }

    public CrashReportCategory addCategory(String string) {
        return this.addCategory(string, 1);
    }

    public CrashReportCategory addCategory(String string, int i) {
        CrashReportCategory crashReportCategory = new CrashReportCategory(string);
        if (this.trackingStackTrace) {
            int j = crashReportCategory.fillInStackTrace(i);
            StackTraceElement[] stackTraceElements = this.exception.getStackTrace();
            StackTraceElement stackTraceElement = null;
            StackTraceElement stackTraceElement2 = null;
            int k = stackTraceElements.length - j;
            if (k < 0) {
                System.out.println("Negative index in crash report handler (" + stackTraceElements.length + "/" + j + ")");
            }
            if (stackTraceElements != null && 0 <= k && k < stackTraceElements.length) {
                stackTraceElement = stackTraceElements[k];
                if (stackTraceElements.length + 1 - j < stackTraceElements.length) {
                    stackTraceElement2 = stackTraceElements[stackTraceElements.length + 1 - j];
                }
            }
            this.trackingStackTrace = crashReportCategory.validateStackTrace(stackTraceElement, stackTraceElement2);
            if (stackTraceElements != null && stackTraceElements.length >= j && 0 <= k && k < stackTraceElements.length) {
                this.uncategorizedStackTrace = new StackTraceElement[k];
                System.arraycopy(stackTraceElements, 0, this.uncategorizedStackTrace, 0, this.uncategorizedStackTrace.length);
            } else {
                this.trackingStackTrace = false;
            }
        }
        this.details.add(crashReportCategory);
        return crashReportCategory;
    }

    private static String getErrorComment() {
        String[] strings = new String[]{"Who set us up the TNT?", "Everything's going to plan. No, really, that was supposed to happen.", "Uh... Did I do that?", "Oops.", "Why did you do that?", "I feel sad now :(", "My bad.", "I'm sorry, Dave.", "I let you down. Sorry :(", "On the bright side, I bought you a teddy bear!", "Daisy, daisy...", "Oh - I know what I did wrong!", "Hey, that tickles! Hehehe!", "I blame Dinnerbone.", "You should try our sister game, Minceraft!", "Don't be sad. I'll do better next time, I promise!", "Don't be sad, have a hug! <3", "I just don't know what went wrong :(", "Shall we play a game?", "Quite honestly, I wouldn't worry myself about that.", "I bet Cylons wouldn't have this problem.", "Sorry :(", "Surprise! Haha. Well, this is awkward.", "Would you like a cupcake?", "Hi. I'm Minecraft, and I'm a crashaholic.", "Ooh. Shiny.", "This doesn't make any sense!", "Why is it breaking :(", "Don't do that.", "Ouch. That hurt :(", "You're mean.", "This is a token for 1 free hug. Redeem at your nearest Mojangsta: [~~HUG~~]", "There are four lights!", "But it works on my machine."};
        try {
            return strings[(int)(Util.getNanos() % (long)strings.length)];
        } catch (Throwable throwable) {
            return "Witty comment unavailable :(";
        }
    }

    public static CrashReport forThrowable(Throwable throwable, String string) {
        while (throwable instanceof CompletionException && throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        CrashReport crashReport = throwable instanceof ReportedException ? ((ReportedException)throwable).getReport() : new CrashReport(string, throwable);
        return crashReport;
    }

    public static void preload() {
        MemoryReserve.allocate();
        new CrashReport("Don't panic!", new Throwable()).getFriendlyReport();
    }
}

