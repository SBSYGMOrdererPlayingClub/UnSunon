package sbsy.ordererplayingclub.unsunon;

import com.github.megatronking.netbare.http.HttpBody;
import com.github.megatronking.netbare.http.HttpRequest;
import com.github.megatronking.netbare.http.HttpRequestHeaderPart;
import com.github.megatronking.netbare.http.HttpResponse;
import com.github.megatronking.netbare.injector.InjectorCallback;
import com.github.megatronking.netbare.injector.SimpleHttpInjector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 拦截订餐APP的"工程模式"(systemModel)请求，强制返回 status=1 表示已开启。
 * 其他所有请求（订餐、登录、查询等）原样放行，不篡改。
 *
 * 原理：sniffRequest 返回 true 进入 inject 流程，
 * 在 onRequestInject 中用 request.respond() 发假响应给客户端，
 * 不调用 callback.onFinished() 阻断原始请求不发到服务器。
 */
public class MyHttpInjector extends SimpleHttpInjector {

    private static final String TAG = "MyHttpInjector";
    private static final String ENGINEERING_MODE_KEY = "systemModel";

    private boolean mIsEngModeRequest = false;
    private HttpRequest mStoredRequest;

    // 工程模式已开启的固定JSON响应
    private static final String FAKE_RESPONSE_BODY = "{\"status\":1,\"data\":{"
            + "\"modelFlag\":1,"
            + "\"barFlag\":1,"
            + "\"readType\":1,"
            + "\"printFlag\":1,"
            + "\"deviceType\":1,"
            + "\"colseFlag\":1,"
            + "\"authCode\":\"\","
            + "\"serverip\":\"\","
            + "\"serverport\":\"\","
            + "\"appname\":\"\","
            + "\"deviceno\":\"\""
            + "},\"msg\":\"ok\"}";

    @Override
    public boolean sniffRequest(HttpRequest request) {
        String url = request.url();
        android.util.Log.d(TAG, "request: " + url);

        if (url != null && url.contains(ENGINEERING_MODE_KEY)) {
            android.util.Log.d(TAG, ">>> 拦截工程模式请求");
            mIsEngModeRequest = true;
            mStoredRequest = request;
            return true;  // 进入 inject 流程，阻断原始请求
        }

        mIsEngModeRequest = false;
        // 非工程模式请求，不拦截，正常放行到服务器
        return false;
    }

    @Override
    public void onRequestInject(HttpRequestHeaderPart header,
                                InjectorCallback callback) throws IOException {
        if (mIsEngModeRequest && mStoredRequest != null) {
            // 发送假的工程模式响应给客户端
            String httpResponse = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: application/json; charset=utf-8\r\n"
                    + "Content-Length: " + FAKE_RESPONSE_BODY.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                    + "Connection: close\r\n"
                    + "\r\n"
                    + FAKE_RESPONSE_BODY;

            mStoredRequest.respond(ByteBuffer.wrap(httpResponse.getBytes(StandardCharsets.UTF_8)));
            android.util.Log.d(TAG, "<<< 已返回工程模式响应");

            // 不调用 callback.onFinished() → 原始请求不会发到服务器
            return;
        }
        // 其他情况正常放行
        callback.onFinished(header);
    }

    @Override
    public void onRequestInject(HttpRequest request, HttpBody body,
                                InjectorCallback callback) throws IOException {
        if (mIsEngModeRequest) {
            // 阻断：不转发 body 到服务器
            return;
        }
        callback.onFinished(body);
    }

    @Override
    public boolean sniffResponse(HttpResponse response) {
        return false;
    }

    @Override
    public void onRequestFinished(HttpRequest request) {
        // 清理状态
        mIsEngModeRequest = false;
        mStoredRequest = null;
    }
}
