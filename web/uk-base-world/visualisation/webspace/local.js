/**
 * Gets the legend content for this example visualisation. In this case it's loaded
 * from an external HTML file, but a HTML string could be dynamically generated
 * if desired.
 * 
 * @returns HTML string for legend content.
 */
function buildLegend() {
    $.get("./legend.html", function(contents) {
        manager.getPanelHandler().setLegend(contents);
    }); 
}

/**
 * Collapses a section in the legend.
 * 
 * @param {String} sectionName name of legend section div
 */
function toggleLegend(sectionName) {
    let section = document.getElementById(sectionName);

    if(section != null) {
        let icon = section.querySelector("#icon")
        let contents = section.querySelector("#contents");

        if(contents.style.display === "block") {
            contents.style.display = "none";
            icon.setAttribute("class", "fas fa-angle-down fa-lg");
        } else {
            contents.style.display = "block";
            icon.setAttribute("class", "fas fa-angle-up fa-lg");
        }
    }
}

/**
 * Builds UI components that are custom to the UK Base World visualisation.
 * 
 * Note that this functionality is specific to the UK Base World visualisation and may
 * break if the layer names or positions within the data.json are changed (without then
 * updating this section).
 */
function showCustomControls() {
    let customControlsContainer = document.createElement("div");
    customControlsContainer.id = "customControlsContainer";
    customControlsContainer.style.display = "none";

    let showRadiusButton = document.createElement("div");
    showRadiusButton.id = "showRadiusButton";
    showRadiusButton.classList.add("controlBlock");
    showRadiusButton.innerHTML = "<p>Hide 1KM radius</p>";

    customControlsContainer.appendChild(showRadiusButton);
    document.body.appendChild(customControlsContainer);

    showRadiusButton.addEventListener("click", function() {
        if(showRadiusButton.innerHTML.includes("Show")) {
            // Show radius
            MapHandler.MAP.setLayoutProperty("0.2.power_renewable_radius_layer", "visibility", "visible");
            MapHandler.MAP.setLayoutProperty("0.2.power_fossil_radius_layer", "visibility", "visible");
            showRadiusButton.innerHTML = "<p>Hide 1KM radius</p>";
        } else {
            // Hide radius
            MapHandler.MAP.setLayoutProperty("0.2.power_renewable_radius_layer", "visibility", "none");
            MapHandler.MAP.setLayoutProperty("0.2.power_fossil_radius_layer", "visibility", "none");
            showRadiusButton.innerHTML = "<p>Show 1KM radius</p>";
        }
    });

    // Add a callback to fire once a feature is selected.
    manager.addSelectionCallback(function(feature) {
        let layerID = feature?.layer?.id;

        if(layerID.endsWith("power_renewable_layer") || layerID.endsWith("power_fossil_layer")) {
            // Only fire for the power plant layers
            customControlsContainer.style.display = "block";
        } else {
            customControlsContainer.style.display = "none";
        }
    });

    // Add a callback to fire once a feature has been unselected/selection is cleared
    manager.addUnselectionCallback(function() {
        // Revert back to original state
        customControlsContainer.style.display = "none";
    });

    // Add a callback to fire when the selection state of the layer tree changes
    manager.addTreeSelectionCallback(function(visibleLayerIDs, hiddenLayerIDs) {
        if(hiddenLayerIDs.includes("0.2.power_renewable_radius_layer") && hiddenLayerIDs.includes("0.2.power_fossil_radius_layer")) {
            customControlsContainer.style.display = "none";
        } else {
            customControlsContainer.style.display = "block";
        }
    });

}
// End of file.