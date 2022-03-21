package com.orwen.hisport.common.dbaccess.repository;

import com.querydsl.core.dml.DeleteClause;
import com.querydsl.core.dml.UpdateClause;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAUpdateClause;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.lang.NonNull;

import javax.persistence.EntityManager;

class OutputQueryDSLSupport<T> extends QuerydslRepositorySupport {
    public OutputQueryDSLSupport(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass);
        setEntityManager(entityManager);
        validate();
    }

    @NonNull
    @Override
    public Querydsl getQuerydsl() {
        return super.getQuerydsl();
    }

    @Override
    public JPQLQuery<Object> from(EntityPath<?>... paths) {
        return super.from(paths);
    }

    @Override
    public <U> JPQLQuery<U> from(EntityPath<U> path) {
        return super.from(path);
    }

    @Override
    public DeleteClause<JPADeleteClause> delete(EntityPath<?> path) {
        return super.delete(path);
    }

    @Override
    public UpdateClause<JPAUpdateClause> update(EntityPath<?> path) {
        return super.update(path);
    }

    @Override
    public PathBuilder<T> getBuilder() {
        return super.getBuilder();
    }
}
