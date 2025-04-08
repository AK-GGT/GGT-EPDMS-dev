package com.ggt.epdm.utils;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.apache.commons.fileupload.FileItem;

import java.io.*;

public class FileManager {

    public static MultipartFile convertToMultipartFile(File file) throws IOException {
        return new CommonsMultipartFile(new DiskFileItem("file", "application/xml", false, file.getName(), (int) file.length(), file.getParentFile()) {{
            try (InputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }});
    }
    
    public static MultipartFile convertFileToMultipartFile(File file) throws IOException {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setRepository(file.getParentFile()); // Temporary storage location for the file

        // Create a new FileItem object
        FileItem fileItem = new DiskFileItem("file", "application/octet-stream", false, file.getName(), (int) file.length(), file.getParentFile());

        // Write the file content into the FileItem
        fileItem.getOutputStream().write(java.nio.file.Files.readAllBytes(file.toPath()));

        // Convert FileItem to MultipartFile
        return new MultipartFile() {
            @Override
            public String getName() {
                return fileItem.getFieldName();
            }

            @Override
            public String getOriginalFilename() {
                return fileItem.getName();
            }

            @Override
            public String getContentType() {
                return fileItem.getContentType();
            }

            @Override
            public boolean isEmpty() {
                return fileItem.getSize() == 0;
            }

            @Override
            public long getSize() {
                return fileItem.getSize();
            }

            @Override
            public byte[] getBytes() throws IOException {
                return java.nio.file.Files.readAllBytes(file.toPath());
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(getBytes());
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                try {
					fileItem.write(dest);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        };
    }
}
