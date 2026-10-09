package com.crediya.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Common contract for all DAOs (DAO pattern + Dependency Inversion: services depend on this). */
public interface GenericDAO<T> {
    /** Inserts the object and returns the generated id. */
    int insert(T entity) throws SQLException;

    List<T> findAll() throws SQLException;

    Optional<T> findById(int id) throws SQLException;
}
