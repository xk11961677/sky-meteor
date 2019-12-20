package com.sky.meteor.rpc.filter;

import com.sky.meteor.common.exception.RpcException;
import com.sky.meteor.rpc.Invocation;
import com.sky.meteor.rpc.Invoker;

/**
 * @author
 */
public interface Filter {

    /**
     *
     * @param invoker
     * @param invocation
     * @param <T>
     * @return
     * @throws RpcException
     */
    <T> T invoke(Invoker invoker, Invocation invocation) throws RpcException;
}
