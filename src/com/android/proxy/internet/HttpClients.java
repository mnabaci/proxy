package com.android.proxy.internet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.util.Log;

public class HttpClients {
    private final static String TAG = "HttpClients";
    private final static boolean DEBUG = false;
    
    public static final int SAVE_FILE_SUCCESS = 0;
    public static final int SAVE_FILE_FAILED = 1;
    
    private Context mContext;
    private int size;
    private HttpClient mHc;
    private HttpPost mPost;
    private HttpGet mGet;
    private HttpResponse mResponse;
    
    private boolean doUpload = false;
    private boolean doDownload = false;
    
    public static class Interfaces {
        public interface TransferListener extends EventListener {
            void onStartTransfer(int totalSize);
            void onTransfer(int transferSize, int totalSize);
            void onFinishTransfer(int transferSize, int totalSize);
        }
    }
    
    public HttpClients(Context context) {
        mContext = context;
        size = 0;
        mPost = new HttpPost();
        mGet = new HttpGet();
    }

    public HttpResponse openConnection(int startOffset) {
        mHc = new DefaultHttpClient(NetWorkSettingInfoManager.getInstance(mContext).getParams());
        NetWorkSettingInfoManager.getInstance(mContext).setHeader(mGet);
        if (startOffset > 0) {
            mGet.addHeader("Range", "bytes=" + startOffset + "-");
        }
        try {
            mResponse = mHc.execute(mGet);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            mResponse = null;
        } catch (IOException e) {
            e.printStackTrace();
            mResponse = null;
        }
        return mResponse;
    }
    
    public HttpResponse openConnection(File uploadFile) {
        mHc = new DefaultHttpClient(NetWorkSettingInfoManager.getInstance(mContext).getParams());
        NetWorkSettingInfoManager.getInstance(mContext).setHeader(mPost, uploadFile);
        try {
            mResponse = mHc.execute(mPost);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            mResponse = null;
        } catch (IOException e) {
            e.printStackTrace();
            mResponse = null;
        }
        return mResponse;
    }
    
    public HttpResponse openConnection(Map<String, String> uploadContextMap) {
        mHc = new DefaultHttpClient(NetWorkSettingInfoManager.getInstance(mContext).getParams());
        NetWorkSettingInfoManager.getInstance(mContext).setHeader(mPost, uploadContextMap);
        try {
            mResponse = mHc.execute(mPost);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            mResponse = null;
        } catch (IOException e) {
            e.printStackTrace();
            mResponse = null;
        }
        return mResponse;
    }
    
    public HttpResponse openConnection(String uploadData) {
        mHc = new DefaultHttpClient(NetWorkSettingInfoManager.getInstance(mContext).getParams());
        NetWorkSettingInfoManager.getInstance(mContext).setHeader(mPost, uploadData);
        try {
            mResponse = mHc.execute(mPost);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            mResponse = null;
        } catch (IOException e) {
            e.printStackTrace();
            mResponse = null;
        }
        return mResponse;
    }
    
    public int downloadFile(HttpResponse response, String outFilePath, boolean append) {
        try {
            File file = new File(outFilePath);
            if (file.exists() == false) {
                file.createNewFile();
            }
            int totalSize;
            if (response.getHeaders(HTTP.CONTENT_LEN) != null) {
                totalSize = Integer.parseInt(response.getFirstHeader(HTTP.CONTENT_LEN).getValue());
            } else {
                totalSize = 0;
                return SAVE_FILE_FAILED;
            }
            if (totalSize == 0) {
                return SAVE_FILE_FAILED;
            }
            
            BufferedInputStream fis = new BufferedInputStream(response.getEntity().getContent(), totalSize);
            LOGD("is available:" + fis.available());
            FileOutputStream fos = new FileOutputStream(file, append);
            
            byte[] buffer = new byte[1024];
            int readLength;
            int downloadSize = 0;
            doDownload = true;
            while (doDownload && (readLength = fis.read(buffer, 0, 1024)) != -1) {
                fos.write(buffer, 0, readLength);
                fos.flush();
                downloadSize += readLength;
            }
            doDownload = false;
            fis.close();
            fos.close();
        } 
        catch (IOException e) {
            e.printStackTrace();
            return SAVE_FILE_FAILED;
        }
        return SAVE_FILE_SUCCESS;
    }

    public int getResponseCode() {
        int code = -1;
        if (mResponse != null) {
            code = mResponse.getStatusLine().getStatusCode();
        }
        return code;
    }

    public int getDownloadSize() {
        return Integer.parseInt(mResponse.getFirstHeader(HTTP.CONTENT_LEN).getValue());
    }

    public boolean isValidResponse() {
        return (mResponse != null);
    }

    public int receiveXMLFile(String outFilePath) {
        try {
            LOGD(">>>>>>>>>>>> begin save the received xml file <<<<<<<<<");
            File f = new File(outFilePath);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(outFilePath, false);
            InputStream is = mResponse.getEntity().getContent();
            byte[] buffer = new byte[1024];
            int readLength = 0;
            while ((readLength = is.read(buffer, 0, 1024)) != -1) {
                size += readLength;
                fos.write(buffer, 0, readLength);
                fos.flush();
            }
            fos.close();
            is.close();
            LOGD("<<<<<<<<< begin dump the received xml file >>>>>>>>>>>");
            dumpReceiveFile(outFilePath);
        } catch (IOException e) {
            LOGD(e.toString());
            return SAVE_FILE_FAILED;
        } catch (NullPointerException npe) {
            LOGD(npe.toString());
            return SAVE_FILE_FAILED;
        } catch (Exception e) {
            LOGD(e.toString());
            return SAVE_FILE_FAILED;
        }
        return SAVE_FILE_SUCCESS;
    }

    public void disConnect() {
        if (mPost.isAborted() == false) mPost.abort();
        if (mGet.isAborted() == false) mGet.abort();
    }

    public void setURL(String urlPath) {
        try {
            String urlPathFixed = urlPath.replace(" ", "");
            URI url = new URI(urlPathFixed);
            if (DEBUG) Log.d(TAG, urlPathFixed);
            if (mPost.isAborted() == true) {
                if (DEBUG) Log.d(TAG, "((((( the original post is aborted, create a new post ))))))");
                mPost = new HttpPost();
            }
            if (mGet.isAborted() == true) {
                if (DEBUG) Log.d(TAG, "((((( the original get is aborted, create a new get ))))))");
                mGet = new HttpGet();
            }
            mPost.setURI(url);
            mGet.setURI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void interruptUpload() {
        doUpload = false;
    }
    
    public void cancelDownload() {
        LOGD("cancelDownload");
        doDownload = false;
    }

    public class SizeEvent extends EventObject {

        private int mSize;
        private int totalSize;

        public SizeEvent(Object source) {
            super(source);
        }

        public int getSize() {
            return mSize;
        }

        public void setSize(int s) {
            mSize = s;
        }

        public int getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(int s) {
            totalSize = s;
        }
    }
    
    private void dumpReceiveFile(String filename) {
        if (DEBUG) {
            try {
                LOGD("-------- begin dump the file = " + filename + " --------");
                File file = new File(filename);
                FileInputStream in = new FileInputStream(file);
                int length = (int) file.length();
                byte[] datas = new byte[length];
                in.read(datas, 0, datas.length);
                String result = new String(datas);
                LOGD(result);
            } catch (Exception e) {
            }
        }
    }

    private void LOGD(String text) {
        if (DEBUG) {
            Log.d(TAG, text);
        }
    }

}
