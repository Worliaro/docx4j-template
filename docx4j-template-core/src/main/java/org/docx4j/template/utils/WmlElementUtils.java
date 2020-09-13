/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.docx4j.template.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.properties.table.tr.TrHeight;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTBackground;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTEm;
import org.docx4j.wml.CTHeight;
import org.docx4j.wml.CTLineNumber;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTSignedHpsMeasure;
import org.docx4j.wml.CTSignedTwipsMeasure;
import org.docx4j.wml.CTTblCellMar;
import org.docx4j.wml.CTTextScale;
import org.docx4j.wml.CTVerticalAlignRun;
import org.docx4j.wml.CTVerticalJc;
import org.docx4j.wml.Color;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Highlight;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.PPrBase.PBdr;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STEm;
import org.docx4j.wml.STLineNumberRestart;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.STPageOrientation;
import org.docx4j.wml.STShd;
import org.docx4j.wml.STVerticalAlignRun;
import org.docx4j.wml.STVerticalJc;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgBorders;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.SectPr.PgSz;
import org.docx4j.wml.SectPr.Type;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.TcPrInner.HMerge;
import org.docx4j.wml.TcPrInner.VMerge;
import org.docx4j.wml.Text;
import org.docx4j.wml.TextDirection;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;

/*
 * TODO
 * @author <a href="https://github.com/hiwepy">hiwepy</a>
 */
@SuppressWarnings("unchecked")
public final class WmlElementUtils {

	protected static ObjectFactory factory = Context.getWmlObjectFactory();

	public static <T> List<T> getChildrenElements(Object source, Class<T> targetClass) {  
        List<T> result = new ArrayList<T>();  
        //获取真实的对象
    	Object target = XmlUtils.unwrap(source);
    	//if (target.getClass().equals(targetClass)) {
        if (targetClass.isAssignableFrom(target.getClass())) { 
            result.add((T)target);
        } else if (target instanceof ContentAccessor) {  
            List<?> children = ((ContentAccessor) target).getContent();  
            //if (children.getClass().equals(targetClass)) {
            if (targetClass.isAssignableFrom(children.getClass())) { 
                result.add((T)children);
            }
        }
        return result;  
    }
	
	/*
	 * 这样会返回一个表示完整的空白（在此时）文档Java对象。现在我们可以使用Docx4J API添加、删除以及更新这个word文档的内容，Docx4J有一些你可以用于遍历该文档的工具类。
	 * 我自己写了几个助手方法使查找指定占位符并用真实内容进行替换的操作变地很简单。让我们来看一下其中的一个，这个计算是几个JAXB计算的包装器，
	 * 允许你针对一个特定的类来搜索指定元素以及它所有的孩子，例如，你可以用它获取文档中所有的表格、表格中所有的行以及其它类似的操作。 
	 */
	public static <T> List<T> getTargetElements(Object source, Class<T> targetClass) {  
        List<T> result = new ArrayList<T>();  
        //获取真实的对象
    	Object target = XmlUtils.unwrap(source);
        //if (target.getClass().equals(targetClass)) {
    	if (targetClass.isAssignableFrom(target.getClass())) { 
            result.add((T) target);  
        } else if (target instanceof ContentAccessor) {  
            List<?> children = ((ContentAccessor) target).getContent();  
            for (Object child : children) {  
                result.addAll(getTargetElements(child, targetClass));  
            }
        }
        return result;  
    }  

	/*
	 * 该方法找到表格，获取第一行并且遍历提供的map向表格添加新行，在将其返回之前删除模版行。这个方法用到了两个助手方法：addRowToTable 和 getTemplateTable。我们首先看一下后面的那个： 
	 */
	public static Tbl getTable(List<Tbl> tables, String placeholder) throws Docx4JException {  
        for (Iterator<Tbl> iterator = tables.iterator(); iterator.hasNext();) {  
        	Tbl tbl = iterator.next();
        	//查找当前table下面的text对象
            List<Text> textElements = getTargetElements(tbl, Text.class);  
            for (Text text : textElements) {
                Text textElement = (Text) text;
                //
                if (textElement.getValue() != null && textElement.getValue().equals(placeholder))  {
                    return (Tbl) tbl;  
                }
            }  
        }  
        return null;  
    }  
    
	public static void replaceTable(String[] placeholders, List<Map<String, String>> textToAdd,  
            WordprocessingMLPackage template) throws Docx4JException, JAXBException {  
	    List<Tbl> tables = getTargetElements(template.getMainDocumentPart(), Tbl.class);  
	  
	    // 1. find the table  
	    Tbl tempTable = getTable(tables, placeholders[0]);  
	    List<Tr> rows = getTargetElements(tempTable, Tr.class);  
	  
	    // first row is header, second row is content  
	    if (rows.size() == 2) {  
	    	
	        // this is our template row  
	        Tr templateRow = (Tr) rows.get(1);  
	  
	        for (Map<String, String> replacements : textToAdd) {  
	            // 2 and 3 are done in this method  
	            addRowToTable(tempTable, templateRow, replacements);  
	        }  
	  
	        // 4. remove the template row  
	        tempTable.getContent().remove(templateRow);  
	    }  
	}  
    
	public static void addRowToTable(Tbl reviewtable, Tr templateRow, Map<String, String> replacements) {  
        Tr workingRow = (Tr) XmlUtils.deepCopy(templateRow);  
        List<?> textElements = getTargetElements(workingRow, Text.class);  
        for (Object object : textElements) {  
            Text text = (Text) object;  
            String replacementValue = (String) replacements.get(text.getValue());  
            if (replacementValue != null)  
                text.setValue(replacementValue);  
        }  
      
        reviewtable.getContent().add(workingRow);  
    } 

	
  
    
    
	/*
     *  First we create a footer, a paragraph, a run and a text. We add the given
     *  given content to the text and add that to the run. The run is then added to
     *  the paragraph, which is in turn added to the footer. Finally we return the
     *  footer.
     *  @param content
     *  @return
     */
	public static Ftr createFooter(String content) {
        Ftr footer = factory.createFtr();
        P paragraph = factory.createP();
        R run = factory.createR();
        Text text = new Text();
        text.setValue(content);
        run.getContent().add(text);
        paragraph.getContent().add(run);
        footer.getContent().add(paragraph);
        return footer;
    }

    /*
     *  创建一个对象工厂并用它创建一个段落和一个可运行块R. 
     *  然后将可运行块添加到段落中. 接下来创建一个图画并将其添加到可运行块R中. 最后我们将内联 
     *  对象添加到图画中并返回段落对象. 
     * 
     * @param   inline 包含图片的内联对象. 
     * @return  包含图片的段落 
     */  
    public static P addInlineImageToParagraph(Inline inline) {
        // 添加内联对象到一个段落中  
        P paragraph = factory.createP();  
        R run = factory.createR();  
        paragraph.getContent().add(run);  
        Drawing drawing = factory.createDrawing();  
        run.getContent().add(drawing);  
        drawing.getAnchorOrInline().add(inline);  
        return paragraph;  
    } 

