// Test.aidl
package it.unicam.project.multiinterfacesender;

import it.unicam.project.multiinterfacesender.IService_Wifi_to_App;

interface IService_App_to_Wifi {
    void register(IService_Wifi_to_App activity);
    void createConnection(String ip, int port);
    void connect();
    void disconnect();
    void setupPackage(in byte[] data);
    void sendPackage();
}

