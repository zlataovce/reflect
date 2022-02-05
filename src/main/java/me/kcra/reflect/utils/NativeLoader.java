package me.kcra.reflect.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.util.Locale;

@UtilityClass
public class NativeLoader {
    public final OperatingSystem OS;
    public final int BITNESS;
    
    public enum OperatingSystem {
        WINDOWS,
        MAC_OSX,
        LINUX,
        SOLARIS,
        BSD,
        UNIX,
        UNKNOWN
    }

    static {
        final String osName = System.getProperty("os.name", "unknown").toLowerCase(Locale.ROOT);
        if (osName.contains("mac") || osName.contains("darwin")) {
            OS = OperatingSystem.MAC_OSX;
        } else if (osName.contains("win")) {
            OS = OperatingSystem.WINDOWS;
        } else if (osName.contains("nux")) {
            OS = OperatingSystem.LINUX;
        } else if (osName.contains("sunos") || osName.contains("solaris")) {
            OS = OperatingSystem.SOLARIS;
        } else if (osName.contains("bsd")) {
            OS = OperatingSystem.UNIX;
        } else if (osName.contains("nix") || osName.contains("aix")) {
            OS = OperatingSystem.UNIX;
        } else {
            OS = OperatingSystem.UNKNOWN;
        }

        int archBits = 64;
        final String dataModel = System.getProperty("sun.arch.data.model");
        if (dataModel != null && dataModel.contains("32")) {
            archBits = 32;
        } else {
            final String osArch = System.getProperty("os.arch");
            if (osArch != null && ((osArch.contains("86") && !osArch.contains("64")) || osArch.contains("32"))) {
                archBits = 32;
            }
        }
        BITNESS = archBits;
    }

    @SneakyThrows
    public static void loadLibraryFromResource(String resource) {
        File tempFile;
        try (final InputStream inputStream = NativeLoader.class.getResourceAsStream(resource.startsWith("/") ? resource : "/" + resource)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Could not find resource " + resource);
            }

            final String fileName = resource.substring(resource.lastIndexOf('/') + 1);
            final int dotIdx = fileName.indexOf('.');
            final String baseName = dotIdx == -1 ? fileName : fileName.substring(0, dotIdx);
            final String suffix = dotIdx == -1 ? ".so" : fileName.substring(dotIdx);
            tempFile = File.createTempFile(baseName + "_", suffix);

            final byte[] buffer = new byte[8192];
            try (final OutputStream os = new FileOutputStream(tempFile)) {
                for (int readBytes; (readBytes = inputStream.read(buffer)) != -1;) {
                    os.write(buffer, 0, readBytes);
                }
            }
        }
        System.load(tempFile.getAbsolutePath());
        tempFile.deleteOnExit();
        try {
            if (tempFile.toPath().getFileSystem().supportedFileAttributeViews().contains("posix")) {
                //noinspection ResultOfMethodCallIgnored
                tempFile.delete();
            }
        } catch (Exception ignored) {
            // ignored
        }
    }
}
