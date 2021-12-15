package com.wyj.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.lang.System;
import java.net.InetAddress;
import java.net.Socket;

@Component
@Slf4j
public class InvokePython {

    @Value("${fileReceiveLocation}")
    private String fileReceiveLocation;

    public static void main(String[] args) {
        InvokePython invokePython = new InvokePython();
//        invokePython.pythonResolve("/home/wyj/IdeaProjects/server_test/src/main/resources/static/desktop.png","classification");
        invokePython.pythonResolve("/home/wyj/IdeaProjects/server_test/src/main/resources/static/desktop.png","image");
    }

    /**
     * 通过socket与python脚本进行交互
     * @param filename 图片的路径
     * @param type 调用哪一个神经网络
     */
    public String pythonResolve(String filename, String type){
        //System.out.println("Hello World!");
        // TODO Auto-generated method stub
        Socket socket = null;
        String poem = "";
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String host=addr.getHostName();
            //String ip=addr.getHostAddress().toString(); //获取本机ip
            //log.info("调用远程接口:host=>"+ip+",port=>"+12345);

            // 初始化套接字，设置访问服务的主机和进程端口号，HOST是访问python进程的主机名称，可以是IP地址或者域名，PORT是python进程绑定的端口号
            socket = new Socket(host,12345);

            // 获取输出流对象
            OutputStream os = socket.getOutputStream();
            PrintStream out = new PrintStream(os);
            // 发送内容
//            out.print( "F:\\xxx\\0000.jpg");
            out.print(filename);
            // 告诉服务进程，内容发送完毕，可以开始处理
//            out.print("over");
            out.print(type);

//            if ("image".equals(type) || "enhance".equals(type)){
//                //启动图片接收线程
//                receiveImageThread receiveImage = new receiveImageThread(socket);
//                receiveImage.start();
//                Thread.sleep(1000);  //给1s的时间图片线程接收图片
////                receiveImage.interrupt(); //中断图片接收线程
//            }
            // 获取服务进程的输入流
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8"));
            String tmp = null;
            StringBuilder sb = new StringBuilder();
            // 读取内容
            if (type == "poem"){
                poem = br.readLine();
            }

            while((tmp=br.readLine())!=null)
                sb.append(tmp).append('\n');
            System.out.print(sb);

            // 解析结果
            //JSONArray res = JSON.parseArray(sb.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {if(socket!=null) socket.close();} catch (IOException e) {}
            System.out.print("远程接口调用结束.");
            if (!poem.equals("")){
                return poem;
            }
            return "远程接口调用结束";
        }
    }

    public void denoise(String filename){
        String denoisePyFilePath = "/root/projects/oppo_camera/pythonModels/IRCNN/test.py";
        System.out.println(filename);
        try {
            String[] args = new String[] { "python", denoisePyFilePath, filename };
            Process proc = Runtime.getRuntime().exec(args);// 执行py文件

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //图片接收线程
    class receiveImageThread extends Thread {

        private Socket socket;

        public receiveImageThread(Socket clientSocket) {
            socket = clientSocket;
        }

        @Override
        public void run() {
            super.run();
            InputStream in = null;
            FileOutputStream fos = null;
            try {
                if (socket != null) {
                    System.out.println("开始图片接收");
                    in = socket.getInputStream();
                    int len = 0;
                    byte[] buf = new byte[1024];
                    fos = new FileOutputStream(fileReceiveLocation+"result.jpg");
                    while ((len = in.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                    System.out.println("图片接收完成");
                }
                in.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