    /*
     * 插入图片
     * @param wPackage
     * @param bm
     * @param file
     * @throws Exception 
     */
	public static void addImage(WordprocessingMLPackage wPackage, CTBookmark bm, String file) throws Exception {
		// 这儿可以对单个书签进行操作，也可以用一个map对所有的书签进行处理
		// 获取该书签的父级段落
		P p = (P) (bm.getParent());
		// R对象是匿名的复杂类型，然而我并不知道具体啥意思，估计这个要好好去看看ooxml才知道
		R run = factory.createR();
		// 读入图片并转化为字节数组，因为docx4j只能字节数组的方式插入图片
	        byte[] bytes = null;//FileUtil.getByteFormBase64DataByImage(file);
 
	        //	InputStream is = new FileInputStream;
        //	byte[] bytes = IOUtils.toByteArray(inputStream);
		//  byte[] bytes = FileUtil.getByteFormBase64DataByImage("");
		// 开始创建一个行内图片
		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wPackage, bytes);
		// createImageInline函数的前四个参数我都没有找到具体啥意思，，，，
		// 最有一个是限制图片的宽度，缩放的依据
		Inline inline = imagePart.createImageInline(null, null, 0, 1, false, 0);
		// 获取该书签的父级段落
		// drawing理解为画布？
		Drawing drawing = factory.createDrawing();
		drawing.getAnchorOrInline().add(inline);
		run.getContent().add(drawing);
		p.getContent().add(run);
	}
	
	
    /*
     * http://53873039oycg.iteye.com/blog/2194849
     *  实现思路:
        主要分在当前行上方插入行和在当前行下方插入行。对首尾2行特殊处理，在有跨行合并情况时，在第一行上面或者在最后一行下面插入是不会跨行的但是可能会跨列。
       对于中间的行，主要参照当前行，如果当前行跨行，则新增行也跨行，如果当前行单元格结束跨行，则新增的上方插入行跨行，下方插入行不跨行，如果当前行单元格开始跨行，则新增的上方插入行不跨行，下发插入行跨行。
       主要思路就是这样，插入的时候需要得到真实位置的单元格，代码如下:
       
     */
    // 按位置得到单元格(考虑跨列合并情况)  
    public Tc getTcByPosition(List<Tc> tcList, int position) {  
        int k = 0;  
        for (int i = 0, len = tcList.size(); i < len; i++) {  
            Tc tc = tcList.get(i);  
            TcPr trPr = tc.getTcPr();  
            if (trPr != null) {  
                GridSpan gridSpan = trPr.getGridSpan();  
                if (gridSpan != null) {  
                    k += gridSpan.getVal().intValue() - 1;  
                }
            }  
            if (k >= position) {  
                return tcList.get(i);  
            }  
            k++;  
        }  
        if (position < tcList.size()) {  
            return tcList.get(position);  
        }  
        return null;  
    }  
    
     


    /*------------------------------------other--------------------------------------------------- */
    /*
     * 新增超链接
     */
    public void createHyperlink(WordprocessingMLPackage wordMLPackage, MainDocumentPart mainPart, ObjectFactory factory,
            P paragraph, String url, String value, String cnFontName, String enFontName, String fontSize)
            throws Exception {
        if (StringUtils.isBlank(enFontName)) {
            enFontName = "Times New Roman";
        }
        if (StringUtils.isBlank(cnFontName)) {
            cnFontName = "微软雅黑";
        }
        if (StringUtils.isBlank(fontSize)) {
            fontSize = "22";
        }
        org.docx4j.relationships.ObjectFactory reFactory = new org.docx4j.relationships.ObjectFactory();
        org.docx4j.relationships.Relationship rel = reFactory.createRelationship();
        rel.setType(Namespaces.HYPERLINK);
        rel.setTarget(url);
        rel.setTargetMode("External");
        mainPart.getRelationshipsPart().addRelationship(rel);
        StringBuffer sb = new StringBuffer();
        // addRelationship sets the rel's @Id
        sb.append("<w:hyperlink r:id=\"");
        sb.append(rel.getId());
        sb.append("\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" ");
        sb.append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" >");
        sb.append("<w:r><w:rPr><w:rStyle w:val=\"Hyperlink\" />");
        sb.append("<w:rFonts w:ascii=\"");
        sb.append(enFontName);
        sb.append("\" w:hAnsi=\"");
        sb.append(enFontName);
        sb.append("\" w:eastAsia=\"");
        sb.append(cnFontName);
        sb.append("\" w:hint=\"eastAsia\"/>");
        sb.append("<w:sz w:val=\"");
        sb.append(fontSize);
        sb.append("\"/><w:szCs w:val=\"");
        sb.append(fontSize);
        sb.append("\"/></w:rPr><w:t>");
        sb.append(value);
        sb.append("</w:t></w:r></w:hyperlink>");

        Hyperlink link = (Hyperlink) XmlUtils.unmarshalString(sb.toString());
        paragraph.getContent().add(link);
    }

    public static String getElementContent(Object obj) throws Exception {
        StringWriter stringWriter = new StringWriter();
        TextUtils.extractText(obj, stringWriter);
        return stringWriter.toString();
    }

    /*
     * 得到指定类型的元素
     */
    public static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
        List<Object> result = new ArrayList<Object>();
        if (obj instanceof JAXBElement)
            obj = ((JAXBElement<?>) obj).getValue();
        if (obj.getClass().equals(toSearch))
            result.add(obj);
        else if (obj instanceof ContentAccessor) {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children) {
                result.addAll(getAllElementFromObject(child, toSearch));
            }
        }
        return result;
    }

    /*
     * 保存WordprocessingMLPackage
     */
    public void saveWordPackage(WordprocessingMLPackage wordPackage, File file) throws Exception {
        wordPackage.save(file);
    }

    /*
     * 新建WordprocessingMLPackage
     */
    public static WordprocessingMLPackage createWordprocessingMLPackage() throws Exception {
        return WordprocessingMLPackage.createPackage();
    }

    /*
     * 加载带密码WordprocessingMLPackage
     */
    public static WordprocessingMLPackage loadWordprocessingMLPackageWithPwd(String filePath, String password)
            throws Exception {
        OpcPackage opcPackage = WordprocessingMLPackage.load(new java.io.File(filePath), password);
        WordprocessingMLPackage wordMLPackage = (WordprocessingMLPackage) opcPackage;
        return wordMLPackage;
    }

    /*
     * 加载WordprocessingMLPackage
     */
    public static WordprocessingMLPackage loadWordprocessingMLPackage(String filePath) throws Exception {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(new java.io.File(filePath));
        return wordMLPackage;
    }

    /*------------------------------------Word 表格相关--------------------------------------------------- */
    /*
     *  跨列合并
     */
    public static void mergeCellsHorizontalByGridSpan(Tbl tbl, int row, int fromCell, int toCell) {
        if (row < 0 || fromCell < 0 || toCell < 0) {
            return;
        }
        List<Tr> trList = getTblAllTr(tbl);
        if (row > trList.size()) {
            return;
        }
        Tr tr = trList.get(row);
        List<Tc> tcList = getTrAllCell(tr);
        for (int cellIndex = Math.min(tcList.size() - 1, toCell); cellIndex >= fromCell; cellIndex--) {
            Tc tc = tcList.get(cellIndex);
            TcPr tcPr = getTcPr(tc);
            if (cellIndex == fromCell) {
                GridSpan gridSpan = tcPr.getGridSpan();
                if (gridSpan == null) {
                    gridSpan = new GridSpan();
                    tcPr.setGridSpan(gridSpan);
                }
                gridSpan.setVal(BigInteger.valueOf(Math.min(tcList.size() - 1, toCell) - fromCell + 1));
            } else {
                tr.getContent().remove(cellIndex);
            }
        }
    }

    /*
     *  跨列合并
     */
    public static void mergeCellsHorizontal(Tbl tbl, int row, int fromCell, int toCell) {
        if (row < 0 || fromCell < 0 || toCell < 0) {
            return;
        }
        List<Tr> trList = getTblAllTr(tbl);
        if (row > trList.size()) {
            return;
        }
        Tr tr = trList.get(row);
        List<Tc> tcList = getTrAllCell(tr);
        for (int cellIndex = fromCell, len = Math.min(tcList.size() - 1, toCell); cellIndex <= len; cellIndex++) {
            Tc tc = tcList.get(cellIndex);
            TcPr tcPr = getTcPr(tc);
            HMerge hMerge = tcPr.getHMerge();
            if (hMerge == null) {
                hMerge = new HMerge();
                tcPr.setHMerge(hMerge);
            }
            if (cellIndex == fromCell) {
                hMerge.setVal("restart");
            } else {
                hMerge.setVal("continue");
            }
        }
    }
 
    /*
     *  跨行合并
     */
    public static void mergeCellsVertically(Tbl tbl, int col, int fromRow, int toRow) {
        if (col < 0 || fromRow < 0 || toRow < 0) {
            return;
        }
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            Tc tc = getTc(tbl, rowIndex, col);
            if (tc == null) {
                break;
            }
            TcPr tcPr = getTcPr(tc);
            VMerge vMerge = tcPr.getVMerge();
            if (vMerge == null) {
                vMerge = new VMerge();
                tcPr.setVMerge(vMerge);
            }
            if (rowIndex == fromRow) {
                vMerge.setVal("restart");
            } else {
                vMerge.setVal("continue");
            }
        }
    }

    /*
     * 得到指定位置的单元格
     */
    public static Tc getTc(Tbl tbl, int row, int cell) {
        if (row < 0 || cell < 0) {
            return null;
        }
        List<Tr> trList = getTblAllTr(tbl);
        if (row >= trList.size()) {
            return null;
        }
        List<Tc> tcList = getTrAllCell(trList.get(row));
        if (cell >= tcList.size()) {
            return null;
        }
        return tcList.get(cell);
    }

    /*
     * 得到所有表格
     */
    public static List<Tbl> getAllTbl(WordprocessingMLPackage wordMLPackage) {
        MainDocumentPart mainDocPart = wordMLPackage.getMainDocumentPart();
        List<Object> objList = getAllElementFromObject(mainDocPart, Tbl.class);
        if (objList == null) {
            return null;
        }
        List<Tbl> tblList = new ArrayList<Tbl>();
        for (Object obj : objList) {
            if (obj instanceof Tbl) {
                Tbl tbl = (Tbl) obj;
                tblList.add(tbl);
            }
        }
        return tblList;
    }

    /*
     * 删除指定位置的表格,删除后表格数量减一
     */
    public static boolean removeTableByIndex(WordprocessingMLPackage wordMLPackage, int index) throws Exception {
        boolean flag = false;
        if (index < 0) {
            return flag;
        }
        List<Object> objList = wordMLPackage.getMainDocumentPart().getContent();
        if (objList == null) {
            return flag;
        }
        int k = -1;
        for (int i = 0, len = objList.size(); i < len; i++) {
            Object obj = XmlUtils.unwrap(objList.get(i));
            if (obj instanceof Tbl) {
                k++;
                if (k == index) {
                    wordMLPackage.getMainDocumentPart().getContent().remove(i);
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /*
     *  获取单元格内容,无分割符
     */
    public static String getTblContentStr(Tbl tbl) throws Exception {
        return getElementContent(tbl);
    }

    /*
     *  获取表格内容
     */
    public static List<String> getTblContentList(Tbl tbl) throws Exception {
        List<String> resultList = new ArrayList<String>();
        List<Tr> trList = getTblAllTr(tbl);
        for (Tr tr : trList) {
            StringBuffer sb = new StringBuffer();
            List<Tc> tcList = getTrAllCell(tr);
            for (Tc tc : tcList) {
                sb.append(getElementContent(tc) + ",");
            }
            resultList.add(sb.toString());
        }
        return resultList;
    }

    public static TblPr getTblPr(Tbl tbl) {
        TblPr tblPr = tbl.getTblPr();
        if (tblPr == null) {
            tblPr = new TblPr();
            tbl.setTblPr(tblPr);
        }
        return tblPr;
    }

    /*
     *  设置表格总宽度
     */
    public static void setTableWidth(Tbl tbl, String width) {
        if (StringUtils.isNotBlank(width)) {
            TblPr tblPr = getTblPr(tbl);
            TblWidth tblW = tblPr.getTblW();
            if (tblW == null) {
                tblW = new TblWidth();
                tblPr.setTblW(tblW);
            }
            tblW.setW(new BigInteger(width));
            tblW.setType("dxa");
        }
    }

    /*
     * 创建表格(默认水平居中,垂直居中)
     */
    public static Tbl createTable(WordprocessingMLPackage wordPackage, int rowNum, int colsNum) throws Exception {
        colsNum = Math.max(1, colsNum);
        rowNum = Math.max(1, rowNum);
        int widthTwips = getWritableWidth(wordPackage);
        int colWidth = widthTwips / colsNum;
        int[] widthArr = new int[colsNum];
        for (int i = 0; i < colsNum; i++) {
            widthArr[i] = colWidth;
        }
        return createTable(rowNum, colsNum, widthArr);
    }
 
    /*
     * 创建表格(默认水平居中,垂直居中)
     */
    public static Tbl createTable(int rowNum, int colsNum, int[] widthArr) throws Exception {
        colsNum = Math.max(1, Math.min(colsNum, widthArr.length));
        rowNum = Math.max(1, rowNum);
        Tbl tbl = new Tbl();
        StringBuffer tblSb = new StringBuffer();
        tblSb.append("<w:tblPr ").append(Namespaces.W_NAMESPACE_DECLARATION).append(">");
        tblSb.append("<w:tblStyle w:val=\"TableGrid\"/>");
        tblSb.append("<w:tblW w:w=\"0\" w:type=\"auto\"/>");
        // 上边框
        tblSb.append("<w:tblBorders>");
        tblSb.append("<w:top w:val=\"single\" w:sz=\"1\" w:space=\"0\" w:color=\"auto\"/>");
        // 左边框
        tblSb.append("<w:left w:val=\"single\" w:sz=\"1\" w:space=\"0\" w:color=\"auto\"/>");
        // 下边框
        tblSb.append("<w:bottom w:val=\"single\" w:sz=\"1\" w:space=\"0\" w:color=\"auto\"/>");
        // 右边框
        tblSb.append("<w:right w:val=\"single\" w:sz=\"1\" w:space=\"0\" w:color=\"auto\"/>");
        tblSb.append("<w:insideH w:val=\"single\" w:sz=\"1\" w:space=\"0\" w:color=\"auto\"/>");
        tblSb.append("<w:insideV w:val=\"single\" w:sz=\"1\" w:space=\"0\" w:color=\"auto\"/>");
        tblSb.append("</w:tblBorders>");
        tblSb.append("</w:tblPr>");
        TblPr tblPr = null;
        tblPr = (TblPr) XmlUtils.unmarshalString(tblSb.toString());
        Jc jc = new Jc();
        // 单元格居中对齐
        jc.setVal(JcEnumeration.CENTER);
        tblPr.setJc(jc);

        tbl.setTblPr(tblPr);

        // 设定各单元格宽度
        TblGrid tblGrid = new TblGrid();
        tbl.setTblGrid(tblGrid);
        for (int i = 0; i < colsNum; i++) {
            TblGridCol gridCol = new TblGridCol();
            gridCol.setW(BigInteger.valueOf(widthArr[i]));
            tblGrid.getGridCol().add(gridCol);
        }
        // 新增行
        for (int j = 0; j < rowNum; j++) {
            Tr tr = new Tr();
            tbl.getContent().add(tr);
            // 列
            for (int i = 0; i < colsNum; i++) {
                Tc tc = new Tc();
                tr.getContent().add(tc);

                TcPr tcPr = new TcPr();
                TblWidth cellWidth = new TblWidth();
                cellWidth.setType("dxa");
                cellWidth.setW(BigInteger.valueOf(widthArr[i]));
                tcPr.setTcW(cellWidth);
                tc.setTcPr(tcPr);

                // 垂直居中
                setTcVAlign(tc, STVerticalJc.CENTER);
                P p = new P();
                PPr pPr = new PPr();
                pPr.setJc(jc);
                p.setPPr(pPr);
                R run = new R();
                p.getContent().add(run);
                tc.getContent().add(p);
            }
        }
        return tbl;
    }

    /*
     * 表格增加边框 可以设置上下左右四个边框样式以及横竖水平线样式
     */
    public void setTblBorders(TblPr tblPr, CTBorder topBorder, CTBorder rightBorder, CTBorder bottomBorder,
            CTBorder leftBorder, CTBorder hBorder, CTBorder vBorder) {
        TblBorders borders = tblPr.getTblBorders();
        if (borders == null) {
            borders = new TblBorders();
            tblPr.setTblBorders(borders);
        }
        if (topBorder != null) {
            borders.setTop(topBorder);
        }
        if (rightBorder != null) {
            borders.setRight(rightBorder);
        }
        if (bottomBorder != null) {
            borders.setBottom(bottomBorder);
        }
        if (leftBorder != null) {
            borders.setLeft(leftBorder);
        }
        if (hBorder != null) {
            borders.setInsideH(hBorder);
        }
        if (vBorder != null) {
            borders.setInsideV(vBorder);
        }
    }

    /*
     *  设置表格水平对齐方式(仅对表格起作用,单元格不一定水平对齐)
     */
    public void setTblJcAlign(Tbl tbl, JcEnumeration jcType) {
        if (jcType != null) {
            TblPr tblPr = getTblPr(tbl);
            Jc jc = tblPr.getJc();
            if (jc == null) {
                jc = new Jc();
                tblPr.setJc(jc);
            }
            jc.setVal(jcType);
        }
    }

    /*
     *  设置表格水平对齐方式(包括单元格),只对该方法前面产生的单元格起作用
     */
    public void setTblAllJcAlign(Tbl tbl, JcEnumeration jcType) {
        if (jcType != null) {
            setTblJcAlign(tbl, jcType);
            List<Tr> trList = getTblAllTr(tbl);
            for (Tr tr : trList) {
                List<Tc> tcList = getTrAllCell(tr);
                for (Tc tc : tcList) {
                    setTcJcAlign(tc, jcType);
                }
            }
        }
    }

    /*
     *  设置表格垂直对齐方式(包括单元格),只对该方法前面产生的单元格起作用
     */
    public void setTblAllVAlign(Tbl tbl, STVerticalJc vAlignType) {
        if (vAlignType != null) {
            List<Tr> trList = getTblAllTr(tbl);
            for (Tr tr : trList) {
                List<Tc> tcList = getTrAllCell(tr);
                for (Tc tc : tcList) {
                    setTcVAlign(tc, vAlignType);
                }
            }
        }
    }

    /*
     *  设置单元格Margin
     */
    public void setTableCellMargin(Tbl tbl, String top, String right, String bottom, String left) {
        TblPr tblPr = getTblPr(tbl);
        CTTblCellMar cellMar = tblPr.getTblCellMar();
        if (cellMar == null) {
            cellMar = new CTTblCellMar();
            tblPr.setTblCellMar(cellMar);
        }
        if (StringUtils.isNotBlank(top)) {
            TblWidth topW = new TblWidth();
            topW.setW(new BigInteger(top));
            topW.setType("dxa");
            cellMar.setTop(topW);
        }
        if (StringUtils.isNotBlank(right)) {
            TblWidth rightW = new TblWidth();
            rightW.setW(new BigInteger(right));
            rightW.setType("dxa");
            cellMar.setRight(rightW);
        }
        if (StringUtils.isNotBlank(bottom)) {
            TblWidth btW = new TblWidth();
            btW.setW(new BigInteger(bottom));
            btW.setType("dxa");
            cellMar.setBottom(btW);
        }
        if (StringUtils.isNotBlank(left)) {
            TblWidth leftW = new TblWidth();
            leftW.setW(new BigInteger(left));
            leftW.setType("dxa");
            cellMar.setLeft(leftW);
        }
    }

    /*
     *  得到表格所有的行
     */
    public static List<Tr> getTblAllTr(Tbl tbl) {
        List<Object> objList = getAllElementFromObject(tbl, Tr.class);
        List<Tr> trList = new ArrayList<Tr>();
        if (objList == null) {
            return trList;
        }
        for (Object obj : objList) {
            if (obj instanceof Tr) {
                Tr tr = (Tr) obj;
                trList.add(tr);
            }
        }
        return trList;

    }

    /*
     * 设置tr高度
     */
    public static void setTrHeight(Tr tr, String heigth) {
        TrPr trPr = getTrPr(tr);
        CTHeight ctHeight = new CTHeight();
        ctHeight.setVal(new BigInteger(heigth));
        TrHeight trHeight = new TrHeight(ctHeight);
        trHeight.set(trPr);
    }

    /*
     *  在表格指定位置新增一行,默认居中
     */
    public static void addTrByIndex(Tbl tbl, int index) {
        addTrByIndex(tbl, index, STVerticalJc.CENTER, JcEnumeration.CENTER);
    }

    /*
     *  在表格指定位置新增一行(默认按表格定义的列数添加)
     */
    public static void addTrByIndex(Tbl tbl, int index, STVerticalJc vAlign, JcEnumeration hAlign) {
        TblGrid tblGrid = tbl.getTblGrid();
        Tr tr = new Tr();
        if (tblGrid != null) {
            List<TblGridCol> gridList = tblGrid.getGridCol();
            for (TblGridCol tblGridCol : gridList) {
                Tc tc = new Tc();
                setTcWidth(tc, tblGridCol.getW().toString());
                if (vAlign != null) {
                    // 垂直居中
                    setTcVAlign(tc, vAlign);
                }
                P p = new P();
                if (hAlign != null) {
                    PPr pPr = new PPr();
                    Jc jc = new Jc();
                    // 单元格居中对齐
                    jc.setVal(hAlign);
                    pPr.setJc(jc);
                    p.setPPr(pPr);
                }
                R run = new R();
                p.getContent().add(run);
                tc.getContent().add(p);
                tr.getContent().add(tc);
            }
        } else {
            // 大部分情况都不会走到这一步
            Tr firstTr = getTblAllTr(tbl).get(0);
            int cellSize = getTcCellSizeWithMergeNum(firstTr);
            for (int i = 0; i < cellSize; i++) {
                Tc tc = new Tc();
                if (vAlign != null) {
                    // 垂直居中
                    setTcVAlign(tc, vAlign);
                }
                P p = new P();
                if (hAlign != null) {
                    PPr pPr = new PPr();
                    Jc jc = new Jc();
                    // 单元格居中对齐
                    jc.setVal(hAlign);
                    pPr.setJc(jc);
                    p.setPPr(pPr);
                }
                R run = new R();
                p.getContent().add(run);
                tc.getContent().add(p);
                tr.getContent().add(tc);
            }
        }
        if (index >= 0 && index < tbl.getContent().size()) {
            tbl.getContent().add(index, tr);
        } else {
            tbl.getContent().add(tr);
        }
    }

    /*
     *  得到行的列数
     */
    public static int getTcCellSizeWithMergeNum(Tr tr) {
        int cellSize = 1;
        List<Tc> tcList = getTrAllCell(tr);
        if (tcList == null || tcList.size() == 0) {
            return cellSize;
        }
        cellSize = tcList.size();
        for (Tc tc : tcList) {
            TcPr tcPr = getTcPr(tc);
            GridSpan gridSpan = tcPr.getGridSpan();
            if (gridSpan != null) {
                cellSize += gridSpan.getVal().intValue() - 1;
            }
        }
        return cellSize;
    }

    /*
     *  删除指定行 删除后行数减一
     */
    public static boolean removeTrByIndex(Tbl tbl, int index) {
        boolean flag = false;
        if (index < 0) {
            return flag;
        }
        List<Object> objList = tbl.getContent();
        if (objList == null) {
            return flag;
        }
        int k = -1;
        for (int i = 0, len = objList.size(); i < len; i++) {
            Object obj = XmlUtils.unwrap(objList.get(i));
            if (obj instanceof Tr) {
                k++;
                if (k == index) {
                    tbl.getContent().remove(i);
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    public static TrPr getTrPr(Tr tr) {
        TrPr trPr = tr.getTrPr();
        if (trPr == null) {
            trPr = new TrPr();
            tr.setTrPr(trPr);
        }
        return trPr;
    }

    /*
     * 隐藏行(只对表格中间的部分起作用,不包括首尾行)
     */
    public static void setTrHidden(Tr tr, boolean hidden) {
        List<Tc> tcList = getTrAllCell(tr);
        for (Tc tc : tcList) {
            setTcHidden(tc, hidden);
        }
    }

    /*
     *  设置单元格宽度
     */
    public static void setTcWidth(Tc tc, String width) {
        if (StringUtils.isNotBlank(width)) {
            TcPr tcPr = getTcPr(tc);
            TblWidth tcW = tcPr.getTcW();
            if (tcW == null) {
                tcW = new TblWidth();
                tcPr.setTcW(tcW);
            }
            tcW.setW(new BigInteger(width));
            tcW.setType("dxa");
        }
    }

    /*
     *  隐藏单元格内容
     */
    public static void setTcHidden(Tc tc, boolean hidden) {
        List<P> pList = getTcAllP(tc);
        for (P p : pList) {
            PPr ppr = getPPr(p);
            List<Object> objRList = getAllElementFromObject(p, R.class);
            if (objRList == null) {
                continue;
            }
            for (Object objR : objRList) {
                if (objR instanceof R) {
                    R r = (R) objR;
                    RPr rpr = getRPr(r);
                    setRPrVanishStyle(rpr, hidden);
                }
            }
            setParaVanish(ppr, hidden);
        }
    }

    public static List<P> getTcAllP(Tc tc) {
        List<Object> objList = getAllElementFromObject(tc, P.class);
        List<P> pList = new ArrayList<P>();
        if (objList == null) {
            return pList;
        }
        for (Object obj : objList) {
            if (obj instanceof P) {
                P p = (P) obj;
                pList.add(p);
            }
        }
        return pList;
    }

    public static TcPr getTcPr(Tc tc) {
        TcPr tcPr = tc.getTcPr();
        if (tcPr == null) {
            tcPr = new TcPr();
            tc.setTcPr(tcPr);
        }
        return tcPr;
    }

    /*
     *  设置单元格垂直对齐方式
     */
    public static void setTcVAlign(Tc tc, STVerticalJc vAlignType) {
        if (vAlignType != null) {
            TcPr tcPr = getTcPr(tc);
            CTVerticalJc vAlign = new CTVerticalJc();
            vAlign.setVal(vAlignType);
            tcPr.setVAlign(vAlign);
        }
    }

    /*
     *  设置单元格水平对齐方式
     */
    public static void setTcJcAlign(Tc tc, JcEnumeration jcType) {
        if (jcType != null) {
            List<P> pList = getTcAllP(tc);
            for (P p : pList) {
                setParaJcAlign(p, jcType);
            }
        }
    }

    public static RPr getRPr(R r) {
        RPr rpr = r.getRPr();
        if (rpr == null) {
            rpr = new RPr();
            r.setRPr(rpr);
        }
        return rpr;
    }

    /*
     *  获取所有的单元格
     */
    public static List<Tc> getTrAllCell(Tr tr) {
        List<Object> objList = getAllElementFromObject(tr, Tc.class);
        List<Tc> tcList = new ArrayList<Tc>();
        if (objList == null) {
            return tcList;
        }
        for (Object tcObj : objList) {
            if (tcObj instanceof Tc) {
                Tc objTc = (Tc) tcObj;
                tcList.add(objTc);
            }
        }
        return tcList;
    }

    /*
     *  获取单元格内容
     */
    public static String getTcContent(Tc tc) throws Exception {
        return getElementContent(tc);
    }

    /*
     * 设置单元格内容,content为null则清除单元格内容
     */
    public void setTcContent(Tc tc, RPr rpr, String content) {
        List<Object> pList = tc.getContent();
        P p = null;
        if (pList != null && pList.size() > 0) {
            if (pList.get(0) instanceof P) {
                p = (P) pList.get(0);
            }
        } else {
            p = new P();
            tc.getContent().add(p);
        }
        R run = null;
        List<Object> rList = p.getContent();
        if (rList != null && rList.size() > 0) {
            for (int i = 0, len = rList.size(); i < len; i++) {
                // 清除内容(所有的r
                p.getContent().remove(0);
            }
        }
        run = new R();
        p.getContent().add(run);
        if (content != null) {
            String[] contentArr = content.split("\n");
            Text text = new Text();
            text.setSpace("preserve");
            text.setValue(contentArr[0]);
            run.setRPr(rpr);
            run.getContent().add(text);

            for (int i = 1, len = contentArr.length; i < len; i++) {
                Br br = new Br();
                run.getContent().add(br);// 换行
                text = new Text();
                text.setSpace("preserve");
                text.setValue(contentArr[i]);
                run.setRPr(rpr);
                run.getContent().add(text);
            }
        }
    }

    /*
     * 设置单元格内容,content为null则清除单元格内容
     */
    public void removeTcContent(Tc tc) {
        List<Object> pList = tc.getContent();
        P p = null;
        if (pList != null && pList.size() > 0) {
            if (pList.get(0) instanceof P) {
                p = (P) pList.get(0);
            }
        } else {
            return;
        }
        List<Object> rList = p.getContent();
        if (rList != null && rList.size() > 0) {
            for (int i = 0, len = rList.size(); i < len; i++) {
                // 清除内容(所有的r
                p.getContent().remove(0);
            }
        }
    }

    /*
     * 删除指定位置的表格
     * @deprecated
     */
    public void deleteTableByIndex2(WordprocessingMLPackage wordMLPackage, int index) throws Exception {
        if (index < 0) {
            return;
        }
        final String xpath = "(//w:tbl)[" + index + "]";
        final List<Object> jaxbNodes = wordMLPackage.getMainDocumentPart().getJAXBNodesViaXPath(xpath, true);
        if (jaxbNodes != null && jaxbNodes.size() > 0) {
            wordMLPackage.getMainDocumentPart().getContent().remove(jaxbNodes.get(0));
        }
    }

    /*
     * 获取NodeList
     * @deprecated
     */
    public List<Object> getObjectByXpath(WordprocessingMLPackage wordMLPackage, String xpath) throws Exception {
        final List<Object> jaxbNodes = wordMLPackage.getMainDocumentPart().getJAXBNodesViaXPath(xpath, true);
        return jaxbNodes;
    }

    /*------------------------------------Word 段落相关--------------------------------------------------- */
    /*
     *  只删除单独的段落，不包括表格内或其他内的段落
     */
    public boolean removeParaByIndex(WordprocessingMLPackage wordMLPackage, int index) {
        boolean flag = false;
        if (index < 0) {
            return flag;
        }
        List<Object> objList = wordMLPackage.getMainDocumentPart().getContent();
        if (objList == null) {
            return flag;
        }
        int k = -1;
        for (int i = 0, len = objList.size(); i < len; i++) {
            if (objList.get(i) instanceof P) {
                k++;
                if (k == index) {
                    wordMLPackage.getMainDocumentPart().getContent().remove(i);
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /*
     *  设置段落水平对齐方式
     */
    public static void setParaJcAlign(P paragraph, JcEnumeration hAlign) {
        if (hAlign != null) {
            PPr pprop = paragraph.getPPr();
            if (pprop == null) {
                pprop = new PPr();
                paragraph.setPPr(pprop);
            }
            Jc align = new Jc();
            align.setVal(hAlign);
            pprop.setJc(align);
        }
    }

    /*
     *  设置段落内容
     */
    public static void setParaRContent(P p, RPr runProperties, String content) {
        R run = null;
        List<Object> rList = p.getContent();
        if (rList != null && rList.size() > 0) {
            for (int i = 0, len = rList.size(); i < len; i++) {
                // 清除内容(所有的r
                p.getContent().remove(0);
            }
        }
        run = new R();
        p.getContent().add(run);
        if (content != null) {
            String[] contentArr = content.split("\n");
            Text text = new Text();
            text.setSpace("preserve");
            text.setValue(contentArr[0]);
            run.setRPr(runProperties);
            run.getContent().add(text);

            for (int i = 1, len = contentArr.length; i < len; i++) {
                Br br = new Br();
                run.getContent().add(br);// 换行
                text = new Text();
                text.setSpace("preserve");
                text.setValue(contentArr[i]);
                run.setRPr(runProperties);
                run.getContent().add(text);
            }
        }
    }

    /*
     *  添加段落内容
     */
    public static void appendParaRContent(P p, RPr runProperties, String content) {
        if (content != null) {
            R run = new R();
            p.getContent().add(run);
            String[] contentArr = content.split("\n");
            Text text = new Text();
            text.setSpace("preserve");
            text.setValue(contentArr[0]);
            run.setRPr(runProperties);
            run.getContent().add(text);

            for (int i = 1, len = contentArr.length; i < len; i++) {
                Br br = new Br();
                run.getContent().add(br);// 换行
                text = new Text();
                text.setSpace("preserve");
                text.setValue(contentArr[i]);
                run.setRPr(runProperties);
                run.getContent().add(text);
            }
        }
    }

    /*
     *  添加图片到段落
     */
    public static void addImageToPara(WordprocessingMLPackage wordMLPackage, ObjectFactory factory, P paragraph,
            String filePath, String content, RPr rpr, String altText, int id1, int id2) throws Exception {
        R run = factory.createR();
        if (content != null) {
            Text text = factory.createText();
            text.setValue(content);
            text.setSpace("preserve");
            run.setRPr(rpr);
            run.getContent().add(text);
        }

        InputStream is = new FileInputStream(filePath);
        byte[] bytes = IOUtils.toByteArray(is);
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);
        Inline inline = imagePart.createImageInline(filePath, altText, id1, id2, false);
        Drawing drawing = factory.createDrawing();
        drawing.getAnchorOrInline().add(inline);
        run.getContent().add(drawing);
        paragraph.getContent().add(run);
    }

    /*
     *  段落添加Br 页面Break(分页符)
     */
    public static void addPageBreak(P para, STBrType sTBrType) {
        Br breakObj = new Br();
        breakObj.setType(sTBrType);
        para.getContent().add(breakObj);
    }

    /*
     *  设置段落是否禁止行号(禁止用于当前行号)
     */
    public static void setParagraphSuppressLineNum(P p) {
        PPr ppr = getPPr(p);
        BooleanDefaultTrue line = ppr.getSuppressLineNumbers();
        if (line == null) {
            line = new BooleanDefaultTrue();
        }
        line.setVal(true);
        ppr.setSuppressLineNumbers(line);
    }

    /*
     *  设置段落底纹(对整段文字起作用)
     */
    public static void setParagraphShdStyle(P p, STShd shdType, String shdColor) {
        PPr ppr = getPPr(p);
        CTShd ctShd = ppr.getShd();
        if (ctShd == null) {
            ctShd = new CTShd();
        }
        if (StringUtils.isNotBlank(shdColor)) {
            ctShd.setColor(shdColor);
        }
        if (shdType != null) {
            ctShd.setVal(shdType);
        }
        ppr.setShd(ctShd);
    }

    /*
     * @param isSpace
     *            是否设置段前段后值
     * @param before
     *            段前磅数
     * @param after
     *            段后磅数
     * @param beforeLines
     *            段前行数
     * @param afterLines
     *            段后行数
     * @param isLine
     *            是否设置行距
     * @param lineValue
     *            行距值
     * @param sTLineSpacingRule
     *            自动auto 固定exact 最小 atLeast 1磅=20 1行=100 单倍行距=240
     */
    public void setParagraphSpacing(P p, boolean isSpace, String before, String after, String beforeLines,
            String afterLines, boolean isLine, String lineValue, STLineSpacingRule sTLineSpacingRule) {
        PPr pPr = getPPr(p);
        Spacing spacing = pPr.getSpacing();
        if (spacing == null) {
            spacing = new Spacing();
            pPr.setSpacing(spacing);
        }
        if (isSpace) {
            if (StringUtils.isNotBlank(before)) {
                // 段前磅数
                spacing.setBefore(new BigInteger(before));
            }
            if (StringUtils.isNotBlank(after)) {
                // 段后磅数
                spacing.setAfter(new BigInteger(after));
            }
            if (StringUtils.isNotBlank(beforeLines)) {
                // 段前行数
                spacing.setBeforeLines(new BigInteger(beforeLines));
            }
            if (StringUtils.isNotBlank(afterLines)) {
                // 段后行数
                spacing.setAfterLines(new BigInteger(afterLines));
            }
        }
        if (isLine) {
            if (StringUtils.isNotBlank(lineValue)) {
                spacing.setLine(new BigInteger(lineValue));
            }
            if (sTLineSpacingRule != null) {
                spacing.setLineRule(sTLineSpacingRule);
            }
        }
    }

    /*
     *  设置段落缩进信息 1厘米≈567
     */
    public void setParagraphIndInfo(P p, String firstLine, String firstLineChar, String hanging, String hangingChar,
            String right, String rigthChar, String left, String leftChar) {
        PPr ppr = getPPr(p);
        Ind ind = ppr.getInd();
        if (ind == null) {
            ind = new Ind();
            ppr.setInd(ind);
        }
        if (StringUtils.isNotBlank(firstLine)) {
            ind.setFirstLine(new BigInteger(firstLine));
        }
        if (StringUtils.isNotBlank(firstLineChar)) {
            ind.setFirstLineChars(new BigInteger(firstLineChar));
        }
        if (StringUtils.isNotBlank(hanging)) {
            ind.setHanging(new BigInteger(hanging));
        }
        if (StringUtils.isNotBlank(hangingChar)) {
            ind.setHangingChars(new BigInteger(hangingChar));
        }
        if (StringUtils.isNotBlank(left)) {
            ind.setLeft(new BigInteger(left));
        }
        if (StringUtils.isNotBlank(leftChar)) {
            ind.setLeftChars(new BigInteger(leftChar));
        }
        if (StringUtils.isNotBlank(right)) {
            ind.setRight(new BigInteger(right));
        }
        if (StringUtils.isNotBlank(rigthChar)) {
            ind.setRightChars(new BigInteger(rigthChar));
        }
    }

    public static PPr getPPr(P p) {
        PPr ppr = p.getPPr();
        if (ppr == null) {
            ppr = new PPr();
            p.setPPr(ppr);
        }
        return ppr;
    }

    public static ParaRPr getParaRPr(PPr ppr) {
        ParaRPr parRpr = ppr.getRPr();
        if (parRpr == null) {
            parRpr = new ParaRPr();
            ppr.setRPr(parRpr);
        }
        return parRpr;

    }

    public static void setParaVanish(PPr ppr, boolean isVanish) {
        ParaRPr parRpr = getParaRPr(ppr);
        BooleanDefaultTrue vanish = parRpr.getVanish();
        if (vanish != null) {
            vanish.setVal(isVanish);
        } else {
            vanish = new BooleanDefaultTrue();
            parRpr.setVanish(vanish);
            vanish.setVal(isVanish);
        }
    }

    /*
     *  设置段落边框样式
     */
    public static void setParagraghBorders(P p, CTBorder topBorder, CTBorder bottomBorder, CTBorder leftBorder,
            CTBorder rightBorder) {
        PPr ppr = getPPr(p);
        PBdr pBdr = new PBdr();
        if (topBorder != null) {
            pBdr.setTop(topBorder);
        }
        if (bottomBorder != null) {
            pBdr.setBottom(bottomBorder);
        }
        if (leftBorder != null) {
            pBdr.setLeft(leftBorder);
        }
        if (rightBorder != null) {
            pBdr.setRight(rightBorder);
        }
        ppr.setPBdr(pBdr);
    }

    /*
     *  设置字体信息
     */
    public static void setFontStyle(RPr runProperties, String cnFontFamily, String enFontFamily, String fontSize,
            String color) {
        setFontFamily(runProperties, cnFontFamily, enFontFamily);
        setFontSize(runProperties, fontSize);
        setFontColor(runProperties, color);
    }

    /*
     *  设置字体大小
     */
    public static void setFontSize(RPr runProperties, String fontSize) {
        if (StringUtils.isNotBlank(fontSize)) {
            HpsMeasure size = new HpsMeasure();
            size.setVal(new BigInteger(fontSize));
            runProperties.setSz(size);
            runProperties.setSzCs(size);
        }
    }

    /*
     *  设置字体
     */
    public static void setFontFamily(RPr runProperties, String cnFontFamily, String enFontFamily) {
        if (StringUtils.isNotBlank(cnFontFamily) || StringUtils.isNotBlank(enFontFamily)) {
            RFonts rf = runProperties.getRFonts();
            if (rf == null) {
                rf = new RFonts();
                runProperties.setRFonts(rf);
            }
            if (cnFontFamily != null) {
                rf.setEastAsia(cnFontFamily);
            }
            if (enFontFamily != null) {
                rf.setAscii(enFontFamily);
            }
        }
    }

    /*
     *  设置字体颜色
     */
    public static void setFontColor(RPr runProperties, String color) {
        if (color != null) {
            Color c = new Color();
            c.setVal(color);
            runProperties.setColor(c);
        }
    }

    /*
     *  设置字符边框
     */
    public static void addRPrBorderStyle(RPr runProperties, String size, STBorder bordType, String space, String color) {
        CTBorder value = new CTBorder();
        if (StringUtils.isNotBlank(color)) {
            value.setColor(color);
        }
        if (StringUtils.isNotBlank(size)) {
            value.setSz(new BigInteger(size));
        }
        if (StringUtils.isNotBlank(space)) {
            value.setSpace(new BigInteger(space));
        }
        if (bordType != null) {
            value.setVal(bordType);
        }
        runProperties.setBdr(value);
    }

    /*
     * 着重号
     */
    public static void addRPrEmStyle(RPr runProperties, STEm emType) {
        if (emType != null) {
            CTEm em = new CTEm();
            em.setVal(emType);
            runProperties.setEm(em);
        }
    }

    /*
     *  空心
     */
    public static void addRPrOutlineStyle(RPr runProperties) {
        BooleanDefaultTrue outline = new BooleanDefaultTrue();
        outline.setVal(true);
        runProperties.setOutline(outline);
    }

    /*
     *  设置上标下标
     */
    public static void addRPrcaleStyle(RPr runProperties, STVerticalAlignRun vAlign) {
        if (vAlign != null) {
            CTVerticalAlignRun value = new CTVerticalAlignRun();
            value.setVal(vAlign);
            runProperties.setVertAlign(value);
        }
    }

    /*
     *  设置字符间距缩进
     */
    public static void addRPrScaleStyle(RPr runProperties, int indent) {
        CTTextScale value = new CTTextScale();
        value.setVal(indent);
        runProperties.setW(value);
    }

    /*
     *  设置字符间距信息
     */
    public static void addRPrtSpacingStyle(RPr runProperties, int spacing) {
        CTSignedTwipsMeasure value = new CTSignedTwipsMeasure();
        value.setVal(BigInteger.valueOf(spacing));
        runProperties.setSpacing(value);
    }

    /*
     *  设置文本位置
     */
    public static void addRPrtPositionStyle(RPr runProperties, int position) {
        CTSignedHpsMeasure ctPosition = new CTSignedHpsMeasure();
        ctPosition.setVal(BigInteger.valueOf(position));
        runProperties.setPosition(ctPosition);
    }

    /*
     *  阴文
     */
    public static void addRPrImprintStyle(RPr runProperties) {
        BooleanDefaultTrue imprint = new BooleanDefaultTrue();
        imprint.setVal(true);
        runProperties.setImprint(imprint);
    }

    /*
     *  阳文
     */
    public static void addRPrEmbossStyle(RPr runProperties) {
        BooleanDefaultTrue emboss = new BooleanDefaultTrue();
        emboss.setVal(true);
        runProperties.setEmboss(emboss);
    }

    /*
     *  设置隐藏
     */
    public static void setRPrVanishStyle(RPr runProperties, boolean isVanish) {
        BooleanDefaultTrue vanish = runProperties.getVanish();
        if (vanish != null) {
            vanish.setVal(isVanish);
        } else {
            vanish = new BooleanDefaultTrue();
            vanish.setVal(isVanish);
            runProperties.setVanish(vanish);
        }
    }

    /*
     *  设置阴影
     */
    public static void addRPrShadowStyle(RPr runProperties) {
        BooleanDefaultTrue shadow = new BooleanDefaultTrue();
        shadow.setVal(true);
        runProperties.setShadow(shadow);
    }

    /*
     *  设置底纹
     */
    public static void addRPrShdStyle(RPr runProperties, STShd shdtype) {
        if (shdtype != null) {
            CTShd shd = new CTShd();
            shd.setVal(shdtype);
            runProperties.setShd(shd);
        }
    }

    /*
     *  设置突出显示文本
     */
    public static void addRPrHightLightStyle(RPr runProperties, String hightlight) {
        if (StringUtils.isNotBlank(hightlight)) {
            Highlight highlight = new Highlight();
            highlight.setVal(hightlight);
            runProperties.setHighlight(highlight);
        }
    }

    /*
     *  设置删除线样式
     */
    public static void addRPrStrikeStyle(RPr runProperties, boolean isStrike, boolean isDStrike) {
        // 删除线
        if (isStrike) {
            BooleanDefaultTrue strike = new BooleanDefaultTrue();
            strike.setVal(true);
            runProperties.setStrike(strike);
        }
        // 双删除线
        if (isDStrike) {
            BooleanDefaultTrue dStrike = new BooleanDefaultTrue();
            dStrike.setVal(true);
            runProperties.setDstrike(dStrike);
        }
    }

    /*
     *  加粗
     */
    public static void addRPrBoldStyle(RPr runProperties) {
        BooleanDefaultTrue b = new BooleanDefaultTrue();
        b.setVal(true);
        runProperties.setB(b);
    }

    /*
     *  倾斜
     */
    public static void addRPrItalicStyle(RPr runProperties) {
        BooleanDefaultTrue b = new BooleanDefaultTrue();
        b.setVal(true);
        runProperties.setI(b);
    }

    /*
     *  添加下划线
     */
    public static void addRPrUnderlineStyle(RPr runProperties, UnderlineEnumeration enumType) {
        U val = new U();
        val.setVal(enumType);
        runProperties.setU(val);
    }

    /*------------------------------------Word 相关--------------------------------------------------- */
    /*
     *  设置分节符 nextPage:下一页 continuous:连续 evenPage:偶数页 oddPage:奇数页
     */
    public static void setDocSectionBreak(WordprocessingMLPackage wordPackage, String sectValType) {
        if (StringUtils.isNotBlank(sectValType)) {
            SectPr sectPr = getDocSectPr(wordPackage);
            Type sectType = sectPr.getType();
            if (sectType == null) {
                sectType = new Type();
                sectPr.setType(sectType);
            }
            sectType.setVal(sectValType);
        }
    }

    /*
     *  设置页面背景色
     */
    public static void setDocumentBackGround(WordprocessingMLPackage wordPackage, ObjectFactory factory, String color)
            throws Exception {
        MainDocumentPart mdp = wordPackage.getMainDocumentPart();
        CTBackground bkground = mdp.getContents().getBackground();
        if (StringUtils.isNotBlank(color)) {
            if (bkground == null) {
                bkground = factory.createCTBackground();
                bkground.setColor(color);
            }
            mdp.getContents().setBackground(bkground);
        }
    }

    /*
     *  设置页面边框
     */
    public static void setDocumentBorders(WordprocessingMLPackage wordPackage, ObjectFactory factory, CTBorder top,
            CTBorder right, CTBorder bottom, CTBorder left) {
        SectPr sectPr = getDocSectPr(wordPackage);
        PgBorders pgBorders = sectPr.getPgBorders();
        if (pgBorders == null) {
            pgBorders = factory.createSectPrPgBorders();
            sectPr.setPgBorders(pgBorders);
        }
        if (top != null) {
            pgBorders.setTop(top);
        }
        if (right != null) {
            pgBorders.setRight(right);
        }
        if (bottom != null) {
            pgBorders.setBottom(bottom);
        }
        if (left != null) {
            pgBorders.setLeft(left);
        }
    }

    /*
     *  设置页面大小及纸张方向 landscape横向
     */
    public static void setDocumentSize(WordprocessingMLPackage wordPackage, ObjectFactory factory, String width, String height,
            STPageOrientation stValue) {
        SectPr sectPr = getDocSectPr(wordPackage);
        PgSz pgSz = sectPr.getPgSz();
        if (pgSz == null) {
            pgSz = factory.createSectPrPgSz();
            sectPr.setPgSz(pgSz);
        }
        if (StringUtils.isNotBlank(width)) {
            pgSz.setW(new BigInteger(width));
        }
        if (StringUtils.isNotBlank(height)) {
            pgSz.setH(new BigInteger(height));
        }
        if (stValue != null) {
            pgSz.setOrient(stValue);
        }
    }

    public static SectPr getDocSectPr(WordprocessingMLPackage wordPackage) {
        SectPr sectPr = wordPackage.getDocumentModel().getSections().get(0).getSectPr();
        return sectPr;
    }

    /*
     * 设置页边距
     */
    public static void setDocMarginSpace(WordprocessingMLPackage wordPackage, ObjectFactory factory, String top, String left,
            String bottom, String right) {
        SectPr sectPr = getDocSectPr(wordPackage);
        PgMar pg = sectPr.getPgMar();
        if (pg == null) {
            pg = factory.createSectPrPgMar();
            sectPr.setPgMar(pg);
        }
        if (StringUtils.isNotBlank(top)) {
            pg.setTop(new BigInteger(top));
        }
        if (StringUtils.isNotBlank(bottom)) {
            pg.setBottom(new BigInteger(bottom));
        }
        if (StringUtils.isNotBlank(left)) {
            pg.setLeft(new BigInteger(left));
        }
        if (StringUtils.isNotBlank(right)) {
            pg.setRight(new BigInteger(right));
        }
    }

    /*
     *  设置行号
     * @param distance
     *            :距正文距离 1厘米=567
     * @param start
     *            :起始编号(0开始)
     * @param countBy
     *            :行号间隔
     * @param restartType
     *            :STLineNumberRestart.CONTINUOUS(continuous连续编号)<br/>
     *            STLineNumberRestart.NEW_PAGE(每页重新编号)<br/>
     *            STLineNumberRestart.NEW_SECTION(每节重新编号)
     */
    public static void setDocInNumType(WordprocessingMLPackage wordPackage, String countBy, String distance, String start,
            STLineNumberRestart restartType) {
        SectPr sectPr = getDocSectPr(wordPackage);
        CTLineNumber lnNumType = sectPr.getLnNumType();
        if (lnNumType == null) {
            lnNumType = new CTLineNumber();
            sectPr.setLnNumType(lnNumType);
        }
        if (StringUtils.isNotBlank(countBy)) {
            lnNumType.setCountBy(new BigInteger(countBy));
        }
        if (StringUtils.isNotBlank(distance)) {
            lnNumType.setDistance(new BigInteger(distance));
        }
        if (StringUtils.isNotBlank(start)) {
            lnNumType.setStart(new BigInteger(start));
        }
        if (restartType != null) {
            lnNumType.setRestart(restartType);
        }
    }

    /*
     * 设置文字方向 tbRl 垂直
     */
    public static void setDocTextDirection(WordprocessingMLPackage wordPackage, String textDirection) {
        if (StringUtils.isNotBlank(textDirection)) {
            SectPr sectPr = getDocSectPr(wordPackage);
            TextDirection textDir = sectPr.getTextDirection();
            if (textDir == null) {
                textDir = new TextDirection();
                sectPr.setTextDirection(textDir);
            }
            textDir.setVal(textDirection);
        }
    }

    /*
     * 设置word 垂直对齐方式(Word默认方式都是"顶端对齐")
     */
    public static void setDocVAlign(WordprocessingMLPackage wordPackage, STVerticalJc valignType) {
        if (valignType != null) {
            SectPr sectPr = getDocSectPr(wordPackage);
            CTVerticalJc valign = sectPr.getVAlign();
            if (valign == null) {
                valign = new CTVerticalJc();
                sectPr.setVAlign(valign);
            }
            valign.setVal(valignType);
        }
    }

    /*
     * 获取文档的可用宽度
     */
    public static int getWritableWidth(WordprocessingMLPackage wordPackage) throws Exception {
        return wordPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();
    }

}


