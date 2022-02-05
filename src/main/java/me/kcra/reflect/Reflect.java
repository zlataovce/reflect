package me.kcra.reflect;

import lombok.NoArgsConstructor;
import me.kcra.reflect.utils.NativeLoader;

import java.util.Locale;

@NoArgsConstructor(staticName = "reflect")
public class Reflect {
    static {
        String suffix;
        switch (NativeLoader.OS) {
            case LINUX:
                suffix = ".so";
                break;
            case MAC_OSX:
                suffix = ".dylib";
                break;
            case WINDOWS:
                suffix = ".dll";
                break;
            default:
                throw new IllegalArgumentException("No native library available for this operating system");
        }
        NativeLoader.loadLibraryFromResource("libreflect-" + NativeLoader.OS.name().toLowerCase(Locale.ROOT) + "-" + NativeLoader.BITNESS + suffix);
    }

    public native Object allocateInstance(Class<?> klass);
}
