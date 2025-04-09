package de.iai.ilcd.service.queue;

import java.util.function.BiFunction;

/**
 * We override javas BiFunction to allow storing throwing bifunctions.
 *
 * @param <T> argument type 1
 * @param <U> argument type 2
 * @param <R> result
 */
public interface ThrowingBiFunction<T, U, R> extends BiFunction<T, U, R> {

    @Override
    default R apply(T t, U u) {

        try {
            return applyThrowing(t, u);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return null;
    }

    R applyThrowing(T t, U u) throws Throwable;

}
