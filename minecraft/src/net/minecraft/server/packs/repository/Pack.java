package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;

public class Pack {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final String id;
	private final Pack.ResourcesSupplier resources;
	private final Component title;
	private final Component description;
	private final PackCompatibility compatibility;
	private final FeatureFlagSet requestedFeatures;
	private final Pack.Position defaultPosition;
	private final boolean required;
	private final boolean fixedPosition;
	private final PackSource packSource;

	@Nullable
	public static Pack readMetaAndCreate(
		String string, Component component, boolean bl, Pack.ResourcesSupplier resourcesSupplier, PackType packType, Pack.Position position, PackSource packSource
	) {
		Pack.Info info = readPackInfo(string, resourcesSupplier);
		return info != null ? create(string, component, bl, resourcesSupplier, info, packType, position, false, packSource) : null;
	}

	public static Pack create(
		String string,
		Component component,
		boolean bl,
		Pack.ResourcesSupplier resourcesSupplier,
		Pack.Info info,
		PackType packType,
		Pack.Position position,
		boolean bl2,
		PackSource packSource
	) {
		return new Pack(string, bl, resourcesSupplier, component, info, info.compatibility(packType), position, bl2, packSource);
	}

	private Pack(
		String string,
		boolean bl,
		Pack.ResourcesSupplier resourcesSupplier,
		Component component,
		Pack.Info info,
		PackCompatibility packCompatibility,
		Pack.Position position,
		boolean bl2,
		PackSource packSource
	) {
		this.id = string;
		this.resources = resourcesSupplier;
		this.title = component;
		this.description = info.description();
		this.compatibility = packCompatibility;
		this.requestedFeatures = info.requestedFeatures();
		this.required = bl;
		this.defaultPosition = position;
		this.fixedPosition = bl2;
		this.packSource = packSource;
	}

	@Nullable
	public static Pack.Info readPackInfo(String string, Pack.ResourcesSupplier resourcesSupplier) {
		try {
			Pack.Info var6;
			try (PackResources packResources = resourcesSupplier.open(string)) {
				PackMetadataSection packMetadataSection = packResources.getMetadataSection(PackMetadataSection.TYPE);
				if (packMetadataSection == null) {
					LOGGER.warn("Missing metadata in pack {}", string);
					return null;
				}

				FeatureFlagsMetadataSection featureFlagsMetadataSection = packResources.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
				FeatureFlagSet featureFlagSet = featureFlagsMetadataSection != null ? featureFlagsMetadataSection.flags() : FeatureFlagSet.of();
				var6 = new Pack.Info(packMetadataSection.getDescription(), packMetadataSection.getPackFormat(), featureFlagSet);
			}

			return var6;
		} catch (Exception var9) {
			LOGGER.warn("Failed to read pack metadata", (Throwable)var9);
			return null;
		}
	}

	public Component getTitle() {
		return this.title;
	}

	public Component getDescription() {
		return this.description;
	}

	public Component getChatLink(boolean bl) {
		return ComponentUtils.wrapInSquareBrackets(this.packSource.decorate(Component.literal(this.id)))
			.withStyle(
				style -> style.withColor(bl ? ChatFormatting.GREEN : ChatFormatting.RED)
						.withInsertion(StringArgumentType.escapeIfRequired(this.id))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.empty().append(this.title).append("\n").append(this.description)))
			);
	}

	public PackCompatibility getCompatibility() {
		return this.compatibility;
	}

	public FeatureFlagSet getRequestedFeatures() {
		return this.requestedFeatures;
	}

	public PackResources open() {
		return this.resources.open(this.id);
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

	public static record Info(Component description, int format, FeatureFlagSet requestedFeatures) {
		public PackCompatibility compatibility(PackType packType) {
			return PackCompatibility.forFormat(this.format, packType);
		}
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

	@FunctionalInterface
	public interface ResourcesSupplier {
		PackResources open(String string);
	}
}
