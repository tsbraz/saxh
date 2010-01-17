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

@XmlType(name = "xs:complexType", propOrder = { "sequence", "attribute",
		"elements", "complexContent", "simpleContent" })
public class ComplexType {
	@XmlAttribute(name = "name")
	protected String name;
	@XmlAttribute(name = "abstract")
	protected String abstractType;
	@XmlElement(name = "xs:sequence")
	protected Sequence sequence;
	@XmlElement(name = "xs:simpleContent")
	protected SimpleContent simpleContent;
	@XmlElement(name = "xs:complexContent")
	protected ComplexContent complexContent;
	@XmlElement(name = "xs:attribute")
	protected List<Attribute> attribute;
	@XmlElement(name = "xs:element")
	protected List<Element> elements;

	public SimpleContent getSimpleContent() {
		return simpleContent;
	}

	public ComplexContent getComplexContent() {
		return complexContent;
	}

	public void setComplexContent(ComplexContent complexContent) {
		this.complexContent = complexContent;
	}

	public void setSimpleContent(SimpleContent simpleContent) {
		this.simpleContent = simpleContent;
	}

	public String getAbstractType() {
		return abstractType;
	}

	public void setAbstractType(String abstractType) {
		this.abstractType = abstractType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Sequence getSequence() {
		return sequence;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	public List<Attribute> getAttribute() {
		return attribute;
	}

	public void setAttribute(List<Attribute> attribute) {
		this.attribute = attribute;
	}

	public List<Element> getElements() {
		return elements;
	}

	public void setElements(List<Element> elements) {
		this.elements = elements;
	}

}
