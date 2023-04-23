package com.example.todolist.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.todolist.entity.Todo;
import com.example.todolist.form.TodoQuery;

public interface TodoDao {
    // Criteria APIによる検索
    Page<Todo> findByCriteria(TodoQuery todoQuery, Pageable pageable);
}
