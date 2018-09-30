// IMyAidlInterface.aidl
package it.unicam.project.multiinterfacesender;

import it.unicam.project.multiinterfacesender.IService_Mobile_to_App;

interface IService_App_to_Mobile {
    void register(IService_Mobile_to_App activity);
    void createConnection(String ip, int port);
    void connect();
    void disconnect();
    void setupPackage(in byte[] data);
    void sendPackage();
}
