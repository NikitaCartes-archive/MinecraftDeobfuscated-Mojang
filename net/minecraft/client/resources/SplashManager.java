/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SplashManager
extends SimplePreparableReloadListener<List<String>> {
    private static final ResourceLocation SPLASHES_LOCATION = new ResourceLocation("texts/splashes.txt");
    private static final RandomSource RANDOM = RandomSource.create();
    private final List<String> splashes = Lists.newArrayList();
    private final User user;

    public SplashManager(User user) {
        this.user = user;
    }

    @Override
    protected List<String> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        List<String> list;
        block8: {
            BufferedReader bufferedReader = Minecraft.getInstance().getResourceManager().openAsReader(SPLASHES_LOCATION);
            try {
                list = bufferedReader.lines().map(String::trim).filter(string -> string.hashCode() != 125780783).collect(Collectors.toList());
                if (bufferedReader == null) break block8;
            } catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (IOException iOException) {
                    return Collections.emptyList();
                }
            }
            bufferedReader.close();
        }
        return list;
    }

    @Override
    protected void apply(List<String> list, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.splashes.clear();
        this.splashes.addAll(list);
    }

    @Nullable
    public String getSplash() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24) {
            return "Merry X-mas!";
        }
        if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1) {
            return "Happy new year!";
        }
        if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31) {
            return "OOoooOOOoooo! Spooky!";
        }
        if (this.splashes.isEmpty()) {
            return null;
        }
        if (this.user != null && RANDOM.nextInt(this.splashes.size()) == 42) {
            return this.user.getName().toUpperCase(Locale.ROOT) + " IS YOU";
        }
        return this.splashes.get(RANDOM.nextInt(this.splashes.size()));
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }
}

