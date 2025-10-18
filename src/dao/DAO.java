package dao;

import java.util.List;
import java.util.Optional;

public interface DAO<T> {

    void add(T entity);
    void update(T entity);
    void delete(int id);
    Optional<T> findByID(int id);
    List<T> findAll();
}
