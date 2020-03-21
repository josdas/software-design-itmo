
import org.bson.Document;


public class Good {
    public final int id;
    public final String name;
    public final String rubel;
    public final String euro;
    public final String dollar;

    public Good(int id, String name, String rubel, String euro, String dollar) {
        this.id = id;
        this.name = name;
        this.rubel = rubel;
        this.euro = euro;
        this.dollar = dollar;
    }

    public Good(Document document) {
        this(document.getInteger("id"),
                document.getString("name"),
                document.getString("rubel"),
                document.getString("euro"),
                document.getString("dollar"));
    }

    public Document toDocument() {
        return new Document("id", id)
                .append("name", name)
                .append("rubel", rubel)
                .append("dollar", dollar)
                .append("euro", euro);
    }

    public String toString(String currency) {
        String price = "";
        switch (currency) {
            case "rubel":
                price = "rubel=" + rubel;
                break;
            case "euro":
                price = "euro=" + euro;
                break;
            case "dollar":
                price = "dollar=" + dollar;
                break;
        }
        return "{" +
                "id=" + id +
                ", name=" + name +
                ", " + price  +
                '}';
    }
}
