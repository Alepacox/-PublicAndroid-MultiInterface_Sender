package it.unicam.project.multiinterfacesender;

import android.app.Activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static android.content.Context.MODE_PRIVATE;

public class RWStorage {
    private Activity myActivity;

    public RWStorage(Activity myActivity){
        this.myActivity=myActivity;
    }

    public String readUserToken() {
        try {
            FileInputStream devicefile = myActivity.openFileInput("uToken");
            InputStreamReader InputRead = new InputStreamReader(devicefile);

            char[] inputBuffer = new char[48];
            String userToken = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                // char to string conversion
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                userToken += readstring;
            }
            InputRead.close();
            if(userToken.length()!=0)return userToken;
            else return null;

        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String readDeviceToken() {
        try {
            FileInputStream devicefile = myActivity.openFileInput("dToken");
            InputStreamReader InputRead = new InputStreamReader(devicefile);
            char[] inputBuffer = new char[48];
            String deviceToken = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                // char to string conversion
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                deviceToken += readstring;
            }
            InputRead.close();
            if(deviceToken.length()!=0)return deviceToken;
            else return null;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void writeUserToken(String uToken){
        try {
            FileOutputStream fileout=myActivity.openFileOutput("uToken", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(uToken);
            outputWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeDeviceToken(String dToken){
        try {
            FileOutputStream fileout=myActivity.openFileOutput("dToken", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write(dToken);
            outputWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
