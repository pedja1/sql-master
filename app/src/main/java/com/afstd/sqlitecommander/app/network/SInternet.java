package com.afstd.sqlitecommander.app.network;

import android.accounts.AccountManager;
import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.afstd.sqlitecommander.app.App;
import com.afstd.sqlitecommander.app.R;
import com.afstd.sqlitecommander.app.acm.SAccountAuthenticator;
import com.afstd.sqlitecommander.app.fragment.FragmentCloud;
import com.afstd.sqlitecommander.app.utility.SettingsManager;
import com.tehnicomsolutions.http.RequestBuilder;
import com.tehnicomsolutions.http.TSHttp;
import com.tehnicomsolutions.http.utility.MyTimer;
import com.tehnicomsolutions.http.utility.TSHttpUtility;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import static com.tehnicomsolutions.http.Internet.Response;

/**
 * @author Predrag Čokulov
 */

public class SInternet
{
    public static final String API_REQUEST_URL = "https://pedjaapps.net:3000/";
    /**
     * HTTP connection timeout
     * */
    public static int CONN_TIMEOUT = 30 * 1000;
    public static int READ_TIMEOUT = 30 * 1000;

    public static String LOG_TAG = SInternet.class.getSimpleName();

    /**
     * URL encoding
     * */
    public static final String ENCODING = "UTF-8";

    private static final boolean printResponse = SettingsManager.DEBUG() && true;

    private static final String LINE_FEED = "\r\n";

    public static final Object loginLock = new Object();
    public static boolean loginInProgress = false;
    public static boolean loginInterrupted = false;

    private SInternet()
    {
    }


    public static Response executeHttpRequest(@NonNull RequestBuilder requestBuilder)
    {
        return executeHttpRequest(null, requestBuilder);
    }

    /**
     * Executes HTTP POST request and returns response as string<br>
     * This method will not check if response code from server is OK ( < 400)<br>
     *
     * @param requestBuilder request builder object, used to build request. cannot be null
     * @return server response as string
     */
    public static Response executeHttpRequest(@Nullable Activity activity, @NonNull RequestBuilder requestBuilder)
    {
        MyTimer timer = new MyTimer();
        Response response = new Response();
        InputStream is = null;
        try
        {
            String authToken = null;
            if ((requestBuilder instanceof SRequestBuilder && ((SRequestBuilder)requestBuilder).requiresAuthentication))//we only pass activity if we have authorization
            //activity is used to start login activity
            {
                //check if login is already in progress and block if so
                while (loginInProgress)
                {
                    synchronized (loginLock)
                    {
                        System.out.println("lock acquire");
                        try
                        {
                            loginInterrupted = false;
                            loginLock.wait();
                            if(loginInterrupted)
                                return response;
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                SInternet.loginInProgress = true;
                authToken = SAccountAuthenticator.getAccessToken(activity);
                SInternet.loginInProgress = false;
                if(authToken == null)loginInterrupted = true;
                synchronized (SInternet.loginLock)
                {
                    System.out.println("lock release");
                    SInternet.loginLock.notifyAll();
                }
                if (TextUtils.isEmpty(authToken))
                {
                    response.code = 401;
                    response.responseMessage = App.get().getString(R.string.failed_auth);
                    SettingsManager.setActiveAccount(null);
                    //activity.finish();
                    //activity.startActivity(new Intent(activity, LandingPageActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
                    //TODO force login
                    return response;
                }
                requestBuilder.addParam("access_token", authToken, true);
            }
            HttpURLConnection conn = (HttpURLConnection) new URL(requestBuilder.getRequestUrl()).openConnection();
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONN_TIMEOUT);
            conn.setRequestMethod(requestBuilder.getMethod().toString());
            conn.setDoInput(true);

            for (String key : requestBuilder.getHeaders().keySet())
            {
                conn.setRequestProperty(key, requestBuilder.getHeaders().get(key));
            }

            switch (requestBuilder.getMethod())
            {
                case PUT:
                    break;
                case POST:
                    conn.setDoOutput(true);
                    switch (requestBuilder.getPostMethod())
                    {
                        case BODY:
                            if (requestBuilder.getRequestBody() == null)
                                throw new IllegalArgumentException("body cannot be null if post method is BODY");
                            conn.setRequestProperty("Content-Type", requestBuilder.getContentType());
                            conn.setRequestProperty("Content-Length", Integer.toString(requestBuilder.getRequestBody().getBytes().length));
                            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                            wr.writeBytes(requestBuilder.getRequestBody());
                            wr.flush();
                            wr.close();
                            break;
                        case X_WWW_FORM_URL_ENCODED:
                            setXWwwFormUrlEncodedParams(conn, requestBuilder);
                            break;
                        case FORM_DATA:
                            final String BOUNDARY = "===" + System.currentTimeMillis() + "===";
                            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
                            OutputStream os = conn.getOutputStream();
                            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, ENCODING), true);
                            for (String key : requestBuilder.getPOSTParams().keySet())
                            {
                                writer.append("--").append(BOUNDARY).append(LINE_FEED);
                                writer.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(LINE_FEED);
                                writer.append("Content-Type: text/plain; charset=" + ENCODING).append(LINE_FEED);
                                writer.append(LINE_FEED);
                                writer.append(requestBuilder.getPOSTParams().get(key)).append(LINE_FEED);
                                writer.flush();
                            }

                            if (requestBuilder.getFiles() != null)
                            {
                                for (int i = 0; i < requestBuilder.getFiles().length; i++)
                                {
                                    RequestBuilder.UploadFile file = requestBuilder.getFiles()[i];

                                    writer.append("--").append(BOUNDARY).append(LINE_FEED);
                                    writer.append("Content-Disposition: form-data; name=\"").append(requestBuilder.getFileParamName()).append("\"; filename=\"").append(file.fileName).append("\"").append(LINE_FEED);
                                    writer.append("Content-Type: ").append(TextUtils.isEmpty(file.mimeType) ? URLConnection.guessContentTypeFromName(file.fileName) : file.mimeType).append(LINE_FEED);
                                    writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                                    writer.append(LINE_FEED);
                                    writer.flush();

                                    InputStream inputStream = createInputStreamFromUploadFile(file);
                                    if(inputStream == null)continue;

                                    byte[] buffer = new byte[4096];
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) != -1)
                                    {
                                        os.write(buffer, 0, bytesRead);
                                    }
                                    os.flush();
                                    inputStream.close();

                                    writer.append(LINE_FEED);
                                    writer.flush();
                                }
                            }
                            writer.append(LINE_FEED).flush();
                            writer.append("--").append(BOUNDARY).append("--").append(LINE_FEED);
                            writer.close();

                            os.close();
                            break;
                    }
                    break;
                case GET:
                    break;
                case DELETE:
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    break;
            }

