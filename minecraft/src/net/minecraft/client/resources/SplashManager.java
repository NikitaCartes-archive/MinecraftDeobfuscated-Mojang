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
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(EnvType.CLIENT)
public class SplashManager extends SimplePreparableReloadListener<List<String>> {
	private static final ResourceLocation SPLASHES_LOCATION = ResourceLocation.withDefaultNamespace("texts/splashes.txt");
	private static final RandomSource RANDOM = RandomSource.create();
	private final List<String> splashes = Lists.<String>newArrayList();
	private final User user;

	public SplashManager(User user) {
		this.user = user;
	}

	protected List<String> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		try {
			BufferedReader bufferedReader = Minecraft.getInstance().getResourceManager().openAsReader(SPLASHES_LOCATION);

			List var4;
			try {
				var4 = (List)bufferedReader.lines().map(String::trim).filter(string -> string.hashCode() != 125780783).collect(Collectors.toList());
			} catch (Throwable var7) {
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (Throwable var6) {
						var7.addSuppressed(var6);
					}
				}

				throw var7;
			}

			if (bufferedReader != null) {
				bufferedReader.close();
			}

			return var4;
		} catch (IOException var8) {
			return Collections.emptyList();
		}
	}

	protected void apply(List<String> list, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		this.splashes.clear();
		this.splashes.addAll(list);
	}

	@Nullable
	public SplashRenderer getSplash() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24) {
			return SplashRenderer.CHRISTMAS;
		} else if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1) {
			return SplashRenderer.NEW_YEAR;
		} else if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31) {
			return SplashRenderer.HALLOWEEN;
		} else if (this.splashes.isEmpty()) {
			return null;
		} else {
			return this.user != null && RANDOM.nextInt(this.splashes.size()) == 42
				? new SplashRenderer(this.user.getName().toUpperCase(Locale.ROOT) + " IS YOU")
				: new SplashRenderer((String)this.splashes.get(RANDOM.nextInt(this.splashes.size())));
		}
	}
}
