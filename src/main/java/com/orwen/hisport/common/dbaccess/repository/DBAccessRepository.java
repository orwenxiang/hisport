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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Transactional
@NoRepositoryBean
public interface DBAccessRepository<T> extends JpaRepository<T, Long>,
        QuerydslPredicateExecutor<T>, JpaSpecificationExecutor<T> {
    JPQLQuery<T> select();

    <U> JPQLQuery<U> select(Expression<U> path);

    UpdateClause<JPAUpdateClause> update();

    UpdateClause<JPAUpdateClause> update(EntityPath<?> path);

    DeleteClause<JPADeleteClause> delete();

    DeleteClause<JPADeleteClause> delete(EntityPath<?> path);

    PathBuilder<T> builder();

    default Stream<T> stream() {
        return stream((Predicate) null);
    }

    default Stream<T> stream(Predicate predicate) {
        return stream(predicate, (Sort) null);
    }

    default Stream<T> stream(OrderSpecifier<?>... sort) {
        return stream(null, sort);
    }

    Stream<T> stream(@Nullable Predicate predicate, @Nullable Sort sort);

    Stream<T> stream(@Nullable Predicate predicate, OrderSpecifier<?>... sort);
}
