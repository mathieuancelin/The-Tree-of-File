package com.mancel01.thetreeof.api;

import com.mancel01.thetreeof.model.Node;

public interface Creator<T> {
    T create(Node parent);
}
