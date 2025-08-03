package dev.pizzawunsch.moduledmysql.builders;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryGroup {
    private final StringBuilder query = new StringBuilder();
    @Getter
    private final List<Object> parameters = new ArrayList<>();

    public QueryGroup and(String condition, Object... values) {
        if (query.length() > 0) query.append(" AND ");
        query.append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    public QueryGroup or(String condition, Object... values) {
        if (query.length() > 0) query.append(" OR ");
        query.append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    public String getQuery() {
        return query.toString();
    }
}