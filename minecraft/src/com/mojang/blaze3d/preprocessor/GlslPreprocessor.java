package com.mojang.blaze3d.preprocessor;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.FileUtil;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

@Environment(EnvType.CLIENT)
public abstract class GlslPreprocessor {
	private static final String C_COMMENT = "/\\*(?:[^*]|\\*+[^/])*\\*+/";
	private static final Pattern REGEX_MOJ_IMPORT = Pattern.compile(
		"(?:/\\*(?:[^*]|\\*+[^/])*\\*+/|\\h)*(#(?:/\\*(?:[^*]|\\*+[^/])*\\*+/|\\h)*moj_import(?:/\\*(?:[^*]|\\*+[^/])*\\*+/|\\h)*(?:\"(.*)\"|<(.*)>))"
	);
	private static final Pattern REGEX_VERSION = Pattern.compile(
		"(?:/\\*(?:[^*]|\\*+[^/])*\\*+/|\\h)*(#(?:/\\*(?:[^*]|\\*+[^/])*\\*+/|\\h)*version(?:/\\*(?:[^*]|\\*+[^/])*\\*+/|\\h)*(\\d+))\\b"
	);

	public List<String> process(String string) {
		GlslPreprocessor.Context context = new GlslPreprocessor.Context();
		List<String> list = this.processImports(string, context, "");
		list.set(0, this.setVersion((String)list.get(0), context.glslVersion));
		return list;
	}

	private List<String> processImports(String string, GlslPreprocessor.Context context, String string2) {
		int i = context.sourceId;
		int j = 0;
		String string3 = "";
		List<String> list = Lists.<String>newArrayList();
		Matcher matcher = REGEX_MOJ_IMPORT.matcher(string);

		while (matcher.find()) {
			String string4 = matcher.group(2);
			boolean bl = string4 != null;
			if (!bl) {
				string4 = matcher.group(3);
			}

			if (string4 != null) {
				String string5 = string.substring(j, matcher.start(1));
				String string6 = string2 + string4;
				String string7 = this.applyImport(bl, string6);
				if (!Strings.isEmpty(string7)) {
					context.sourceId = context.sourceId + 1;
					int k = context.sourceId;
					List<String> list2 = this.processImports(string7, context, bl ? FileUtil.getFullResourcePath(string6) : "");
					list2.set(0, String.format("#line %d %d\n%s", 0, k, this.processVersions((String)list2.get(0), context)));
					if (!StringUtils.isBlank(string5)) {
						list.add(string5);
					}

					list.addAll(list2);
				} else {
					String string8 = bl ? String.format("/*#moj_import \"%s\"*/", string4) : String.format("/*#moj_import <%s>*/", string4);
					list.add(string3 + string5 + string8);
				}

				int k = StringUtil.lineCount(string.substring(0, matcher.end(1)));
				string3 = String.format("#line %d %d", k, i);
				j = matcher.end(1);
			}
		}

		String string4x = string.substring(j);
		if (!StringUtils.isBlank(string4x)) {
			list.add(string3 + string4x);
		}

		return list;
	}

	private String processVersions(String string, GlslPreprocessor.Context context) {
		Matcher matcher = REGEX_VERSION.matcher(string);
		if (matcher.find()) {
			context.glslVersion = Math.max(context.glslVersion, Integer.parseInt(matcher.group(2)));
			return string.substring(0, matcher.start(1)) + "/*" + string.substring(matcher.start(1), matcher.end(1)) + "*/" + string.substring(matcher.end(1));
		} else {
			return string;
		}
	}

	private String setVersion(String string, int i) {
		Matcher matcher = REGEX_VERSION.matcher(string);
		return matcher.find() ? string.substring(0, matcher.start(2)) + Math.max(i, Integer.parseInt(matcher.group(2))) + string.substring(matcher.end(2)) : string;
	}

	@Nullable
	public abstract String applyImport(boolean bl, String string);

	@Environment(EnvType.CLIENT)
	static final class Context {
		private int glslVersion;
		private int sourceId;

		private Context() {
		}
	}
}
