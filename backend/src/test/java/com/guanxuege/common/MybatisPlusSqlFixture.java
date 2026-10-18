package com.guanxuege.common;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;

/**
 * 测试夹具：在纯单元测试（无 Spring 上下文）里初始化 MyBatis-Plus 的表元数据。
 * <p>
 * 背景：LambdaWrapper 对列名的解析是「懒解析」的。
 * {@code eq(Post::getCommunity, "voice")} 只是把方法引用暂存进条件片段，
 * 要等到真正生成 SQL 时才翻译成 {@code community = ?}，而翻译依赖 TableInfo 缓存。
 * Spring 启动时由 MyBatis-Plus 自动建好这份缓存，但纯 Mockito 单测里它是空的，
 * 一旦调用 getCustomSqlSegment() 就会抛
 * 「can not find lambda cache for this entity」。
 * <p>
 * 补上这一步之后，测试就能直接断言拼出来的 SQL 条件，
 * 从而验证「查询帖子 / 圈子时必须带上 community 过滤」这类真正重要的不变量。
 */
public final class MybatisPlusSqlFixture {

    private static final MapperBuilderAssistant ASSISTANT = newAssistant();

    private MybatisPlusSqlFixture() {
    }

    /** 为指定实体建好 TableInfo 缓存（幂等，重复调用无副作用） */
    public static void initTableInfo(Class<?>... entityClasses) {
        for (Class<?> entityClass : entityClasses) {
            TableInfoHelper.initTableInfo(ASSISTANT, entityClass);
        }
    }

    private static MapperBuilderAssistant newAssistant() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        assistant.setCurrentNamespace("unit-test");
        return assistant;
    }
}
