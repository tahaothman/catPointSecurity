module com.udacity.catpoint.security{
    exports com.udacity.catpoint.security.data;
    exports com.udacity.catpoint.security.service;
    exports com.udacity.catpoint.security.application;
    requires java.desktop;
    requires com.google.common;
    requires com.google.gson;
    requires java.prefs;
    requires com.udacity.catpoint.image;

    opens com.udacity.catpoint.security.data to com.google.gson;


}