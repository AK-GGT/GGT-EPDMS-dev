package de.iai.ilcd.model.process.contentdeclaration.utils;

import java.util.function.Consumer;

public final class Fn {

    private Fn() {
    }

    public static <T> void with(T t, Consumer<T> fn) {
        if (t != null && fn != null) {
            fn.accept(t);
        }
    }
}
