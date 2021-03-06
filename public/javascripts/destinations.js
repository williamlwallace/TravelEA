let activeInfoWindow;
let map;
let toggled;
let requestOrder = 0;
let lastRecievedRequestOrder = -1;
let paginationHelper;

/**
 * Initializes destination page and map
 * @param {Number} userId - ID of user to get destinations for
 */
function onPageLoad(userId) {
    paginationHelper = new PaginationHelper(1, 1, refreshData,
        "pagination-destinations");
    const options = {
        zoom: 2.2,
        center: {lat: 0, lng: 0}
    };
    map = new DestinationMap(options, true, userId);
    refreshData();

    google.maps.event.addListener(map.map, 'click', function (event) {
        if (!map.creativeMode) {
            return;
        }
        if (map.newMarker) {
            map.newMarker.setPosition(event.latLng);
        } else {
            map.newMarker = map.placeMarker(event.latLng, null);
        }
        $('#latitude').val(event.latLng.lat);
        $('#longitude').val(event.latLng.lng);
        toggled = false;
        toggleDestinationForm();
    });

    toggleDestinationForm();

}

/**
 * Returns a boolean of the getonlymine selector tick box
 * @returns {Boolean} True if the getonlymine text box it ticked, false otherwise
 */
function getOnlyGetMine() {
    return $('#onlyGetMine').is(":checked");
}

/**
 * Returns the string of the searchQuery field.
 * @returns {String} a string of the data in the searchQuery field
 */
function getSearchQuery() {
    return $('#searchQuery').val();
}

/**
 * Get the value of the number of results to show per page
 * @returns {Number} The number of results shown per page
 */
function getPageSize() {
    return $('#pageSize').val();
}

/**
 * Returns the name of the db column to search by
 * @returns {String} Name of db column to search by
 */
function getSortBy() {
    return $('#sortBy').val();
}

/**
 * Gets whether or not to sort by ascending
 * @returns {String} Either 'true' or 'false', where true is ascending, false is descending
 */
function getAscending() {
    return $('#ascending').val();
}

/**
 * Gets the destinations from the database given the query parameters set by the
 * user with the destination filters.
 * @returns {Object} Returns a promise of the get request sent
 */
function getDestinations() {
    const url = new URL(
        destinationRouter.controllers.backend.DestinationController.getPagedDestinations().url,
        window.location.origin);

    // Append non-list params
    if (getSearchQuery() !== "") {
        url.searchParams.append("searchQuery", getSearchQuery());
    }

    url.searchParams.append("onlyGetMine", getOnlyGetMine().toString());

    const pageSize = getPageSize();
    const pageSizeSelector = $('#pageSize');
    if (pageSize > 100) {
        pageSizeSelector.val(100);
        toast("Results per page too large",
            "The maximum results per page is 100, only 100 results will be returned",
            "warning", 7500);
    } else if (pageSize < 1) {
        pageSizeSelector.val(1);
        toast("Results per page too small",
            "The minimum results per page is 1, 1 result will be returned",
            "warning", 7500);
    }

    // Append pagination params
    url.searchParams.append("pageNum", paginationHelper.getCurrentPageNumber());
    url.searchParams.append("pageSize", pageSize.toString());
    url.searchParams.append("sortBy", getSortBy());
    url.searchParams.append("ascending", getAscending());
    url.searchParams.append("requestOrder", requestOrder.toString());
    requestOrder++;

    return get(url)
    .then(response => {
        return response.json()
        .then(json => {
            if (response.status !== 200) {
                toast("Error with destinations", "Cannot load destinations",
                    "danger")
            } else {
                if (lastRecievedRequestOrder < json.requestOrder) {
                    lastRecievedRequestOrder = json.requestOrder;
                    paginationHelper.setTotalNumberOfPages(
                        json.totalNumberPages);
                    return json;
                }
            }
        });
    });
}

/**
 * Refreshes the destination data and populates the map the destination cards
 * with this.
 */
function refreshData() {
    getDestinations().then(destinations => {
        map.populateMarkers(destinations);
        createDestinationCards(destinations);
    });
}

/**
 * Resets the fields of the destinations filter
 */
function clearDestinationsFilter() {
    $("#searchQuery").val("");
    $("#pageSize").val(25);
    $("#sortBy").val("name");
    $("#sortBy").selectpicker("refresh");
    $("#ascending").val("true");
    $("#ascending").selectpicker("refresh");
}

/**
 * Takes a list of destinations and creates destination cards out of these.
 * @param {Object} dests - An array of destinations to display
 */
function createDestinationCards(dests) {
    $("#destinationCardList").html("");
    dests.data.forEach((dest) => {
        const card = createDestinationCard(dest);

        $("#destinationCardList").get(0).appendChild(card);
    });
    $('#destinationTags').tagsinput({
        trimValue: true
    });
}

/**
 * Creates a destination card out of the destination details provided
 *
 * @param {Object} dest - Destination details
 * @returns {Node | ActiveX.IXMLDOMNode} - ID of destination card
 */
