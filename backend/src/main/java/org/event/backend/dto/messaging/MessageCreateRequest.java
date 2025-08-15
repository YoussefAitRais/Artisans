package org.event.backend.dto.messaging;

import jakarta.validation.constraints.NotBlank;

/** Payload to send a message. */
public class MessageCreateRequest {
    @NotBlank
    private String body;

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
