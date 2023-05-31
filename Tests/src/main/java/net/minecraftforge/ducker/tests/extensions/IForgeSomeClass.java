package net.minecraftforge.ducker.tests.extensions;

import net.minecraftforge.ducker.tests.SomeClass;

public interface IForgeSomeClass {
    SomeClass self();

    default boolean isBool() {
        return self().maybe;
    }
}
