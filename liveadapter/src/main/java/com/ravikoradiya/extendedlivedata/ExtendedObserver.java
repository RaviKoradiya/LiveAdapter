package com.ravikoradiya.extendedlivedata;

import androidx.lifecycle.LiveData;

/**
 * A simple callback that can receive from {@link LiveData}.
 *
 * @param <T> The type of the parameter
 * @see LiveData LiveData - for a usage description.
 */
public interface ExtendedObserver<T> {
    /**
     * Called when the data is changed.
     *
     * @param o The old data
     * @param n The new data
     */
    void onChanged(T o, T n);
}