package com.kyxs.cloud.core.base.plugin;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.kyxs.cloud.core.base.utils.UserInfoUtil;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wangsf
 * @since 2023/2/26
 * @description 拦截所有的INSERT和UPDATE并添加create_time,update_time,creator,operator,
 */
@Intercepts({@Signature(type = StatementHandler.class,method = "prepare", args = {Connection.class, Integer.class})})
//@Slf4j
// mybatis的拦截器在springboot中必须添加@Component注解注入到容器中，否则不会生效
@Component
public class InsertUpdateInterceptor implements Interceptor {

    private static final Log log = LogFactory.getLog(InsertUpdateInterceptor.class);

    //以 yyyy-MM-dd HH:mm:ss 格式化的话会报错 TimestampValue不接受这种格式
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final String OPERTOR_COLUMN="operator";

    private static final String CREATOR_COLUMN="creator";

    private static final String UPDATE_TIME_COLUMN="update_time";

    private static final String CREATE_TIME_COLUMN ="create_time";

    private static final String NO_AUTO_SUFFIX="NoAutoAddSuffix";




    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //获取操作人Id
        long userId = 0L;
        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement)metaObject.getValue("delegate.mappedStatement");

        // 如果不是UPDATE和INSERT就直接跳过拦截
        SqlCommandType sqlCmdType = mappedStatement.getSqlCommandType();
        log.debug("查询类型为："+sqlCmdType.name());
        if(sqlCmdType != SqlCommandType.UPDATE && sqlCmdType != SqlCommandType.INSERT) {
            return invocation.proceed();
        }
        String namespace = mappedStatement.getId();
        String methodName= namespace.substring(namespace.lastIndexOf(".") + 1,namespace.length());
        if (methodName.endsWith(NO_AUTO_SUFFIX)){
            log.debug("执行方法以"+NO_AUTO_SUFFIX+"结尾，不进行自动填充");
            return invocation.proceed();
        }
        try {
            userId = UserInfoUtil.getUserInfo().getUserId();
            if (userId==0L){
                log.warn("Mybatis拦截器未获取到当前用户");
            }
        } catch (Exception e) {
            log.warn("Mybatis拦截器未获取到当前用户");
        }
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        //获取原始sql
        String originalSql = boundSql.getSql();
        log.debug("原始sql: "+originalSql);
        String newSql=null;

        if (sqlCmdType==SqlCommandType.INSERT){
            newSql=insertData(originalSql,userId);
        }
        if (sqlCmdType==SqlCommandType.UPDATE){
            newSql=updateData(originalSql,userId);
        }

        if (newSql.length()>0){
            log.debug("更新后的sql语句为: "+newSql);
            metaObject.setValue("delegate.boundSql.sql", newSql);
        }
        return invocation.proceed();
    }

    public String insertData(String originalSql, long operateUserId){
        Statement stmt=null;
        String[] split = originalSql.split(";");
        try {
            for (int i = 0; i < split.length; i++) {
                // 这东西不知道在openjdk下会不会有。
                String sql = split[i];
                log.debug("转换前的sql:"+sql);
                stmt = CCJSqlParserUtil.parse(sql);
                if (!(stmt instanceof Insert)){
                    return originalSql;
                }
                Insert insert=(Insert) stmt;
                //字段列
                List<Column> columns = insert.getColumns();
                // 获取参数集合
                ItemsList itemsList = insert.getItemsList();
                //创建字段值
                TimestampValue timestamp = new TimestampValue(TIMESTAMP_FORMAT.format(new Date()));
                LongValue operatePerson = new LongValue(operateUserId);
                if (itemsList instanceof ExpressionList){
                    ExpressionList expressionList=(ExpressionList)itemsList;
                    List<Expression> expressions = expressionList.getExpressions();
                    // 添加创建时间和创建人
                    if (!contains(columns, CREATE_TIME_COLUMN)){
                        columns.add(new Column(CREATE_TIME_COLUMN));
                        expressions.add(timestamp);
                    }
                    if (!contains(columns,CREATOR_COLUMN)){
                        columns.add(new Column(CREATOR_COLUMN));
                        expressions.add(operatePerson);
                    }
                }else if(itemsList instanceof MultiExpressionList) { //批量插入
                    MultiExpressionList multiExpressionList=(MultiExpressionList) itemsList;
                    List<ExpressionList> exprList = multiExpressionList.getExprList();
                    if (!contains(columns, CREATE_TIME_COLUMN)){
                        columns.add(new Column(CREATE_TIME_COLUMN));
                        exprList.forEach(item->{
                            List<Expression> expressions = item.getExpressions();
                            expressions.add(timestamp);
                        });
                    }
                    if (!contains(columns,CREATOR_COLUMN)){
                        columns.add(new Column(CREATOR_COLUMN));
                        exprList.forEach(item->{
                            List<Expression> expressions = item.getExpressions();
                            expressions.add(operatePerson);
                        });
                    }
                }
                split[i]=sql;
            }
            originalSql = Arrays.asList(split).stream().collect(Collectors.joining(";"));
        } catch (JSQLParserException e) {
            e.printStackTrace();
            return originalSql;
        }
        return stmt.toString();
    }

    public String updateData(String originalSql, long operateUserId){
        Statement stmt=null;
        try {
            stmt= CCJSqlParserUtil.parse(originalSql);
            if (!(stmt instanceof Update)){
                return originalSql;
            }
            Update update=(Update) stmt;
            List<UpdateSet> updateSets = update.getUpdateSets();
            List<Column> columns = new ArrayList<>();
            for(UpdateSet updateSet:updateSets){
                columns.addAll(updateSet.getColumns());
            }
            if(!contains(columns,UPDATE_TIME_COLUMN)) {
                update.addUpdateSet(new UpdateSet(new Column(UPDATE_TIME_COLUMN), new TimestampValue(TIMESTAMP_FORMAT.format(new Date()))));
            }
            if (!contains(columns,OPERTOR_COLUMN)){
                update.addUpdateSet(new UpdateSet(new Column(OPERTOR_COLUMN), new LongValue(operateUserId)));
            }
            return update.toString();

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        return stmt.toString();
    }

    private boolean contains(List<Column> columns, String columnName){
        if(columns == null || columns.size() <= 0){
            return false;
        }
        if(columnName == null || columnName.length() <= 0){
            return false;
        }
        for(Column column : columns){
            if(column.getColumnName().equalsIgnoreCase(columnName)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target,this);
    }

    @Override
    public void setProperties(Properties properties) {
    }

}