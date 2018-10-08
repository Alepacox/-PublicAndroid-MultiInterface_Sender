package it.unicam.project.multiinterfacesender;

// Declare any non-default types here with import statements

interface IService_Wifi_to_App {
    void wifiHandler(int code);
    void getProcessID(int code);
    void setupPackage(in byte[] data);
    void packageComplete();
}
