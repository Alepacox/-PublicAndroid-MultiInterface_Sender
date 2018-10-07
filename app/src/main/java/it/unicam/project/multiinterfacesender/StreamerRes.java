package it.unicam.project.multiinterfacesender;

import java.util.Arrays;

public class StreamerRes {
    public byte[] Message;

    StreamerRes(byte[] chunk_ID, byte[] buffer) {
        Message = Arrays.copyOf(buffer, buffer.length + 2);
        Message[Message.length - 2] = chunk_ID[0];
        Message[Message.length - 1] = chunk_ID[1];
    }
}
