package com.mancel01.thetreeof.api;

public interface Visitable<T> {
    void visit(Visitor<T> visitor);
}
