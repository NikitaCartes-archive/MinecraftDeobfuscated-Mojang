/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsServiceException
extends Exception {
    public final int httpResultCode;
    public final String rawResponse;
    @Nullable
    public final RealmsError realmsError;

    public RealmsServiceException(int i, String string, RealmsError realmsError) {
        super(string);
        this.httpResultCode = i;
        this.rawResponse = string;
        this.realmsError = realmsError;
    }

    public RealmsServiceException(int i, String string) {
        super(string);
        this.httpResultCode = i;
        this.rawResponse = string;
        this.realmsError = null;
    }

    @Override
    public String toString() {
        if (this.realmsError != null) {
            String string = "mco.errorMessage." + this.realmsError.getErrorCode();
            String string2 = I18n.exists(string) ? I18n.get(string, new Object[0]) : this.realmsError.getErrorMessage();
            return String.format(Locale.ROOT, "Realms service error (%d/%d) %s", this.httpResultCode, this.realmsError.getErrorCode(), string2);
        }
        return String.format(Locale.ROOT, "Realms service error (%d) %s", this.httpResultCode, this.rawResponse);
    }

    public int realmsErrorCodeOrDefault(int i) {
        return this.realmsError != null ? this.realmsError.getErrorCode() : i;
    }
}

