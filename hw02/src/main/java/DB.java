import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.Success;
import rx.Observable;

import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

public class DB {
    public static MongoClient client = MongoClients.create("mongodb://localhost:27017");

    public static Success registerUser(User user) {
        return client.getDatabase("db_test")
                .getCollection("user")
                .insertOne(user.toDocument())
                .timeout(1, TimeUnit.SECONDS)
                .toBlocking()
                .single();
    }

    public static Success addGood(Good good) {
        return client.getDatabase("db_test")
                .getCollection("good")
                .insertOne(good.toDocument())
                .timeout(1, TimeUnit.SECONDS)
                .toBlocking()
                .single();
    }

    public static Observable<String> getAllGoods(Integer id) {
        return client.getDatabase("db_test")
                .getCollection("user")
                .find(eq("id", id))
                .first()
                .map(d -> new User(d).currency)
                .flatMap(
                        cur -> client.getDatabase("db_test")
                                .getCollection("good")
                                .find()
                                .toObservable()
                                .map(d -> new Good(d).toString(cur))
                                .reduce((str1, str2) -> str1 + ", " + str2));
    }
}
