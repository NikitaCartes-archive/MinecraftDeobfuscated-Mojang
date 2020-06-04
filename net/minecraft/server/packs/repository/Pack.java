/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
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
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class Pack
implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final PackMetadataSection BROKEN_ASSETS_FALLBACK = new PackMetadataSection(new TranslatableComponent("resourcePack.broken_assets").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), SharedConstants.getCurrentVersion().getPackVersion());
    private final String id;
    private final Supplier<PackResources> supplier;
    private final Component title;
    private final Component description;
    private final PackCompatibility compatibility;
    private final Position defaultPosition;
    private final boolean required;
    private final boolean fixedPosition;
    private final PackSource packSource;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public static <T extends Pack> T create(String string, boolean bl, Supplier<PackResources> supplier, PackConstructor<T> packConstructor, Position position, PackSource packSource) {
        try (PackResources packResources = supplier.get();){
            PackMetadataSection packMetadataSection = packResources.getMetadataSection(PackMetadataSection.SERIALIZER);
            if (bl && packMetadataSection == null) {
                LOGGER.error("Broken/missing pack.mcmeta detected, fudging it into existance. Please check that your launcher has downloaded all assets for the game correctly!");
                packMetadataSection = BROKEN_ASSETS_FALLBACK;
            }
            if (packMetadataSection != null) {
                T t = packConstructor.create(string, bl, supplier, packResources, packMetadataSection, position, packSource);
                return t;
            }
            LOGGER.warn("Couldn't find pack meta for pack {}", (Object)string);
            return null;
        } catch (IOException iOException) {
            LOGGER.warn("Couldn't get pack info for: {}", (Object)iOException.toString());
        }
        return null;
    }

    public Pack(String string, boolean bl, Supplier<PackResources> supplier, Component component, Component component2, PackCompatibility packCompatibility, Position position, boolean bl2, PackSource packSource) {
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

    public Pack(String string, boolean bl, Supplier<PackResources> supplier, PackResources packResources, PackMetadataSection packMetadataSection, Position position, PackSource packSource) {
        this(string, bl, supplier, new TextComponent(packResources.getName()), packMetadataSection.getDescription(), PackCompatibility.forFormat(packMetadataSection.getPackFormat()), position, false, packSource);
    }

    @Environment(value=EnvType.CLIENT)
    public Component getTitle() {
        return this.title;
    }

    @Environment(value=EnvType.CLIENT)
    public Component getDescription() {
        return this.description;
    }

    public Component getChatLink(boolean bl) {
        return ComponentUtils.wrapInSquareBrackets(this.packSource.decorate(new TextComponent(this.id))).withStyle(style -> style.withColor(bl ? ChatFormatting.GREEN : ChatFormatting.RED).withInsertion(StringArgumentType.escapeIfRequired(this.id)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("").append(this.title).append("\n").append(this.description))));
    }

    public PackCompatibility getCompatibility() {
        return this.compatibility;
    }

    public PackResources open() {
        return this.supplier.get();
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

    public Position getDefaultPosition() {
        return this.defaultPosition;
    }

    @Environment(value=EnvType.CLIENT)
    public PackSource getPackSource() {
        return this.packSource;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Pack)) {
            return false;
        }
        Pack pack = (Pack)object;
        return this.id.equals(pack.id);
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public void close() {
    }

    public static enum Position {
        TOP,
        BOTTOM;


        public <T, P extends Pack> int insert(List<T> list, T object, Function<T, P> function, boolean bl) {
            Pack pack;
            int i;
            Position position;
            Position position2 = position = bl ? this.opposite() : this;
            if (position == BOTTOM) {
                Pack pack2;
                int i2;
                for (i2 = 0; i2 < list.size() && (pack2 = (Pack)function.apply(list.get(i2))).isFixedPosition() && pack2.getDefaultPosition() == this; ++i2) {
                }
                list.add(i2, object);
                return i2;
            }
            for (i = list.size() - 1; i >= 0 && (pack = (Pack)function.apply(list.get(i))).isFixedPosition() && pack.getDefaultPosition() == this; --i) {
            }
            list.add(i + 1, object);
            return i + 1;
        }

        public Position opposite() {
            return this == TOP ? BOTTOM : TOP;
        }
    }

    @FunctionalInterface
    public static interface PackConstructor<T extends Pack> {
        @Nullable
        public T create(String var1, boolean var2, Supplier<PackResources> var3, PackResources var4, PackMetadataSection var5, Position var6, PackSource var7);
    }
}

