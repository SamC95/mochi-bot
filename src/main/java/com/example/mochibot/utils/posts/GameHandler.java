package com.example.mochibot.utils.posts;

import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Mono;

public interface GameHandler {
    Mono<Void> handleScheduledPost(GatewayDiscordClient gateway);
}
