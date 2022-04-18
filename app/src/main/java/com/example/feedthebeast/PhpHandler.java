// Inspired by Vishnusivadas Advanced HttpURLConnection code

package com.example.feedthebeast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PhpHandler extends Thread {
    private final String utf_charset = StandardCharsets.UTF_8.name();
    private final String latin_charset = StandardCharsets.ISO_8859_1.name();
    private String url = "";
    private String method = "";

    public String[] field;
    public String[] data;

    private String result = "";

    public String getResult() {
        return this.result;
    }

    private void setResult(String value) {
        this.result = value;
    }

    public PhpHandler(String url, String method, String[] field, String[] data) {
        this.url = url;
        this.method = method;
        this.field = new String[field.length];
        this.data = new String[data.length];
        System.arraycopy(field, 0, this.field, 0, field.length);
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    public void sendRequest() {
        this.start();
    }

    public boolean resultReady() {
        while (true) {
            if (!this.isAlive()) {
                return true;
            }
        }
    }

    @Override
    public void run() {
        try {
            URL url = new URL(this.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(this.method);
            connection.setDoOutput(true);
            connection.setDoInput(true);

            // Send data
            OutputStream outputStream = connection.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, this.utf_charset);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            StringBuilder query = new StringBuilder();

            for (int i = 0; i < this.field.length; i++) {
                String field = URLEncoder.encode(this.field[i], this.utf_charset);
                String data = URLEncoder.encode(this.data[i], this.utf_charset);
                query.append(field).append('=').append(data).append('&');
            }

            bufferedWriter.write(query.toString());

            bufferedWriter.flush();
            bufferedWriter.close();
            outputStreamWriter.close();
            outputStream.close();

            // Get data
            InputStream inputStream = connection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, this.latin_charset);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder result = new StringBuilder();

            int result_int = bufferedReader.read();

            while (result_int != -1) {
                result.append((char) result_int);
                result_int = bufferedReader.read();
            }

            setResult(result.toString());

            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
