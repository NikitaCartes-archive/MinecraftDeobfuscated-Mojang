package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;

public class Pack {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final PackLocationInfo location;
	private final Pack.ResourcesSupplier resources;
	private final Pack.Metadata metadata;
	private final PackSelectionConfig selectionConfig;

	@Nullable
	public static Pack readMetaAndCreate(
		PackLocationInfo packLocationInfo, Pack.ResourcesSupplier resourcesSupplier, PackType packType, PackSelectionConfig packSelectionConfig
	) {
		int i = SharedConstants.getCurrentVersion().getPackVersion(packType);
		Pack.Metadata metadata = readPackMetadata(packLocationInfo, resourcesSupplier, i);
		return metadata != null ? new Pack(packLocationInfo, resourcesSupplier, metadata, packSelectionConfig) : null;
	}

	public Pack(PackLocationInfo packLocationInfo, Pack.ResourcesSupplier resourcesSupplier, Pack.Metadata metadata, PackSelectionConfig packSelectionConfig) {
		this.location = packLocationInfo;
		this.resources = resourcesSupplier;
		this.metadata = metadata;
		this.selectionConfig = packSelectionConfig;
	}

	@Nullable
	public static Pack.Metadata readPackMetadata(PackLocationInfo packLocationInfo, Pack.ResourcesSupplier resourcesSupplier, int i) {
		try {
			Pack.Metadata var11;
			try (PackResources packResources = resourcesSupplier.openPrimary(packLocationInfo)) {
				PackMetadataSection packMetadataSection = packResources.getMetadataSection(PackMetadataSection.TYPE);
				if (packMetadataSection == null) {
					LOGGER.warn("Missing metadata in pack {}", packLocationInfo.id());
					return null;
				}

				FeatureFlagsMetadataSection featureFlagsMetadataSection = packResources.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
				FeatureFlagSet featureFlagSet = featureFlagsMetadataSection != null ? featureFlagsMetadataSection.flags() : FeatureFlagSet.of();
				InclusiveRange<Integer> inclusiveRange = getDeclaredPackVersions(packLocationInfo.id(), packMetadataSection);
				PackCompatibility packCompatibility = PackCompatibility.forVersion(inclusiveRange, i);
				OverlayMetadataSection overlayMetadataSection = packResources.getMetadataSection(OverlayMetadataSection.TYPE);
				List<String> list = overlayMetadataSection != null ? overlayMetadataSection.overlaysForVersion(i) : List.of();
				var11 = new Pack.Metadata(packMetadataSection.description(), packCompatibility, featureFlagSet, list);
			}

			return var11;
		} catch (Exception var14) {
			LOGGER.warn("Failed to read pack {} metadata", packLocationInfo.id(), var14);
			return null;
		}
	}

	private static InclusiveRange<Integer> getDeclaredPackVersions(String string, PackMetadataSection packMetadataSection) {
		int i = packMetadataSection.packFormat();
		if (packMetadataSection.supportedFormats().isEmpty()) {
			return new InclusiveRange(i);
		} else {
			InclusiveRange<Integer> inclusiveRange = (InclusiveRange<Integer>)packMetadataSection.supportedFormats().get();
			if (!inclusiveRange.isValueInRange(i)) {
				LOGGER.warn("Pack {} declared support for versions {} but declared main format is {}, defaulting to {}", string, inclusiveRange, i, i);
				return new InclusiveRange(i);
			} else {
				return inclusiveRange;
			}
		}
	}

	public PackLocationInfo location() {
		return this.location;
	}

	public Component getTitle() {
		return this.location.title();
	}

	public Component getDescription() {
		return this.metadata.description();
	}

	public Component getChatLink(boolean bl) {
		return this.location.createChatLink(bl, this.metadata.description);
	}

	public PackCompatibility getCompatibility() {
		return this.metadata.compatibility();
	}

	public FeatureFlagSet getRequestedFeatures() {
		return this.metadata.requestedFeatures();
	}

	public PackResources open() {
		return this.resources.openFull(this.location, this.metadata);
	}

	public String getId() {
		return this.location.id();
	}

	public PackSelectionConfig selectionConfig() {
		return this.selectionConfig;
	}

	public boolean isRequired() {
		return this.selectionConfig.required();
	}

	public boolean isFixedPosition() {
		return this.selectionConfig.fixedPosition();
	}

	public Pack.Position getDefaultPosition() {
		return this.selectionConfig.defaultPosition();
	}

	public PackSource getPackSource() {
		return this.location.source();
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof Pack pack) ? false : this.location.equals(pack.location);
		}
	}

	public int hashCode() {
		return this.location.hashCode();
	}

	public static record Metadata(Component description, PackCompatibility compatibility, FeatureFlagSet requestedFeatures, List<String> overlays) {
	}

	public static enum Position {
		TOP,
		BOTTOM;

		public <T> int insert(List<T> list, T object, Function<T, PackSelectionConfig> function, boolean bl) {
			Pack.Position position = bl ? this.opposite() : this;
			if (position == BOTTOM) {
				int i;
				for (i = 0; i < list.size(); i++) {
					PackSelectionConfig packSelectionConfig = (PackSelectionConfig)function.apply(list.get(i));
					if (!packSelectionConfig.fixedPosition() || packSelectionConfig.defaultPosition() != this) {
						break;
					}
				}

				list.add(i, object);
				return i;
			} else {
				int i;
				for (i = list.size() - 1; i >= 0; i--) {
					PackSelectionConfig packSelectionConfig = (PackSelectionConfig)function.apply(list.get(i));
					if (!packSelectionConfig.fixedPosition() || packSelectionConfig.defaultPosition() != this) {
						break;
					}
				}

				list.add(i + 1, object);
				return i + 1;
			}
		}

		public Pack.Position opposite() {
			return this == TOP ? BOTTOM : TOP;
		}
	}

	public interface ResourcesSupplier {
		PackResources openPrimary(PackLocationInfo packLocationInfo);

		PackResources openFull(PackLocationInfo packLocationInfo, Pack.Metadata metadata);
	}
}
