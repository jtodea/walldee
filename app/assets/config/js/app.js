/*global define, angular */

'use strict';

// Declare here that angular is the US version - other locales can be easily substituted.

define('angular', ['webjars!angular.js', 'webjars!angular-route.js', 'webjars!angular-resource.js'], function () {
    return angular;
});

requirejs.config({
    shim: {
        'webjars!angular-route.js': ['webjars!angular.js']
    }
});

require([ 'angular', './controllers', './directives', './filters', './services'], function (angular) {

// Declare app level module which depends on filters, and services

    angular.module('walldee', ['ngRoute', 'ngResource', 'walldee.filters', 'walldee.services', 'walldee.directives', 'walldee.controllers']).
        config(['$routeProvider', function ($routeProvider) {
            $routeProvider.when('/projects', {templateUrl: '/config/projects', reloadOnSearch: false});
            $routeProvider.when('/teams', {templateUrl: '/config/teams', reloadOnSearch: false});
            $routeProvider.when('/projects/:projectId/statusMonitors', {templateUrl: '/config/statusMonitor'});
            $routeProvider.when('/projects/:projectId/statusMonitors/:statusMonitorId', {templateUrl: '/config/statusMonitor'});
            $routeProvider.when('/displays', {templateUrl: '/config/displays', reloadOnSearch: false});
            $routeProvider.otherwise({redirectTo: '/'});
        }]);

    angular.bootstrap(document, ['walldee']);
});
