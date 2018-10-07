package it.unicam.project.multiinterfacesender.Receive;

import java.io.*;

public class SendedData implements Serializable {

    private static final long serialVersionUID = 5950169519310163575L;

    String dtoken;
    String fileName;
    byte[] fileChunk;

    /**
     * @param _isFileName
     * @param _dtoken dtoken
     */
    public SendedData(String _dtoken,boolean _isFileName)
    {
        if(_isFileName==true)
        {
            this.dtoken = _dtoken;
            this.fileName = null;
            this.fileChunk = null;
        }
        else
        {
            this.dtoken = null;
            this.fileName = _dtoken;
            this.fileChunk = null;
        }

    }

    /**
     *
     * @param fileChunk byte_array
     */
    public SendedData(byte[] fileChunk)
    {
        this.dtoken = null;
        this.fileName = null;
        this.fileChunk = fileChunk;
    }

    public SendedData(byte[] fileChunk, String fileName)
    {
        this.dtoken = null;
        this.fileName = fileName;
        this.fileChunk = fileChunk;
    }
}
