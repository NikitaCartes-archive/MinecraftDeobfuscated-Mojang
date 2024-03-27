package net.minecraft.server.packs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.InclusiveRange;

public record OverlayMetadataSection(List<OverlayMetadataSection.OverlayEntry> overlays) {
	private static final Pattern DIR_VALIDATOR = Pattern.compile("[-_a-zA-Z0-9.]+");
	private static final Codec<OverlayMetadataSection> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(OverlayMetadataSection.OverlayEntry.CODEC.listOf().fieldOf("entries").forGetter(OverlayMetadataSection::overlays))
				.apply(instance, OverlayMetadataSection::new)
	);
	public static final MetadataSectionType<OverlayMetadataSection> TYPE = MetadataSectionType.fromCodec("overlays", CODEC);

	private static DataResult<String> validateOverlayDir(String string) {
		return !DIR_VALIDATOR.matcher(string).matches() ? DataResult.error(() -> string + " is not accepted directory name") : DataResult.success(string);
	}

	public List<String> overlaysForVersion(int i) {
		return this.overlays.stream().filter(overlayEntry -> overlayEntry.isApplicable(i)).map(OverlayMetadataSection.OverlayEntry::overlay).toList();
	}

	public static record OverlayEntry(InclusiveRange<Integer> format, String overlay) {
		static final Codec<OverlayMetadataSection.OverlayEntry> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						InclusiveRange.codec(Codec.INT).fieldOf("formats").forGetter(OverlayMetadataSection.OverlayEntry::format),
						Codec.STRING.validate(OverlayMetadataSection::validateOverlayDir).fieldOf("directory").forGetter(OverlayMetadataSection.OverlayEntry::overlay)
					)
					.apply(instance, OverlayMetadataSection.OverlayEntry::new)
		);

		public boolean isApplicable(int i) {
			return this.format.isValueInRange(i);
		}
	}
}
