package com.graduation.GMS.Config;

import com.pusher.rest.Pusher;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@AllArgsConstructor
public class PusherConfig {

    @Bean
    public Pusher pusher() {
        Pusher pusher = new Pusher("2003345", "d044493efec8a33cec65", "b7bd750753e1a276b885");
        pusher.setCluster("ap2");
        pusher.setEncrypted(true);
        return pusher;
    }

}
