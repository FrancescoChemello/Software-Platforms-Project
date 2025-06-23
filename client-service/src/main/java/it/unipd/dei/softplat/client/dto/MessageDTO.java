/**
 * MessageDTO.java
 * 
 * @author Francesco Chemello
 * @version 1.0.0
 * @since 1.0.0
 */

package it.unipd.dei.softplat.client.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class MessageDTO {
    @NotNull @NotEmpty
    private String message;
    @NotNull @NotEmpty
    private String status;

    /**
     * Default constructor for MessageDTO.
     * This constructor is required for frameworks that require a no-argument constructor,
     * such as Spring when deserializing JSON requests.
     */
    public MessageDTO() { }

    /**
     * Default constructor.
     * @param message the message to set
     * @param status the status to set
     */
    public MessageDTO(String message, String status) {
        this.message = message;
        this.status = status;
    }

    /**
     * Gets the message.
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     * @param message the message to set
     * @throws IllegalArgumentException if the message is null or empty
     */
    public void setMessage(String message) {
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
        this.message = message;
    }

    /**
     * Gets the status.
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     * @param status the status to set
     * @throws IllegalArgumentException if the status is null or empty
     */
    public void setStatus(String status) {
        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        this.status = status;
    }
}
