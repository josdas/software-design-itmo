import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(final String[] args) {
        HttpServer
                .newServer(8000)
                .start((req, resp) -> {
                    String queryType = req.getDecodedPath().substring(1);
                    Map<String, List<String>> params = req.getQueryParameters();
                    Observable<String> response;
                    int id = Integer.parseInt(params.get("id").get(0));
                    switch (queryType) {
                        case "register":
                            String name = params.get("name").get(0);
                            String currency = params.get("currency").get(0);
                            response = Observable.just(DB.registerUser(new User(id, name, currency)).toString());
                            resp.setStatus(HttpResponseStatus.OK);
                            break;
                        case "add":
                            response = Observable.just("{ id = " + id + ", goods = [")
                                    .concatWith(DB.getAllGoods(id))
                                    .concatWith(Observable.just("]}"));
                            resp.setStatus(HttpResponseStatus.OK);
                            break;
                        case "get":
                            name = params.get("name").get(0);
                            String euro = params.get("euro").get(0);
                            String dollar = params.get("dollar").get(0);
                            String rubel = params.get("rubel").get(0);
                            response = Observable.just(DB.addGood(new Good(id, name, dollar, euro, rubel)).toString());
                            resp.setStatus(HttpResponseStatus.OK);
                            break;
                        default:
                            response = Observable.just("Unknown request");
                            resp.setStatus(HttpResponseStatus.BAD_REQUEST);
                    }
                    return resp.writeString(response);
                })
                .awaitShutdown();
    }
}
