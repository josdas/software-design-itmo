package users;

import java.net.URI;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;


public class BrokerClient {
    private final static CloseableHttpClient HTTP_CLIENT = HttpClientBuilder.create()
            .setConnectionTimeToLive(15, TimeUnit.SECONDS)
            .build();
    private final static String HOST_URL = "http://127.0.0.1:8081/";

    private String sendRequest(String path) throws IOException, URISyntaxException {
        HttpGet get = new HttpGet(new URI(path));
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(get)) {
            return EntityUtils.toString(response.getEntity());
        }
    }

    public Shares getShareInfo(int companyId) {
        try {
            String response = sendRequest(HOST_URL + "get_shares?id=" + companyId);
            String[] parts = response.split("\n")[1].split(";");
            int shares = Integer.parseInt(parts[0]);
            int price = Integer.parseInt(parts[1]);
            return new Shares(shares, price);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sellToBroker(int shareId, int count) {
        try {
            sendRequest(HOST_URL + "sell_shares?id=" + shareId + "&shares=" + count);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void buyFromBroker(int shareId, int count) {
        try {
            sendRequest(HOST_URL + "buy_shares?id=" + shareId + "&shares=" + count);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
