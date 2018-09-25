package it.unicam.project.multiinterfacesender;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SyncToServerTasks {
    public static final String URL = "http://ec2-18-191-173-32.us-east-2.compute.amazonaws.com:80";

    public static class LoginTask extends AsyncTask<Void, Void, String> {
        private String username;
        private String password;
        private String deviceToken=null;

        public LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }
        public LoginTask(String username, String password, String deviceToken) {
            this.username = username;
            this.password = password;
            this.deviceToken= deviceToken;
        }

        protected String doInBackground(Void... urls) {
            String api = "/api/login";
            HttpURLConnection urlConnection;
            JSONObject json = new JSONObject();
            try {
                json.put("username", username);
                json.put("psw", password);
                if(deviceToken!=null){
                    json.put("devicetoken", deviceToken);
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
            String string = json.toString();
            String result;
            try {
                urlConnection = (HttpURLConnection) ((new URL(URL + api).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.connect();
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(string);
                writer.close();
                outputStream.close();
                if (urlConnection.getResponseCode() == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    bufferedReader.close();
                    return sb.toString();
                } else if (urlConnection.getResponseCode() == 401) {
                    return "unauthorized";
                } else {
                    Log.e("Response code", String.valueOf(urlConnection.getResponseCode()));
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    public static class RegistationTask extends AsyncTask<Void, Void, String> {
        private String username;
        private String password;
        private String email;

        public RegistationTask(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
        }

        protected String doInBackground(Void... urls) {
            String api = "/api/registration";
            HttpURLConnection urlConnection;
            JSONObject json = new JSONObject();
            try {
                json.put("username", username);
                json.put("psw", password);
                json.put("email", email);
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
            String string = json.toString();
            try {
                urlConnection = (HttpURLConnection) ((new URL(URL + api).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.connect();
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(string);
                writer.close();
                outputStream.close();
                if (urlConnection.getResponseCode() == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    bufferedReader.close();
                    return sb.toString();
                } else if (urlConnection.getResponseCode() == 409) {
                    return "duplicate";
                } else {
                    Log.e("Response code", String.valueOf(urlConnection.getResponseCode()));
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class GetInfoTask extends AsyncTask<Void, Void, String> {
        private String uToken;
        private String dToken;

        public GetInfoTask(String uToken, String dToken) {
            //true request, false report
            this.uToken= uToken;
            this.dToken = dToken;
        }

        protected String doInBackground(Void... urls) {
            String api="/api/getinfo";
            try {
                URL url = new URL(URL + api +"?utoken="+uToken+"&dtoken="+dToken);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    if (urlConnection.getResponseCode()==200) {
                        InputStreamReader streamReader = new
                                InputStreamReader(urlConnection.getInputStream());
                        BufferedReader bufferedReader = new BufferedReader(streamReader);
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return stringBuilder.toString();
                    } else if(urlConnection.getResponseCode()==401) {
                        InputStreamReader streamReader = new
                                InputStreamReader(urlConnection.getErrorStream());
                        BufferedReader bufferedReader = new BufferedReader(streamReader);
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return stringBuilder.toString();
                    } else {
                        Log.e("Response code", String.valueOf(urlConnection.getResponseCode()));
                        return null;
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    public static class NewDeviceTask extends AsyncTask<Void, Void, String> {
        private String userToken;
        private String devicename;

        public NewDeviceTask(String userToken, String devicename) {
            this.userToken=userToken;
            this.devicename=devicename;
        }

        protected String doInBackground(Void... urls) {
            String api="/api/device/add";
            HttpURLConnection urlConnection;
            JSONObject json = new JSONObject();
            try {
                json.put("utoken", userToken);
                json.put("devicename", devicename);
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
            String string = json.toString();
            try {
                urlConnection = (HttpURLConnection) ((new URL(URL + api).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.connect();
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(string);
                writer.close();
                outputStream.close();
                if (urlConnection.getResponseCode() == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    bufferedReader.close();
                    return sb.toString();
                } else if(urlConnection.getResponseCode() == 409){
                    return "duplicate";
                } else if (urlConnection.getResponseCode() == 401) {
                    return "unauthorized";
                } else return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    public static class GetDeviceListTask extends AsyncTask<Void, Void, String> {
        private String uToken;

        public GetDeviceListTask(String userToken) {
            this.uToken=userToken;
        }

        protected String doInBackground(Void... urls) {
            String api="/api/device/getall";
            try {
                URL url = new URL(URL + api +"?usertoken="+uToken);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    if (urlConnection.getResponseCode()==200)  {
                        InputStreamReader streamReader = new
                                InputStreamReader(urlConnection.getInputStream());
                        BufferedReader bufferedReader = new BufferedReader(streamReader);
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        bufferedReader.close();
                        return stringBuilder.toString();
                    } else if (urlConnection.getResponseCode()==401){
                        return "unauthorized";
                    } else {
                        Log.e("Response code", String.valueOf(urlConnection.getResponseCode()));
                        return null;
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    public static class SessionConnectTask extends AsyncTask<Void, Void, String> {
        private String userToken;
        private String deviceToken;
        private String sessionCode;

        public SessionConnectTask(String userToken, String deviceToken, String sessionCode) {
            this.userToken=userToken;
            this.deviceToken=deviceToken;
            this.sessionCode=sessionCode;
        }

        protected String doInBackground(Void... urls) {
            String api="/api/session/connect";
            HttpURLConnection urlConnection;
            JSONObject json = new JSONObject();
            try {
                json.put("utoken", userToken);
                json.put("dtoken", deviceToken);
                json.put("sessioncode", sessionCode);
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
            String string = json.toString();
            try {
                urlConnection = (HttpURLConnection) ((new URL(URL + api).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.connect();
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(string);
                writer.close();
                outputStream.close();
                if (urlConnection.getResponseCode() == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    bufferedReader.close();
                    return sb.toString();
                } else if(urlConnection.getResponseCode()==401){
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), "UTF-8"));
                    String line = null;
                    StringBuilder sb = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    bufferedReader.close();
                    return sb.toString();
                } else {
                    Log.e("Response code", String.valueOf(urlConnection.getResponseCode()));
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    public static boolean checkConnection(Activity myActivity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) myActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else return false;
    }
}

