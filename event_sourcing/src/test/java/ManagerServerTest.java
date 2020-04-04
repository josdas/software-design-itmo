import com.mongodb.rx.client.Success;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.util.*;

public class ManagerServerTest {
    private static final String DATABASE_NAME = "managertest";

    ReactiveMongoDriver mongoDriver;
    ManagerServer server;

    @Before
    public void clearDB() {
        TestSubscriber<Success> subscriber = new TestSubscriber<>();
        ReactiveMongoDriver.client.getDatabase(DATABASE_NAME).drop().subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        mongoDriver = new ReactiveMongoDriver(DATABASE_NAME);
        server = new ManagerServer(mongoDriver);
    }

    @Test
    public void testManger() throws Throwable {
        Map<String, List<String>> queryParam = new HashMap<>();
        queryParam.put("id", Collections.singletonList("0"));
        queryParam.put("creationDate", Collections.singletonList("23-01-1998"));
        queryParam.put("expirationDate", Collections.singletonList("23-03-1998"));
        server.addTicket(queryParam);
        queryParam.replace("id", Collections.singletonList("0"));
        queryParam.put("creationDate", Collections.singletonList("23-02-1998"));
        queryParam.put("expirationDate", Collections.singletonList("23-04-1998"));
        server.addTicket(queryParam);

        List<Ticket> tickets = mongoDriver.getAllTicketVersions(0);
        Assert.assertEquals(2, tickets.size());
        Ticket ticket = tickets.get(1);
        Assert.assertEquals(0, ticket.getId());
        Assert.assertEquals("Mon Feb 23 00:00:00 MSK 1998", ticket.getCreationDate().toString());
        Assert.assertEquals("Thu Apr 23 00:00:00 MSD 1998", ticket.getExpirationDate().toString());
    }
}
