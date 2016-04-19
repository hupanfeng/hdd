package com.hdd.common.mybatis.batch;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.logging.jdbc.ConnectionLogger;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;

/**
 * 修正使用sql自动生成插件导致的BatchExecutor无法正常使用的BUG。
 * 如果项目中配置了sql自动生成插件则务必要同时配置本插件，
 * 若没有使用或不使用批量更新功能则没必要使用本插件。
 *
 * @author 湖畔微风
 */
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class BatchInterceptor implements Interceptor {
    private static final Log logger = LogFactory.getLog(BatchInterceptor.class);
    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final ReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Executor executorProxy = (Executor) invocation.getTarget();
        MetaObject metaExecutor = MetaObject.forObject(executorProxy, DEFAULT_OBJECT_FACTORY,
                DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
        // 分离代理对象链
        while (metaExecutor.hasGetter("h")) {
            Object object = metaExecutor.getValue("h");
            metaExecutor = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
        }
        // 分离最后一个代理对象的目标类
        while (metaExecutor.hasGetter("target")) {
            Object object = metaExecutor.getValue("target");
            metaExecutor = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
        }

        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        // 如果开启了cache，则从CachingExecutor里分离委派的目标类-BatchExecutor
        Executor executor = null;
        if (metaExecutor.hasGetter("delegate")) {
            executor = (Executor) metaExecutor.getValue("delegate");
        } else {
            executor = (Executor) metaExecutor.getOriginalObject();
        }
        if (executor instanceof BatchExecutor) {
            metaExecutor = MetaObject.forObject(executor, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
            return this.update(metaExecutor, ms, parameterObject);
        } else {
            return executor.update(ms, parameterObject);
        }
    }

    public int update(MetaObject metaExecutor, MappedStatement ms, Object parameterObject) throws SQLException {

        ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());
        BatchExecutor batchExecutor = (BatchExecutor) metaExecutor.getOriginalObject();
        if (batchExecutor.isClosed())
            throw new ExecutorException("Executor was closed.");
        batchExecutor.clearLocalCache();

        final Configuration configuration = ms.getConfiguration();
        final StatementHandler handler = configuration.newStatementHandler(batchExecutor, ms, parameterObject,
                RowBounds.DEFAULT, null, null);

        Connection connection = getConnection((Transaction) metaExecutor.getValue("transaction"), ms.getStatementLog());
        Statement stmt = handler.prepare(connection);
        BoundSql boundSql = handler.getBoundSql();
        String sql = boundSql.getSql();

        String currentSql = (String) metaExecutor.getValue("currentSql");
        MappedStatement currentStatement = (MappedStatement) metaExecutor.getValue("currentStatement");
        List<Statement> statementList = (List<Statement>) metaExecutor.getValue("statementList");
        List<BatchResult> batchResultList = (List<BatchResult>) metaExecutor.getValue("batchResultList");
        if (sql.equals(currentSql) && ms.equals(currentStatement)) {
            int last = statementList.size() - 1;
            // 关闭prepare方法打开的Statement
            stmt.close();
            // 复用存在的Statement以提高batch的效率
            stmt = statementList.get(last);
            BatchResult batchResult = batchResultList.get(last);
            batchResult.addParameterObject(parameterObject);
        } else {
            metaExecutor.setValue("currentSql", sql);
            metaExecutor.setValue("currentStatement", ms);
            statementList.add(stmt);
            batchResultList.add(new BatchResult(ms, sql, parameterObject));
        }
        handler.parameterize(stmt);
        handler.batch(stmt);
        return BatchExecutor.BATCH_UPDATE_RETURN_VALUE;
    }

    /**
     * 只拦截BatchExecutor，其他的直接返回目标本身
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }

    private Connection getConnection(Transaction transaction, Log statementLog) throws SQLException {
        Connection connection = transaction.getConnection();
        if (statementLog.isDebugEnabled()) {
            return ConnectionLogger.newInstance(connection, statementLog, 0);
        } else {
            return connection;
        }
    }

}
