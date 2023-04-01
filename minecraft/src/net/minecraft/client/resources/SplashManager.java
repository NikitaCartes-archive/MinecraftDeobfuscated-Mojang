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
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;

@Environment(EnvType.CLIENT)
public class SplashManager extends SimplePreparableReloadListener<List<String>> {
	private static final ResourceLocation SPLASHES_LOCATION = new ResourceLocation("texts/splashes.txt");
	private static final RandomSource RANDOM = RandomSource.create();
	private final List<String> splashes = Lists.<String>newArrayList();
	private final User user;

	public SplashManager(User user) {
		this.user = user;
	}

	protected List<String> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
		try {
			BufferedReader bufferedReader = Minecraft.getInstance().getResourceManager().openAsReader(SPLASHES_LOCATION);

			List var6;
			try {
				Stream<String> stream = bufferedReader.lines().map(String::trim).filter(string -> string.hashCode() != 125780783);
				Stream<String> stream2 = Stream.of("Exactly " + BuiltInRegistries.RULE.size() + " rules to vote on!");
				var6 = (List)Stream.concat(stream, stream2).collect(Collectors.toList());
			} catch (Throwable var8) {
				if (bufferedReader != null) {
					try {
						bufferedReader.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}
				}

				throw var8;
			}

			if (bufferedReader != null) {
				bufferedReader.close();
			}

			return var6;
		} catch (IOException var9) {
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
