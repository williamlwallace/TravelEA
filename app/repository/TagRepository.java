package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.PagedList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.Enums.TagType;
import models.UsedTag;
import play.db.ebean.EbeanConfig;
import util.objects.Pair;

@Singleton
public class TagRepository {

    private final EbeanServer ebeanServer;

    @Inject
    public TagRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
    }

    /**
     * Returns a collection of tags (the type of the tag is dependant on tagType given)
     * Only tags where name exactly matches search criteria are returned
     *
     * @param tagType The kind of tag to search for
     * @param name Name of tag to search for
     * @return Collection of tags
     */
    public CompletableFuture<PagedList<?>> searchTags(TagType tagType, String name, int pageNum, int pageSize) {
        return supplyAsync(() ->
            ebeanServer.find(tagType.getClassType())
                .fetch("tag")
                .where()
                .ieq("tag.name", name)
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList()
            );
    }

    /**
     * Returns a collection of tags (the type of the tag is dependant on tagType given)
     * Only tags where name exactly matches search criteria are returned
     *
     * @param tagType The kind of tag to search for
     * @param name Name of tag to search for
     * @param limit The maximum number of results to search for
     * @return Pair where first item is Tags returned, second item is total number of tags (for paging)
     */
    public CompletableFuture<PagedList<?>> searchTags(TagType tagType, int pageNum, int pageSize) {
        return supplyAsync(() ->
            ebeanServer.find(tagType.getClassType())
                .fetch("tag")
                .where()
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList()
            );
    }

    /**
     * Gets all tags used by a user
     *
     * @param userId Id of user to receive tags for
     * @param pageNum The page to receive
     * @param pageSize Number of entries on a page
     * @return Pair where thee firrst item (data) is the used tags and the second is the total number of pages
     */
    public CompletableFuture<PagedList<?>> getRecentUserTags(long userId, int pageNum, int pageSize) {
        return supplyAsync(() ->
            ebeanServer.find(UsedTag.class)
                .where()
                .eq("user_id", userId)
                .orderBy()
                .desc("timeUsed")
                .setFirstRow((pageNum - 1) * pageSize)
                .setMaxRows(pageSize)
                .findPagedList()
            );
    }

}
