package models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.time.LocalDateTime;
import java.util.List;

public class NewsFeedResponseItem {

    public String message;
    public String name;
    public String thumbnail;
    public Long eventerId;
    public Object data;
    public List<Long> eventIds;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime created;

    public String eventType;

    /**
     * Constructor to initialize all fields
     * @param message Message to show to user
     * @param name The name of the destination/profile
     * @param photo The photo to display as a thumbnail
     * @param eventerId The id of the initiating object (either a destination or user)
     * @param data Data involved with the event
     */
    public NewsFeedResponseItem(String message, String name, Photo photo, Long eventerId, Object data, List<Long> eventIds) {
        this.message = message;
        this.name = name;
        this.thumbnail = photo != null ? photo.thumbnailFilename : null;
        this.eventerId = eventerId;
        this.data = data;
        this.eventIds = eventIds;
    }

    /**
     * default constructor for deserializing
     */
    public NewsFeedResponseItem() {}
}
