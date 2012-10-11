package com.xunlei.starter;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.xunlei.netty.httpserver.Bootstrap;
import com.xunlei.spring.BeanUtil;

/**
 * @author ZengDong
 * @since 2011-10-5 ÏÂÎç05:05:37
 */
@Service
public class XdriveLaunch {

   
    public static void main(String[] args) throws IOException {
        Bootstrap.main(args, new Runnable() {

            @Override
            public void run() {
                XdriveLaunch launch = BeanUtil.getTypedBean(XdriveLaunch.class);
                launch.start();
            }
        }, "classpath:xdriveApplicationContext.xml");
    }

    private void start() {}
}
