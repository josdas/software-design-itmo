import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import users.UsersManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Testcontainers
public class AppTest {
    @ClassRule
    public static GenericContainer brokerServer
            = new FixedHostPortGenericContainer("broker:1.0-SNAPSHOT")
            .withFixedExposedPort(8081, 8081)
            .withExposedPorts(8081);
    private final static String BROKER = "http://127.0.0.1:8081/";
    private final static UsersManager USERS_MANAGER = new UsersManager();

    Map<String, List<String>> parseParam(String params) {
        String[] parts = params.split("&");
        Map<String, List<String>> res = new HashMap<>();
        for (String part : parts) {
            String[] kv = part.split("=");
            res.put(kv[0], Collections.singletonList(kv[1]));
        }
        return res;
    }

    public void addUserTesting() throws InterruptedException, IOException, URISyntaxException {
        Assert.assertEquals("add: ok",
                USERS_MANAGER.addUser(parseParam("id=1&name=lol&balance=100")));
        Assert.assertEquals("already exist",
                USERS_MANAGER.addUser(parseParam("id=1&name=lol&balance=100")));
        Assert.assertEquals("add: ok",
                USERS_MANAGER.addUser(parseParam("id=2&name=bar&balance=1000")));
        Assert.assertEquals("already exist",
                USERS_MANAGER.addUser(parseParam("id=1&name=lol&balance=100")));
        Assert.assertEquals("already exist",
                USERS_MANAGER.addUser(parseParam("id=2&name=lol&balance=100")));
    }

    public void addMoneyTesting() throws InterruptedException, IOException, URISyntaxException {
        Assert.assertEquals("100$",
                USERS_MANAGER.getTotalBalance(parseParam("id=1")));
        Assert.assertEquals("1000$",
                USERS_MANAGER.getTotalBalance(parseParam("id=2")));
        Assert.assertEquals("replenish: ok",
                USERS_MANAGER.replenishBalance(parseParam("id=2&money=200")));
        Assert.assertEquals("1200$",
                USERS_MANAGER.getTotalBalance(parseParam("id=2")));
    }

    public void addCompanyTesting() throws InterruptedException, IOException, URISyntaxException {
        Assert.assertEquals("ok",
                sendRequest(BROKER + "add_company?id=1&name=apple&price=10&shares=100"));
        Assert.assertEquals("ok",
                sendRequest(BROKER + "add_company?id=2&name=google&price=100&shares=10"));
        Assert.assertEquals("already exist",
                sendRequest(BROKER + "add_company?id=1&name=apple&price=10&shares=100"));
    }

    public void sharesCompanyTesting() throws InterruptedException, IOException, URISyntaxException {
        Assert.assertEquals("company apple has 100 shares by price:10\n100;10",
                sendRequest(BROKER + "get_shares?id=1"));
        Assert.assertEquals("ok",
                sendRequest(BROKER + "add_shares?id=1&shares=1"));
        Assert.assertEquals("company apple has 101 shares by price:10\n101;10",
                sendRequest(BROKER + "get_shares?id=1"));
        Assert.assertEquals("ok",
                sendRequest(BROKER + "change_price?id=1&price=11"));
        Assert.assertEquals("company apple has 101 shares by price:11\n101;11",
                sendRequest(BROKER + "get_shares?id=1"));
    }

    public void buySharesTesting() throws IOException, URISyntaxException {
        Assert.assertEquals("buy: ok",
                USERS_MANAGER.buyShares(parseParam("id=2&share_id=1&count=1")));
        Assert.assertEquals("1200$",
                USERS_MANAGER.getTotalBalance(parseParam("id=2")));
        Assert.assertEquals("ok",
                sendRequest(BROKER + "change_price?id=1&price=10"));
        Assert.assertEquals("1199$",
                USERS_MANAGER.getTotalBalance(parseParam("id=2")));
    }

    public void sellSharesTesting() throws IOException, URISyntaxException {
        Assert.assertEquals("sell: ok",
                USERS_MANAGER.sellShares(parseParam("id=2&share_id=1&count=1")));
        Assert.assertEquals("1199$",
                USERS_MANAGER.getTotalBalance(parseParam("id=2")));
        Assert.assertEquals("ok",
                sendRequest(BROKER + "change_price?id=1&price=100"));
        Assert.assertEquals("1199$",
                USERS_MANAGER.getTotalBalance(parseParam("id=2")));
    }

    @Test
    public void OrderedTest() throws InterruptedException, IOException, URISyntaxException {
        addUserTesting();
        addMoneyTesting();
        addCompanyTesting();
        sharesCompanyTesting();
        buySharesTesting();
        sellSharesTesting();
    }

    private String sendRequest(String path) throws IOException, URISyntaxException {
        CloseableHttpClient HTTP_CLIENT = HttpClientBuilder.create()
                .build();
        HttpGet get = new HttpGet(new URI(path));
        String res;
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(get)) {
            res = EntityUtils.toString(response.getEntity());
        }
        HTTP_CLIENT.close();
        return res;
    }
}