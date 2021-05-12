package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(EnvType.CLIENT)
public class SplashManager extends SimplePreparableReloadListener<List<String>> {
	private static final ResourceLocation SPLASHES_LOCATION = new ResourceLocation("texts/splashes.txt");
	private static final Random RANDOM = new Random();
	private final List<String> splashes = Lists.<String>newArrayList();
	private final User user;

	public SplashManager(User user) {
		this.user = user;
	}

	protected List<String> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		try {
			Resource resource = Minecraft.getInstance().getResourceManager().getResource(SPLASHES_LOCATION);

			List var5;
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

				try {
					var5 = (List)bufferedReader.lines().map(String::trim).filter(string -> string.hashCode() != 125780783).collect(Collectors.toList());
				} catch (Throwable var9) {
					try {
						bufferedReader.close();
					} catch (Throwable var8) {
						var9.addSuppressed(var8);
					}

					throw var9;
				}

				bufferedReader.close();
			} catch (Throwable var10) {
				if (resource != null) {
					try {
						resource.close();
					} catch (Throwable var7) {
						var10.addSuppressed(var7);
					}
				}

				throw var10;
			}

			if (resource != null) {
				resource.close();
			}

			return var5;
		} catch (IOException var11) {
			return Collections.emptyList();
		}
	}

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
		} else if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1) {
			return "Happy new year!";
		} else if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31) {
			return "OOoooOOOoooo! Spooky!";
		} else if (this.splashes.isEmpty()) {
			return null;
		} else {
			return this.user != null && RANDOM.nextInt(this.splashes.size()) == 42
				? this.user.getName().toUpperCase(Locale.ROOT) + " IS YOU"
				: (String)this.splashes.get(RANDOM.nextInt(this.splashes.size()));
		}
	}
}
