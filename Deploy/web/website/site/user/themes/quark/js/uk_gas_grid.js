/*
 * Copyright (c) CMCL Innovations - All Rights Reserved
 *  
 * This application and all inherent data, source files, information and graphics are 
 * the copyright and sole property of Computational Modelling Cambridge Ltd (CMCL Innovations). 
 *  
 * Any unauthorised redistribution or reproduction of part, or all, of the contents of this 
 * applicationin any form is prohibited under UK Copyright Law. You may not, except with the 
 * express written permission of CMCL Innovations, distribute or commercially exploit this
 * application or it's content. All other rights reserved.
 *  
 * For more information please contact support@cmclinnovations.com
 * 
 * ------------------------------------------------------------
 * 
 * This script contains functionality for the page containing the
 * UK Digital Twin.
 */

// Determine the original page location (in it's container)
var root = window.location.hostname;
var originalPage = "http://" + root + "/digital-twin/gas";

if(!originalPage.startsWith("http")) {
	originalPage = "http://" + originalPage;
}

// Pipe into iframe
let mapFrame = document.getElementById("map-frame");
mapFrame.setAttribute("src", originalPage);