package com.dongxuny.put;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;

public class App {
    private static final Logger logger = Logger.getLogger(App.class);
    private static final String EOF = "-----------------------------";
    private static final long KB = 1024;
    private static final long MB = 1024 * KB;
    private static final String FILE_PATH_DIR = "./PutTest/";
    private static final String FILE_NAME_PREFIX = "60KB-";
    private static final long FILE_SIZE = 60 * KB;
    private static final String COS_KEY_PREFIX = "/PutCos/";
    
    public static void main(String[] args) throws Exception {
        int loop = 1;
        String secretId = "";
        String secretKey = "";
        String bucketName = "";
        String region = "";
        
        if (args.length < 5) {
            logger.error("Not enough args!");
            logger.error("Need <put count> <secretId> <secretKey> <region> <bucket> as orders");
            logger.error("Example:");
            logger.error("java -jar put-jar-with-dependencies.jar 10 XXX XXX ap-shanghai my-bucket");
            return;
        }
        
        loop = Integer.parseInt(args[0]);
        secretId = args[1];
        secretKey = args[2];
        region = args[3];
        bucketName = args[4];
        
        logger.info("Start PUT Operation...");
        
        put(loop, secretId, secretKey, region, bucketName);
    }
    
    private static void put(int loop, String secretId, String secretKey, String region, String bucketName) throws Exception {
        logger.info("Init COS Client...");
        
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        
        logger.info("Init COS Client success!");
        logger.info(EOF);

        for (int i = 0; i < loop; i++) {
            logger.info(String.format("Start create file with file path:%s, size:%d", FILE_PATH_DIR + FILE_NAME_PREFIX + "XXX", FILE_SIZE));
            File file = create60KBFile();

            logger.info(String.format("Init FileInputStream with file path:%s", file.getAbsolutePath()));
            FileInputStream in = new FileInputStream(file);
            
            logger.info(String.format("Init Objece metadata with contentLength:%d", in.available()));
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(in.available());
            
            long start = System.currentTimeMillis();

            logger.info(String.format("Start Put to Bucket:%s with Key:%s", bucketName, COS_KEY_PREFIX + file.getName()));
            PutObjectResult res = cosclient.putObject(bucketName, "/PutTest/" + file.getName(), in, meta);
            logger.info(String.format("Put to Bucket:%s with Key:%s with RequestId:%s Success!", bucketName, COS_KEY_PREFIX + file.getName(), res.getRequestId()));
            
            long end = System.currentTimeMillis();
            
            logger.info(String.format("Cost:%d ms", end-start));

            logger.info("Thread sleep for 100 ms...");
            logger.info("EOF");
            logger.info(EOF);
            Thread.sleep(100);
        }
        
        cosclient.shutdown();
    }
    
    private static File create60KBFile() throws IOException {
        // Create directory if needed
        File dir = new File(FILE_PATH_DIR);
        if (!dir.exists()) {
            try {
                dir.mkdir();
            } catch(SecurityException se){
                se.printStackTrace();
            }
        }
        
        File file = new File(FILE_PATH_DIR + FILE_NAME_PREFIX + RandomStringUtils.randomAlphanumeric(5));
        file.createNewFile();
        
        PrintWriter writer = new PrintWriter(file);
        
        for (int i = 0; i < 60 * KB; i++) {
            writer.write('a');
        }
        
        writer.close();
        
        return file;
    }
}
