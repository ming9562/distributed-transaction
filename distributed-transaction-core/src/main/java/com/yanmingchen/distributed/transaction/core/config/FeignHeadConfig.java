package com.yanmingchen.distributed.transaction.core.config;

import com.yanmingchen.distributed.transaction.core.connection.TransactionThreadLocalUtils;
import com.yanmingchen.distributed.transaction.core.constant.CommonConstant;
import com.yanmingchen.distributed.transaction.core.utils.CommonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class FeignHeadConfig implements RequestInterceptor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null) {
                Map<String, String> headers = new HashMap<>(2);
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    String value = request.getHeader(name);
                    /**
                     * 遍历请求头里面的属性字段，将groupId添加到新的请求头中转发到下游服务
                     * */
                    if (CommonConstant.GROUP_ID.equalsIgnoreCase(name)) {
                        logger.debug("添加自定义请求头key:" + name + ",value:" + value);
                        headers.put(name, value);
                    } else {
                        logger.debug("FeignHeadConfiguration", "非自定义请求头key:" + name + ",value:" + value + "不需要添加!");
                    }
                }
                if (headers.containsKey(CommonConstant.GROUP_ID)) {
                    template.header(CommonConstant.GROUP_ID, headers.get(CommonConstant.GROUP_ID));
                    template.header(CommonConstant.TRANSACTION_ID, CommonUtils.getWorkerId().toString());
                } else {
                    Map<String, String> map = TransactionThreadLocalUtils.get();
                    template.header(CommonConstant.GROUP_ID, map.get(CommonConstant.GROUP_ID));
                    template.header(CommonConstant.TRANSACTION_ID, CommonUtils.getWorkerId().toString());
                }
            } else {
                logger.warn("FeignHeadConfiguration", "获取请求头失败！");
            }
        }
    }

}