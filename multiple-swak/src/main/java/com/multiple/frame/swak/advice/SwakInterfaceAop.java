package com.multiple.frame.swak.advice;


import com.multiple.frame.swak.annotation.SwakBiz;
import com.multiple.frame.swak.entity.InterfaceExecuteInfo;
import com.multiple.frame.swak.entity.SwakContext;
import com.multiple.frame.swak.entity.SwakLocal;
import com.multiple.frame.swak.register.SwakRegister;
import com.multiple.frame.swak.utils.ClassUtils;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @author: junqing.li
 * @date: 2019/9/4
 */
@Getter
@Setter
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Aspect
public class SwakInterfaceAop {

    @Autowired
    private SwakRegister swakRegister;

    /**
     * 拦截类上的 注解用 @within
     */
    @Pointcut("@within(com.multiple.frame.swak.annotation.SwakBiz)")
    private void annotationPoint() {

    }


    @Around("annotationPoint()")
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {

        try {

            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Object[] args = joinPoint.getArgs();

            if (ClassUtils.isOriginMethod(method)) {
                return joinPoint.proceed(args);
            }

            SwakBiz swakBiz = AnnotationUtils.findAnnotation(joinPoint.getThis().getClass(), SwakBiz.class);
            if (Objects.isNull(swakBiz)) {
                return joinPoint.proceed(args);
            }

            SwakContext swakContext = getContext(args);
            InterfaceExecuteInfo selectInfo = new InterfaceExecuteInfo();
            BeanUtils.copyProperties(swakContext, selectInfo);
            selectInfo.setTarget(joinPoint.getThis());

            // 寻找的目标执行信息
            InterfaceExecuteInfo executeInfo = swakRegister.lookUp(selectInfo);
            Assert.notNull(executeInfo, "no find interface execute info");

            // 获取真实对象
            Class<?> clazz = AopUtils.getTargetClass(executeInfo.getTarget());
            // 获得对应的method
            Method executeMethod = ClassUtils.getMethod(clazz, method.getName(), method.getParameterTypes());

            // 判断是否是代理对象
            Object bean = executeInfo.getTarget();
            if (AopUtils.isAopProxy(bean)) {
                bean = AopProxyUtils.getSingletonTarget(executeInfo.getTarget());
            }
            return executeMethod.invoke(bean, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);

        } finally {

            SwakLocal.getCurrent().clear();
        }
    }


    private SwakContext getContext(Object[] args) {

        SwakContext swakContext = getContextByParam(args);
        if (Objects.nonNull(swakContext)) {
            return swakContext;
        }

        // 从local中取
        swakContext = SwakLocal.getCurrent().getContext();
        if (Objects.nonNull(swakContext)) {
            return swakContext;
        }

        Assert.notNull(swakContext, "swak must set swak context");
        return swakContext;
    }

    private SwakContext getContextByParam(Object[] args) {

        if (ObjectUtils.isEmpty(args)) {
            return null;
        }

        Optional<SwakContext> optional = Arrays.stream(args).filter(object -> object.getClass()
                .isAssignableFrom(SwakContext.class)).map(object -> (SwakContext) object).findFirst();
        if (!optional.isPresent()) {
            return null;
        }
        return optional.get();
    }
}
