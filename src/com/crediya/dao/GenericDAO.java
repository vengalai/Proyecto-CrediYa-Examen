package com.crediya.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


public interface GenericDAO<T> {

    int insert(T entity) throws SQLException;

    List<T> findAll() throws SQLException;

    Optional<T> findById(int id) throws SQLException;
}
