package util.validation;

import com.fasterxml.jackson.databind.JsonNode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Provides validation for json form data
 */
public class Validator {
    private final JsonNode form; //json form data
    private ErrorResponse errorResponse;

    public Validator(JsonNode form, ErrorResponse errorResponse) {
        this.form = form;
        this.errorResponse = errorResponse;
    }
    
    /**
     * Checks field is not empty
     * @param field json field name
     * @return Boolean whether validation succeeds
     */
    protected Boolean required(String field) {
        if (this.form.get(field).asText("") == "") {
            this.errorResponse.map(String.format("%s required", field), field);
            return false;
        }
        return true;
    }
    
    /**
     * Checks field is longer then given length
     * @param field json field name
     * @param min Min field length
     * @return Boolean whether validation succeeds
     */
    protected Boolean min(String field, int min) {
        if (this.form.get(field).asText("").length() < min) {
            this.errorResponse.map(String.format("%s has a min length of %d", field, min), field);
            return false;
        }
        return true;
    }

    /**
     * Checks field is shorter then given length
     * @param field json field name
     * @param max max field length
     * @return Boolean whether validation succeeds
     */
    protected Boolean max(String field, int max) {
        if (this.form.get(field).asText("").length() > max) {
            this.errorResponse.map(String.format("%s has a max length of %d", field, max), field);
            return false;
        }
        return true;
    }

    /**
     * Checks field if valid email
     * @param field json field name
     * @return Boolean whether validation succeeds
     */
    protected Boolean email(String field) {
        String email = this.form.get(field).asText("");
        Pattern pattern = Pattern.compile("^.+@.+\\..+$");
        if (!pattern.matcher(email).matches()) {
            this.errorResponse.map("Invalid email", field);
            return false;
        }
        return true;
    }

    /**
     * Checks field if valid date
     * @param field json field name
     * @return Boolean whether validation succeeds
     */
    protected Boolean date(String field) {
        String date = this.form.get(field).asText("");
        try {
            new SimpleDateFormat("yyyy-MM-dd").parse(date);
        }
        catch (ParseException e) {
            this.errorResponse.map("Invalid date", field);
            return false;
        }
        return true;
    }

    /**
     * Checks field if valid gender
     * @param field json field name
     * @return Boolean whether validation succeeds
     */
    protected Boolean gender(String field) {
        String gender;
        try {
            gender = this.form.get(field).asText("");
        } 
        catch (NullPointerException e) {
            this.errorResponse.map("Invalid gender", field);
            return false;
        }
        if (gender.equals("Male") || gender.equals("Female") || gender.equals("Other") || gender.isEmpty()) {
            return true;
        }
        this.errorResponse.map("Invalid gender", field);
        return false;
    }

    /**
     * errorResponse getter
     * @return ErrorResponse
     */
    protected ErrorResponse getErrorResponse() {
        return this.errorResponse;
    }
}