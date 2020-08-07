package com.mojang.realmsclient.client;

import com.mojang.realmsclient.exception.RealmsHttpException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class Request<T extends Request<T>> {
	protected HttpURLConnection connection;
	private boolean connected;
	protected String url;

	public Request(String string, int i, int j) {
		try {
			this.url = string;
			Proxy proxy = RealmsClientConfig.getProxy();
			if (proxy != null) {
				this.connection = (HttpURLConnection)new URL(string).openConnection(proxy);
			} else {
				this.connection = (HttpURLConnection)new URL(string).openConnection();
			}

			this.connection.setConnectTimeout(i);
			this.connection.setReadTimeout(j);
		} catch (MalformedURLException var5) {
			throw new RealmsHttpException(var5.getMessage(), var5);
		} catch (IOException var6) {
			throw new RealmsHttpException(var6.getMessage(), var6);
		}
	}

	public void cookie(String string, String string2) {
		cookie(this.connection, string, string2);
	}

	public static void cookie(HttpURLConnection httpURLConnection, String string, String string2) {
		String string3 = httpURLConnection.getRequestProperty("Cookie");
		if (string3 == null) {
			httpURLConnection.setRequestProperty("Cookie", string + "=" + string2);
		} else {
			httpURLConnection.setRequestProperty("Cookie", string3 + ";" + string + "=" + string2);
		}
	}

	public int getRetryAfterHeader() {
		return getRetryAfterHeader(this.connection);
	}

	public static int getRetryAfterHeader(HttpURLConnection httpURLConnection) {
		String string = httpURLConnection.getHeaderField("Retry-After");

		try {
			return Integer.valueOf(string);
		} catch (Exception var3) {
			return 5;
		}
	}

	public int responseCode() {
		try {
			this.connect();
			return this.connection.getResponseCode();
		} catch (Exception var2) {
			throw new RealmsHttpException(var2.getMessage(), var2);
		}
	}

	public String text() {
		try {
			this.connect();
			String string = null;
			if (this.responseCode() >= 400) {
				string = this.read(this.connection.getErrorStream());
			} else {
				string = this.read(this.connection.getInputStream());
			}

			this.dispose();
			return string;
		} catch (IOException var2) {
			throw new RealmsHttpException(var2.getMessage(), var2);
		}
	}

	private String read(InputStream inputStream) throws IOException {
		if (inputStream == null) {
			return "";
		} else {
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
			StringBuilder stringBuilder = new StringBuilder();

			for (int i = inputStreamReader.read(); i != -1; i = inputStreamReader.read()) {
				stringBuilder.append((char)i);
			}

			return stringBuilder.toString();
		}
	}

	private void dispose() {
		byte[] bs = new byte[1024];

		try {
			InputStream inputStream = this.connection.getInputStream();

			while (inputStream.read(bs) > 0) {
			}

			inputStream.close();
			return;
		} catch (Exception var9) {
			try {
				InputStream inputStream2 = this.connection.getErrorStream();
				if (inputStream2 != null) {
					while (inputStream2.read(bs) > 0) {
					}

					inputStream2.close();
					return;
				}
			} catch (IOException var8) {
				return;
			}
		} finally {
			if (this.connection != null) {
				this.connection.disconnect();
			}
		}
	}

	protected T connect() {
		if (this.connected) {
			return (T)this;
		} else {
			T request = this.doConnect();
			this.connected = true;
			return request;
		}
	}

	protected abstract T doConnect();

	public static Request<?> get(String string) {
		return new Request.Get(string, 5000, 60000);
	}

	public static Request<?> get(String string, int i, int j) {
		return new Request.Get(string, i, j);
	}

	public static Request<?> post(String string, String string2) {
		return new Request.Post(string, string2, 5000, 60000);
	}

	public static Request<?> post(String string, String string2, int i, int j) {
		return new Request.Post(string, string2, i, j);
	}

	public static Request<?> delete(String string) {
		return new Request.Delete(string, 5000, 60000);
	}

	public static Request<?> put(String string, String string2) {
		return new Request.Put(string, string2, 5000, 60000);
	}

	public static Request<?> put(String string, String string2, int i, int j) {
		return new Request.Put(string, string2, i, j);
	}

	public String getHeader(String string) {
		return getHeader(this.connection, string);
	}

	public static String getHeader(HttpURLConnection httpURLConnection, String string) {
		try {
			return httpURLConnection.getHeaderField(string);
		} catch (Exception var3) {
			return "";
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Delete extends Request<Request.Delete> {
		public Delete(String string, int i, int j) {
			super(string, i, j);
		}

		public Request.Delete doConnect() {
			try {
				this.connection.setDoOutput(true);
				this.connection.setRequestMethod("DELETE");
				this.connection.connect();
				return this;
			} catch (Exception var2) {
				throw new RealmsHttpException(var2.getMessage(), var2);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Get extends Request<Request.Get> {
		public Get(String string, int i, int j) {
			super(string, i, j);
		}

		public Request.Get doConnect() {
			try {
				this.connection.setDoInput(true);
				this.connection.setDoOutput(true);
				this.connection.setUseCaches(false);
				this.connection.setRequestMethod("GET");
				return this;
			} catch (Exception var2) {
				throw new RealmsHttpException(var2.getMessage(), var2);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Post extends Request<Request.Post> {
		private final String content;

		public Post(String string, String string2, int i, int j) {
			super(string, i, j);
			this.content = string2;
		}

		public Request.Post doConnect() {
			try {
				if (this.content != null) {
					this.connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
				}

				this.connection.setDoInput(true);
				this.connection.setDoOutput(true);
				this.connection.setUseCaches(false);
				this.connection.setRequestMethod("POST");
				OutputStream outputStream = this.connection.getOutputStream();
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
				outputStreamWriter.write(this.content);
				outputStreamWriter.close();
				outputStream.flush();
				return this;
			} catch (Exception var3) {
				throw new RealmsHttpException(var3.getMessage(), var3);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Put extends Request<Request.Put> {
		private final String content;

		public Put(String string, String string2, int i, int j) {
			super(string, i, j);
			this.content = string2;
		}

		public Request.Put doConnect() {
			try {
				if (this.content != null) {
					this.connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
				}

				this.connection.setDoOutput(true);
				this.connection.setDoInput(true);
				this.connection.setRequestMethod("PUT");
				OutputStream outputStream = this.connection.getOutputStream();
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
				outputStreamWriter.write(this.content);
				outputStreamWriter.close();
				outputStream.flush();
				return this;
			} catch (Exception var3) {
				throw new RealmsHttpException(var3.getMessage(), var3);
			}
		}
	}
}
