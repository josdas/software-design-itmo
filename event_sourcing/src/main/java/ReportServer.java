import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static java.lang.Math.max;


public class ReportServer {
    private ConcurrentLinkedQueue<Event> events;

    public ReportServer(ConcurrentLinkedQueue<Event> events) {
        this.events = events;
    }

    public void run() {
        HttpServer.newServer(8083).start((req, resp) -> {
            Observable<String> response;
            String statType = req.getDecodedPath().substring(1);
            switch (statType) {
                case "entry_count":
                    response = Observable.just(dailyEntryCount());
                    resp.setStatus(HttpResponseStatus.OK);
                    break;
                case "mean":
                    response = Observable.just(meanVisitTime());
                    resp.setStatus(HttpResponseStatus.OK);
                    break;
                default:
                    response = Observable.just("bad request");
                    resp.setStatus(HttpResponseStatus.BAD_REQUEST);
            }
            return resp.writeString(response);
        }).awaitShutdown();
    }

    public String dailyEntryCount() {
        Map<String, Integer> eventsByDay = new TreeMap<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        for (Event event : events) {
            if (event.getEventType() == Event.EventType.ENTER) {
                calendar.setTime(event.getTime());
                String dataKey = format.format(calendar.getTime());

                eventsByDay.put(dataKey, eventsByDay.getOrDefault(dataKey, 0) + 1);
            }
        }
        if (eventsByDay.isEmpty()) {
            return "no records";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> e : eventsByDay.entrySet()) {
            stringBuilder.append(e.getKey()).append(" ").append(e.getValue()).append("\n");
        }
        return stringBuilder.toString();
    }

    public String meanVisitTime() {
        Map<Integer, List<Event>> eventsByTicketId = events.stream().collect(Collectors.groupingBy(Event::getTicketId));
        if (eventsByTicketId.isEmpty()) {
            return "no records";
        }
        long sumTime = 0;
        int numSessions = 0;
        long balance = 0;
        long lastEventTime = 0;
        for (List<Event> eventList : eventsByTicketId.values()) {
            for (Event event : eventList) {
                if (event.getEventType() == Event.EventType.ENTER) {
                    sumTime -= event.getTime().getTime();
                    numSessions++;
                    balance++;
                } else {
                    sumTime += event.getTime().getTime();
                    balance--;
                }
                lastEventTime = max(lastEventTime, event.getTime().getTime());
            }
        }
        long mean = (sumTime + balance * lastEventTime) / numSessions;
        return mean + " MILLISECONDS";
    }
}
