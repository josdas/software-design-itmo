package users;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServer;
import rx.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersManager {
    BrokerClient brokerClient;
    Map<Integer, User> userMap;

    public UsersManager() {
        brokerClient = new BrokerClient();
        userMap = new HashMap<>();
    }

    public void run() {
        HttpServer.newServer(8082).start((req, resp) -> {
            Observable<String> response;
            try {
                String action = req.getDecodedPath().substring(1);
                Map<String, List<String>> queryParam = req.getQueryParameters();
                switch (action) {
                    case "add_user":
                        response = Observable.just(addUser(queryParam));
                        resp.setStatus(HttpResponseStatus.OK);
                        break;
                    case "replenish_balance":
                        response = Observable.just(replenishBalance(queryParam));
                        resp.setStatus(HttpResponseStatus.OK);
                        break;
                    case "get_shares":
                        response = Observable.just(getShares(queryParam));
                        resp.setStatus(HttpResponseStatus.OK);
                        break;
                    case "get_total_balance":
                        response = Observable.just(getTotalBalance(queryParam));
                        resp.setStatus(HttpResponseStatus.OK);
                        break;
                    case "buy":
                        response = Observable.just(buyShares(queryParam));
                        resp.setStatus(HttpResponseStatus.OK);
                        break;
                    case "sell":
                        response = Observable.just(sellShares(queryParam));
                        resp.setStatus(HttpResponseStatus.OK);
                        break;
                    default:
                        response = Observable.just("bad request");
                        resp.setStatus(HttpResponseStatus.BAD_REQUEST);
                }
            } catch (Exception e) {
                resp.setStatus(HttpResponseStatus.BAD_REQUEST);
                response = Observable.just(e.toString());
            }
            return resp.writeString(response);
        }).awaitShutdown();
    }

    public String sellShares(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        int shareId = Integer.parseInt(queryParam.get("share_id").get(0));
        int count = Integer.parseInt(queryParam.get("count").get(0));
        if (!userMap.containsKey(id)) {
            return ("id not exist");
        }
        User user = userMap.get(id);
        if (count > user.shares.getOrDefault(shareId, 0)) {
            return ("not enough shares");
        }
        int price = brokerClient.getShareInfo(shareId).price;
        brokerClient.sellToBroker(shareId, count);
        user.balance += price * count;
        user.shares.put(shareId, user.shares.getOrDefault(shareId, 0) - count);
        return ("sell: ok");
    }

    public String buyShares(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        int shareId = Integer.parseInt(queryParam.get("share_id").get(0));
        int count = Integer.parseInt(queryParam.get("count").get(0));
        if (!userMap.containsKey(id)) {
            return ("id not exist");
        }
        Shares shareInfo = brokerClient.getShareInfo(shareId);
        User user = userMap.get(id);
        if (shareInfo.price * count > user.balance) {
            return ("not enough money");
        }
        if (count > shareInfo.shares) {
            return ("too many shares");
        }
        brokerClient.buyFromBroker(shareId, count);
        user.balance -= shareInfo.price * count;
        user.shares.put(shareId, user.shares.getOrDefault(shareId, 0) + count);
        System.out.println();
        return ("buy: ok");
    }

    public String getTotalBalance(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        if (!userMap.containsKey(id)) {
            return ("id not exist");
        }
        User user = userMap.get(id);
        int total = user.balance;
        for (Map.Entry<Integer, Integer> kv : user.shares.entrySet()) {
            total += kv.getValue() * brokerClient.getShareInfo(kv.getKey()).price;
        }
        return (total + "$");
    }

    public String getShares(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        if (!userMap.containsKey(id)) {
            return ("id not exist");
        }
        User user = userMap.get(id);
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Integer, Integer> kv : user.shares.entrySet()) {
            stringBuilder
                    .append(kv.getKey())
                    .append(": ")
                    .append(kv.getValue())
                    .append(" by price ")
                    .append(brokerClient.getShareInfo(kv.getKey()).price)
                    .append("$\n");
        }
        return (stringBuilder.toString());
    }

    public String replenishBalance(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        int money = Integer.parseInt(queryParam.get("money").get(0));
        if (!userMap.containsKey(id)) {
            return ("id not exist");
        }
        User user = userMap.get(id);
        user.balance += money;
        return ("replenish: ok");
    }

    public String addUser(Map<String, List<String>> queryParam) {
        int id = Integer.parseInt(queryParam.get("id").get(0));
        String name = queryParam.get("name").get(0);
        int balance = Integer.parseInt(queryParam.get("balance").get(0));
        if (userMap.containsKey(id)) {
            return ("already exist");
        }
        userMap.put(id, new User(name, balance));
        return ("add: ok");
    }
}
