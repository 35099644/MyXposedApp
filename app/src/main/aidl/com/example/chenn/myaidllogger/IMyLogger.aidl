// IMyLogger.aidl
package com.example.chenn.myaidllogger;

// Declare any non-default types here with import statements

interface IMyLogger {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void show_message(String message);
}
