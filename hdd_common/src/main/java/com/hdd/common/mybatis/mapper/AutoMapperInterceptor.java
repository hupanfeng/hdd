package com.hdd.common.mybatis.mapper;

import java.sql.Connection;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
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

import com.hdd.common.mybatis.mapper.builder.SqlBuilder;

/**
 * 通过拦截<code>StatementHandler</code>的<code>prepare</code>
 * 方法，根据参数parameterObject配置的注解信息，自动生成sql语句。 老规矩，签名里要拦截的类型只能是接口。
 * 
 * @author 湖畔微风
 * 
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class})})
public class AutoMapperInterceptor implements Interceptor {
    private static final Log logger = LogFactory.getLog(AutoMapperInterceptor.class);
    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final ReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        MetaObject metaStatementHandler = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY,
                DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
        // 分离代理对象链
        while (metaStatementHandler.hasGetter("h")) {
            Object object = metaStatementHandler.getValue("h");
            metaStatementHandler = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
        }
        // 分离最后一个代理对象的目标类
        while (metaStatementHandler.hasGetter("target")) {
            Object object = metaStatementHandler.getValue("target");
            metaStatementHandler = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
        }
        String originalSql = (String) metaStatementHandler.getValue("delegate.boundSql.sql");
        Configuration configuration = (Configuration) metaStatementHandler.getValue("delegate.configuration");
        Object parameterObject = metaStatementHandler.getValue("delegate.boundSql.parameterObject");

        if (null == originalSql || "".equals(originalSql)) {
            String newSql = "";
            MappedStatement mappedStatement = (MappedStatement) metaStatementHandler
                    .getValue("delegate.mappedStatement");
            String id = mappedStatement.getId();
            id = id.substring(id.lastIndexOf(".") + 1);
            if ("insert".equals(id)) {
                newSql = SqlBuilder.buildInsertSql(parameterObject);
            } else if ("update".equals(id)) {
                newSql = SqlBuilder.buildUpdateSql(parameterObject);
            } else if ("delete".equals(id)) {
                newSql = SqlBuilder.buildDeleteSql(parameterObject);
            } else if ("select".equals(id)) {
                newSql = SqlBuilder.buildSelectSql(parameterObject);
            }
            logger.debug("Auto generated sql:" + newSql);
            SqlSource sqlSource = buildSqlSource(configuration, newSql, parameterObject.getClass());
            List<ParameterMapping> parameterMappings = sqlSource.getBoundSql(parameterObject).getParameterMappings();
            metaStatementHandler.setValue("delegate.boundSql.sql", sqlSource.getBoundSql(parameterObject).getSql());
            metaStatementHandler.setValue("delegate.boundSql.parameterMappings", parameterMappings);
        }
        // 调用原始statementHandler的prepare方法完成原本的逻辑
        statementHandler = (StatementHandler) metaStatementHandler.getOriginalObject();
        statementHandler.prepare((Connection) invocation.getArgs()[0]);
        // 传递给下一个拦截器处理
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }

    private SqlSource buildSqlSource(Configuration configuration, String originalSql, Class<?> parameterType) {
        SqlSourceBuilder builder = new SqlSourceBuilder(configuration);
        return builder.parse(originalSql, parameterType, null);
    }
}
