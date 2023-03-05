package com.kyxs.cloud.core.base.interceptor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ParameterUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectModel;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import com.kyxs.cloud.core.base.mybatisplus.FilterItem;
import com.kyxs.cloud.core.base.mybatisplus.PageQuery;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

/**
 * 分页拦截器，对前端过滤条件主动追加查询，不再需要业务处理时单独拼接条件
 * Created by wangsf on 2023/2/25.
 * @De
 */
public class MyPaginationInnerInterceptor extends PaginationInnerInterceptor {
    static final Map<String, Function<String, Expression>> REGEX_2_EXPRESSION_MAP = new HashMap(16);

    public MyPaginationInnerInterceptor() {
    }

    public boolean willDoQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        IPage<?> page = (IPage) ParameterUtils.findPage(parameter).orElse(null);
        if (page != null && page.getSize() >= 0L && page.searchCount()) {
            MappedStatement countMs = this.buildCountMappedStatement(ms, page.countId());
            BoundSql countSql;
            if (countMs != null) {
                countSql = countMs.getBoundSql(parameter);
            } else {
                countMs = this.buildAutoCountMappedStatement(ms);
                String countSqlStr = this.autoCountSql(page, boundSql.getSql());
                PageQuery pageQuery = (PageQuery)page;
                List<FilterItem> filterItems = pageQuery.filters();
                if (CollectionUtils.isNotEmpty(filterItems)) {
                    countSqlStr = this.concatFilter(countSqlStr, filterItems);
                }

                PluginUtils.MPBoundSql mpBoundSql = PluginUtils.mpBoundSql(boundSql);
                countSql = new BoundSql(countMs.getConfiguration(), countSqlStr, mpBoundSql.parameterMappings(), parameter);
                PluginUtils.setAdditionalParameter(countSql, mpBoundSql.additionalParameters());
            }

            CacheKey cacheKey = executor.createCacheKey(countMs, parameter, rowBounds, countSql);
            List<Object> result = executor.query(countMs, parameter, rowBounds, resultHandler, cacheKey, countSql);
            long total = 0L;
            if (CollectionUtils.isNotEmpty(result)) {
                Object o = result.get(0);
                if (o != null) {
                    total = Long.parseLong(o.toString());
                }
            }

            page.setTotal(total);
            return this.continuePage(page);
        } else {
            return true;
        }
    }

    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        IPage<?> page = (IPage)ParameterUtils.findPage(parameter).orElse(null);
        if (null != page) {
            boolean addFilter = false;
            String buildSql = boundSql.getSql();
            PageQuery pageQuery = (PageQuery)page;
            List<FilterItem> filterItems = pageQuery.filters();
            if (CollectionUtils.isNotEmpty(filterItems)) {
                addFilter = true;
                buildSql = this.concatFilter(buildSql, filterItems);
            }

            boolean addOrdered = false;
            List<OrderItem> orders = page.orders();
            if (CollectionUtils.isNotEmpty(orders)) {
                addOrdered = true;
                buildSql = this.concatOrderBy(buildSql, orders);
            }

            Long _limit = page.maxLimit() != null ? page.maxLimit() : this.maxLimit;
            if (page.getSize() < 0L && null == _limit) {
                if (addFilter) {
                    PluginUtils.mpBoundSql(boundSql).sql(buildSql);
                }

                if (addOrdered) {
                    PluginUtils.mpBoundSql(boundSql).sql(buildSql);
                }
            } else {
                this.handlerLimit(page, _limit);
                IDialect dialect = this.findIDialect(executor);
                Configuration configuration = ms.getConfiguration();
                DialectModel model = dialect.buildPaginationSql(buildSql, page.offset(), page.getSize());
                PluginUtils.MPBoundSql mpBoundSql = PluginUtils.mpBoundSql(boundSql);
                List<ParameterMapping> mappings = mpBoundSql.parameterMappings();
                Map<String, Object> additionalParameter = mpBoundSql.additionalParameters();
                model.consumers(mappings, configuration, additionalParameter);
                mpBoundSql.sql(model.getDialectSql());
                mpBoundSql.parameterMappings(mappings);
            }
        }

    }

    public String concatFilter(String originalSql, List<FilterItem> filterItems) {
        try {
            Select select = (Select) CCJSqlParserUtil.parse(originalSql);
            SelectBody selectBody = select.getSelectBody();
            if (selectBody instanceof PlainSelect) {
                PlainSelect plainSelect = (PlainSelect)selectBody;
                Expression whereElementsReturn = this.addFilterElements(filterItems, plainSelect.getWhere());
                plainSelect.setWhere(whereElementsReturn);
                return select.toString();
            }

            if (selectBody instanceof SetOperationList) {
                SetOperationList setOperationList = (SetOperationList)selectBody;
                List<SelectBody> selectBodyList = setOperationList.getSelects();
                selectBodyList.forEach((s) -> {
                    PlainSelect ps = (PlainSelect)s;
                    Expression expression = this.addFilterElements(filterItems, ps.getWhere());
                    ps.setWhere(expression);
                });
                return select.toString();
            }

            if (selectBody instanceof WithItem) {
                return originalSql;
            }

            return originalSql;
        } catch (JSQLParserException var8) {
            this.logger.warn("failed to concat orderBy from IPage, exception:\n" + var8.getCause());
        } catch (Exception var9) {
            this.logger.warn("failed to concat orderBy from IPage, exception:\n" + var9);
        }

        return originalSql;
    }

    protected Expression addFilterElements(List<FilterItem> filterList, Expression where) {
        Expression exp = where;
        String type = "";
        Iterator var4 = filterList.iterator();

        while(var4.hasNext()) {
            FilterItem item = (FilterItem)var4.next();
            type = item.getType();
            if (exp == null) {
                exp = getExpression(item);
            } else if ("or".equals(type)) {
                exp = new OrExpression(exp, getExpression(item));
            } else {
                exp = new AndExpression(exp, getExpression(item));
            }
        }
        return exp;
    }

    private static Expression getExpression(FilterItem filterItem) {
        String operator = filterItem.getOperator();
        String column = filterItem.getColumn();
        String value = filterItem.getValue();
        Expression exp = null;
        Between between;
        String[] split;
        if ("between".equals(operator)) {
            between = new Between();
            split = value.split(",");
            between.setLeftExpression(new Column(column));
            between.setBetweenExpressionStart(new StringValue(split[0]));
            between.setBetweenExpressionEnd(new StringValue(split[1]));
            exp = between;
        } else if ("dateBetween".equals(operator)) {
            between = new Between();
            between.setLeftExpression(new Column(column));
            split = value.split(",");
            Iterator var7 = REGEX_2_EXPRESSION_MAP.entrySet().iterator();

            while(var7.hasNext()) {
                Map.Entry<String, Function<String, Expression>> entry = (Map.Entry)var7.next();
                String regex = entry.getKey();
                if (split[0].matches(regex)) {
                    between.setBetweenExpressionStart((Expression)((Function)entry.getValue()).apply(split[0]));
                }

                if (split[1].matches(regex)) {
                    between.setBetweenExpressionEnd((Expression)((Function)entry.getValue()).apply(split[1]));
                }
            }

            exp = between;
        } else if (!"in".equals(operator) && !"not in".equals(operator)) {
            if ("=".equals(operator)) {
                exp = new EqualsTo();
            } else if ("!=".equals(operator)) {
                exp = new NotEqualsTo();
            } else if (">".equals(operator)) {
                exp = new GreaterThan();
            } else if (">=".equals(operator)) {
                exp = new GreaterThanEquals();
            } else if ("<".equals(operator)) {
                exp = new MinorThan();
            } else if ("<=".equals(operator)) {
                exp = new MinorThanEquals();
            } else {
                IsNullExpression ex;
                Column columnExp;
                if ("is null".equals(operator)) {
                    ex = new IsNullExpression();
                    columnExp = new Column();
                    columnExp.setColumnName(column);
                    ex.setLeftExpression(columnExp);
                    return ex;
                }

                if ("is not null".equals(operator)) {
                    ex = new IsNullExpression();
                    columnExp = new Column();
                    columnExp.setColumnName(column);
                    ex.setLeftExpression(columnExp);
                    ex.setNot(true);
                    return ex;
                }

                if ("like%".equals(operator)) {
                    exp = new LikeExpression();
                    value = value + "%";
                } else if ("%like".equals(operator)) {
                    exp = new LikeExpression();
                    value = "%" + value;
                } else if ("like".equals(operator)) {
                    exp = new LikeExpression();
                    value = "%" + value + "%";
                } else if ("not like".equals(operator)) {
                    LikeExpression likeExpression = (LikeExpression)exp;
                    likeExpression.setNot(true);
                    exp = likeExpression;
                }
            }

            if (exp != null) {
                BinaryExpression ex = (BinaryExpression)exp;
                ex.setLeftExpression(new Column(column));
                ex.setRightExpression(new StringValue(value));
                exp = ex;
            }
        } else {
            String[] splits = value.split(",");
            List<Expression> valList = new ArrayList();
            String[] var17 = splits;
            int var19 = splits.length;

            for(int var21 = 0; var21 < var19; ++var21) {
                String s = var17[var21];
                valList.add(new StringValue(s));
            }

            InExpression inExpression = new InExpression();
            if ("not in".equals(operator)) {
                inExpression.setNot(true);
            }

            inExpression.setLeftExpression(new Column(column));
            ExpressionList expressionList = new ExpressionList();
            expressionList.setExpressions(valList);
            inExpression.setRightItemsList(expressionList);
            exp = inExpression;
        }

        return (Expression)exp;
    }
}
