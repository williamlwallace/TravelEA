package repository;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.PagedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import models.CountryDefinition;
import play.db.ebean.EbeanConfig;

@Singleton
public class CountryDefinitionRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public CountryDefinitionRepository(EbeanConfig ebeanConfig,
        DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Insert a new country definition to database, auto-increments a new id for definition and
     * returns this id.
     *
     * @param definition Country definition to add, ID field is not used
     * @return The ID of the newly created country definition
     */
    public CompletableFuture<Long> insertCountryDefinition(CountryDefinition definition) {
        return supplyAsync(() -> {
            ebeanServer.insert(definition);
            return definition.id;
        }, executionContext);
    }

    /**
     * Finds the country with exact name as specified E.g When getting the id of a country from a
     * dropdown box.
     *
     * @param name Exact name of country to retrieve
     * @return CountryDefinition object if one found, otherwise null
     */
    public CompletableFuture<CountryDefinition> findCountryByExactName(String name) {
        return supplyAsync(() ->
                ebeanServer.find(CountryDefinition.class)
                    .where()
                    .eq("name", name)
                    .findOneOrEmpty()
                    .orElse(null),
            executionContext);
    }

    /**
     * Finds the country with the id specified.
     *
     * @param id Exact country to retrieve
     * @return CountryDefinition of object found, otherwise null
     */
    public CompletableFuture<CountryDefinition> findCountryByID(long id) {
        return supplyAsync(() ->
                ebeanServer.find(CountryDefinition.class)
                    .where()
                    .idEq(id)
                    .findOneOrEmpty()
                    .orElse(null),
            executionContext);
    }

    /**
     * Gets all countries from the database.
     *
     * @return A list of all the countries from the database
     */
    public CompletableFuture<List<CountryDefinition>> getAllCountries() {
        return supplyAsync(() ->
                ebeanServer.find(CountryDefinition.class).findList(),
            executionContext);
    }

    /**
     * Returns a paged list of countries that have a name containing some search criteria E.g
     * Criteria "ew " would return N(ew )Zealand, N(ew )Caledonia, etc..
     *
     * @param criteria Criteria that must be contained in country name
     * @param pageNumber Number of page results to get
     * @param pageSize The maximum number of entries per page
     * @param sortAscending Whether to sort alphabetically A-Z (true) or Z-A (false)
     * @return Paged list of country definitions matching search criteria
     */
    public CompletableFuture<PagedList<CountryDefinition>> findCountriesByNameCriteria(
        String criteria, int pageNumber, int pageSize, boolean sortAscending) {
        return supplyAsync(() ->
                ebeanServer.find(CountryDefinition.class)
                    .where()
                    //Where criteria found in any part of string
                    .ilike("name", "%" + criteria + "%")
                    .orderBy("name " + ((sortAscending) ? "asc" : "desc"))
                    .setFirstRow(pageNumber * pageSize)
                    .setMaxRows(pageSize)
                    .findPagedList(),
            executionContext);
    }
}