function createDestinationCard(dest) {
    const template = $("#destinationCardTemplate").get(0);
    const clone = template.content.cloneNode(true);
    let tags = "";
    let travellerTypes = "";

    $(clone).find("#card-header").append(dest.name);
    if (dest.primaryPhoto) {
        $(clone).find("#card-thumbnail").attr("src",
            "../user_content/" + dest.primaryPhoto.thumbnailFilename);
    }
    $(clone).find("#district").append(
        dest.district ? dest.district : "No district");
    $(clone).find("#country").append(dest.country.name);
    $(clone).find("#destType").append(
        dest.destType ? dest.destType : "No type");
    $(clone).find("#card-header").attr("data-id", dest.id.toString());
    $(clone).find("#card-header").attr("id",
        "destinationCard-" + dest.id.toString());

    $($(clone).find('#destinationCard-' + dest.id.toString())).click(
        function () {
            location.href = '/destinations/' + $(this).data().id;
        });

    dest.tags.forEach(item => {
        tags += item.name + ", ";
    });
    tags = tags.slice(0, -2);

    dest.travellerTypes.forEach(item => {
        travellerTypes += item.description + ", ";
    });
    travellerTypes = travellerTypes.slice(0, -2);

    $(clone).find("#destinatonCardTravellerTypes").append(
        travellerTypes ? travellerTypes : "No traveller types");
    $(clone).find("#tags").append(tags ? tags : "No tags");

    return clone;
}

/**
 * Add destination to database
 * @param {String} url - API URI to add destination
 * @param {String} redirect - URI of redirect page
 * @param {Number} userId - The id of the user adding the destination
 */
function addDestination(url, redirect, userId) {
    // Read data from destination form
    const formData = new FormData(
        document.getElementById("addDestinationForm"));

    // Convert data to json object
    const data = Array.from(formData.entries()).reduce((memo, pair) => ({
        ...memo,
        [pair[0]]: pair[1],
    }), {});

    // Convert lat and long to double values, and id to int
    data.latitude = parseFloat(data.latitude);
    data.longitude = parseFloat(data.longitude);
    data.countryId = parseInt(data.countryId);
    data.user = {
        id: userId
    };

    // Convert country id to country object
    data.country = {"id": data.countryId};

    // Privacy
    const destinationPrivacy = $('#destinationPrivacy').html();
    data.isPublic = destinationPrivacy === "Public";

    const destinationTags = tagPicker.getTags();
    data.tags = destinationTags.map((tag) => {
        return {name: tag}
    });

    //Create response handler
    const handler = function (status, json) {
        if (status !== 200) {
            if (json === "Duplicate destination") {
                toast("Destination could not be created!",
                    "The destination already exists.", "danger", 5000);
                $('#createDestinationModal').modal('hide');
                resetDestinationModal();
            } else {
                showErrors(json);
            }
        } else {
            toast("Destination Created!",
                "The new destination will be added.",
                "success");
            $('#createDestinationModal').modal('hide');
            resetDestinationModal();

            refreshData();
            toggled = true;
            toggleDestinationForm();

        }
    }.bind({userId, data, map});
    const inverseHandler = function (status, json) {
        if (status === 200) {
            refreshData();
        }
    }.bind({map});
    // Post json data to given url
    addNonExistingCountries([data.country]).then(result => {
        const reqData = new ReqData(requestTypes['CREATE'], url, handler, data);
        undoRedo.sendAndAppend(reqData, inverseHandler);
    });
}

/**
 * Clears all fields and error labels in the create destination modal form
 */
function resetDestinationModal() {
    document.getElementById("addDestinationForm").reset();
    tagPicker.clearTags();
    hideErrors("addDestinationForm");
    $('#destinationPrivacyStatus').siblings('label').html('Private');
    $("#countryDropDown").val("004"); //Resets to Afghanistan
    $("#countryDropDown").selectpicker("refresh"); //Resets to Afghanistan
}

/**
 * The latitude field listener. Enforces -90 < latitude < 90
 * Moves the marker on the map when the latitude changes
 */
$('#latitude').on('input', () => {
    if ($('#latitude').val() > 90) {
        $('#latitude').val('90');
    }
    if ($('#latitude').val() < -90) {
        $('#latitude').val('-90');
    }

    map.setNewMarker($('#latitude').val(), null);
});

/**
 * The longitude field listener. Enforces -90 < longitude < 90
 * Moves the marker on the map when the longitude changes
 */
$('#longitude').on('input', () => {
    if ($('#longitude').val() > 180) {
        $('#longitude').val('180');
    }
    if ($('#longitude').val() < -180) {
        $('#longitude').val('-180');
    }

    map.setNewMarker(null, $('#longitude').val());
});

/**
 * Opens and closes the create new destination form.
 */
function toggleDestinationForm() {
    toggled = toggled || false;
    if (toggled) {
        $("#mainSection").attr('class', 'col-md-12');
        $("#createDestinationPopOut").attr('class',
            'col-md-0 hideCreateDestinationPopOut');
        $("#arrow").attr('class', "fas fa-1x fa-arrow-left");
        toggled = false;
    } else {
        $("#mainSection").attr('class', 'col-md-8');
        $("#createDestinationPopOut").attr('class',
            'col-md-4 showCreateDestinationPopOut');
        $("#arrow").attr('class', "fas fa-1x fa-arrow-right");
        toggled = true;
    }
}

/**
 * Toggles the privacy toggle button text between public and private.
 */
function toggleDestinationPrivacy() {
    if ($('#destinationPrivacyStatus').is(':checked')) {
        $('#destinationPrivacyStatus').siblings('label').html('Public');
    } else {
        $('#destinationPrivacyStatus').siblings('label').html('Private');
    }
}

/**
 * On click listener for the create destinations cancel button
 */
$('#CreateDestinationCancelButton').click(function () {
    resetDestinationModal();
    map.removeNewMarker();
});

/**
 * Destination button on click
 */
function createDestination() {
    getUserId().then(userId => {
        addDestination(
            destinationRouter.controllers.backend.DestinationController.addNewDestination().url,
            "/", userId);
    });
}

/**
 * Pressing enter while within the destination filter will search
 */
$('#collapseDestinationFilter').keypress(function (e) {
    const key = e.which;
    if (key === 13) {
        refreshData();
    }
});