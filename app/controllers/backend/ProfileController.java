package controllers.backend;

import actions.ActionState;
import actions.Authenticator;
import actions.roles.Everyone;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import models.CountryDefinition;
import models.Profile;
import models.TravellerTypeDefinition;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import repository.ProfileRepository;
import repository.TravellerTypeDefinitionRepository;
import util.validation.ErrorResponse;
import util.validation.UserValidator;

/**
 * Manage a database of users
 */
public class ProfileController extends Controller {

    private final ProfileRepository profileRepository;
    private final TravellerTypeDefinitionRepository travellerTypeDefinitionRepository;

    @Inject
    public ProfileController(ProfileRepository profileRepository,
        TravellerTypeDefinitionRepository travellerTypeDefinitionRepository) {
        this.profileRepository = profileRepository;
        this.travellerTypeDefinitionRepository = travellerTypeDefinitionRepository;
    }

    /**
     * Gets all possible traveller types currently stored in db
     *
     * @return JSON list of traveller types
     */
    public CompletableFuture<Result> getAllTravellerTypes() {
        return travellerTypeDefinitionRepository.getAllTravellerTypeDefinitions()
            .thenApplyAsync(allTravellerTypes -> ok(Json.toJson(allTravellerTypes)));
    }

    /**
     * Adds a new profile received as body of a post request to database
     *
     * @param request Contains the HTTP request info
     * @return Returns CompletableFuture type: ok if profile created and added successfully,
     * badRequest if profile already exists
     */
    public CompletableFuture<Result> addNewProfile(Http.Request request) {
        // Get json parameters
        JsonNode data = request.body().asJson();

        // Run the Validator
        ErrorResponse validatorResult = new UserValidator(data).profile();

        if (validatorResult.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(validatorResult.toJson()));
        } else {
            //We are assuming that the json is sent perfectly, object may have to be hand-created later
            Profile newProfile = Json.fromJson(data, Profile.class);

            return profileRepository.findID(newProfile.userId)
                .thenComposeAsync(userId -> {
                    if (userId != null) {   //If the profile for the user exists (is not null)
                        return null;
                    } else {                //Else insert the profile
                        return profileRepository.addProfile(newProfile);
                    }
                })
                .thenApplyAsync(insertedId -> {
                    if (insertedId == null) {
                        validatorResult.map("Profile already created for this user", "other");
                        return badRequest(validatorResult.toJson());
                    } else {
                        return ok(Json.toJson(insertedId));
                    }
                });
        }
    }


    /**
     * Gets a profile based on the userID specified in auth
     *
     * @return Ok with profile json object if profile found, badRequest if request malformed or
     * profile not found
     */
    @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> getMyProfile(Http.Request request) {
        User user = request.attrs().get(ActionState.USER);
        return getProfile(user.id);
    }


    /**
     * Gets a profile based on the userID specified in the request
     *
     * @param userId The user ID to return data for
     * @return Ok with profile json object if profile found, badRequest if request malformed or
     * profile not found
     */
    public CompletableFuture<Result> getProfile(Long userId) {
        return profileRepository.findID(userId)
            .thenApplyAsync(profile -> {
                if (profile == null) {
                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.map("Profile for that user not found", "other");
                    return notFound(errorResponse.toJson());
                } else {
                    return ok(Json.toJson(profile));
////                        List<Long> travellerTypes = new ArrayList<>();
////                        List<Long> nationalities = new ArrayList<>();
////                        List<Long> passports = new ArrayList<>();
//                        String travellerTypes = "";
//                        String nationalities = "";
//                        String passports = "";
//
//                        for (TravellerTypeDefinition travellerType : profile.travellerTypes) {
////                            travellerTypes.add(travellerType.id);
//                            travellerTypes += travellerType.id + ",";
//                        }
//                        travellerTypes = travellerTypes
//                            .substring(0, max(0, travellerTypes.length() - 1));
//
//                        for (CountryDefinition country : profile.nationalities) {
////                            nationalities.add(country.id);
//                            nationalities += country.id + ",";
//                        }
//                        nationalities = nationalities
//                            .substring(0, max(0, nationalities.length() - 1));
//
//                        for (CountryDefinition country : profile.passports) {
////                            passports.add(country.id);
//                            passports += country.id + ",";
//                        }
//                        passports = passports.substring(0, max(0, passports.length() - 1));
//
//                        JsonNode profileJson = Json.toJson(profile);
//                        ObjectNode customProfileJson = (ObjectNode) profileJson;
//
//                        customProfileJson.put("travellerTypes", travellerTypes);
//                        customProfileJson.put("nationalities", nationalities);
//                        customProfileJson.put("passports", passports);
//
//                        return ok(customProfileJson);
                }
            });

    }

    /**
     * Updates the profile received in the body of the request as well as the related nationalities,
     * passports and traveller types
     *
     * @param request Contains the HTTP request info
     * @return Ok if updated successfully, badRequest if profile json malformed
     */
    @With({Everyone.class, Authenticator.class})
    public CompletionStage<Result> updateProfile(Http.Request request) {
        //Get user
        User user = request.attrs().get(ActionState.USER);
        // Get json parameters
        JsonNode json = request.body().asJson();
        return updateProfileHelper(json, user.id);
    }

    /**
     * Updates the profile received in the body of the request as well as the related nationalities,
     * passports and traveller types
     *
     * @param request Contains the HTTP request info
     * @return Ok if updated successfully, badRequest if profile json malformed
     */
    // TODO: set auth to admin role @With({Everyone.class, Authenticator.class})
    public CompletableFuture<Result> updateProfile(Http.Request request, Long userId) {
        // Get json parameters
        JsonNode data = request.body().asJson();
        return updateProfileHelper(data, userId);
    }

    /**
     * Updates the profile received in the body of the request as well as the related nationalities,
     * passports and traveller types
     *
     * @return Ok if updated successfully, badRequest if profile json malformed
     */
    private CompletableFuture<Result> updateProfileHelper(JsonNode data, Long userId) {
        // Run the Validator
        ErrorResponse errorResponse = new UserValidator(data).profile();

        if (errorResponse.error()) {
            return CompletableFuture.supplyAsync(() -> badRequest(errorResponse.toJson()));
        } else {
            return profileRepository.findID(userId)
                .thenComposeAsync(profile -> {
                    if (profile == null) {
                        return null;
                    } else {
                        Profile updatedProfile = Json.fromJson(data, Profile.class);
                        updatedProfile.userId = userId;

                        return profileRepository.updateProfile(updatedProfile);
                    }
                }).thenApplyAsync(updatedUserId -> {
                    if (updatedUserId == null) {
                        errorResponse.map("Profile for that user not found", "other");
                        return badRequest(errorResponse.toJson());
                    } else {
                        return ok(Json.toJson(updatedUserId));
                    }
                });
        }
    }

    /**
     * Deletes a profile based on the userID specified in the request
     *
     * @param id Contains the HTTP request info
     * @return Returns CompletableFuture type: ok if profile is deleted, badRequest if profile is
     * not found for that userID.
     */
    //TODO: Authorization for admin only
    public CompletableFuture<Result> deleteProfile(Long id) {
        return profileRepository.deleteProfile(id).thenApplyAsync(rowsDeleted -> {
            if (rowsDeleted < 1) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.map("Profile not found for that user", "other");
                return badRequest(errorResponse.toJson());
            } else {
                return ok(Json.toJson(rowsDeleted));
            }
        });
    }

    /**
     * Retrieves all profiles, filters them and then returns the filtered list of profiles.
     *
     * @param nationalityId nationality request
     * @param gender gender requested
     * @param minAge minimum age for filter
     * @param maxAge maximum age for filter
     * @param travellerTypeId traveller type requested
     * @return List of profiles within requested parameters
     */
    public CompletableFuture<List<Profile>> searchProfiles(Long nationalityId, String gender,
        int minAge, int maxAge, Long travellerTypeId) {
        return profileRepository.getAllProfiles().thenApplyAsync(profiles -> {

            List<Profile> toReturn = new ArrayList<>(profiles);

            for (Profile profile : profiles) {
                if (gender != null) {
                    if (!profile.gender.equalsIgnoreCase(gender)) {
                        toReturn.remove(profile);
                        continue;
                    }
                }

//                LocalDate birthDate = LocalDate.parse(profile.dateOfBirth, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
//                int age = Period.between(birthDate, LocalDate.now()).getYears();
                int age = profile.calculateAge();

                if (age < minAge || age > maxAge) {
                    toReturn.remove(profile);
                    continue;
                }

                if (nationalityId != 0) {
                    boolean found = false;
                    for (CountryDefinition country : profile.nationalities) {
                        if (country.id.equals(nationalityId)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        toReturn.remove(profile);
                        continue;
                    }
                }

                if (travellerTypeId != 0) {
                    boolean found = false;
                    for (TravellerTypeDefinition travellerTypeDefinition : profile.travellerTypes) {
                        if (travellerTypeDefinition.id.equals(travellerTypeId)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        toReturn.remove(profile);
                    }
                }

            }

            return toReturn;
        });
    }

    /**
     * Retrieves all profiles, filters them and returns the result inside a Result object as JSON
     *
     * @param nationalityId nationality request
     * @param gender gender requested
     * @param minAge minimum age for filter
     * @param maxAge maximum age for filter
     * @param travellerTypeId traveller type requested
     * @return A ok result containing the JSON of the profiles matching search criteria
     */
    public CompletableFuture<Result> searchProfilesJson(Long nationalityId, String gender,
        int minAge, int maxAge, Long travellerTypeId) {
        return searchProfiles(nationalityId, gender, minAge, maxAge, travellerTypeId)
            .thenApplyAsync(profiles ->
                ok(Json.toJson(profiles)));
    }
}


