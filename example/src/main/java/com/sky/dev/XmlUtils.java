package com.sky.dev;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class XmlUtils {

    public static void main(String[] args) throws IOException {
        String fileUrl = "https://gc-attachment.s3.cn-north-1.amazonaws.com.cn/train-1589867100594.cpt";
        URL url = new URL(fileUrl);
        String content = FileUtil.readString(url, "utf-8");
        System.err.println(content);

        Document document = readXML(url.openStream());
        Object path = XmlUtil.getByXPath("/WorkBook/TableDataMap/TableData/Connection/DatabaseName", document, XPathConstants.STRING);
        NodeList databaseNameNodeList = document.getElementsByTagName("DatabaseName");
        System.out.println(databaseNameNodeList.item(0).getTextContent());
        System.err.println(path);


    }

    /**
     * 默认的DocumentBuilderFactory实现
     */
    private static String defaultDocumentBuilderFactory = "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl";

    /**
     * 读取解析XML文件<br>
     * 编码在XML中定义
     *
     * @param inputStream XML流
     * @return XML文档对象
     * @throws UtilException IO异常或转换异常
     * @since 3.0.9
     */
    public static Document readXML(InputStream inputStream) throws UtilException {
        return readXML(new InputSource(inputStream));
    }

    /**
     * 读取解析XML文件<br>
     * 编码在XML中定义
     *
     * @param source {@link InputSource}
     * @return XML文档对象
     * @since 3.0.9
     */
    public static Document readXML(InputSource source) {
        final DocumentBuilder builder = createDocumentBuilder();
        try {
            return builder.parse(source);
        } catch (Exception e) {
            throw new UtilException(e, "Parse XML from stream error!");
        }
    }

    /**
     * 创建XML文档<br>
     * 创建的XML默认是utf8编码，修改编码的过程是在toStr和toFile方法里，即XML在转为文本的时候才定义编码
     *
     * @return XML文档
     * @since 4.0.8
     */
    public static Document createXml() {
        return createDocumentBuilder().newDocument();
    }

    /**
     * 创建 DocumentBuilder
     *
     * @return DocumentBuilder
     * @since 4.1.2
     */
    public static DocumentBuilder createDocumentBuilder() {
        DocumentBuilder builder;
        try {
            builder = createDocumentBuilderFactory().newDocumentBuilder();
        } catch (Exception e) {
            throw new UtilException(e, "Create xml document error!");
        }
        return builder;
    }

    /**
     * 创建{@link DocumentBuilderFactory}
     * <p>
     * 默认使用"com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl"<br>
     * 如果使用第三方实现，请调用{@link #disableDefaultDocumentBuilderFactory()}
     * </p>
     *
     * @return {@link DocumentBuilderFactory}
     */
    public static DocumentBuilderFactory createDocumentBuilderFactory() {
        final DocumentBuilderFactory factory;
        if (StrUtil.isNotEmpty(defaultDocumentBuilderFactory)) {
            factory = DocumentBuilderFactory.newInstance(defaultDocumentBuilderFactory, null);
        } else {
            factory = DocumentBuilderFactory.newInstance();
        }
        // 默认打开NamespaceAware，getElementsByTagNameNS可以使用命名空间
        factory.setNamespaceAware(true);
        return disableXXE(factory);
    }

    /**
     * 关闭XXE，避免漏洞攻击<br>
     * see: https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J
     *
     * @param dbf DocumentBuilderFactory
     * @return DocumentBuilderFactory
     */
    private static DocumentBuilderFactory disableXXE(DocumentBuilderFactory dbf) {
        String feature;
        try {
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            feature = "http://apache.org/xml/features/disallow-doctype-decl";
            dbf.setFeature(feature, true);
            // If you can't completely disable DTDs, then at least do the following:
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            // JDK7+ - http://xml.org/sax/features/external-general-entities
            feature = "http://xml.org/sax/features/external-general-entities";
            dbf.setFeature(feature, false);
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            // JDK7+ - http://xml.org/sax/features/external-parameter-entities
            feature = "http://xml.org/sax/features/external-parameter-entities";
            dbf.setFeature(feature, false);
            // Disable external DTDs as well
            feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
            dbf.setFeature(feature, false);
            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
            // ignore
        }
        return dbf;
    }

    /**
     * 禁用默认的DocumentBuilderFactory，禁用后如果有第三方的实现（如oracle的xdb包中的xmlparse），将会自动加载实现。
     */
    synchronized public static void disableDefaultDocumentBuilderFactory() {
        defaultDocumentBuilderFactory = null;
    }
}
