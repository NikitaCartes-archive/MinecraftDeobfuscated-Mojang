/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.preprocessor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class GlslPreprocessor {
    private static final String C_COMMENT = "/\\*(?:[^*]|\\*+[^*/])*\\*+/";
    private static final String LINE_COMMENT = "//[^\\v]*";
    private static final Pattern REGEX_MOJ_IMPORT = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*moj_import(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(?:\"(.*)\"|<(.*)>))");
    private static final Pattern REGEX_VERSION = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*version(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(\\d+))\\b");
    private static final Pattern REGEX_ENDS_WITH_WHITESPACE = Pattern.compile("(?:^|\\v)(?:\\s|/\\*(?:[^*]|\\*+[^*/])*\\*+/|(//[^\\v]*))*\\z");

    public List<String> process(String string) {
        Context context = new Context();
        List<String> list = this.processImports(string, context, "");
        list.set(0, this.setVersion(list.get(0), context.glslVersion));
        return list;
    }

    private List<String> processImports(String string, Context context, String string2) {
        String string4;
        int i = context.sourceId;
        int j = 0;
        String string3 = "";
        ArrayList<String> list = Lists.newArrayList();
        Matcher matcher = REGEX_MOJ_IMPORT.matcher(string);
        while (matcher.find()) {
            int k;
            boolean bl;
            if (GlslPreprocessor.isDirectiveDisabled(string, matcher, j)) continue;
            string4 = matcher.group(2);
            boolean bl2 = bl = string4 != null;
            if (!bl) {
                string4 = matcher.group(3);
            }
            if (string4 == null) continue;
            String string5 = string.substring(j, matcher.start(1));
            String string6 = string2 + string4;
            Object string7 = this.applyImport(bl, string6);
            if (!Strings.isNullOrEmpty((String)string7)) {
                if (!StringUtil.endsWithNewLine((String)string7)) {
                    string7 = (String)string7 + System.lineSeparator();
                }
                ++context.sourceId;
                k = context.sourceId;
                List<String> list2 = this.processImports((String)string7, context, bl ? FileUtil.getFullResourcePath(string6) : "");
                list2.set(0, String.format(Locale.ROOT, "#line %d %d\n%s", 0, k, this.processVersions(list2.get(0), context)));
                if (!StringUtils.isBlank(string5)) {
                    list.add(string5);
                }
                list.addAll(list2);
            } else {
                String string8 = bl ? String.format(Locale.ROOT, "/*#moj_import \"%s\"*/", string4) : String.format(Locale.ROOT, "/*#moj_import <%s>*/", string4);
                list.add(string3 + string5 + string8);
            }
            k = StringUtil.lineCount(string.substring(0, matcher.end(1)));
            string3 = String.format(Locale.ROOT, "#line %d %d", k, i);
            j = matcher.end(1);
        }
        string4 = string.substring(j);
        if (!StringUtils.isBlank(string4)) {
            list.add(string3 + string4);
        }
        return list;
    }

    private String processVersions(String string, Context context) {
        Matcher matcher = REGEX_VERSION.matcher(string);
        if (matcher.find() && GlslPreprocessor.isDirectiveEnabled(string, matcher)) {
            context.glslVersion = Math.max(context.glslVersion, Integer.parseInt(matcher.group(2)));
            return string.substring(0, matcher.start(1)) + "/*" + string.substring(matcher.start(1), matcher.end(1)) + "*/" + string.substring(matcher.end(1));
        }
        return string;
    }

    private String setVersion(String string, int i) {
        Matcher matcher = REGEX_VERSION.matcher(string);
        if (matcher.find() && GlslPreprocessor.isDirectiveEnabled(string, matcher)) {
            return string.substring(0, matcher.start(2)) + Math.max(i, Integer.parseInt(matcher.group(2))) + string.substring(matcher.end(2));
        }
        return string;
    }

    private static boolean isDirectiveEnabled(String string, Matcher matcher) {
        return !GlslPreprocessor.isDirectiveDisabled(string, matcher, 0);
    }

    private static boolean isDirectiveDisabled(String string, Matcher matcher, int i) {
        int j = matcher.start() - i;
        if (j == 0) {
            return false;
        }
        Matcher matcher2 = REGEX_ENDS_WITH_WHITESPACE.matcher(string.substring(i, matcher.start()));
        if (!matcher2.find()) {
            return true;
        }
        int k = matcher2.end(1);
        return k == matcher.start();
    }

    @Nullable
    public abstract String applyImport(boolean var1, String var2);

    @Environment(value=EnvType.CLIENT)
    static final class Context {
        int glslVersion;
        int sourceId;

        Context() {
        }
    }
}

