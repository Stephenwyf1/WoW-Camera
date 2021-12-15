package com.wyj;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ServerTestApplicationTests {

    @Test
    public void test(){
        String poem = "123。123";
        poem = poem.replace("。","\n");
        System.out.println(poem);
    }

    @Test
    public void testDenoise(){
        String filename = "./test/test.png";
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

    @Test
    public void testDenoise2(){
        Process proc;
        try {
            proc = Runtime.getRuntime().exec("python /root/projects/oppo_camera/pythonModels/IRCNN/test.py");// 执行py文件

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

}
