package com.graduation.GMS.Config;

import com.pusher.rest.Pusher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PusherConfig {

    @Bean
    public Pusher pusher() {
        Pusher pusher = new Pusher("2003345", "14b9b936cf3d2b6a6503", "593edadd6eeebfbb83aa");
        pusher.setCluster("ap2");
        pusher.setEncrypted(true);
        return pusher;
    }

}
