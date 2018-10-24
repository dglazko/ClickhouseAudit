package ru.anarok.audit.dao;

public interface Dao<T, ID> {
    T save(T t);
    T update(T t);
    T get(ID id);
    void delete(ID id);
}
