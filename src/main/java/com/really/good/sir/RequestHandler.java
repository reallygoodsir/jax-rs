package com.really.good.sir;

import java.lang.reflect.Method;
import java.util.List;

class RequestHandler {
    final Object instance;
    final Method method;
    final List<String> consumes;
    final List<String> produces;

    RequestHandler(Object instance, Method method, List<String> consumes, List<String> produces) {
        this.instance = instance;
        this.method = method;
        this.consumes = consumes;
        this.produces = produces;
    }

    @Override
    public String toString() {
        return "RequestHandler{" +
                "instance=" + instance +
                ", method=" + method +
                ", consumes=" + consumes +
                ", produces=" + produces +
                '}';
    }
}
