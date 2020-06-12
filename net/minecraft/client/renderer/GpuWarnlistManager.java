/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GpuWarnlistManager
extends SimplePreparableReloadListener<Preparations> {
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

    @Override
    protected Preparations prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ArrayList<Pattern> list = Lists.newArrayList();
        ArrayList<Pattern> list2 = Lists.newArrayList();
        ArrayList<Pattern> list3 = Lists.newArrayList();
        profilerFiller.startTick();
        JsonObject jsonObject = GpuWarnlistManager.parseJson(resourceManager, profilerFiller);
        if (jsonObject != null) {
            profilerFiller.push("compile_regex");
            GpuWarnlistManager.compilePatterns(jsonObject.getAsJsonArray("renderer"), list);
            GpuWarnlistManager.compilePatterns(jsonObject.getAsJsonArray("version"), list2);
            GpuWarnlistManager.compilePatterns(jsonObject.getAsJsonArray("vendor"), list3);
            profilerFiller.pop();
        }
        profilerFiller.endTick();
        return new Preparations(list, list2, list3);
    }

    @Override
    protected void apply(Preparations preparations, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.warnings = preparations.apply();
    }

    private static void compilePatterns(JsonArray jsonArray, List<Pattern> list) {
        jsonArray.forEach(jsonElement -> list.add(Pattern.compile(jsonElement.getAsString(), 2)));
    }

    @Nullable
    private static JsonObject parseJson(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        profilerFiller.push("parse_json");
        JsonObject jsonObject = null;
        try (Resource resource = resourceManager.getResource(GPU_WARNLIST_LOCATION);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));){
            jsonObject = new JsonParser().parse(bufferedReader).getAsJsonObject();
        } catch (IOException iOException) {
            // empty catch block
        }
        profilerFiller.pop();
        return jsonObject;
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    @Environment(value=EnvType.CLIENT)
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
            ArrayList<String> list2 = Lists.newArrayList();
            for (Pattern pattern : list) {
                Matcher matcher = pattern.matcher(string);
                while (matcher.find()) {
                    list2.add(matcher.group());
                }
            }
            return String.join((CharSequence)", ", list2);
        }

        private ImmutableMap<String, String> apply() {
            String string3;
            String string2;
            ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
            String string = Preparations.matchAny(this.rendererPatterns, GlUtil.getRenderer());
            if (!string.isEmpty()) {
                builder.put("renderer", string);
            }
            if (!(string2 = Preparations.matchAny(this.versionPatterns, GlUtil.getOpenGLVersion())).isEmpty()) {
                builder.put("version", string2);
            }
            if (!(string3 = Preparations.matchAny(this.vendorPatterns, GlUtil.getVendor())).isEmpty()) {
                builder.put("vendor", string3);
            }
            return builder.build();
        }
    }
}

