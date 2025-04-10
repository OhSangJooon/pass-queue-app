package com.aptner.pass.app.queue.model;

import lombok.Builder;

@Builder
public record QueueStatusResponse(
        String status,
        int position
) {
}
