import org.bson.Document;

public class User {
    public final int id;
    public final String name;
    public final String currency;

    public User(int id, String name, String currency) {
        this.id = id;
        this.name = name;
        this.currency = currency;
    }

    public User(Document doc) {
        this(doc.getInteger("id"),
                doc.getString("name"),
                doc.getString("currency"));
    }

    public Document toDocument() {
        return new Document("id", id)
                .append("name", name)
                .append("currency", currency);
    }
}
