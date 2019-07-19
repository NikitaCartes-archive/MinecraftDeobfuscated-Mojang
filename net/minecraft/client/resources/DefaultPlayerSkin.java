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
    private static final ResourceLocation STEVE_SKIN_LOCATION = new ResourceLocation("textures/entity/steve.png");
    private static final ResourceLocation ALEX_SKIN_LOCATION = new ResourceLocation("textures/entity/alex.png");

    public static ResourceLocation getDefaultSkin() {
        return STEVE_SKIN_LOCATION;
    }

    public static ResourceLocation getDefaultSkin(UUID uUID) {
        if (DefaultPlayerSkin.isAlexDefault(uUID)) {
            return ALEX_SKIN_LOCATION;
        }
        return STEVE_SKIN_LOCATION;
    }

    public static String getSkinModelName(UUID uUID) {
        if (DefaultPlayerSkin.isAlexDefault(uUID)) {
            return "slim";
        }
        return "default";
    }

    private static boolean isAlexDefault(UUID uUID) {
        return (uUID.hashCode() & 1) == 1;
    }
}

