import actor.Child;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Timeout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import search.SearchEngine;
import search.SearchResponse;
import search.StubSearchClient;

import java.util.concurrent.TimeUnit;

import static akka.pattern.PatternsCS.ask;
import static org.assertj.core.api.Assertions.assertThat;

public class ChildTest {
    private ActorSystem system;

    @Before
    public void setUp() {
        system = ActorSystem.create("ChildTest");
    }

    @After
    public void tearDown() {
        system.terminate();
    }

    @Test
    public void testChildActor() {
        ActorRef childActor = system.actorOf(Props.create(Child.class,
                new StubSearchClient(SearchEngine.MAIL, 1)));

        SearchResponse response = (SearchResponse) ask(
                childActor,
                "query",
                Timeout.apply(10, TimeUnit.SECONDS)
        ).toCompletableFuture().join();

        assertThat(response.getResponses().get(0).text).isEqualTo("Search result: 0 engine: MAIL query: query");
        assertThat(response.getResponses().size()).isEqualTo(10);
    }
}
