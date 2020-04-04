
import org.bson.Document;

import java.util.Date;

public class Ticket {

    private final int id;
    private final Date creationDate;
    private final Date expirationDate;

    public Ticket(Document document) {
        this(document.getInteger("id"),
                document.getDate("creationDate"),
                document.getDate("expirationDate"));
    }

    public Ticket(int id, Date creationDate, Date expirationDate) {
        this.id = id;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
    }

    public int getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }


    public Document getDocument() {
        return new Document("id", id).append("creationDate", creationDate).append("expirationDate", expirationDate);
    }

    @Override
    public String toString() {
        return "id=" + id + ",created = " + creationDate.toString() + ",expired=" + expirationDate.toString();
    }
}