import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.Success;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

public class ReactiveMongoDriver {
    public static final MongoClient client = MongoClients.create("mongodb://localhost:27017");

    private final String databaseName;
    private ConcurrentLinkedQueue<Event> eventStorage;

    public ReactiveMongoDriver(String databaseName) {
        this.databaseName = databaseName;
        this.eventStorage = new ConcurrentLinkedQueue<>();
        client.getDatabase(databaseName).getCollection("event").find().maxTime(10, TimeUnit.SECONDS)
                .toObservable().map(Event::new).toBlocking().subscribe(this.eventStorage::add);
    }

    ConcurrentLinkedQueue<Event> getEvents() {
        return eventStorage;
    }

    public Success addTicket(Ticket ticket) {
        return client.getDatabase(databaseName).getCollection("ticket").insertOne(ticket.getDocument())
                .timeout(15, TimeUnit.SECONDS).toBlocking().single();
    }

    public Success addEvent(Event event) {
        Success result = client.getDatabase(databaseName).getCollection("event").insertOne(event.getDocument())
                .timeout(15, TimeUnit.SECONDS).toBlocking().single();
        if (result == Success.SUCCESS) {
            eventStorage.add(event);
        }
        return result;
    }

    public Ticket getLatestTicketVersion(Integer id) {
        return getAllTicketVersions(id).stream().max(Comparator.comparing(Ticket::getCreationDate)).orElse(null);
    }

    public List<Ticket> getAllTicketVersions(Integer id) {
        List<Ticket> tickets = new ArrayList<>();
        client.getDatabase(databaseName).getCollection("ticket").find(eq("id", id))
                .maxTime(15, TimeUnit.SECONDS).toObservable().map(Ticket::new).toBlocking().subscribe(tickets::add);
        return tickets;
    }
}