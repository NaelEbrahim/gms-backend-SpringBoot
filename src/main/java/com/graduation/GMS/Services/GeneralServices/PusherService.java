package com.graduation.GMS.Services.GeneralServices;

import com.pusher.rest.Pusher;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Getter
@AllArgsConstructor
public class PusherService {

    private final Pusher pusher;

    @Async
    public void sendPusherEvent(String channel, String event, Map<String, String> payload) {
        pusher.trigger(channel, event, payload);
    }

}
