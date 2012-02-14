package com.mancel01.thetreeof.api;

public interface Change<T, O> {
    void applyChange(T t, O o);    
}
