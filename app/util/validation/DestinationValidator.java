package util.validation;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A class which validates Destination information.
 */
public class DestinationValidator extends Validator {

    private static final String TYPE = "destType";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String DISTRICT = "district";
    private final JsonNode form;

    public DestinationValidator(JsonNode form) {
        super(form, new ErrorResponse());
        this.form = form;
    }

    /**
     * Validates data when creating a new destination.
     *
     * @return an ErrorResponse object with relevant information about each failed field
     */
    public ErrorResponse validateDestination(boolean isUpdating) {
        // Check that the destination name is present, is text, and not empty
        if (this.required("name", "Destination Name") && this.isText("name")) {
            this.minTextLength("name", "Name", 1);
        }

        // Checks the destination has an owner with an ID if it is being created not updated
        if (!isUpdating && this.required("user", "User") && this.form.get("user").get("id")
            .asText("").equals("")) {
            this.getErrorResponse().map("User ID required", "userId");
        }

        // Check that the destination type is present, is text, and not empty
        if (this.required(TYPE, "Destination Type") && this.isText(TYPE)) {
            this.minTextLength(TYPE, "Destination Type", 1);
        }

        // Check that the destination district is present, is text, and not empty
        if (this.required(DISTRICT, "District") && this.isText(DISTRICT)) {
            this.minTextLength(DISTRICT, "District", 1);
        }

        // Check that the destination's country id is present, is text, and not empty
        this.required("country", "Country");

        // Check that the latitude is present and within bounds
        if (this.required(LATITUDE, "Latitude") && this.isDoubleOrInt(LATITUDE)) {
            this.maxDoubleValue(LATITUDE, 90);
            this.minDoubleValue(LATITUDE, -90);
        }

        // Check that the longitude is present and within bounds
        if (this.required(LONGITUDE, "Longitude") && this.isDoubleOrInt(LONGITUDE)) {
            this.maxDoubleValue(LONGITUDE, 180);
            this.minDoubleValue(LONGITUDE, -180);
        }

        // Checks the destination has tags
        this.requiredTags("tags", "Tags");

        return this.getErrorResponse();
    }
}
