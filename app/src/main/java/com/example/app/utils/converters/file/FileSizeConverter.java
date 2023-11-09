package com.example.app.utils.converters.file;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileSizeConverter {
    public static String convert(Long fileSize){

        if(fileSize>=0L&&fileSize<1024){
            return fileSize+" Bytes";
        }
        else if(fileSize>=1024L&&fileSize<1048576){
           return String.format(java.util.Locale.US,"%.2f", (float) fileSize /1024) + " KB";
        }
        else return String.format(java.util.Locale.US,"%.2f", (float) fileSize /1048576) + " MB";
    }

}
