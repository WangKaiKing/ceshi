package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.tls.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UploadService {

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    @Autowired
    private FastFileStorageClient storageClient;

    public String upload(MultipartFile file) {
        String url = "";
        try {
            //0.校验是否为空
            if (file.getSize() <= 0) {
                logger.info("文件的内容为空");
                return null;
            }
            //1.校验后缀
            Set<String> allowExt = new HashSet<>();
            allowExt.add("image/jpeg");
            allowExt.add("image/png");

            String contentType = file.getContentType();

            if (!allowExt.contains(contentType)) {
                logger.info("文件后缀类型不匹配:{}",contentType);
                return null;
            }

            //2.校验内容
            BufferedImage read = ImageIO.read(file.getInputStream());
            if (read == null) {
                logger.info("文件内容有误不是个图片");
                return null;
            }

            //3 IO保存图片
//            file.transferTo(new File("D:/test/upload/",file.getOriginalFilename()));
            String ext = StringUtils.substringAfter(file.getOriginalFilename(),".");
            StorePath storePath = this.storageClient.uploadFile(file.getInputStream(), file.getSize(), ext, null);
            return "http://image.leyou.com/" + storePath.getFullPath();
        } catch (IOException e) {
            return null;
        }

    }
}
