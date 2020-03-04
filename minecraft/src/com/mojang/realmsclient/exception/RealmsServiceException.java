package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;

@Environment(EnvType.CLIENT)
public class RealmsServiceException extends Exception {
	public final int httpResultCode;
	public final String httpResponseContent;
	public final int errorCode;
	public final String errorMsg;

	public RealmsServiceException(int i, String string, RealmsError realmsError) {
		super(string);
		this.httpResultCode = i;
		this.httpResponseContent = string;
		this.errorCode = realmsError.getErrorCode();
		this.errorMsg = realmsError.getErrorMessage();
	}

	public RealmsServiceException(int i, String string, int j, String string2) {
		super(string);
		this.httpResultCode = i;
		this.httpResponseContent = string;
		this.errorCode = j;
		this.errorMsg = string2;
	}

	public String toString() {
		if (this.errorCode == -1) {
			return "Realms (" + this.httpResultCode + ") " + this.httpResponseContent;
		} else {
			String string = "mco.errorMessage." + this.errorCode;
			String string2 = I18n.get(string);
			return (string2.equals(string) ? this.errorMsg : string2) + " - " + this.errorCode;
		}
	}
}
