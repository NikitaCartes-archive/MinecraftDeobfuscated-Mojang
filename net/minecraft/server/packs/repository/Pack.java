/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.repository;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class Pack {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String id;
    private final ResourcesSupplier resources;
    private final Component title;
    private final Component description;
    private final PackCompatibility compatibility;
    private final FeatureFlagSet requestedFeatures;
    private final Position defaultPosition;
    private final boolean required;
    private final boolean fixedPosition;
    private final PackSource packSource;

    @Nullable
    public static Pack readMetaAndCreate(String string, Component component, boolean bl, ResourcesSupplier resourcesSupplier, PackType packType, Position position, PackSource packSource) {
        Info info = Pack.readPackInfo(string, resourcesSupplier);
        return info != null ? Pack.create(string, component, bl, resourcesSupplier, info, packType, position, false, packSource) : null;
    }

    public static Pack create(String string, Component component, boolean bl, ResourcesSupplier resourcesSupplier, Info info, PackType packType, Position position, boolean bl2, PackSource packSource) {
        return new Pack(string, bl, resourcesSupplier, component, info, info.compatibility(packType), position, bl2, packSource);
    }

    private Pack(String string, boolean bl, ResourcesSupplier resourcesSupplier, Component component, Info info, PackCompatibility packCompatibility, Position position, boolean bl2, PackSource packSource) {
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

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public static Info readPackInfo(String string, ResourcesSupplier resourcesSupplier) {
        try (PackResources packResources = resourcesSupplier.open(string);){
            PackMetadataSection packMetadataSection = packResources.getMetadataSection(PackMetadataSection.TYPE);
            if (packMetadataSection == null) {
                LOGGER.warn("Missing metadata in pack {}", (Object)string);
                Info info = null;
                return info;
            }
            FeatureFlagsMetadataSection featureFlagsMetadataSection = packResources.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
            FeatureFlagSet featureFlagSet = featureFlagsMetadataSection != null ? featureFlagsMetadataSection.flags() : FeatureFlagSet.of();
            Info info = new Info(packMetadataSection.getDescription(), packMetadataSection.getPackFormat(), featureFlagSet);
            return info;
        } catch (Exception exception) {
            LOGGER.warn("Failed to read pack metadata", exception);
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
        return ComponentUtils.wrapInSquareBrackets(this.packSource.decorate(Component.literal(this.id))).withStyle(style -> style.withColor(bl ? ChatFormatting.GREEN : ChatFormatting.RED).withInsertion(StringArgumentType.escapeIfRequired(this.id)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.empty().append(this.title).append("\n").append(this.description))));
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

    public Position getDefaultPosition() {
        return this.defaultPosition;
    }

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

    @FunctionalInterface
    public static interface ResourcesSupplier {
        public PackResources open(String var1);
    }

    public record Info(Component description, int format, FeatureFlagSet requestedFeatures) {
        public PackCompatibility compatibility(PackType packType) {
            return PackCompatibility.forFormat(this.format, packType);
        }
    }

    public static enum Position {
        TOP,
        BOTTOM;


        public <T> int insert(List<T> list, T object, Function<T, Pack> function, boolean bl) {
            Pack pack;
            int i;
            Position position;
            Position position2 = position = bl ? this.opposite() : this;
            if (position == BOTTOM) {
                Pack pack2;
                int i2;
                for (i2 = 0; i2 < list.size() && (pack2 = function.apply(list.get(i2))).isFixedPosition() && pack2.getDefaultPosition() == this; ++i2) {
                }
                list.add(i2, object);
                return i2;
            }
            for (i = list.size() - 1; i >= 0 && (pack = function.apply(list.get(i))).isFixedPosition() && pack.getDefaultPosition() == this; --i) {
            }
            list.add(i + 1, object);
            return i + 1;
        }

        public Position opposite() {
            return this == TOP ? BOTTOM : TOP;
        }
    }
}

