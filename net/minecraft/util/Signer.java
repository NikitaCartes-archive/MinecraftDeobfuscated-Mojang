/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.security.PrivateKey;
import java.security.Signature;
import net.minecraft.util.SignatureUpdater;
import org.slf4j.Logger;

public interface Signer {
    public static final Logger LOGGER = LogUtils.getLogger();

    public byte[] sign(SignatureUpdater var1);

    default public byte[] sign(byte[] bs) {
        return this.sign(output -> output.update(bs));
    }

    public static Signer from(PrivateKey privateKey, String string) {
        return signatureUpdater -> {
            try {
                Signature signature = Signature.getInstance(string);
                signature.initSign(privateKey);
                signatureUpdater.update(signature::update);
                return signature.sign();
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to sign message", exception);
            }
        };
    }
}

