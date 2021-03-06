package repository;

import akka.actor.ActorSystem;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.libs.concurrent.CustomExecutionContext;

/**
 * Custom execution context, so that blocking database operations don't happen on the rendering
 * thread pool.
 *
 * @link https://www.playframework.com/documentation/latest/ThreadPools
 */
@Singleton
public class DatabaseExecutionContext extends CustomExecutionContext {

    @Inject
    public DatabaseExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "database.dispatcher");
    }
}
