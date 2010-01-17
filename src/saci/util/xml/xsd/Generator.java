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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import saci.util.Types;
import saci.util.xml.Marshaller;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Generator {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Use: Generator xsdfile [path to generated VOs]");
			return;
		}
		try {
			String xsd = args[0];
			Generator generator = new Generator(new FileInputStream(xsd));
			if (args.length > 0) {
				generator.generateVO(args[1]);
			} else {
				generator.generateVO();
			}
			System.out.println("Generated");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static final String JAVA_EXT = ".java";
	private static Map<String, String> requiredTypes;
	private static Map<String, String> types;
	private Schema schema;
	private File path;
	private String packageName;
	private Configuration templateConfig;
	private List<ComplexType> complexTypes;
	
	private Generator() {
		if (types == null) {
			types = new HashMap<String, String>();
			types.put("xs:short", "Short");
			types.put("xs:int", "Integer");
			types.put("xs:integer", "Integer");
			types.put("xs:long", "Long");
			types.put("xs:float", "Float");
			types.put("xs:double", "Double");
			types.put("xs:string", "String");
			types.put("xs:date", "java.util.Date");
			requiredTypes = new HashMap<String, String>();
			requiredTypes.put("xs:short", "short");
			requiredTypes.put("xs:int", "int");
			requiredTypes.put("xs:integer", "int");
			requiredTypes.put("xs:long", "long");
			requiredTypes.put("xs:float", "float");
			requiredTypes.put("xs:double", "double");
			requiredTypes.put("xs:string", "String");
			requiredTypes.put("xs:date", "java.util.Date");
		}
	}
	
	public Generator(InputStream xsd) throws IOException {
		this();
		schema = new Marshaller().unmarshal(xsd, Schema.class);
	}
	
	public void generateVO() {
		generateVO(new File("./"));
	}
	
	public void generateVO(String path) {
		generateVO(new File(path));
	}
	
	public void generateVO(File path) {
		if (!path.exists()) {
			throw new IllegalArgumentException("Path does not exist");
		}
		if (!path.isDirectory()) {
			throw new IllegalArgumentException("Path is not a valid path");
		}
		this.path = path;
		generate();
	}
	
	private void generate() {
		String namespace = schema.getTargetNamespace();
		if (Types.isNullOrEmpty(namespace)) {
			namespace = schema.xmlns;
			if (Types.isNullOrEmpty(namespace)) {
				throw new RuntimeException("Namespace must be defined");
			}
		}
		packageName = getPackageName(namespace);
		createPackageDirectory(packageName);
		listComplexTypes();
		for (ComplexType complexType : complexTypes) {
			makeVO(complexType);
		}
	}
	
	private void listComplexTypes() {
		complexTypes = new ArrayList<ComplexType>();
		if (schema.complexTypes != null) {
			for (ComplexType complexType : schema.complexTypes) {
				getComplexType(complexType);
			}
		}
		if (schema.elements != null) {
			listComplexTypesInElements(schema.elements);
		}
	}

	private void getComplexType(ComplexType complexType) {
		complexTypes.add(complexType);
		if (complexType.elements != null) {
			listComplexTypesInElements(complexType.elements);
		} else if (complexType.sequence != null) {
			listComplexTypesInSequence(complexType.sequence);
		} else if (complexType.complexContent != null) {
			listComplexTypesInSequence(complexType.complexContent.sequence);
			if (complexType.complexContent.extension != null) {
				listComplexTypesInSequence(complexType.complexContent.extension.sequence);
			}
		} else if (complexType.simpleContent != null && complexType.simpleContent.extension != null) {
			listComplexTypesInSequence(complexType.simpleContent.extension.sequence);
		}
	}
	
	private void listComplexTypesInElements(List<Element> elements) {
		if (elements != null) {
			for (Element element : elements) {
				if (element.complexType != null) {
					element.complexType.name = element.name;
					getComplexType(element.complexType);
				}
			}
		}
	}
	
	private void listComplexTypesInSequence(Sequence sequence) {
		if (sequence != null) {
			if (sequence.elements != null) {
				listComplexTypesInElements(sequence.elements);
			}
		}
	}
	
	private void makeVO(ComplexType complexType) {
		Map<String, Object> map = new HashMap<String, Object>();
		String className = getJavaName(complexType.name, true);
		map.put("packageName", packageName);
		map.put("className", className);
		map.put("type", complexType);
		map.put("name", complexType.name);
		map.put("abstract", "true".equals(complexType.abstractType) ? "true" : "false");
		if (complexType.attribute != null) {
			for (Attribute attribute : complexType.attribute) {
				attribute.type = getType(attribute);
				attribute.javaName = getJavaName(attribute.name, false);
			}
			map.put("attributes", complexType.attribute);
		}
		List<Element> elements = new ArrayList<Element>();
		if (complexType.sequence != null && complexType.sequence.elements != null) {
			elements.addAll(complexType.sequence.elements);
		}
		if (complexType.complexContent != null) {
			if (complexType.complexContent.sequence != null) {
				elements.addAll(complexType.complexContent.sequence.elements);
			}
			if (complexType.complexContent.extension.sequence != null) {
				elements.addAll(complexType.complexContent.extension.sequence.elements);
			}
		}
		if (complexType.simpleContent != null) {
			if (complexType.simpleContent.extension != null && complexType.simpleContent.extension.sequence != null) {
				elements.addAll(complexType.simpleContent.extension.sequence.elements);
			}
		}
		if (elements.size() > 0) {
			StringBuilder propOrder = new StringBuilder();
			for (Element element : elements) {
				element.type = getType(element);
				element.javaName = getJavaName(element.name, false);
				if (propOrder.length() > 0) {
					propOrder.append(",");
				}
				propOrder.append('"');
				propOrder.append(element.javaName);
				propOrder.append('"');
			}
			map.put("elements", elements);
			map.put("propOrder", propOrder);
		}
		if (complexType.complexContent != null && complexType.complexContent.extension != null) {
			map.put("extension", getJavaName(complexType.complexContent.extension.base, true));
		} else if (complexType.simpleContent != null && complexType.simpleContent.extension != null) {
			map.put("extension", getJavaName(complexType.simpleContent.extension.base, true));
		}
		makeFile(className, map);
	}
	
	private void makeFile(String className, Map<String, Object> parameters) {
		try {
			Template template = getTemplate();
			FileWriter out = new FileWriter(new File(path, className + JAVA_EXT));
			template.process(parameters, out);
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TemplateException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Template getTemplate() throws IOException {
		if (templateConfig == null) {
			templateConfig = new Configuration();
			templateConfig.setClassForTemplateLoading(getClass(), ".");
			templateConfig.setObjectWrapper(new DefaultObjectWrapper());
		}
		return templateConfig.getTemplate("vo.ftl");
	}
	
	private String getJavaName(String name, boolean isClass) {
		StringBuilder sb = new StringBuilder();
		boolean upper = false;
		sb.append(isClass ? (Character.toUpperCase(name.charAt(0))) : (Character.toLowerCase(name.charAt(0))));
		for (int i = 1; i < name.length(); i++) {
			char c = name.charAt(i);
			if (c == '_') {
				upper = true;
			} else {
				if (upper) {
					sb.append(Character.toUpperCase(c));
				} else {
					sb.append(c);
				}
				upper = false;
			}
		}
		return sb.toString();
	}
	
	private String getType(Attribute attribute) {
		String type = null;
		if (!Types.isNullOrEmpty(attribute.ref)) {
			Attribute ref = findAttribute(attribute.ref);
			if (ref == null) {
				System.out.println("Reference not found " + attribute.ref);
				attribute.name = attribute.ref;
				attribute.javaName = getJavaName(attribute.name, false);
				attribute.type = "xs:string";
			} else {
				attribute.name = ref.name;
				attribute.javaName = ref.javaName;
				attribute.type = ref.type;
				attribute.use = ref.use;
			}
		}
		if ("required".equals(attribute.getUse())) {
			type = requiredTypes.get(attribute.type);
		} else {
			type = types.get(attribute.type);
		}
		return Types.isNullOrEmpty(type) ? "String" : type;
	}
	
	private String getType(Element element) {
		String type = null;
		if (!Types.isNullOrEmpty(element.ref)) {
			Element ref = findElement(element.ref);
			if (ref == null) {
				throw new RuntimeException("Reference not found " + element.ref);
			}
			element.name = ref.name;
			element.javaName = ref.javaName;
			element.type = ref.type;
			element.use = ref.use;
		}
		if ("required".equals(element.getUse())) {
			type = requiredTypes.get(element.type);
		} else {
			type = types.get(element.type);
		}
		return type == null ? checkType(element) : type;
	}
	
	private String checkType(Element type) {
		ComplexType complexType = findComplexType(type.type);
		if (complexType != null) {
			if (isList(type.maxOccurs)) {
				return "java.util.List<" + getJavaName(complexType.name,true) + ">";
			}
			return getJavaName(complexType.name,true);
		}
		return "String";
	}
	
	private boolean isList(String maxOccurs) {
		return !Types.isNullOrEmpty(maxOccurs) && !maxOccurs.equals("0") && !maxOccurs.equals("1");
	}
	
	private ComplexType findComplexType(String type) {
		for (ComplexType complexType : complexTypes) {
			if (complexType.name.equals(type)) {
				return complexType;
			}
		}
		return null;
	}
	
	private Attribute findAttribute(String type) {
		if (schema.attributes != null) {
			for (Attribute attribute : schema.attributes) {
				if (attribute.name.equals(type)) {
					return attribute;
				}
			}
		}
		for (ComplexType complexType : complexTypes) {
			if (complexType.attribute != null) {
				for (Attribute attribute : complexType.attribute) {
					if (attribute.name.equals(type)) {
						return attribute;
					}
				}
			}
		}
		return null;
	}
	
	private Element findElement(String type) {
		if (schema.elements != null) {
			for (Element element : schema.elements) {
				if (element.name.equals(type)) {
					return element;
				}
			}
		}
		for (ComplexType complexType : complexTypes) {
			if (complexType.elements != null) {
				for (Element element : complexType.elements) {
					if (element.name.equals(type)) {
						return element;
					}
				}
			}
			if (complexType.sequence != null) {
				for (Element element : complexType.sequence.elements) {
					if (element.name.equals(type)) {
						return element;
					}
				}
			}
		}
		return null;
	}
	
	private String getPackageName(String namespace) {
		try {
			URL url = new URL(namespace);
			String path = url.getHost();
			String subPath = url.getPath();
			String[] s = path.split("[.]");
			StringBuilder sb = new StringBuilder();
			for (int i = s.length-1; i > -1; i--) {
				if (sb.length() > 0) sb.append(".");
				sb.append(s[i]);
			}
			return sb.append(subPath.replace('/','.')).toString();
		} catch (MalformedURLException e) {
			throw new RuntimeException("Invalid namespace");
		}
	}
	
	private void createPackageDirectory(String packageName) {
		path = new File(path, packageName.replace('.', '/'));
		if (!path.exists()) {
			if (!path.mkdirs()) {
				throw new RuntimeException("Failure creating directory " + path);
			}
		}
	}
	
}
