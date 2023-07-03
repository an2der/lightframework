package com.lightframework.util.excel;

import com.alibaba.fastjson2.util.TypeUtils;
import com.lightframework.common.BusinessException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

/** 
 * @author yg
 * @date 2023/4/10 19:02
 * @version 1.0
 */
public class ExcelUtil {

    private static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);
    /**
     * 读取excel数据，默认忽略第一行
     * @param request
     * @return
     * @throws IOException
     */
    public static <T> List<T> readData(HttpServletRequest request, Class<T>  clazz){
        return readData(request,true,clazz);
    }

    /**
     * 读取excel数据
     * @param request
     * @param ignoreFirstRow 忽略第一行
     * @return
     * @throws IOException
     */
    public static <T> List<T>  readData(HttpServletRequest request,boolean ignoreFirstRow,Class<T> clazz) {
        List<T> resultData = new ArrayList<>();
        InputStream inputStream = null;
        try {
            CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver(request.getSession().getServletContext());
            if(multipartResolver.isMultipart(request)) {
                //将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                //获取multiRequest 中所有的文件名
                Iterator iter = multiRequest.getFileNames();
                while(iter.hasNext()) {
                    //一次遍历所有文件
                    MultipartFile file=multiRequest.getFile(iter.next().toString());
                    if(file!=null) {
                        inputStream = file.getInputStream();
                        Workbook workbook = WorkbookFactory.create(inputStream);
                        Cell cell;
                        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
                        PropertyDescriptor [] propertyDescriptors = beanInfo.getPropertyDescriptors();
                        Constructor<T> constructor = clazz.getConstructor();
                        if(!constructor.isAccessible()){
                            constructor.setAccessible(true);
                        }
                        for(int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets();sheetIndex++){
                            Sheet sheet = workbook.getSheetAt(sheetIndex);
                            for(int rowIndex = ignoreFirstRow?1:0;rowIndex < sheet.getLastRowNum()+1;rowIndex++){
                                Row row = sheet.getRow(rowIndex);
                                if(row == null){
                                    continue;
                                }
                                if(row.getLastCellNum() > propertyDescriptors.length){
                                    throw new BusinessException("Excel数据格式错误！");
                                }
                                T obj = constructor.newInstance();
                                for (short cellIndex = 0;cellIndex < row.getLastCellNum();cellIndex++){
                                    PropertyDescriptor propertyDescriptor = propertyDescriptors[cellIndex];
                                    cell = row.getCell(cellIndex);
                                    if(cell != null){
                                        propertyDescriptor.getWriteMethod().invoke(obj,new Object[]{TypeUtils.cast(cell.getStringCellValue().trim(),propertyDescriptor.getPropertyType())});
                                    }else{
                                        propertyDescriptor.getWriteMethod().invoke(obj,new Object[]{null});
                                    }
                                }
                                resultData.add(obj);
                            }
                        }
                    }

                }
            }
        }catch (BusinessException e){
            throw e;
        } catch (Exception e) {
            logger.error("读取excel发生异常",e);
            throw new BusinessException("文件读取错误");
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                return null;
            }
        }
        return resultData;
    }

    public static <T> void exportExcel(String filename, LinkedHashMap<String,String> heads, List<T> data, HttpServletResponse response) {
        OutputStream outputStream = null;
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook();
        List<Integer> columnWidth = new ArrayList<>();
        int maxWidth = 30 * 256;
        try {
            outputStream = response.getOutputStream();
            String fileName = new String((filename + ".xlsx").getBytes("gb2312"), "iso-8859-1");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            response.setContentType("application/octet-stream");
            SXSSFSheet sxssfSheet = null;
            SXSSFRow dataRow;
            SXSSFCell dataCell;
            //创建数据行
            Font dataFont = sxssfWorkbook.createFont();
            dataFont.setFontName("宋体");
            dataFont.setFontHeightInPoints((short) 11);
            CellStyle dataStyle = sxssfWorkbook.createCellStyle();
            dataStyle.setFont(dataFont);
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0;i < data.size();i++){
                int index = i % (1048576 - 1); //xlsx最大数据行 减去一行标题
                if(index == 0){
                    for (int j = 0; j < columnWidth.size(); j++) {
                        sxssfSheet.setColumnWidth(j,columnWidth.get(j) > maxWidth?maxWidth:columnWidth.get(j));
                    }
                    sxssfSheet = sxssfWorkbook.createSheet();
                    //创建首行，并设置样式
                    SXSSFRow headRow = sxssfSheet.createRow(0);
                    Font headFont = sxssfWorkbook.createFont();
                    headFont.setFontName("宋体");
                    headFont.setFontHeightInPoints((short) 11);
                    headFont.setBold(true);
                    CellStyle headStyle = sxssfWorkbook.createCellStyle();
                    headStyle.setFont(headFont);
                    headStyle.setAlignment(HorizontalAlignment.CENTER);
                    headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    headStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.index);
                    headStyle.setWrapText(true);
                    SXSSFCell headCell;
                    int headIndex = 0;
                    for(String head:heads.keySet()){
                        headCell = headRow.createCell(headIndex++);
                        headCell.setCellStyle(headStyle);
                        headCell.setCellValue(head);
                        columnWidth.add(head.getBytes().length  * 256);
                    }
                }
                dataRow = sxssfSheet.createRow(index+1);
                T obj = data.get(i);
                Class objClass = obj.getClass();
                int dataIndex = 0;
                for(String head:heads.keySet()){
                    dataCell = dataRow.createCell(dataIndex);
                    dataCell.setCellStyle(dataStyle);
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(heads.get(head),objClass);
                    Method method = propertyDescriptor.getReadMethod();
                    Object value = method.invoke(obj);
                    if(value != null){
                        if(value.getClass() == Date.class){
                            dataCell.setCellValue(simpleDateFormat.format(value));
                        }else{
                            dataCell.setCellValue(value.toString());
                        }
                        int width = (dataCell.getStringCellValue().getBytes().length + 3) * 256;
                        if(width > columnWidth.get(dataIndex)){
                            columnWidth.set(dataIndex,width);
                        }
                    }else {
                        dataCell.setCellValue("");
                    }
                    dataIndex++;
                }
            }
            for (int i = 0; i < columnWidth.size(); i++) {
                sxssfSheet.setColumnWidth(i,columnWidth.get(i) > maxWidth?maxWidth:columnWidth.get(i));
            }
            sxssfWorkbook.write(outputStream);
        } catch (Exception e) {
            logger.error("导出excel发生异常",e);
        }finally {
            try {
                outputStream.close();
                sxssfWorkbook.close();
            } catch (IOException e) {
                return;
            }

        }
    }
}