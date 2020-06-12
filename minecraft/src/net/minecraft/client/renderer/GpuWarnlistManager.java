package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(EnvType.CLIENT)
public class GpuWarnlistManager extends SimplePreparableReloadListener<GpuWarnlistManager.Preparations> {
	private static final ResourceLocation GPU_WARNLIST_LOCATION = new ResourceLocation("gpu_warnlist.json");
	private ImmutableMap<String, String> warnings = ImmutableMap.of();

	public boolean hasWarnings() {
		return !this.warnings.isEmpty();
	}

	@Nullable
	public String getRendererWarnings() {
		return this.warnings.get("renderer");
	}

	@Nullable
	public String getVersionWarnings() {
		return this.warnings.get("version");
	}

	@Nullable
	public String getVendorWarnings() {
		return this.warnings.get("vendor");
	}

	protected GpuWarnlistManager.Preparations prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		List<Pattern> list = Lists.<Pattern>newArrayList();
		List<Pattern> list2 = Lists.<Pattern>newArrayList();
		List<Pattern> list3 = Lists.<Pattern>newArrayList();
		profilerFiller.startTick();
		JsonObject jsonObject = parseJson(resourceManager, profilerFiller);
		if (jsonObject != null) {
			profilerFiller.push("compile_regex");
			compilePatterns(jsonObject.getAsJsonArray("renderer"), list);
			compilePatterns(jsonObject.getAsJsonArray("version"), list2);
			compilePatterns(jsonObject.getAsJsonArray("vendor"), list3);
			profilerFiller.pop();
		}

		profilerFiller.endTick();
		return new GpuWarnlistManager.Preparations(list, list2, list3);
	}

	protected void apply(GpuWarnlistManager.Preparations preparations, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		this.warnings = preparations.apply();
	}

	private static void compilePatterns(JsonArray jsonArray, List<Pattern> list) {
		jsonArray.forEach(jsonElement -> list.add(Pattern.compile(jsonElement.getAsString(), 2)));
	}

	@Nullable
	private static JsonObject parseJson(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		profilerFiller.push("parse_json");
		JsonObject jsonObject = null;

		try {
			Resource resource = resourceManager.getResource(GPU_WARNLIST_LOCATION);
			Throwable var4 = null;

			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
				Throwable var6 = null;

				try {
					jsonObject = new JsonParser().parse(bufferedReader).getAsJsonObject();
				} catch (Throwable var31) {
					var6 = var31;
					throw var31;
				} finally {
					if (bufferedReader != null) {
						if (var6 != null) {
							try {
								bufferedReader.close();
							} catch (Throwable var30) {
								var6.addSuppressed(var30);
							}
						} else {
							bufferedReader.close();
						}
					}
				}
			} catch (Throwable var33) {
				var4 = var33;
				throw var33;
			} finally {
				if (resource != null) {
					if (var4 != null) {
						try {
							resource.close();
						} catch (Throwable var29) {
							var4.addSuppressed(var29);
						}
					} else {
						resource.close();
					}
				}
			}
		} catch (IOException var35) {
		}

		profilerFiller.pop();
		return jsonObject;
	}

	@Environment(EnvType.CLIENT)
	public static final class Preparations {
		private final List<Pattern> rendererPatterns;
		private final List<Pattern> versionPatterns;
		private final List<Pattern> vendorPatterns;

		private Preparations(List<Pattern> list, List<Pattern> list2, List<Pattern> list3) {
			this.rendererPatterns = list;
			this.versionPatterns = list2;
			this.vendorPatterns = list3;
		}

		private static String matchAny(List<Pattern> list, String string) {
			List<String> list2 = Lists.<String>newArrayList();

			for (Pattern pattern : list) {
				Matcher matcher = pattern.matcher(string);

				while (matcher.find()) {
					list2.add(matcher.group());
				}
			}

			return String.join(", ", list2);
		}

		private ImmutableMap<String, String> apply() {
			Builder<String, String> builder = new Builder<>();
			String string = matchAny(this.rendererPatterns, GlUtil.getRenderer());
			if (!string.isEmpty()) {
				builder.put("renderer", string);
			}

			String string2 = matchAny(this.versionPatterns, GlUtil.getOpenGLVersion());
			if (!string2.isEmpty()) {
				builder.put("version", string2);
			}

			String string3 = matchAny(this.vendorPatterns, GlUtil.getVendor());
			if (!string3.isEmpty()) {
				builder.put("vendor", string3);
			}

			return builder.build();
		}
	}
}
