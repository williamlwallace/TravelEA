package repository;

import com.google.common.collect.Iterables;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.Photo;
import play.db.ebean.EbeanConfig;
import play.mvc.Result;
import util.customObjects.Pair;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;

/**
 * A repository that executees database operations on the Photo database table
 */
public class PhotoRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public PhotoRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     *  Inserts new photo into database
     *
     * @param photo Photo to be added
     * @return Ok on success
     */
    public CompletableFuture<Result> addPhoto(Photo photo) {
        return supplyAsync(() -> {
            ebeanServer.insert(photo);
            return ok();
        }, executionContext);
    }

    /**
     * Clears any existing profile photo for a user, and returns filenames of old files
     *
     * @param userID user ID to clear profile photo of
     * @return OK if successfully cleared existing profile pic, notFound if none found
     */
    public CompletableFuture<Pair<String, String>> clearProfilePhoto(long userID) {
        return supplyAsync(() -> {
            Pair<String, String> returnPair;
            Photo profilePhoto = ebeanServer.find(Photo.class)
                    .where()
                    .eq("user_id", userID)
                    .eq("is_profile", true)
                    .findOneOrEmpty().orElse(null);
            if(profilePhoto == null) {
                return null;
            } else {
                returnPair = new Pair<>(profilePhoto.filename, profilePhoto.thumbnailFilename);
                ebeanServer.delete(profilePhoto);
                return returnPair;
            }
        });
    }

    /**
     * Finds all photos in database related to the given user ID
     *
     * @param userID User to find all photos for
     * @return List of Photo objects with the specified user ID
     */
    public CompletableFuture<List<Photo>> getAllUserPhotos(long userID) {
        return supplyAsync(() ->
             ebeanServer.find(Photo.class)
                    .where()
                    .eq("user_id", userID)
                     .eq("is_profile", false)
                    .findList(),
            executionContext);
    }

    /**
     * Finds the profile picture in the database for the given user ID
     *
     * @param userID User to find profile picture for
     * @return a photo
     */
    public CompletableFuture<Photo> getUserProfilePicture(Long userID) {
        return supplyAsync(() -> {
            Photo photo = ebeanServer.find(Photo.class)
                    .where()
                    .eq("user_id", userID)
                    .eq("is_profile", true)
                    .findOneOrEmpty().orElse(null);
            if(photo != null) {
                photo.filename = "assets/" + photo.filename;
                photo.thumbnailFilename = "assets/" + photo.thumbnailFilename;
            }
            return photo;
        }, executionContext);
    }

    /**
     * Adds photos into the database. Will replace the users profile picture if needed.
     *
     * @param photos A list of photos to upload
     * @return an ok response.
     */
    public CompletableFuture<Result> addPhotos(Collection<Photo> photos) {
        return supplyAsync(() -> {
            if (photos.size() == 1) {
                Photo pictureToUpload = Iterables.get(photos, 0);
                if (pictureToUpload.isProfile) {
                    ebeanServer.find(Photo.class)
                            .where()
                            .eq("user_id", pictureToUpload.userId)
                            .eq("is_profile", true)
                            .delete();
                }
            }
            ebeanServer.insertAll(photos);
            return ok();
        }, executionContext);
    }

    /**
     * Deletes a photo from the database.
     *
     * @param id Unique photo ID of destination to be deleted
     * @return The number of rows deleted
     */
    public CompletableFuture<Integer> deletePhoto(Long id) {
        return supplyAsync(() ->
                        ebeanServer.find(Photo.class)
                                .where()
                                .eq("guid", id)
                                .delete()
                , executionContext);
    }

    private Collection<Photo> appendAssetsUrl(Collection<Photo> photos) {
        for(Photo photo : photos) {
            photo.filename = "assets/" + photo.filename;
            photo.thumbnailFilename = "assets/" + photo.thumbnailFilename;
        }
        return photos;
    }
}
