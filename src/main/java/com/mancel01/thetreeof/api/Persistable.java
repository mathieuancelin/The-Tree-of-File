package com.mancel01.thetreeof.api;

import com.mancel01.thetreeof.util.Promise;

public interface Persistable<T> {
    Promise<T> persist();
}
