package me.kcra.reflect;

import lombok.NoArgsConstructor;
import me.kcra.reflect.utils.NativeLoader;

import java.util.Locale;

@NoArgsConstructor(staticName = "reflect")
public class Reflect {
    static {
        NativeLoader.loadNative("libreflect");
    }

    public native Object allocateInstance(Class<?> klass);
}
