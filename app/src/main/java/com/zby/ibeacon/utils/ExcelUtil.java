package com.zby.ibeacon.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import com.zby.ibeacon.database.DataBean;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * 创建时间：2020-12-25
 * 编写人：kanghb
 * 功能描述：将数据导出excel 经测试4万条学生数据一秒左右完成
 */
public class ExcelUtil {

    private static WritableFont       arial14font   = null;
    private static WritableCellFormat arial14format = null;
    private static WritableFont       arial10font   = null;
    private static WritableCellFormat arial10format = null;
    private static WritableFont       arial12font   = null;
    private static WritableCellFormat arial12format = null;

    private final static String UTF8_ENCODING = "UTF-8";

    /**
     * 单元格的格式设置 字体大小 颜色 对齐方式、背景颜色等...
     */
    private static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);

            arial10font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(Colour.GRAY_25);

            arial12font = new WritableFont(WritableFont.ARIAL, 12);
            arial12format = new WritableCellFormat(arial12font);
            //对齐格式
            arial12format.setAlignment(jxl.format.Alignment.CENTRE);
            //设置边框
            arial12format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化Excel表格
     *
     * @param filePath 存放excel文件的路径（MPCMS/Export/demo.xls）
     * @param sheetName Excel表格的表名
     * @param colName excel中包含的列名（可以有多个）
     */
    public static void initExcel(String filePath, String sheetName, String[] colName) {
        format();
        WritableWorkbook workbook = null;

        try {
            File file = getSaveFile();
            makeDir(file);
            File saveFile = new File(file, filePath);
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }

            workbook = Workbook.createWorkbook(saveFile);
            //设置表格的名字
            WritableSheet sheet = workbook.createSheet(sheetName, 0);
            //创建标题栏
            sheet.addCell((WritableCell) new Label(0, 0, filePath, arial14format));
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));
            }
            //设置行高
            sheet.setRowView(0, 340);
            workbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 将制定类型的List写入Excel中
     *
     * @param objList 待写入的list,
     * @param fileName 文件名：用户名_模块_任务名_时间
     */
    @SuppressWarnings("unchecked")
    public static void writeObjListToExcel(List<DataBean> objList, String fileName,
            Context c) {
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writebook = null;
            InputStream in = null;
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                File file = getSaveFile();
                makeDir(file);
                File saveFile = new File(file, fileName);
                if (!saveFile.exists()) {
                    saveFile.createNewFile();
                }

                in = new FileInputStream(saveFile);
                Workbook workbook = Workbook.getWorkbook(in);
                writebook = Workbook.createWorkbook(saveFile, workbook);
                WritableSheet sheet = writebook.getSheet(0);
                DataBean dataBean;
                List<String> list;
                for (int j = 0; j < objList.size(); j++) {
                    dataBean = objList.get(j);
                    list = dataBean.getStringList();
                    for (int i = 0; i < list.size(); i++) {
                        sheet.addCell(new Label(i, j + 1, list.get(i), arial12format));
                        if (list.get(i).length() <= 5) {
                            //设置列宽
                            sheet.setColumnView(i, list.get(i).length() + 8);
                        } else {
                            //设置列宽
                            sheet.setColumnView(i, list.get(i).length() + 5);
                        }
                    }
                    //设置行高
                    sheet.setRowView(j + 1, 350);
                }

                writebook.write();
                Toast.makeText(c, "导出Excel成功" + fileName, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }

    public static SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static File getSaveFile() {
        File file = Environment.getExternalStorageDirectory();
        File parentFile = new File(file, "BleTest");
        return parentFile;
    }

    public static String getFileName() {
        return mSdf.format(new Date()) + ".xls";
    }
}
