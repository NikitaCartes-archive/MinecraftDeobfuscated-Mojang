package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Pack implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final PackMetadataSection BROKEN_ASSETS_FALLBACK = new PackMetadataSection(
		new TranslatableComponent("resourcePack.broken_assets").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC}),
		SharedConstants.getCurrentVersion().getPackVersion()
	);
	private final String id;
	private final Supplier<PackResources> supplier;
	private final Component title;
	private final Component description;
	private final PackCompatibility compatibility;
	private final Pack.Position defaultPosition;
	private final boolean required;
	private final boolean fixedPosition;
	private final PackSource packSource;

	@Nullable
	public static Pack create(
		String string, boolean bl, Supplier<PackResources> supplier, Pack.PackConstructor packConstructor, Pack.Position position, PackSource packSource
	) {
		try (PackResources packResources = (PackResources)supplier.get()) {
			PackMetadataSection packMetadataSection = packResources.getMetadataSection(PackMetadataSection.SERIALIZER);
			if (bl && packMetadataSection == null) {
				LOGGER.error(
					"Broken/missing pack.mcmeta detected, fudging it into existance. Please check that your launcher has downloaded all assets for the game correctly!"
				);
				packMetadataSection = BROKEN_ASSETS_FALLBACK;
			}

			if (packMetadataSection != null) {
				return packConstructor.create(string, bl, supplier, packResources, packMetadataSection, position, packSource);
			}

			LOGGER.warn("Couldn't find pack meta for pack {}", string);
		} catch (IOException var22) {
			LOGGER.warn("Couldn't get pack info for: {}", var22.toString());
		}

		return null;
	}

	public Pack(
		String string,
		boolean bl,
		Supplier<PackResources> supplier,
		Component component,
		Component component2,
		PackCompatibility packCompatibility,
		Pack.Position position,
		boolean bl2,
		PackSource packSource
	) {
		this.id = string;
		this.supplier = supplier;
		this.title = component;
		this.description = component2;
		this.compatibility = packCompatibility;
		this.required = bl;
		this.defaultPosition = position;
		this.fixedPosition = bl2;
		this.packSource = packSource;
	}

	public Pack(
		String string,
		boolean bl,
		Supplier<PackResources> supplier,
		PackResources packResources,
		PackMetadataSection packMetadataSection,
		Pack.Position position,
		PackSource packSource
	) {
		this(
			string,
			bl,
			supplier,
			new TextComponent(packResources.getName()),
			packMetadataSection.getDescription(),
			PackCompatibility.forFormat(packMetadataSection.getPackFormat()),
			position,
			false,
			packSource
		);
	}

	@Environment(EnvType.CLIENT)
	public Component getTitle() {
		return this.title;
	}

	@Environment(EnvType.CLIENT)
	public Component getDescription() {
		return this.description;
	}

	public Component getChatLink(boolean bl) {
		return ComponentUtils.wrapInSquareBrackets(this.packSource.decorate(new TextComponent(this.id)))
			.withStyle(
				style -> style.withColor(bl ? ChatFormatting.GREEN : ChatFormatting.RED)
						.withInsertion(StringArgumentType.escapeIfRequired(this.id))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("").append(this.title).append("\n").append(this.description)))
			);
	}

	public PackCompatibility getCompatibility() {
		return this.compatibility;
	}

	public PackResources open() {
		return (PackResources)this.supplier.get();
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

	@Environment(EnvType.CLIENT)
	public PackSource getPackSource() {
		return this.packSource;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof Pack)) {
			return false;
		} else {
			Pack pack = (Pack)object;
			return this.id.equals(pack.id);
		}
	}

	public int hashCode() {
		return this.id.hashCode();
	}

	public void close() {
	}

	@FunctionalInterface
	public interface PackConstructor {
		@Nullable
		Pack create(
			String string,
			boolean bl,
			Supplier<PackResources> supplier,
			PackResources packResources,
			PackMetadataSection packMetadataSection,
			Pack.Position position,
			PackSource packSource
		);
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
}
