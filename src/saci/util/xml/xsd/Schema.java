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

package saci.util.xml.xsd;

import java.util.List;

import saci.util.xml.XmlAttribute;
import saci.util.xml.XmlElement;
import saci.util.xml.XmlType;

@XmlType(name="xs:schema", propOrder={"attributes","elements","complexTypes"})
public class Schema {
	@XmlAttribute(name="xmlns")
	protected String xmlns;
	@XmlAttribute(name="xmlns:xs")
	protected String xmlnsXs;
	@XmlAttribute(name="targetNamespace")
	protected String targetNamespace;
	@XmlAttribute(name="elementFormDefault")
	protected String elementFormDefault;
	@XmlElement(name="xs:element")
	protected List<Element> elements;
	@XmlElement(name="xs:complexType")
	protected List<ComplexType> complexTypes;
	@XmlElement(name="xs:attribute")
	protected List<Attribute> attributes;

	public String getXmlns() {
		return xmlns;
	}

	public void setXmlns(String xmlns) {
		this.xmlns = xmlns;
	}

	public String getXmlnsXs() {
		return xmlnsXs;
	}

	public void setXmlnsXs(String xmlnsXs) {
		this.xmlnsXs = xmlnsXs;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	public String getElementFormDefault() {
		return elementFormDefault;
	}

	public void setElementFormDefault(String elementFormDefault) {
		this.elementFormDefault = elementFormDefault;
	}

	public List<Element> getElements() {
		return elements;
	}

	public void setElements(List<Element> elements) {
		this.elements = elements;
	}

	public List<ComplexType> getComplexTypes() {
		return complexTypes;
	}

	public void setComplexTypes(List<ComplexType> complexTypes) {
		this.complexTypes = complexTypes;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	
}
