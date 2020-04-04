import com.mongodb.rx.client.Success;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.util.*;

public class TurnstileServerTest {
    private static final String DATABASE_NAME = "turnstiletest";

    ReactiveMongoDriver mongoDriver;
    TurnstileServer server;

    @Before
    public void clearDB() {
        TestSubscriber<Success> subscriber = new TestSubscriber<>();
        ReactiveMongoDriver.client.getDatabase(DATABASE_NAME).drop().subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        mongoDriver = new ReactiveMongoDriver(DATABASE_NAME);
        server = new TurnstileServer(mongoDriver);
    }

    @Test
    public void testEnterExit() {
        Map<String, List<String>> queryParam = new HashMap<>();
        queryParam.put("id", Collections.singletonList("0"));

        TestSubscriber<String> subscriber = new TestSubscriber<>();
        server.enter(queryParam, new Date()).subscribe(subscriber);
        subscriber.assertValues("no tickers");

        Date creation = new Date();
        Date enterTime = new Date(creation.getTime() + 1);
        Date exitTime = new Date(creation.getTime() + 2);
        Date expiration = new Date(creation.getTime() + 4);

        mongoDriver.addTicket(new Ticket(0, creation, expiration));
        TestSubscriber<String> subscriberEnter = new TestSubscriber<>();
        server.enter(queryParam, enterTime).subscribe(subscriberEnter);
        subscriberEnter.assertValue("enter");
        subscriberEnter.awaitTerminalEvent();
        TestSubscriber<String> subscriberExit = new TestSubscriber<>();
        server.exit(queryParam, exitTime).subscribe(subscriberExit);
        subscriberExit.assertValue("exit");
        subscriberExit.awaitTerminalEvent();

        Event enterE = (Event) mongoDriver.getEvents().toArray()[0];
        Event exitE = (Event) mongoDriver.getEvents().toArray()[1];
        Assert.assertEquals(Event.EventType.ENTER, enterE.getEventType());
        Assert.assertEquals(Event.EventType.EXIT, exitE.getEventType());
        Assert.assertEquals(enterTime, enterE.getTime());
        Assert.assertEquals(exitTime, exitE.getTime());
        Assert.assertEquals(0, enterE.getTicketId());
        Assert.assertEquals(0, exitE.getTicketId());
    }
}