// IMyAidlInterface2.aidl
package it.unicam.project.multiinterfacesender;

// Declare any non-default types here with import statements

interface IService_Mobile_to_App {
    void connection_Created();
    void connection_Established();
    void connection_Closed();
    void from_Server(in byte[] data);
}
