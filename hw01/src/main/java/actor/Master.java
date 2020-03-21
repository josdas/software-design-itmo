package actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;
import scala.concurrent.duration.Duration;
import search.SearchClient;
import search.SearchResponse;

import java.util.concurrent.TimeUnit;

public class Master extends UntypedActor {
    private final Duration timeout = Duration.create(1, TimeUnit.SECONDS);
    private SearchClient[] clients;
    private SearchResponse aggregateResponse;
    private int gotResponses;
    private ActorRef sender;

    public Master(SearchClient[] clients) {
        this.clients = clients;
        this.aggregateResponse = new SearchResponse();
        this.gotResponses = 0;
    }

    @Override
    public void onReceive(Object o) throws Throwable {
        if (o instanceof String) {
            String query = (String) o;
            for (SearchClient client : clients) {
                ActorRef child = getContext().actorOf(Props.create(Child.class, client));
                child.tell(query, self());
            }
            getContext().setReceiveTimeout(this.timeout);
            sender = getSender();
        } else if (o instanceof SearchResponse) {
            gotResponses += 1;
            SearchResponse respond = (SearchResponse) o;
            aggregateResponse.updateWith(respond);
        }
        if (gotResponses == clients.length || o instanceof ReceiveTimeout) {
            sender.tell(aggregateResponse, self());
            getContext().stop(self());
        }
    }
}
