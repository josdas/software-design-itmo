package actor;

import akka.actor.UntypedActor;
import search.SearchClient;

public class Child extends UntypedActor {
    private final SearchClient client;

    public Child(SearchClient client) {
        this.client = client;
    }

    @Override
    public void onReceive(Object o) throws Throwable {
        if (o instanceof String) {
            String query = (String) o;
            getSender().tell(client.search(query), self());
            getContext().stop(self());
        }
    }
}