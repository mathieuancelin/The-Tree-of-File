package com.mancel01.thetreeof.api;

import com.mancel01.thetreeof.util.F;

public interface Store {
    
    public void persist();
    public F.Option<String> get(String name);
    public boolean containsKey(String name);
    public void remove(String name);
    public F.Option<String> set(String name, String value);    
}
