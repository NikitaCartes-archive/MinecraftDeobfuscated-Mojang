/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.client;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsAnvilLevelStorageSource;
import net.minecraft.realms.RealmsLevelSummary;
import net.minecraft.realms.RealmsSharedConstants;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class FileDownload {
    private static final Logger LOGGER = LogManager.getLogger();
    private volatile boolean cancelled;
    private volatile boolean finished;
    private volatile boolean error;
    private volatile boolean extracting;
    private volatile File tempFile;
    private volatile File resourcePackPath;
    private volatile HttpGet request;
    private Thread currentThread;
    private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
    private static final String[] INVALID_FILE_NAMES = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long contentLength(String string) {
        Closeable closeableHttpClient = null;
        HttpGet httpGet = null;
        try {
            httpGet = new HttpGet(string);
            closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
            CloseableHttpResponse closeableHttpResponse = ((CloseableHttpClient)closeableHttpClient).execute(httpGet);
            long l = Long.parseLong(closeableHttpResponse.getFirstHeader("Content-Length").getValue());
            return l;
        } catch (Throwable throwable) {
            LOGGER.error("Unable to get content length for download");
            long l = 0L;
            return l;
        } finally {
            if (httpGet != null) {
                httpGet.releaseConnection();
            }
            if (closeableHttpClient != null) {
                try {
                    closeableHttpClient.close();
                } catch (IOException iOException) {
                    LOGGER.error("Could not close http client", (Throwable)iOException);
                }
            }
        }
    }

    public void download(final WorldDownload worldDownload, final String string, final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, final RealmsAnvilLevelStorageSource realmsAnvilLevelStorageSource) {
        if (this.currentThread != null) {
            return;
        }
        this.currentThread = new Thread(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             * Enabled aggressive block sorting
             * Enabled unnecessary exception pruning
             * Enabled aggressive exception aggregation
             */
            @Override
            public void run() {
                Closeable closeableHttpClient = null;
                try {
                    FileDownload.this.tempFile = File.createTempFile("backup", ".tar.gz");
                    FileDownload.this.request = new HttpGet(worldDownload.downloadLink);
                    closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(FileDownload.this.requestConfig).build();
                    CloseableHttpResponse httpResponse = ((CloseableHttpClient)closeableHttpClient).execute(FileDownload.this.request);
                    downloadStatus.totalBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
                    if (httpResponse.getStatusLine().getStatusCode() != 200) {
                        FileDownload.this.error = true;
                        FileDownload.this.request.abort();
                        return;
                    }
                    FileOutputStream outputStream2 = new FileOutputStream(FileDownload.this.tempFile);
                    ProgressListener progressListener = new ProgressListener(string.trim(), FileDownload.this.tempFile, realmsAnvilLevelStorageSource, downloadStatus, worldDownload);
                    DownloadCountingOutputStream downloadCountingOutputStream2 = new DownloadCountingOutputStream(outputStream2);
                    downloadCountingOutputStream2.setListener(progressListener);
                    IOUtils.copy(httpResponse.getEntity().getContent(), (OutputStream)downloadCountingOutputStream2);
                    return;
                } catch (Exception exception2) {
                    LOGGER.error("Caught exception while downloading: " + exception2.getMessage());
                    FileDownload.this.error = true;
                    return;
                } finally {
                    block40: {
                        block41: {
                            CloseableHttpResponse httpResponse;
                            FileDownload.this.request.releaseConnection();
                            if (FileDownload.this.tempFile != null) {
                                FileDownload.this.tempFile.delete();
                            }
                            if (FileDownload.this.error) break block40;
                            if (worldDownload.resourcePackUrl.isEmpty() || worldDownload.resourcePackHash.isEmpty()) break block41;
                            try {
                                FileDownload.this.tempFile = File.createTempFile("resources", ".tar.gz");
                                FileDownload.this.request = new HttpGet(worldDownload.resourcePackUrl);
                                httpResponse = ((CloseableHttpClient)closeableHttpClient).execute(FileDownload.this.request);
                                downloadStatus.totalBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
                                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                                    FileDownload.this.error = true;
                                    FileDownload.this.request.abort();
                                    return;
                                }
                            } catch (Exception exception2) {
                                LOGGER.error("Caught exception while downloading: " + exception2.getMessage());
                                FileDownload.this.error = true;
                            }
                            FileOutputStream outputStream2 = new FileOutputStream(FileDownload.this.tempFile);
                            ResourcePackProgressListener resourcePackProgressListener2 = new ResourcePackProgressListener(FileDownload.this.tempFile, downloadStatus, worldDownload);
                            DownloadCountingOutputStream downloadCountingOutputStream2 = new DownloadCountingOutputStream(outputStream2);
                            downloadCountingOutputStream2.setListener(resourcePackProgressListener2);
                            IOUtils.copy(httpResponse.getEntity().getContent(), (OutputStream)downloadCountingOutputStream2);
                            break block40;
                            finally {
                                FileDownload.this.request.releaseConnection();
                                if (FileDownload.this.tempFile != null) {
                                    FileDownload.this.tempFile.delete();
                                }
                            }
                        }
                        FileDownload.this.finished = true;
                    }
                    if (closeableHttpClient != null) {
                        try {
                            closeableHttpClient.close();
                        } catch (IOException iOException2) {
                            LOGGER.error("Failed to close Realms download client");
                        }
                    }
                }
            }
        };
        this.currentThread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
        this.currentThread.start();
    }

    public void cancel() {
        if (this.request != null) {
            this.request.abort();
        }
        if (this.tempFile != null) {
            this.tempFile.delete();
        }
        this.cancelled = true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean isError() {
        return this.error;
    }

    public boolean isExtracting() {
        return this.extracting;
    }

    public static String findAvailableFolderName(String string) {
        string = string.replaceAll("[\\./\"]", "_");
        for (String string2 : INVALID_FILE_NAMES) {
            if (!string.equalsIgnoreCase(string2)) continue;
            string = "_" + string + "_";
        }
        return string;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void untarGzipArchive(String string, File file, RealmsAnvilLevelStorageSource realmsAnvilLevelStorageSource) throws IOException {
        String string2;
        Pattern pattern = Pattern.compile(".*-([0-9]+)$");
        int i = 1;
        for (char c : RealmsSharedConstants.ILLEGAL_FILE_CHARACTERS) {
            string = string.replace(c, '_');
        }
        if (StringUtils.isEmpty(string)) {
            string = "Realm";
        }
        string = FileDownload.findAvailableFolderName(string);
        try {
            Object object = realmsAnvilLevelStorageSource.getLevelList().iterator();
            while (object.hasNext()) {
                RealmsLevelSummary realmsLevelSummary = (RealmsLevelSummary)object.next();
                if (!realmsLevelSummary.getLevelId().toLowerCase(Locale.ROOT).startsWith(string.toLowerCase(Locale.ROOT))) continue;
                Matcher matcher = pattern.matcher(realmsLevelSummary.getLevelId());
                if (matcher.matches()) {
                    if (Integer.valueOf(matcher.group(1)) <= i) continue;
                    i = Integer.valueOf(matcher.group(1));
                    continue;
                }
                ++i;
            }
        } catch (Exception exception) {
            LOGGER.error("Error getting level list", (Throwable)exception);
            this.error = true;
            return;
        }
        if (!realmsAnvilLevelStorageSource.isNewLevelIdAcceptable(string) || i > 1) {
            string2 = string + (i == 1 ? "" : "-" + i);
            if (!realmsAnvilLevelStorageSource.isNewLevelIdAcceptable(string2)) {
                boolean bl = false;
                while (!bl) {
                    string2 = string + (++i == 1 ? "" : "-" + i);
                    if (!realmsAnvilLevelStorageSource.isNewLevelIdAcceptable(string2)) continue;
                    bl = true;
                }
            }
        } else {
            string2 = string;
        }
        TarArchiveInputStream tarArchiveInputStream = null;
        File file2 = new File(Realms.getGameDirectoryPath(), "saves");
        try {
            file2.mkdir();
            tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(file))));
            TarArchiveEntry tarArchiveEntry = tarArchiveInputStream.getNextTarEntry();
            while (tarArchiveEntry != null) {
                File file3 = new File(file2, tarArchiveEntry.getName().replace("world", string2));
                if (tarArchiveEntry.isDirectory()) {
                    file3.mkdirs();
                } else {
                    file3.createNewFile();
                    byte[] bs = new byte[1024];
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file3));
                    int j = 0;
                    while ((j = tarArchiveInputStream.read(bs)) != -1) {
                        bufferedOutputStream.write(bs, 0, j);
                    }
                    bufferedOutputStream.close();
                    bs = null;
                }
                tarArchiveEntry = tarArchiveInputStream.getNextTarEntry();
            }
        } catch (Exception exception2) {
            LOGGER.error("Error extracting world", (Throwable)exception2);
            this.error = true;
        } finally {
            if (tarArchiveInputStream != null) {
                tarArchiveInputStream.close();
            }
            if (file != null) {
                file.delete();
            }
            RealmsAnvilLevelStorageSource realmsAnvilLevelStorageSource2 = realmsAnvilLevelStorageSource;
            realmsAnvilLevelStorageSource2.renameLevel(string2, string2.trim());
            File file3 = new File(file2, string2 + File.separator + "level.dat");
            Realms.deletePlayerTag(file3);
            this.resourcePackPath = new File(file2, string2 + File.separator + "resources.zip");
        }
    }

    @Environment(value=EnvType.CLIENT)
    class DownloadCountingOutputStream
    extends CountingOutputStream {
        private ActionListener listener;

        public DownloadCountingOutputStream(OutputStream outputStream) {
            super(outputStream);
        }

        public void setListener(ActionListener actionListener) {
            this.listener = actionListener;
        }

        @Override
        protected void afterWrite(int i) throws IOException {
            super.afterWrite(i);
            if (this.listener != null) {
                this.listener.actionPerformed(new ActionEvent(this, 0, null));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ResourcePackProgressListener
    implements ActionListener {
        private final File tempFile;
        private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
        private final WorldDownload worldDownload;

        private ResourcePackProgressListener(File file, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, WorldDownload worldDownload) {
            this.tempFile = file;
            this.downloadStatus = downloadStatus;
            this.worldDownload = worldDownload;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            this.downloadStatus.bytesWritten = ((DownloadCountingOutputStream)actionEvent.getSource()).getByteCount();
            if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled) {
                try {
                    String string = Hashing.sha1().hashBytes(Files.toByteArray(this.tempFile)).toString();
                    if (string.equals(this.worldDownload.resourcePackHash)) {
                        FileUtils.copyFile(this.tempFile, FileDownload.this.resourcePackPath);
                        FileDownload.this.finished = true;
                    } else {
                        LOGGER.error("Resourcepack had wrong hash (expected " + this.worldDownload.resourcePackHash + ", found " + string + "). Deleting it.");
                        FileUtils.deleteQuietly(this.tempFile);
                        FileDownload.this.error = true;
                    }
                } catch (IOException iOException) {
                    LOGGER.error("Error copying resourcepack file", (Object)iOException.getMessage());
                    FileDownload.this.error = true;
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class ProgressListener
    implements ActionListener {
        private final String worldName;
        private final File tempFile;
        private final RealmsAnvilLevelStorageSource levelStorageSource;
        private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
        private final WorldDownload worldDownload;

        private ProgressListener(String string, File file, RealmsAnvilLevelStorageSource realmsAnvilLevelStorageSource, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, WorldDownload worldDownload) {
            this.worldName = string;
            this.tempFile = file;
            this.levelStorageSource = realmsAnvilLevelStorageSource;
            this.downloadStatus = downloadStatus;
            this.worldDownload = worldDownload;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            this.downloadStatus.bytesWritten = ((DownloadCountingOutputStream)actionEvent.getSource()).getByteCount();
            if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled && !FileDownload.this.error) {
                try {
                    FileDownload.this.extracting = true;
                    FileDownload.this.untarGzipArchive(this.worldName, this.tempFile, this.levelStorageSource);
                } catch (IOException iOException) {
                    LOGGER.error("Error extracting archive", (Throwable)iOException);
                    FileDownload.this.error = true;
                }
            }
        }
    }
}

