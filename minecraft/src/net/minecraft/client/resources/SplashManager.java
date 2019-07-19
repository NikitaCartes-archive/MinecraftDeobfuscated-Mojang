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
			Throwable var4 = null;

			List var7;
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
				Throwable var6 = null;

				try {
					var7 = (List)bufferedReader.lines().map(String::trim).filter(string -> string.hashCode() != 125780783).collect(Collectors.toList());
				} catch (Throwable var32) {
					var6 = var32;
					throw var32;
				} finally {
					if (bufferedReader != null) {
						if (var6 != null) {
							try {
								bufferedReader.close();
							} catch (Throwable var31) {
								var6.addSuppressed(var31);
							}
						} else {
							bufferedReader.close();
						}
					}
				}
			} catch (Throwable var34) {
				var4 = var34;
				throw var34;
			} finally {
				if (resource != null) {
					if (var4 != null) {
						try {
							resource.close();
						} catch (Throwable var30) {
							var4.addSuppressed(var30);
						}
					} else {
						resource.close();
					}
				}
			}

			return var7;
		} catch (IOException var36) {
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
