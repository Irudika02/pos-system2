package com.devstack.pos.env;

public class StaticResource {
    private final static String VERSION = "1.0.0";
    private final static String COMPANY = "Wijenayake Stores";
    private final static String DEVELOPER = "Irudika Wijenayake";

    public static String getVERSION() {
        return VERSION;
    }

    public static String getCOMPANY() {
        return COMPANY;
    }

    public static String getDEVELOPER() {
        return DEVELOPER;
    }
}
