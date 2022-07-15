/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.Version;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.CrashReportCategory;
import org.slf4j.Logger;

public class NativeModuleLister {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LANG_MASK = 65535;
    private static final int DEFAULT_LANG = 1033;
    private static final int CODEPAGE_MASK = -65536;
    private static final int DEFAULT_CODEPAGE = 0x4B00000;

    public static List<NativeModuleInfo> listModules() {
        if (!Platform.isWindows()) {
            return ImmutableList.of();
        }
        int i = Kernel32.INSTANCE.GetCurrentProcessId();
        ImmutableList.Builder builder = ImmutableList.builder();
        List<Tlhelp32.MODULEENTRY32W> list = Kernel32Util.getModules(i);
        for (Tlhelp32.MODULEENTRY32W mODULEENTRY32W : list) {
            String string = mODULEENTRY32W.szModule();
            Optional<NativeModuleVersion> optional = NativeModuleLister.tryGetVersion(mODULEENTRY32W.szExePath());
            builder.add(new NativeModuleInfo(string, optional));
        }
        return builder.build();
    }

    private static Optional<NativeModuleVersion> tryGetVersion(String string) {
        try {
            IntByReference intByReference = new IntByReference();
            int i = Version.INSTANCE.GetFileVersionInfoSize(string, intByReference);
            if (i == 0) {
                int j = Native.getLastError();
                if (j == 1813 || j == 1812) {
                    return Optional.empty();
                }
                throw new Win32Exception(j);
            }
            Memory pointer = new Memory(i);
            if (!Version.INSTANCE.GetFileVersionInfo(string, 0, i, pointer)) {
                throw new Win32Exception(Native.getLastError());
            }
            IntByReference intByReference2 = new IntByReference();
            Pointer pointer2 = NativeModuleLister.queryVersionValue(pointer, "\\VarFileInfo\\Translation", intByReference2);
            int[] is = pointer2.getIntArray(0L, intByReference2.getValue() / 4);
            OptionalInt optionalInt = NativeModuleLister.findLangAndCodepage(is);
            if (!optionalInt.isPresent()) {
                return Optional.empty();
            }
            int k = optionalInt.getAsInt();
            int l = k & 0xFFFF;
            int m = (k & 0xFFFF0000) >> 16;
            String string2 = NativeModuleLister.queryVersionString(pointer, NativeModuleLister.langTableKey("FileDescription", l, m), intByReference2);
            String string3 = NativeModuleLister.queryVersionString(pointer, NativeModuleLister.langTableKey("CompanyName", l, m), intByReference2);
            String string4 = NativeModuleLister.queryVersionString(pointer, NativeModuleLister.langTableKey("FileVersion", l, m), intByReference2);
            return Optional.of(new NativeModuleVersion(string2, string4, string3));
        } catch (Exception exception) {
            LOGGER.info("Failed to find module info for {}", (Object)string, (Object)exception);
            return Optional.empty();
        }
    }

    private static String langTableKey(String string, int i, int j) {
        return String.format(Locale.ROOT, "\\StringFileInfo\\%04x%04x\\%s", i, j, string);
    }

    private static OptionalInt findLangAndCodepage(int[] is) {
        OptionalInt optionalInt = OptionalInt.empty();
        for (int i : is) {
            if ((i & 0xFFFF0000) == 0x4B00000 && (i & 0xFFFF) == 1033) {
                return OptionalInt.of(i);
            }
            optionalInt = OptionalInt.of(i);
        }
        return optionalInt;
    }

    private static Pointer queryVersionValue(Pointer pointer, String string, IntByReference intByReference) {
        PointerByReference pointerByReference = new PointerByReference();
        if (!Version.INSTANCE.VerQueryValue(pointer, string, pointerByReference, intByReference)) {
            throw new UnsupportedOperationException("Can't get version value " + string);
        }
        return pointerByReference.getValue();
    }

    private static String queryVersionString(Pointer pointer, String string, IntByReference intByReference) {
        try {
            Pointer pointer2 = NativeModuleLister.queryVersionValue(pointer, string, intByReference);
            byte[] bs = pointer2.getByteArray(0L, (intByReference.getValue() - 1) * 2);
            return new String(bs, StandardCharsets.UTF_16LE);
        } catch (Exception exception) {
            return "";
        }
    }

    public static void addCrashSection(CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Modules", () -> NativeModuleLister.listModules().stream().sorted(Comparator.comparing(nativeModuleInfo -> nativeModuleInfo.name)).map(nativeModuleInfo -> "\n\t\t" + nativeModuleInfo).collect(Collectors.joining()));
    }

    public static class NativeModuleInfo {
        public final String name;
        public final Optional<NativeModuleVersion> version;

        public NativeModuleInfo(String string, Optional<NativeModuleVersion> optional) {
            this.name = string;
            this.version = optional;
        }

        public String toString() {
            return this.version.map(nativeModuleVersion -> this.name + ":" + nativeModuleVersion).orElse(this.name);
        }
    }

    public static class NativeModuleVersion {
        public final String description;
        public final String version;
        public final String company;

        public NativeModuleVersion(String string, String string2, String string3) {
            this.description = string;
            this.version = string2;
            this.company = string3;
        }

        public String toString() {
            return this.description + ":" + this.version + ":" + this.company;
        }
    }
}

