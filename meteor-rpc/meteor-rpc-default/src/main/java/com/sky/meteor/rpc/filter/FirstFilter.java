package com.sky.meteor.rpc.filter;

import com.sky.meteor.common.exception.RpcException;
import com.sky.meteor.common.spi.SpiMetadata;
import com.sky.meteor.rpc.Invocation;
import com.sky.meteor.rpc.Invoker;
import lombok.extern.slf4j.Slf4j;

/**
 * @author
 */
@Slf4j
@SpiMetadata(name = "firstFilter")
public class FirstFilter implements Filter {

    @Override
    public <T> T invoke(Invoker invoker, Invocation invocation) throws RpcException {
        log.info("first filter :{}");
        return invoker.invoke(invocation);
    }
}
