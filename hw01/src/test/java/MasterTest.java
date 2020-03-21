import actor.Child;
import actor.Master;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Timeout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import search.SearchClient;
import search.SearchEngine;
import search.SearchResponse;
import search.StubSearchClient;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static akka.pattern.PatternsCS.ask;
import static org.assertj.core.api.Assertions.assertThat;

public class MasterTest {
    private ActorSystem system;

    @Before
    public void setUp() {
        system = ActorSystem.create("MasterTest");
    }

    @After
    public void tearDown() {
        system.terminate();
    }

    @Test
    public void testMasterActor() {
        SearchClient[] clients = {
                new StubSearchClient(SearchEngine.MAIL, 100),
                new StubSearchClient(SearchEngine.YANDEX, 2000),
                new StubSearchClient(SearchEngine.GOOGLE, 3000),
        };
        ActorRef masterActor = system.actorOf(Props.create(Master.class, (Object) clients));

        SearchResponse response = (SearchResponse) ask(
                masterActor,
                "query",
                Timeout.apply(10, TimeUnit.SECONDS)
        ).toCompletableFuture().join();

        assertThat(response.getResponses().get(0).text).isEqualTo("Search result: 0 engine: MAIL query: query");
        assertThat(response.getResponses().size()).isEqualTo(10);
    }

    @Test
    public void testMasterActorTimeout() {
        SearchClient[] clients = {
                new StubSearchClient(SearchEngine.MAIL, 100),
                new StubSearchClient(SearchEngine.YANDEX, 200),
                new StubSearchClient(SearchEngine.GOOGLE, 300),
        };
        ActorRef masterActor = system.actorOf(Props.create(Master.class, (Object) clients));

        SearchResponse response = (SearchResponse) ask(
                masterActor,
                "query",
                Timeout.apply(10, TimeUnit.SECONDS)
        ).toCompletableFuture().join();

        assertThat(response.getResponses().get(0).text).isEqualTo("Search result: 0 engine: MAIL query: query");
        assertThat(response.getResponses().size()).isEqualTo(30);
    }
}
