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
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UnopenedPack implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final PackMetadataSection BROKEN_ASSETS_FALLBACK = new PackMetadataSection(
		new TranslatableComponent("resourcePack.broken_assets").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.ITALIC}),
		SharedConstants.getCurrentVersion().getPackVersion()
	);
	private final String id;
	private final Supplier<Pack> supplier;
	private final Component title;
	private final Component description;
	private final PackCompatibility compatibility;
	private final UnopenedPack.Position defaultPosition;
	private final boolean required;
	private final boolean fixedPosition;

	@Nullable
	public static <T extends UnopenedPack> T create(
		String string, boolean bl, Supplier<Pack> supplier, UnopenedPack.UnopenedPackConstructor<T> unopenedPackConstructor, UnopenedPack.Position position
	) {
		try {
			Pack pack = (Pack)supplier.get();
			Throwable var6 = null;

			UnopenedPack var8;
			try {
				PackMetadataSection packMetadataSection = pack.getMetadataSection(PackMetadataSection.SERIALIZER);
				if (bl && packMetadataSection == null) {
					LOGGER.error(
						"Broken/missing pack.mcmeta detected, fudging it into existance. Please check that your launcher has downloaded all assets for the game correctly!"
					);
					packMetadataSection = BROKEN_ASSETS_FALLBACK;
				}

				if (packMetadataSection == null) {
					LOGGER.warn("Couldn't find pack meta for pack {}", string);
					return null;
				}

				var8 = unopenedPackConstructor.create(string, bl, supplier, pack, packMetadataSection, position);
			} catch (Throwable var19) {
				var6 = var19;
				throw var19;
			} finally {
				if (pack != null) {
					if (var6 != null) {
						try {
							pack.close();
						} catch (Throwable var18) {
							var6.addSuppressed(var18);
						}
					} else {
						pack.close();
					}
				}
			}

			return (T)var8;
		} catch (IOException var21) {
			LOGGER.warn("Couldn't get pack info for: {}", var21.toString());
			return null;
		}
	}

	public UnopenedPack(
		String string,
		boolean bl,
		Supplier<Pack> supplier,
		Component component,
		Component component2,
		PackCompatibility packCompatibility,
		UnopenedPack.Position position,
		boolean bl2
	) {
		this.id = string;
		this.supplier = supplier;
		this.title = component;
		this.description = component2;
		this.compatibility = packCompatibility;
		this.required = bl;
		this.defaultPosition = position;
		this.fixedPosition = bl2;
	}

	public UnopenedPack(String string, boolean bl, Supplier<Pack> supplier, Pack pack, PackMetadataSection packMetadataSection, UnopenedPack.Position position) {
		this(
			string,
			bl,
			supplier,
			new TextComponent(pack.getName()),
			packMetadataSection.getDescription(),
			PackCompatibility.forFormat(packMetadataSection.getPackFormat()),
			position,
			false
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
		return ComponentUtils.wrapInSquareBrackets(new TextComponent(this.id))
			.withStyle(
				style -> style.withColor(bl ? ChatFormatting.GREEN : ChatFormatting.RED)
						.withInsertion(StringArgumentType.escapeIfRequired(this.id))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("").append(this.title).append("\n").append(this.description)))
			);
	}

	public PackCompatibility getCompatibility() {
		return this.compatibility;
	}

	public Pack open() {
		return (Pack)this.supplier.get();
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

	public UnopenedPack.Position getDefaultPosition() {
		return this.defaultPosition;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof UnopenedPack)) {
			return false;
		} else {
			UnopenedPack unopenedPack = (UnopenedPack)object;
			return this.id.equals(unopenedPack.id);
		}
	}

	public int hashCode() {
		return this.id.hashCode();
	}

	public void close() {
	}

	public static enum Position {
		TOP,
		BOTTOM;

		public <T, P extends UnopenedPack> int insert(List<T> list, T object, Function<T, P> function, boolean bl) {
			UnopenedPack.Position position = bl ? this.opposite() : this;
			if (position == BOTTOM) {
				int i;
				for (i = 0; i < list.size(); i++) {
					P unopenedPack = (P)function.apply(list.get(i));
					if (!unopenedPack.isFixedPosition() || unopenedPack.getDefaultPosition() != this) {
						break;
					}
				}

				list.add(i, object);
				return i;
			} else {
				int i;
				for (i = list.size() - 1; i >= 0; i--) {
					P unopenedPack = (P)function.apply(list.get(i));
					if (!unopenedPack.isFixedPosition() || unopenedPack.getDefaultPosition() != this) {
						break;
					}
				}

				list.add(i + 1, object);
				return i + 1;
			}
		}

		public UnopenedPack.Position opposite() {
			return this == TOP ? BOTTOM : TOP;
		}
	}

	@FunctionalInterface
	public interface UnopenedPackConstructor<T extends UnopenedPack> {
		@Nullable
		T create(String string, boolean bl, Supplier<Pack> supplier, Pack pack, PackMetadataSection packMetadataSection, UnopenedPack.Position position);
	}
}
