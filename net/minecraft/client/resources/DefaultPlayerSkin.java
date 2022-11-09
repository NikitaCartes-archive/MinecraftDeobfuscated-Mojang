/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.resources;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public class DefaultPlayerSkin {
    private static final SkinType[] DEFAULT_SKINS = new SkinType[]{new SkinType("textures/entity/player/slim/alex.png", ModelType.SLIM), new SkinType("textures/entity/player/slim/ari.png", ModelType.SLIM), new SkinType("textures/entity/player/slim/efe.png", ModelType.SLIM), new SkinType("textures/entity/player/slim/kai.png", ModelType.SLIM), new SkinType("textures/entity/player/slim/makena.png", ModelType.SLIM), new SkinType("textures/entity/player/slim/noor.png", ModelType.SLIM), new SkinType("textures/entity/player/slim/steve.png", ModelType.SLIM), new SkinType("textures/entity/player/slim/sunny.png", ModelType.SLIM), new SkinType("textures/entity/player/slim/zuri.png", ModelType.SLIM), new SkinType("textures/entity/player/wide/alex.png", ModelType.WIDE), new SkinType("textures/entity/player/wide/ari.png", ModelType.WIDE), new SkinType("textures/entity/player/wide/efe.png", ModelType.WIDE), new SkinType("textures/entity/player/wide/kai.png", ModelType.WIDE), new SkinType("textures/entity/player/wide/makena.png", ModelType.WIDE), new SkinType("textures/entity/player/wide/noor.png", ModelType.WIDE), new SkinType("textures/entity/player/wide/steve.png", ModelType.WIDE), new SkinType("textures/entity/player/wide/sunny.png", ModelType.WIDE), new SkinType("textures/entity/player/wide/zuri.png", ModelType.WIDE)};

    public static ResourceLocation getDefaultSkin() {
        return DEFAULT_SKINS[6].texture();
    }

    public static ResourceLocation getDefaultSkin(UUID uUID) {
        return DefaultPlayerSkin.getSkinType((UUID)uUID).texture;
    }

    public static String getSkinModelName(UUID uUID) {
        return DefaultPlayerSkin.getSkinType((UUID)uUID).model.id;
    }

    private static SkinType getSkinType(UUID uUID) {
        return DEFAULT_SKINS[Math.floorMod(uUID.hashCode(), DEFAULT_SKINS.length)];
    }

    @Environment(value=EnvType.CLIENT)
    record SkinType(ResourceLocation texture, ModelType model) {
        public SkinType(String string, ModelType modelType) {
            this(new ResourceLocation(string), modelType);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum ModelType {
        SLIM("slim"),
        WIDE("default");

        final String id;

        private ModelType(String string2) {
            this.id = string2;
        }
    }
}

