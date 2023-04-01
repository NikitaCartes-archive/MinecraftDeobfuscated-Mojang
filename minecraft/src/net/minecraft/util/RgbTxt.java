package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public class RgbTxt {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final List<RgbTxt.Entry> COLORS;
	public static final Map<String, RgbTxt.Entry> BY_NAME;
	public static final Codec<RgbTxt.Entry> CODEC;

	static {
		Builder<RgbTxt.Entry> builder = ImmutableList.builder();
		InputStream inputStream = RgbTxt.class.getResourceAsStream("/rgb.txt");
		if (inputStream != null) {
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

				try {
					bufferedReader.lines().forEach(string -> {
						if (!string.startsWith("#") && !string.isBlank()) {
							int i = string.indexOf(9);
							if (i == -1) {
								LOGGER.warn("Weird line: {}", string);
							} else {
								String string2 = string.substring(0, i);
								String string3 = string.substring(i + 1);
								if (!string3.startsWith("#")) {
									LOGGER.warn("Weird line: {}", string);
								}

								int j;
								try {
									j = Integer.parseInt(string3.substring(1, 7), 16);
								} catch (NumberFormatException var7x) {
									LOGGER.warn("Weird line: {}", string);
									return;
								}

								builder.add(new RgbTxt.Entry(string2, j));
							}
						}
					});
				} catch (Throwable var6) {
					try {
						bufferedReader.close();
					} catch (Throwable var5) {
						var6.addSuppressed(var5);
					}

					throw var6;
				}

				bufferedReader.close();
			} catch (Exception var7) {
				LOGGER.warn("Failed to read rgb.txt", (Throwable)var7);
			}
		} else {
			LOGGER.warn("Where rgb.txt?");
		}

		COLORS = builder.build();
		BY_NAME = (Map<String, RgbTxt.Entry>)COLORS.stream().collect(ImmutableMap.toImmutableMap(RgbTxt.Entry::name, entry -> entry));
		CODEC = ExtraCodecs.stringResolverCodec(StringRepresentable::getSerializedName, BY_NAME::get);
	}

	public static record Entry(String name, int rgb) implements StringRepresentable {
		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}
