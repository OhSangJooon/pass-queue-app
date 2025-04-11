package com.aptner.pass.app.queue.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public record QueueStatusRequest(
        @JsonProperty("userId")
        String userId
) {
}
