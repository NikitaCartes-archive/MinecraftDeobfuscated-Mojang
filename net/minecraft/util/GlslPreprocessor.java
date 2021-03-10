/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class GlslPreprocessor {
    private static final Pattern REGEX_MOJ_IMPORT = Pattern.compile("(?:/\\*(?:[^*]|\\*+[^/])*\\*+/|\\h)*(#(?:/\\*(?:[^*]|\\*+[^/])*\\*+/|\\h)*moj_import(?:/\\*(?:[^*]|\\*+[^/])*\\*+/|\\h)*(?:\"(.*)\"|<(.*)>))");
    private static final Pattern REGEX_VERSION = Pattern.compile("(?:/\\*(?:[^*]|\\*+[^/])*\\*+/|\\h)*(#(?:/\\*(?:[^*]|\\*+[^/])*\\*+/|\\h)*version(?:/\\*(?:[^*]|\\*+[^/])*\\*+/|\\h)*(\\d+))\\b");

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
            string4 = matcher.group(2);
            boolean bl2 = bl = string4 != null;
            if (!bl) {
                string4 = matcher.group(3);
            }
            if (string4 == null) continue;
            String string5 = string.substring(j, matcher.start(1));
            String string6 = string2 + string4;
            String string7 = this.applyImport(bl, string6);
            if (!Strings.isEmpty(string7)) {
                context.sourceId = context.sourceId + 1;
                k = context.sourceId;
                List<String> list2 = this.processImports(string7, context, bl ? FileUtil.getFullResourcePath(string6) : "");
                list2.set(0, String.format("#line %d %d\n%s", 0, k, this.processVersions(list2.get(0), context)));
                if (!StringUtils.isBlank(string5)) {
                    list.add(string5);
                }
                list.addAll(list2);
            } else {
                String string8 = bl ? String.format("/*#moj_import \"%s\"*/", string4) : String.format("/*#moj_import <%s>*/", string4);
                list.add(string3 + string5 + string8);
            }
            k = StringUtil.lineCount(string.substring(0, matcher.end(1)));
            string3 = String.format("#line %d %d", k, i);
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
        if (matcher.find()) {
            context.glslVersion = Math.max(context.glslVersion, Integer.parseInt(matcher.group(2)));
            return string.substring(0, matcher.start(1)) + "/*" + string.substring(matcher.start(1), matcher.end(1)) + "*/" + string.substring(matcher.end(1));
        }
        return string;
    }

    private String setVersion(String string, int i) {
        Matcher matcher = REGEX_VERSION.matcher(string);
        if (matcher.find()) {
            return string.substring(0, matcher.start(2)) + Math.max(i, Integer.parseInt(matcher.group(2))) + string.substring(matcher.end(2));
        }
        return string;
    }

    @Nullable
    public abstract String applyImport(boolean var1, String var2);

    @Environment(value=EnvType.CLIENT)
    static final class Context {
        private int glslVersion;
        private int sourceId;

        private Context() {
        }
    }
}

