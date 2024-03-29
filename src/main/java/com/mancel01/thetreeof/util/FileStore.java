package com.mancel01.thetreeof.util;

import com.google.common.io.Files;
import com.mancel01.thetreeof.api.Store;
import com.mancel01.thetreeof.util.F.ExceptionWrapper;
import com.mancel01.thetreeof.util.F.Option;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Properties;

public class FileStore implements Store {
    
    private final String path;
    private Properties props;

    public FileStore(String path) {
        this.path = path;
        try {
            Reader reader = Files.newReader(new File(path), Charset.forName("utf-8"));
            this.props = new Properties();
            this.props.load(reader);
        } catch (IOException ex) {
            //throw new ExceptionWrapper(ex);
        }
    }

    @Override
    public void persist() {
        if (props != null) {
            try {
                props.store(Files.newWriter(new File(path), Charset.forName("utf-8")), "persit");
            } catch (Exception ex) {
                throw new ExceptionWrapper(ex);
            }
        }
    }
    
    @Override
    public Option<String> get(String name) {
        if (props != null) {
            return Option.maybe(props.getProperty(name));
        }
        return Option.none();
    }
    
    @Override
    public boolean containsKey(String name) {
        if (props != null) {
            return props.containsKey(name);
        }
        return false;
    }
    
    @Override
    public void remove(String name) {
        if (props != null) {
            props.remove(name);
        }
    }
    
    @Override
    public Option<String> set(String name, String value) {
        if (props != null) {
            String ret = (String) props.put(name, value);
            return Option.maybe(ret);
        }
        return Option.none();
    }
}
