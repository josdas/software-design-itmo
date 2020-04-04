import com.mongodb.rx.client.Success;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ManagerServer {

    private final ReactiveMongoDriver mongoDriver;

    public ManagerServer(ReactiveMongoDriver mongoDriver) {
        this.mongoDriver = mongoDriver;
    }

    public void run() {
        HttpServer.newServer(8085).start((req, resp) -> {
            Observable<String> response;
            String action = req.getDecodedPath().substring(1);
            Map<String, List<String>> queryParam = req.getQueryParameters();
            switch (action) {
                case "get":
                    response = getTicket(queryParam);
                    resp.setStatus(HttpResponseStatus.OK);
                    break;
                case "add":
                    response = addTicket(queryParam);
                    resp.setStatus(HttpResponseStatus.OK);
                    break;
                default:
                    response = Observable.just("bad request");
                    resp.setStatus(HttpResponseStatus.BAD_REQUEST);
            }
            return resp.writeString(response);
        }).awaitShutdown();
    }

    public Observable<String> getTicket(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        Ticket ticket = mongoDriver.getLatestTicketVersion(id);
        if (ticket == null) {
            return Observable.just("no ticket");
        } else {
            return Observable.just(ticket.toString());
        }
    }

    public Observable<String> addTicket(Map<String, List<String>> queryParam) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        Date created, expired;
        try {
            created = format.parse(queryParam.get("creationDate").get(0));
            expired = format.parse(queryParam.get("expirationDate").get(0));
        } catch (ParseException e) {
            return Observable.just("date error");
        }
        if (created.after(expired)) {
            return Observable.just("created > expired error");
        }
        int id = Integer.parseInt(queryParam.get("id").get(0));
        if (mongoDriver.addTicket(new Ticket(id, created, expired)) == Success.SUCCESS) {
            return Observable.just("new ticket");
        } else {
            return Observable.just("md error");
        }
    }
}