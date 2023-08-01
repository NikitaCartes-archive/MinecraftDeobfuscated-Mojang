package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;

public class Pack {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final String id;
	private final Pack.ResourcesSupplier resources;
	private final Component title;
	private final Pack.Info info;
	private final Pack.Position defaultPosition;
	private final boolean required;
	private final boolean fixedPosition;
	private final PackSource packSource;

	@Nullable
	public static Pack readMetaAndCreate(
		String string, Component component, boolean bl, Pack.ResourcesSupplier resourcesSupplier, PackType packType, Pack.Position position, PackSource packSource
	) {
		int i = SharedConstants.getCurrentVersion().getPackVersion(packType);
		Pack.Info info = readPackInfo(string, resourcesSupplier, i);
		return info != null ? create(string, component, bl, resourcesSupplier, info, position, false, packSource) : null;
	}

	public static Pack create(
		String string,
		Component component,
		boolean bl,
		Pack.ResourcesSupplier resourcesSupplier,
		Pack.Info info,
		Pack.Position position,
		boolean bl2,
		PackSource packSource
	) {
		return new Pack(string, bl, resourcesSupplier, component, info, position, bl2, packSource);
	}

	private Pack(
		String string,
		boolean bl,
		Pack.ResourcesSupplier resourcesSupplier,
		Component component,
		Pack.Info info,
		Pack.Position position,
		boolean bl2,
		PackSource packSource
	) {
		this.id = string;
		this.resources = resourcesSupplier;
		this.title = component;
		this.info = info;
		this.required = bl;
		this.defaultPosition = position;
		this.fixedPosition = bl2;
		this.packSource = packSource;
	}

	@Nullable
	public static Pack.Info readPackInfo(String string, Pack.ResourcesSupplier resourcesSupplier, int i) {
		try {
			Pack.Info var11;
			try (PackResources packResources = resourcesSupplier.openPrimary(string)) {
				PackMetadataSection packMetadataSection = packResources.getMetadataSection(PackMetadataSection.TYPE);
				if (packMetadataSection == null) {
					LOGGER.warn("Missing metadata in pack {}", string);
					return null;
				}

				FeatureFlagsMetadataSection featureFlagsMetadataSection = packResources.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
				FeatureFlagSet featureFlagSet = featureFlagsMetadataSection != null ? featureFlagsMetadataSection.flags() : FeatureFlagSet.of();
				InclusiveRange<Integer> inclusiveRange = getDeclaredPackVersions(string, packMetadataSection);
				PackCompatibility packCompatibility = PackCompatibility.forVersion(inclusiveRange, i);
				OverlayMetadataSection overlayMetadataSection = packResources.getMetadataSection(OverlayMetadataSection.TYPE);
				List<String> list = overlayMetadataSection != null ? overlayMetadataSection.overlaysForVersion(i) : List.of();
				var11 = new Pack.Info(packMetadataSection.description(), packCompatibility, featureFlagSet, list);
			}

			return var11;
		} catch (Exception var14) {
			LOGGER.warn("Failed to read pack {} metadata", string, var14);
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

	public Component getTitle() {
		return this.title;
	}

	public Component getDescription() {
		return this.info.description();
	}

	public Component getChatLink(boolean bl) {
		return ComponentUtils.wrapInSquareBrackets(this.packSource.decorate(Component.literal(this.id)))
			.withStyle(
				style -> style.withColor(bl ? ChatFormatting.GREEN : ChatFormatting.RED)
						.withInsertion(StringArgumentType.escapeIfRequired(this.id))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.empty().append(this.title).append("\n").append(this.info.description)))
			);
	}

	public PackCompatibility getCompatibility() {
		return this.info.compatibility();
	}

	public FeatureFlagSet getRequestedFeatures() {
		return this.info.requestedFeatures();
	}

	public PackResources open() {
		return this.resources.openFull(this.id, this.info);
	}

	public String getId() {
		return this.id;
	}

	public boolean isRequired() {
		return this.required;
	}

	public boolean isFixedPosition() {
		return this.fixedPosition;
	}

	public Pack.Position getDefaultPosition() {
		return this.defaultPosition;
	}

	public PackSource getPackSource() {
		return this.packSource;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof Pack pack) ? false : this.id.equals(pack.id);
		}
	}

	public int hashCode() {
		return this.id.hashCode();
	}

	public static record Info(Component description, PackCompatibility compatibility, FeatureFlagSet requestedFeatures, List<String> overlays) {
	}

	public static enum Position {
		TOP,
		BOTTOM;

		public <T> int insert(List<T> list, T object, Function<T, Pack> function, boolean bl) {
			Pack.Position position = bl ? this.opposite() : this;
			if (position == BOTTOM) {
				int i;
				for (i = 0; i < list.size(); i++) {
					Pack pack = (Pack)function.apply(list.get(i));
					if (!pack.isFixedPosition() || pack.getDefaultPosition() != this) {
						break;
					}
				}

				list.add(i, object);
				return i;
			} else {
				int i;
				for (i = list.size() - 1; i >= 0; i--) {
					Pack pack = (Pack)function.apply(list.get(i));
					if (!pack.isFixedPosition() || pack.getDefaultPosition() != this) {
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
		PackResources openPrimary(String string);

		PackResources openFull(String string, Pack.Info info);
	}
}
