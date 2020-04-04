import org.bson.Document;

import java.util.Date;

public class Event {
    public enum EventType {
        ENTER, EXIT
    }

    private final int ticketId;
    private final Date time;
    private final EventType eventType;

    public Event(Document document) {
        this(document.getInteger("ticketId"),
                document.getDate("time"),
                Enum.valueOf(EventType.class, document.getString("eventType").toUpperCase()));
    }

    public Event(int ticketId, Date time, EventType eventType) {
        this.ticketId = ticketId;
        this.time = time;
        this.eventType = eventType;
    }

    public int getTicketId() {
        return ticketId;
    }

    public Date getTime() {
        return time;
    }

    public EventType getEventType() {
        return eventType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof Event) {
            Event other = (Event) obj;
            return this.ticketId == other.ticketId && this.time.equals(other.time) && this.eventType == other.eventType;
        }
        return false;
    }

    public Document getDocument() {
        return new Document("ticketId", ticketId).append("time", time).append("eventType", eventType.toString());
    }
}