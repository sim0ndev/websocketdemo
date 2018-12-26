package com.allhealth.websocketdemo;
/**
 * @Auther: Administrator
 * @Date: 2018/12/24 17:57
 * @Description:
 */

/**
 * @ClassName WebSocketConfig
 * @Description TODO
 * @Auther: Administrator
 * @Date: 2018/12/24 17:57
 * @Version 1.0
 **/
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