            conn.connect();

            response.code = conn.getResponseCode();
            response.responseData = readStreamToString(is = response.code < 400 ? conn.getInputStream() : conn.getErrorStream());
            response.responseMessage = response.code < 400 ? null : conn.getResponseMessage();

            if ((requestBuilder instanceof SRequestBuilder && ((SRequestBuilder)requestBuilder).requiresAuthentication))
            {
                boolean authTokenExpired = response.code == 401;
                if (authTokenExpired)//token expired, clear it and request new one
                {
                    AccountManager am = AccountManager.get(App.get());
                    //clear token from am, so that we can request new one either with refresh token or by prompting user to login
                    am.invalidateAuthToken(FragmentCloud.ACCOUNT_TYPE, authToken);
                    //retry request
                    if(requestBuilder.retriesLeft <= 0)
                    {
                        String warning = "request failed after " + requestBuilder.getMaxRetries() + " left.";
                        if(SettingsManager.DEBUG())Log.w(LOG_TAG, warning);
                        //Crashlytics.log(warning);

                        return response;
                    }
                    else
                    {
                        requestBuilder.retriesLeft--;
                        return executeHttpRequest(activity, requestBuilder);
                    }
                }
            }
        }
        catch (IOException e)
        {
            if(requestBuilder.retriesLeft <= 0 || isNonRetriableException(e))
            {
                response.responseMessage = TSHttp.getContext().getString(R.string.network_error);
                response.responseDetailedMessage = e.getMessage();
                return response;
            }
            else
            {
                if(SettingsManager.DEBUG())
                {
                    Log.w(LOG_TAG, "request failed with message: " + e.getMessage());
                    Log.w(LOG_TAG, "retries left: "+ requestBuilder.retriesLeft);
                }
                requestBuilder.retriesLeft--;
                return executeHttpRequest(activity, requestBuilder);
            }
        }
        finally
        {
            response.request = requestBuilder.getRequestUrl();
            if (TSHttp.LOGGING)
                Log.d(TSHttp.LOG_TAG, "executeHttpRequest[" + requestBuilder.getRequestUrl() + "]: Took:'" + timer.get() + "', " + response);
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }

        return response;
    }

    /**
     * returns true if retry should not be performed if exception is of specific type*/
    private static boolean isNonRetriableException(IOException e)
    {
        return e instanceof MalformedURLException || e instanceof ProtocolException
                || e instanceof UnsupportedEncodingException || e instanceof FileNotFoundException || e instanceof UnknownHostException;
    }

    private static InputStream createInputStreamFromUploadFile(RequestBuilder.UploadFile file) throws FileNotFoundException
    {
        if (file.uri.startsWith("content://"))
        {
            return TSHttp.getContext().getContentResolver().openInputStream(Uri.parse(file.uri));
        }
        else if (file.uri.startsWith("/"))
        {
            return new FileInputStream(new File(file.uri));
        }
        else if (file.uri.startsWith("file://"))
        {
            return new FileInputStream(new File(file.uri.replace("file://", "")));
        }
        return null;
    }

    private static void setXWwwFormUrlEncodedParams(HttpURLConnection conn, RequestBuilder requestBuilder) throws IOException
    {
        StringBuilder builder = new StringBuilder();
        for (String key : requestBuilder.getPOSTParams().keySet())
        {
            builder.append("&").append(key).append("=").append(TSHttpUtility.encodeString(requestBuilder.getPOSTParams().get(key)));
        }
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(builder.toString().getBytes().length));
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(builder.toString());
        wr.flush();
        wr.close();
    }

    public static String readStreamToString(InputStream stream) throws IOException
    {
        MyTimer timer = new MyTimer();
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        StringBuilder string = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null)
        {
            string.append(line);
        }
        timer.log("Internet::readStreamToString");
        return string.toString();
    }
}