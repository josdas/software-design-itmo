import com.mongodb.rx.client.Success;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

import java.util.*;

public class TurnstileServer {
    private final ReactiveMongoDriver mongoDriver;

    public TurnstileServer(ReactiveMongoDriver mongoDriver) {
        this.mongoDriver = mongoDriver;
    }

    public void run() {
        HttpServer.newServer(8095).start((req, resp) -> {
            Observable<String> response;
            String action = req.getDecodedPath().substring(1);
            Map<String, List<String>> queryParam = req.getQueryParameters();
            switch (action) {
                case "enter":
                    response = enter(queryParam, new Date());
                    resp.setStatus(HttpResponseStatus.OK);
                    break;
                case "exit":
                    response = exit(queryParam, new Date());
                    resp.setStatus(HttpResponseStatus.OK);
                    break;
                default:
                    response = Observable.just("bad request");
                    resp.setStatus(HttpResponseStatus.BAD_REQUEST);
            }
            return resp.writeString(response);
        }).awaitShutdown();
    }

    public Observable<String> enter(Map<String, List<String>> queryParam, Date date) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        Ticket ticket = mongoDriver.getLatestTicketVersion(id);
        if (ticket == null) {
            return Observable.just("no tickers");
        }
        if (date.after(ticket.getExpirationDate())) {
            return Observable.just("expired");
        }
        Event event = new Event(id, date, Event.EventType.ENTER);
        if (mongoDriver.addEvent(event) == Success.SUCCESS) {
            return Observable.just("enter");
        } else {
            return Observable.just("error");
        }
    }

    public Observable<String> exit(Map<String, List<String>> queryParam, Date date) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        Event event = new Event(id, date, Event.EventType.EXIT);
        if (mongoDriver.addEvent(event) == Success.SUCCESS) {
            return Observable.just("exit");
        } else {
            return Observable.just("error");
        }
    }
}