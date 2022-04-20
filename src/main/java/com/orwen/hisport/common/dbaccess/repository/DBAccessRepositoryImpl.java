package com.orwen.hisport.common.dbaccess.repository;


import com.querydsl.core.dml.DeleteClause;
import com.querydsl.core.dml.UpdateClause;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAUpdateClause;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;

import javax.persistence.EntityManager;
import java.util.stream.Stream;

@Slf4j
@NoRepositoryBean
public class DBAccessRepositoryImpl<T> extends QuerydslJpaRepository<T, Long>
        implements DBAccessRepository<T> {
    private final OutputQueryDSLSupport<T> dslSupport;
    private final EntityPath<T> path;

    public DBAccessRepositoryImpl(JpaEntityInformation<T, Long> entityInformation,
                                  EntityManager entityManager) {
        this(entityInformation, entityManager, SimpleEntityPathResolver.INSTANCE);
    }

    public DBAccessRepositoryImpl(JpaEntityInformation<T, Long> entityInformation,
                                  EntityManager entityManager,
                                  EntityPathResolver resolver) {
        super(entityInformation, entityManager, resolver);
        this.path = resolver.createPath(entityInformation.getJavaType());
        dslSupport = new OutputQueryDSLSupport<>(entityInformation.getJavaType(), entityManager);
    }


    @Override
    public DeleteClause<JPADeleteClause> delete() {
        return dslSupport.delete(path);
    }

    @Override
    public DeleteClause<JPADeleteClause> delete(EntityPath<?> path) {
        return dslSupport.delete(path);
    }

    @Override
    public UpdateClause<JPAUpdateClause> update() {
        return dslSupport.update(path);
    }

    @Override
    public UpdateClause<JPAUpdateClause> update(EntityPath<?> path) {
        return dslSupport.update(path);
    }

    @Override
    public PathBuilder<T> builder() {
        return dslSupport.getBuilder();
    }

    @Override
    public JPQLQuery<T> select() {
        return dslSupport.from(path);
    }

    @Override
    public <U> JPQLQuery<U> select(Expression<U> expression) {
        return dslSupport.from(path).select(expression);
    }

    @Override
    public Stream<T> stream(@Nullable Predicate predicate, @Nullable Sort sort) {
        if (sort == null) {
            return createQuery(predicate).select(path).stream();
        }
        return dslSupport.getQuerydsl()
                .applySorting(sort, createQuery(predicate).select(path)).stream();
    }

    @Override
    public Stream<T> stream(@Nullable Predicate predicate, OrderSpecifier<?>... sort) {
        return createQuery(predicate).select(path).orderBy(sort).stream();
    }
}
