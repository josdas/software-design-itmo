package broker;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Broker {
    Map<Integer, Company> companyMap;

    public Broker() {
        companyMap = new HashMap<>();
    }

    public void run() {
        HttpServer.newServer(8081).start((req, resp) -> {
            Observable<String> response;
            String action = req.getDecodedPath().substring(1);
            Map<String, List<String>> queryParam = req.getQueryParameters();
            switch (action) {
                case "add_company":
                    response = addCompany(queryParam);
                    resp.setStatus(HttpResponseStatus.OK);
                    break;
                case "add_shares":
                    response = addShares(queryParam);
                    resp.setStatus(HttpResponseStatus.OK);
                    break;
                case "get_shares":
                    response = getShares(queryParam);
                    resp.setStatus(HttpResponseStatus.OK);
                    break;
                case "buy_shares":
                    response = buyShares(queryParam);
                    resp.setStatus(HttpResponseStatus.OK);
                    break;
                case "sell_shares":
                    response = sellShares(queryParam);
                    resp.setStatus(HttpResponseStatus.OK);
                    break;
                case "change_price":
                    response = changePrice(queryParam);
                    resp.setStatus(HttpResponseStatus.OK);
                    break;
                default:
                    response = Observable.just("bad request");
                    resp.setStatus(HttpResponseStatus.BAD_REQUEST);
            }
            return resp.writeString(response);
        }).awaitShutdown();
    }

    private Observable<String> changePrice(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        int price = Integer.parseInt(queryParam.get("price").get(0));
        if (!companyMap.containsKey(id)) {
            return Observable.just("id not exist");
        }
        companyMap.get(id).shares.price = price;
        return Observable.just("ok");
    }

    private Observable<String> sellShares(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        int shares = Integer.parseInt(queryParam.get("shares").get(0));
        if (!companyMap.containsKey(id)) {
            return Observable.just("id not exist");
        }
        Company cmpn = companyMap.get(id);
        cmpn.shares.shares += shares;
        return Observable.just(cmpn.shares.price * shares + "$");
    }

    private Observable<String> buyShares(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        int shares = Integer.parseInt(queryParam.get("shares").get(0));
        if (!companyMap.containsKey(id)) {
            return Observable.just("id not exist");
        }
        Company cmpn = companyMap.get(id);
        if (cmpn.shares.shares < shares) {
            return Observable.just("only " + cmpn.shares + " shares available");
        }
        cmpn.shares.shares -= shares;
        return Observable.just(cmpn.shares.price * shares + "$");
    }

    private Observable<String> getShares(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        if (!companyMap.containsKey(id)) {
            return Observable.just("id not exist");
        }
        Company cmpn = companyMap.get(id);
        return Observable.just("company " + cmpn.name + " has " + cmpn.shares.shares +
                " shares by price:" + cmpn.shares.price + "\n" + cmpn.shares.shares + ";" + cmpn.shares.price + "");
    }

    private Observable<String> addShares(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        int shares = Integer.parseInt(queryParam.get("shares").get(0));
        if (!companyMap.containsKey(id)) {
            return Observable.just("id not exist");
        }
        companyMap.get(id).shares.shares += shares;
        return Observable.just("ok");
    }

    private Observable<String> addCompany(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        String name = queryParam.get("name").get(0);
        int price = Integer.parseInt(queryParam.get("price").get(0));
        int shares = Integer.parseInt(queryParam.get("shares").get(0));
        if (companyMap.containsKey(id)) {
            return Observable.just("already exist");
        }
        companyMap.put(id, new Company(name, price, shares));
        return Observable.just("ok");
    }
}