
var log4js = require('log4js');
var logger = log4js.getLogger();
logger.level = 'debug';

var express = require('express');



var mapRouterFactory = function (router, getCoordinatesData, texts, view) {
    var cdataPromise = getCDataPromise();
    var cData;

    var textsOb   = {
        title:texts.title || "Map",
        subtitle:texts.subtitle || "Map",
       description:texts.description || ""
    }




    cdataPromise.then(function (promisedData) {
        cData = promisedData;
    });//TODO: onReject

    router.get('/', function(req, res, next) {
        res.render(view);//TODO: render with provided texts
        //send a file
    });

    router.get('/coordinates', function (req, res, next) {

        if(!cData){
            console.log("server did not load data")
            next(new Error("Server can not provide the data now.Please try again"))
        } else{
            res.json(cData);
        }});

    return router;

    function  getCDataPromise() {
        return new Promise(function (resolve, reject) {
            console.log("getting coordi")
            getCoordinatesData( function (err, data) {

                if(err){
                    console.log(err)
                    logger.debug(err)

                    reject(err);
                }
                logger.debug("Coordi:")
                logger.debug(data)
                resolve(data);
            });
        });
    }


}

















module.exports = mapRouterFactory;
