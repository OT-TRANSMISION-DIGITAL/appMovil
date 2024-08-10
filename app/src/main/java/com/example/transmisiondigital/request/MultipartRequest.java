package com.example.transmisiondigital.request;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;
    private final Map<String, String> mHeaders;
    private final File mFilePart;
    private final String mFilePartName;

    public MultipartRequest(String url, Response.ErrorListener errorListener, Response.Listener<NetworkResponse> listener, File file, String filePartName) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mHeaders = new HashMap<>();
        this.mFilePart = file;
        this.mFilePartName = filePartName;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Add file part
            buildPart(bos, mFilePart, mFilePartName);
            // End of multipart/form-data.
            bos.write(("--" + boundary + "--").getBytes());
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    private void buildPart(ByteArrayOutputStream bos, File file, String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"").append(fileName).append("\"; filename=\"").append(file.getName()).append("\"\r\n");
        sb.append("Content-Type: ").append("image/png").append("\r\n\r\n");
        bos.write(sb.toString().getBytes());

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        bos.write("\r\n".getBytes());
        fis.close();
    }

    private final String boundary = "apiclient-" + System.currentTimeMillis();
}