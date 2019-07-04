package com.panchan.pc.appmanage.nanohttp;

import android.text.TextUtils;
import android.util.Log;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author lixm
 *
 */
public class HttpServerImpl extends NanoHTTPD {

	//  http://172.22.158.31:8080/getFileList?dirPath=/sdcard
	//  http://172.22.158.31:8080/getFile?fileName=/sdcard/FaceFingerMatch_AD
	
    public static final int DEFAULT_SERVER_PORT = 8088;
    public static final String TAG = "lixm";

    private static HttpServerImpl httpServer;
    private static final String REQUEST_ROOT = "/";   //post 传参数
    private static final String REQUEST_TEST = "/test";
    private static final String REQUEST_ACTION_GET_FILE = "/getFile";
    private static final String REQUEST_ACTION_GET_FILE_LIST = "/getFileList";
    private static final String REQUEST_POST = "/api";    //post 传参数
    ResultMessage resultMessage;
    private OnOpenDoorListener onOpenDoorListener = null;



    public HttpServerImpl() {
        super(DEFAULT_SERVER_PORT);

    }

    public static HttpServerImpl getInstance() {     //单例模式
        if (null == httpServer) {
            httpServer = new HttpServerImpl();
        }
        return httpServer;
    }

    public interface OnOpenDoorListener {
        public void onOpenDoor(String param);
    }

    public void setOnOpenDoorListener(OnOpenDoorListener openDoorListener) {   //回调方法
        onOpenDoorListener =openDoorListener;
    }


    @Override
    public Response serve(IHTTPSession session) {
    	String strUri = session.getUri();
    	String method = session.getMethod().name();
        Log.d(TAG,"Response serve uri = " + strUri + ", method = " + method);

        
       /* if(REQUEST_ROOT.equals(strUri)) {   // 根目录
            return responseRootPage(session);
        }else */if(REQUEST_TEST.equals(strUri)){    // 返回给调用端json串
        	return responseJson();
        }else if(REQUEST_ACTION_GET_FILE_LIST.equals(strUri)){    // 获取文件列表
        	Map<String,String> params = session.getParms();

        	String dirPath = params.get("dirPath");
        	if(!TextUtils.isEmpty(dirPath)){
        		return responseFileList(session,dirPath);
        	}        	
        }else if(REQUEST_ACTION_GET_FILE.equals(strUri)){ // 下载文件
        	Map<String,String> params = session.getParms();
        	// 下载的文件名称
        	String fileName = params.get("fileName");

        	File file = new File(fileName);
        	if(file.exists()){
        		if(file.isDirectory()){
        			return responseFileList(session,fileName);
        		}else{
        			return responseFileStream(session,fileName);
        		}
        	}        	
        }else if(REQUEST_ROOT.equals(strUri)){


            Map<String, String> files = new HashMap<String, String>();
            /*获取header信息，NanoHttp的header不仅仅是HTTP的header，还包括其他信息。*/
            Map<String, String> header = session.getHeaders();

            try {
                session.parseBody(files);
                String param=files.get("postData");

                Log.d("123","header : " + header);
                Log.d("123","files : " + files);
                Log.d("123","param : " + param);

              /*  try {
                    Thread.sleep(600000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

                resultMessage = new ResultMessage("1","success");


                Log.d("123","onOpenDoorListener : " + onOpenDoorListener);
                  if (onOpenDoorListener != null) {
                      onOpenDoorListener.onOpenDoor(param);
                  }

//                Message msg = new Message();
//                msg.what = 0x001;
//                msg.obj = param;
//                handler.sendMessage(msg);    //handler传值

            } catch (IOException e) {
                e.printStackTrace();
               resultMessage = new ResultMessage("0","fail");


            } catch (ResponseException e) {
                e.printStackTrace();
                resultMessage = new ResultMessage("0","fail");

            }

            return responseJsonC();
        }

        return response404(session);
    }

    private Response responseRootPage(IHTTPSession session) {

        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");
        builder.append("这是测试! \n");
        builder.append("</body></html>\n");
        //return Response.newFixedLengthResponse(Status.OK, "application/octet-stream", builder.toString());
        return Response.newFixedLengthResponse(builder.toString());
    }

    /**
     * 返回给调用端LOG日志文件
     * @param session
     * @return
     */
    private Response responseFileStream(IHTTPSession session, String filePath) {
    	Log.d("lixm", "responseFileStream() ,fileName = " + filePath);
        try {
            FileInputStream fis = new FileInputStream(filePath);
            //application/octet-stream
            return Response.newChunkedResponse(Status.OK, "application/octet-stream", fis);
        }
        catch (FileNotFoundException e) {
            Log.d("lixm", "responseFileStream FileNotFoundException :" ,e);
            return response404(session);
        }
    }

    /**
     * 
     * @param session http请求
     * @param dirPath 文件夹路径名称
     * @return
     */
    private Response responseFileList(IHTTPSession session, String dirPath) {
    	Log.d("lixm", "responseFileList() , dirPath = " + dirPath);
    	List<String> fileList = FileUtils.getFilePaths(dirPath, false);
    	StringBuilder sb = new StringBuilder();
    	for(String filePath : fileList){
    		sb.append("<a href=" + REQUEST_ACTION_GET_FILE + "?fileName=" + filePath + ">" + filePath + "</a>" + "<br>");
    	}
    	return Response.newFixedLengthResponse(sb.toString());
    }  

    /**
     * 调用的路径出错
     * @param session
     * @param
     * @return
     */
    private Response response404(IHTTPSession session) {
    	String url = session.getUri();
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html><body>");        
        builder.append("Sorry, Can't Found "+url + " !");        
        builder.append("</body></html>\n");
        return Response.newFixedLengthResponse(builder.toString());
    }

    /**
     * 返回给调用端json字符串
     * @return
     */
    private Response responseJson(){
    	return Response.newFixedLengthResponse("调用成功");
    }

    /**
     * 返回给调用端json字符串
     * @return
     */
    private Response responseJsonC(){
        return Response.newFixedLengthResponse(resultMessage.toString());
    }

}
