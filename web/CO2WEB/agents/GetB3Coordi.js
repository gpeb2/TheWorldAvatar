/**
 * This will most likely be rewritten to merge with existing services
 *
 */
/***
 * A function to get Biodiesel plant 3 coordinates information
 * @param callback
 */
var log4js = require('log4js');
var logger = log4js.getLogger();
logger.level = 'debug';
const xmlProcessor = require("./fileConnection"),
    config = require("../config")
function  getB3Coordi(callback) {
    /***
     *
     */
    logger.debug("getB3Coordi")
    xmlProcessor.getChildrenRecur({topnode : config.b3Node}, function (err, results) {

        if(err){
            logger.debug(err);
            callback(err)
            return;
        }


        logger.debug("!!!!!!!!!!!!!!!!!!!!!!!")
        logger.debug(JSON.stringify(results.geoCoords))

        callback(null, format(results.geoCoords));


    });

    function format(list) {
        return list.map((item)=>{return {uri: item.url, location:{lat: item.coord.y, lng:item.coord.x}, type:  item.type}});
    }

}



module.exports = getB3Coordi
