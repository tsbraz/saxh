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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Marshaller {
    public void marshal(Object bean, Writer writer) throws IOException {
        marshal(bean, writer, false);
    }

    public void marshal(Object bean, OutputStream outputStream) throws IOException {
        marshal(bean, outputStream, false);
    }
    
    public void marshal(Object bean, OutputStream outputStream, String charsetName) throws IOException {
        marshal(bean, outputStream, charsetName, false);
    }

    public void marshal(Object bean, OutputStream outputStream, boolean prettyPrint) throws IOException {
        marshal(bean, new OutputStreamWriter(outputStream), prettyPrint);
    }
    
    public void marshal(Object bean, OutputStream outputStream, String charsetName, boolean prettyPrint) throws IOException {
        marshal(bean, new OutputStreamWriter(outputStream, charsetName), charsetName, prettyPrint);
    }

    public void marshal(Object bean, Writer writer, boolean prettyPrint) throws IOException {
        marshal(bean, writer, null, prettyPrint);
    }
    
    public void marshal(Object bean, Writer writer, String charsetName, boolean prettyPrint) throws IOException {
        XmlWriter xml = new XmlWriter(writer, charsetName, prettyPrint);
        xml.writeBean(bean);
    }

    public <T> T unmarshal(InputStream inputStream, Class<T> type) throws IOException {
        BeanParser xml = new BeanParser(inputStream);
        return xml.fillBean(type);
    }
}
