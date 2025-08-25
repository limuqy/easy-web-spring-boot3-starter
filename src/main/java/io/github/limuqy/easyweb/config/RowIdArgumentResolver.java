package io.github.limuqy.easyweb.config;

import io.github.limuqy.easyweb.core.annotation.RowId;
import io.github.limuqy.easyweb.core.exception.RowIdException;
import io.github.limuqy.easyweb.core.util.IOUtil;
import io.github.limuqy.easyweb.core.util.JsonUtil;
import io.github.limuqy.easyweb.core.util.RowIdUtil;
import io.github.limuqy.easyweb.core.util.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.data.util.CastUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class RowIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(RowId.class);
    }

    @Override
    public Object resolveArgument(@Nonnull MethodParameter parameter, ModelAndViewContainer mavContainer, @Nonnull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws IOException {
        Class<?> type = parameter.getParameter().getType();
        String name = parameter.getParameter().getName();
        try {
            RowId annotation = parameter.getParameterAnnotation(RowId.class);
            if (annotation == null) {
                return null;
            }
            String[] values = webRequest.getParameterValues(name);
            if ((values == null && !Long.class.equals(type)) || parameter.getMethodAnnotation(PostMapping.class) != null) {
                HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
                if (request == null) {
                    return null;
                }
                String text = IOUtil.read(request.getInputStream(), StandardCharsets.UTF_8);
                if (StringUtil.isNotEmpty(text) && !"{}".equals(text) && !"[]".equals(text)) {
                    return getRequestBody(type, name, text, annotation);
                }
            }
            return getParameterValue(type, values, annotation);
        } catch (Exception e) {
            log.error("RowId参数处理！", e);
            if (e instanceof RowIdException rowIdException) {
                rowIdException.mappingErr(name, type, name);
                throw e;
            }
            throw new RowIdException(HttpStatus.BAD_REQUEST);
        }
    }

    private Object getRequestBody(Class<?> type, String name, String text, RowId rowId) {
        if (StringUtil.isBlank(text)) {
            return null;
        }
        String tableName = RowIdUtil.getEntityTableName(rowId, null);
        if (Collection.class.isAssignableFrom(type)) {
            List<String> list;
            if (text.startsWith("{")) {
                Map<?, ?> map = JsonUtil.parseObject(text, Map.class);
                if (map.get(name) instanceof List<?> data) {
                    list = CastUtils.cast(data);
                } else {
                    throw new RowIdException(HttpStatus.BAD_REQUEST);
                }
            } else {
                list = JsonUtil.parseArray(text, String.class);
            }
            return list.stream().map(id -> RowIdUtil.decryptRowId(id, tableName)).toList();
        } else if (String.class.equals(type)) {
            return String.valueOf(RowIdUtil.decryptRowId(text, tableName));
        } else if (Number.class.isAssignableFrom(type)) {
            return RowIdUtil.decryptRowId(text, tableName);
        } else if (type.isArray()) {
            List<Long> list = JsonUtil.parseArray(text, String.class)
                    .stream()
                    .map(id -> RowIdUtil.decryptRowId(id, tableName))
                    .toList();
            return list.toArray(new Long[0]);
        }
        return null;
    }

    private Object getParameterValue(Class<?> type, String[] values, RowId rowId) {
        if (values == null || values.length == 0) {
            return null;
        }
        String tableName = RowIdUtil.getEntityTableName(rowId, null);
        if (Collection.class.isAssignableFrom(type)) {
            List<String> strList = new ArrayList<>();
            for (String value : values) {
                if (value.contains(",")) {
                    strList.addAll(Arrays.stream(value.split(",")).toList());
                } else {
                    strList.add(value);
                }
            }
            return strList.stream().map(id -> RowIdUtil.decryptRowId(id, tableName)).toList();
        } else if (String.class.equals(type)) {
            return String.valueOf(RowIdUtil.decryptRowId(values[0], tableName));
        } else if (Number.class.isAssignableFrom(type)) {
            return RowIdUtil.decryptRowId(values[0], tableName);
        } else if (type.isArray()) {
            return Arrays.stream(values).map(id -> RowIdUtil.decryptRowId(id, tableName)).toList().toArray(new Long[0]);
        }
        return null;
    }

}
