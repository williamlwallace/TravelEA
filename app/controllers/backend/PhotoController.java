package controllers.backend;

import com.google.inject.Inject;
import play.Environment;
import play.api.Play;
import play.libs.Files;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

public class PhotoController extends Controller {

    @Inject
    public PhotoController() {

    }

    /**
     * Uploads any number of photos from a multipart/form-data request
     * @return Result of query
     */
    public CompletableFuture<Result> upload(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            // Get the request body, and turn it into a multipart formdata collection of temporary files
            Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();
            // Iterate through all files in the request
            for(Http.MultipartFormData.FilePart<Files.TemporaryFile> file : body.getFiles()){
                if (file != null) {
                    // Get the filename, filesize and content-type of the file
                    String fileName = file.getFilename();
                    long fileSize = file.getFileSize();
                    String contentType = file.getContentType();
                    // Store the file locally, using the provided filename and current epoch milliseconds
                    Files.TemporaryFile tempFile = file.getRef();
                    if(Environment.simple().isTest()) {
                        tempFile.copyTo(Paths.get("storage/photos/test/" + System.currentTimeMillis() + "_" + fileName), true);
                    } else {
                        tempFile.copyTo(Paths.get("storage/photos/" + System.currentTimeMillis() + "_" + fileName), true);
                    }
                    // Add to db
                } else {
                    // If any uploads fail, return bad request immediately
                    return badRequest().flashing("error", "Missing file");
                }
            }
            // Return OK if no issues were encountered
            return ok("File uploaded");
        });
    }
}
