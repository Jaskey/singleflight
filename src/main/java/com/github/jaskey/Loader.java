package com.github.jaskey;

public interface Loader<K, R> {
    R load(K t) throws Exception;
}
