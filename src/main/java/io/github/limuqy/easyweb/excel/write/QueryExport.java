package io.github.limuqy.easyweb.excel.write;

import cn.idev.excel.converters.Converter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.limuqy.easyweb.core.function.Func2;
import io.github.limuqy.easyweb.core.util.BeanUtil;
import io.github.limuqy.easyweb.core.util.LambdaUtil;
import io.github.limuqy.easyweb.core.util.ServletUtil;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class QueryExport<T, M> {
    private final QueryWrapper<M> wrapper;
    private Func2<Page<M>, QueryWrapper<M>, List<M>> listQuery;
    private Function<List<M>, List<T>> mapFun;
    private Function<List<T>, List<T>> applyFun;
    private final SimpleExport<T> simpleExport;

    private QueryExport(QueryWrapper<M> wrapper, Class<T> clazz) {
        this.wrapper = wrapper;
        this.simpleExport = SimpleExport.build(clazz);
        mapFun = (List<M> list) -> {
            if (clazz == wrapper.getEntityClass()) {
                return list.stream().map(clazz::cast).toList();
            }
            return BeanUtil.copyToList(list, clazz);
        };
    }

    /**
     * @param wrapper 条件构造器
     * @param <T>     实际导出的类型
     */
    public static <T> QueryExport<T, T> build(QueryWrapper<T> wrapper) {
        return new QueryExport<>(wrapper, wrapper.getEntityClass());
    }

    /**
     * @param wrapper 条件构造器
     * @param clazz   实际导出的类
     * @param <T>     实际导出的类型
     * @param <M>     分页查询的类型
     */
    public static <T, M> QueryExport<T, M> build(QueryWrapper<M> wrapper, Class<T> clazz) {
        return new QueryExport<>(wrapper, clazz);
    }

    public QueryExport<T, M> limit(Integer limit) {
        simpleExport.limit(limit);
        return this;
    }

    /**
     * 设置输出流
     *
     * @param outputStream 输出流
     */
    public QueryExport<T, M> out(OutputStream outputStream) {
        simpleExport.out(outputStream);
        return this;
    }

    public QueryExport<T, M> out(HttpServletResponse response, String fileName) {
        try {
            ServletUtil.processResponse(fileName, response);
            return out(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public QueryExport<T, M> out(HttpServletResponse response) {
        return out(response, "export_%s.xlsx".formatted(System.currentTimeMillis()));
    }

    public QueryExport<T, M> converter(Converter<?> converter) {
        simpleExport.converter(converter);
        return this;
    }

    /**
     * 查询数据
     *
     * @param listQuery 查询获取后续数据
     */
    public QueryExport<T, M> query(Func2<Page<M>, QueryWrapper<M>, List<M>> listQuery) {
        this.listQuery = listQuery;
        return this;
    }

    /**
     * 类型转换，用于查询类型和导出类型不一致时，或者需要对查询结果集处理
     *
     * @param mapFun 转换方法
     */
    public QueryExport<T, M> map(Function<List<M>, List<T>> mapFun) {
        this.mapFun = mapFun;
        return this;
    }

    /**
     * 对查询结果集处理
     *
     * @param applyFun 处理方法
     */
    public QueryExport<T, M> apply(Function<List<T>, List<T>> applyFun) {
        this.applyFun = applyFun;
        return this;
    }

    /**
     * 对查询结果集处理
     */
    public QueryExport<T, M> include(Collection<String> includeColumnFieldNames) {
        simpleExport.include(includeColumnFieldNames);
        return this;
    }

    /**
     * 对查询结果集处理
     */
    public QueryExport<T, M> exclude(Collection<String> excludeColumnFieldNames) {
        simpleExport.exclude(excludeColumnFieldNames);
        return this;
    }

    /**
     * 对查询结果集处理
     */
    @SafeVarargs
    public final QueryExport<T, M> include(SFunction<T, ?>... fields) {
        simpleExport.include(Arrays.stream(fields).map(LambdaUtil::getFieldName).toList());
        return this;
    }

    /**
     * 对查询结果集处理
     */
    @SafeVarargs
    public final QueryExport<T, M> exclude(SFunction<T, ?>... fields) {
        simpleExport.exclude(Arrays.stream(fields).map(LambdaUtil::getFieldName).toList());
        return this;
    }

    public QueryExport<T, M> head(List<List<String>> head) {
        simpleExport.head(head);
        return this;
    }

    public QueryExport<T, M> addHead(List<String> head) {
        simpleExport.addHead(head);
        return this;
    }

    /**
     * 执行导出
     */
    public void doExport() {
        simpleExport.query((pageNum, pageSize) -> {
            Page<M> page = Page.of(pageNum, pageSize);
            List<M> list = listQuery.apply(page, wrapper);
            List<T> res = mapFun.apply(list);
            if (applyFun != null) {
                res = applyFun.apply(res);
            }
            return res;
        }).doExport();
    }
}
