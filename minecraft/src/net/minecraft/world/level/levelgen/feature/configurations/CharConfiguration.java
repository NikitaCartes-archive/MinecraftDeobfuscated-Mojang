package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.math.OctahedralGroup;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class CharConfiguration implements FeatureConfiguration {
	public final BlockStateProvider material;
	public final char ch;
	public final OctahedralGroup orientation;
	private static final Char2ObjectMap<byte[]> CONTENTS = createContents();
	private static final byte[] REPLACEMENT = CONTENTS.get('�');

	public static Char2ObjectMap<byte[]> createContents() {
		Char2ObjectOpenHashMap<byte[]> char2ObjectOpenHashMap = new Char2ObjectOpenHashMap<>();

		try {
			InputStream inputStream = CharConfiguration.class.getResourceAsStream("/chars.bin");
			Throwable var2 = null;

			try {
				DataInputStream dataInputStream = new DataInputStream(inputStream);

				while (true) {
					char c = dataInputStream.readChar();
					byte[] bs = new byte[8];
					if (dataInputStream.read(bs) != 8) {
						break;
					}

					char2ObjectOpenHashMap.put(c, bs);
				}
			} catch (Throwable var15) {
				var2 = var15;
				throw var15;
			} finally {
				if (inputStream != null) {
					if (var2 != null) {
						try {
							inputStream.close();
						} catch (Throwable var14) {
							var2.addSuppressed(var14);
						}
					} else {
						inputStream.close();
					}
				}
			}
		} catch (EOFException var17) {
		} catch (IOException var18) {
		}

		return char2ObjectOpenHashMap;
	}

	@Nullable
	public byte[] getBytes() {
		return CONTENTS.getOrDefault(this.ch, REPLACEMENT);
	}

	public CharConfiguration(BlockStateProvider blockStateProvider, char c, OctahedralGroup octahedralGroup) {
		this.material = blockStateProvider;
		this.ch = c;
		this.orientation = octahedralGroup;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("material"),
					this.material.serialize(dynamicOps),
					dynamicOps.createString("char"),
					dynamicOps.createInt(this.ch),
					dynamicOps.createString("orientation"),
					dynamicOps.createString(this.orientation.getSerializedName())
				)
			)
		);
	}

	public static <T> CharConfiguration deserialize(Dynamic<T> dynamic) {
		BlockStateProvider blockStateProvider = (BlockStateProvider)dynamic.get("material")
			.map(
				dynamicx -> {
					ResourceLocation resourceLocation = (ResourceLocation)dynamicx.get("type").asString().map(ResourceLocation::new).get();
					BlockStateProviderType<?> blockStateProviderType = (BlockStateProviderType<?>)Registry.BLOCKSTATE_PROVIDER_TYPES
						.getOptional(resourceLocation)
						.orElseThrow(() -> new IllegalStateException(resourceLocation.toString()));
					return blockStateProviderType.deserialize(dynamicx);
				}
			)
			.orElseThrow(IllegalStateException::new);
		char c = (char)dynamic.asInt(66);
		OctahedralGroup octahedralGroup = (OctahedralGroup)dynamic.get("orientation").asString().flatMap(OctahedralGroup::byName).orElse(OctahedralGroup.IDENTITY);
		return new CharConfiguration(blockStateProvider, c, octahedralGroup);
	}

	public static CharConfiguration random(Random random) {
		BlockStateProvider blockStateProvider = BlockStateProvider.random(random);
		char c = Util.<Character>randomObject(random, ImmutableList.copyOf(CONTENTS.keySet()));
		OctahedralGroup octahedralGroup = Util.randomObject(random, OctahedralGroup.values());
		return new CharConfiguration(blockStateProvider, c, octahedralGroup);
	}
}
