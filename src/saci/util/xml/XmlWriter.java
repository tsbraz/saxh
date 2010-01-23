/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2009 SACI Informática Ltda.
 */

package saci.util.xml;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import java.util.List;
import saci.util.Types;

class XmlWriter {

    private boolean prettyPrint, prettyPrintCloseTag;
    private int level;
    private Writer writer;
    private String charsetName;
    private XmlMapper mapper = new XmlMapper();

    XmlWriter(Writer writer, String charsetName, boolean prettyPrint) {
        setWriter(writer);
        this.charsetName = charsetName == null ? java.nio.charset.Charset.defaultCharset().name() : charsetName;
        this.prettyPrint = prettyPrint;
    }

    public void setWriter(Writer writer) {
        assert (writer != null);
        this.writer = writer;
    }

    private void write(String s) throws IOException {
        writer.write(s);
    }

    private void write(char s) throws IOException {
        writer.write(s);
    }

    private void writeValue(String value) throws IOException {
        if (value == null) {
            write("");
        }

        for (int i = 0; i < value.length(); i++) {
            switch (value.charAt(i)) {
                case '<':
                    write("&lt;");
                    break;
                case '>':
                    write("&gt;");
                    break;
                case '&':
                    write("&amp;");
                    break;
                case '"':
                    write("&quot;");
                    break;
                case '\'':
                    write("&#39;");
                    break;
                default:
                    write(value.charAt(i));
            }
        }
    }

    public void writeBean(Object bean) throws IOException {
        writeHeader();
        writeBeanInfo(bean);
        writer.flush();
    }

    private void writeEncodingInfo() throws IOException {
        write("<?xml version=\"1.0\" encoding=\"");
        write(charsetName);
        write("\"?>");
    }

    private void writeHeader() throws IOException {
        writeEncodingInfo();
    }

    private void writeBeanInfo(Object bean) throws IOException {
        writeBeanInfo(bean, null);
    }

    /**
     * Escreve o bean como XML
     * @param bean o bean que deve ser descrito no XML
     * @param fieldName o nome do campo que contém este bean, caso o mesmo não seja anotado,
     * o nome da classe no XML fica igual ao nome do campo
     * @throws IOException
     */
    private void writeBeanInfo(Object bean, String fieldName) throws IOException {
        try {
            if (bean.getClass().isArray()) {
                List<?> list = Arrays.asList((Object[])bean);
                writeList(list, fieldName);
            } else {
                XmlMap xmlMap = mapper.mapBean(bean);
                String beanName = xmlMap.annontationless && fieldName != null ? fieldName : xmlMap.name;
                writeBeanTag(bean, xmlMap, beanName);
                writeBeanFields(bean, xmlMap);
                closeTag(beanName);
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeList(Collection<?> collection, String fieldName) throws IOException {
        for (Object object : collection) {
            if (object != null) {
                if (Types.isPrintable(object.getClass())) {
                    writeSimpleTag(fieldName, object.toString(), false);
                } else {
                    writeBeanInfo(object);
                }
            }
        }
    }

    private void writeBeanFields(Object bean, XmlMap xmlMap) throws InvocationTargetException, IOException {
        for (int i = 0; i < xmlMap.fields.size(); i++) {
            FieldMap fieldMap = xmlMap.fields.get(i);
            try {
                Object value = fieldMap.field.get(bean);
                if (value == null) {
                    if (fieldMap.nillable) {
                        openEmptyTag(fieldMap.name);
                    }
                } else {
                    if (value instanceof Collection<?>) {
                        writeList((Collection<?>) value, fieldMap.name);
                    } else {
                        // Não dá pra testar todos os objetos antes da execução
                        // se for "Object" nunca vai ser "printable" mas o objeto
                        // instanciado pode ser...
                        if (fieldMap.printable || Types.isPrintable(value.getClass())) {
                            openTag(fieldMap.name);
                            if (value instanceof Date) {
                                write(Types.parseString((Date) value));
                            } else {
                                writeValue(value.toString());
                            }
                            closeTag(fieldMap.name);
                        } else {
                            if (value != bean) {
                                writeBeanInfo(value, fieldMap.name);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void writeSimpleTag(String tagName, String value, boolean nillable) throws IOException {
        if (value == null) {
            if (nillable) {
                openEmptyTag(tagName);
            }
        } else {
            openTag(tagName);
            writeValue(value);
            closeTag(tagName);
        }
    }

    /**
     * Escreve a tag do XML
     * @param bean o objeto que será descrito pela tag
     * @param xmlMap o map do objeto (@see XmlMapper)
     * @param name o nome da tag, caso null o xmlMap.name é usado
     * @throws InvocationTargetException
     * @throws IOException
     */
    private void writeBeanTag(Object bean, XmlMap xmlMap, String name) throws InvocationTargetException, IOException {
        if (prettyPrint) {
            printLevel();
            level++;
            prettyPrintCloseTag = false;
        }
        if (name == null) {
            name = xmlMap.name;
        }
        write("<");
        write(name);
        if (xmlMap.attributes.size() > 0) {
            for (int i = 0; i < xmlMap.attributes.size(); i++) {
                FieldMap fieldMap = xmlMap.attributes.get(i);
                try {
                    Object value = fieldMap.field.get(bean);
                    try {
                        if (value == null) {
                            if (fieldMap.required) {
                                write(' ');
                                write(fieldMap.name);
                                write("=\"\"");
                            }
                        } else {
                            write(' ');
                            write(fieldMap.name);
                            write("=\"");
                            if (value instanceof Date) {
                                write(Types.parseString((Date) value));
                            } else {
                                writeValue(value.toString());
                            }
                            write("\"");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!Types.isNullOrEmpty(xmlMap.namespace)) {
            write(" xmlns=\"");
            write(xmlMap.namespace);
            write("\"");
        }
        write(">");
    }

    private void openTag(String tagName) throws IOException {
        if (prettyPrint) {
            printLevel();
            level++;
            prettyPrintCloseTag = false;
        }
        write("<");
        write(tagName);
        write(">");
    }

    private void openEmptyTag(String tagName) throws IOException {
        write("<");
        write(tagName);
        write("/>");
        if (prettyPrint) {
            prettyPrintCloseTag = false;
            write('\n');
        }
    }

    private void closeTag(String tagName) throws IOException {
        if (prettyPrint) {
            level--;
            if (prettyPrintCloseTag) {
                printLevel();
            }
            prettyPrintCloseTag = true;
        }
        write("</");
        write(tagName);
        write(">");
    }

    private void printLevel() throws IOException {
        write('\n');
        for (int i = 0; i < level; i++) {
            write("  ");
        }
    }
}
