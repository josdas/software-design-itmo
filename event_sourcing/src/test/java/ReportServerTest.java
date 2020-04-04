import com.mongodb.rx.client.Success;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ReportServerTest {
    private static final String DATABASE_NAME = "reporttest";

    ReactiveMongoDriver mongoDriver;
    ReportServer server;

    @Before
    public void clearDB() {
        TestSubscriber<Success> subscriber = new TestSubscriber<>();
        ReactiveMongoDriver.client.getDatabase(DATABASE_NAME).drop().subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        mongoDriver = new ReactiveMongoDriver(DATABASE_NAME);
        server = new ReportServer(mongoDriver.getEvents());
    }

    @Test
    public void testEmpty() {
        Assert.assertEquals("no records", server.dailyEntryCount());
        Assert.assertEquals("no records", server.meanVisitTime());
    }

    @Test
    public void testMediumDuration() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2014, Calendar.JUNE, 23);
        Date creation = calendar.getTime();
        Date expiration = new Date(creation.getTime() + TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS));
        mongoDriver.addTicket(new Ticket(0, creation, expiration));

        Date entry = new Date(creation.getTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
        Date exit = new Date(entry.getTime() + TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS));
        mongoDriver.addEvent(new Event(0, entry, Event.EventType.ENTER));
        mongoDriver.addEvent(new Event(0, exit, Event.EventType.EXIT));

        entry = new Date(creation.getTime() + TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS));
        exit = new Date(entry.getTime() + TimeUnit.MILLISECONDS.convert(3, TimeUnit.HOURS));
        mongoDriver.addEvent(new Event(0, entry, Event.EventType.ENTER));
        mongoDriver.addEvent(new Event(0, exit, Event.EventType.EXIT));

        Assert.assertEquals("9000000 MILLISECONDS", server.meanVisitTime());
        Assert.assertEquals("24-06-2014 1\n26-06-2014 1\n", server.dailyEntryCount());
    }
}