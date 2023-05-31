package net.minecraftforge.ducker.util;

import net.minecraftforge.ducker.mixin.DuckerExecutorMixinService;
import org.objectweb.asm.ClassWriter;

public class DuckerClassWriter extends ClassWriter {
    private final DuckerExecutorMixinService service;
    public DuckerClassWriter(int flags, DuckerExecutorMixinService service) {
        super(0);
        this.service = service;
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        Class<?> class1;
        try {
            class1 = service.getClassProvider().findClass(type1.replace('/', '.'), false);
        } catch (ClassNotFoundException e) {
            throw new TypeNotPresentException(type1, e);
        }
        Class<?> class2;
        try {
            class2 = service.getClassProvider().findClass(type2.replace('/', '.'), false);
        } catch (ClassNotFoundException e) {
            throw new TypeNotPresentException(type2, e);
        }
        if (class1.isAssignableFrom(class2)) {
            return type1;
        }
        if (class2.isAssignableFrom(class1)) {
            return type2;
        }
        if (class1.isInterface() || class2.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                class1 = class1.getSuperclass();
            } while (!class1.isAssignableFrom(class2));
            return class1.getName().replace('.', '/');
        }
    }
}
