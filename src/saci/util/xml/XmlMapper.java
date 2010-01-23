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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import saci.util.Types;

class XmlMapper {

    private HashMap<Class<?>, XmlMap> xmlMap = new HashMap<Class<?>, XmlMap>();

    XmlMap mapBean(Object bean) {
        Class<?> clazz = bean.getClass();
        if (xmlMap.containsKey(clazz)) {
            return xmlMap.get(clazz);
        } else {
            XmlMap map = null;
            if (!clazz.isAnnotationPresent(XmlType.class)) {
                map = mapAnnotationlessBean(bean);
                map.annontationless = true;
            } else {
                XmlType type = clazz.getAnnotation(XmlType.class);
                List<String> propOrder = new ArrayList<String>();
                if (type.propOrder() != null) {
                	propOrder.addAll(Arrays.asList(type.propOrder()));
                }
                Class<?> superclass = clazz;
                while ((superclass = superclass.getSuperclass()).isAnnotationPresent(XmlType.class)) {
                    XmlType supertype = superclass.getAnnotation(XmlType.class);
                    if (supertype.propOrder() != null) {
                    	propOrder.addAll(Arrays.asList(supertype.propOrder()));
                    }
                }
                map = new XmlMap();
                map.name = type.name();
            	map.propOrder = new String[propOrder.size()];
            	propOrder.toArray(map.propOrder);
                map.namespace = type.namespace();
                mapFields(clazz, map);
                mapAttributes(clazz, map);
                map.annontationless = false;
            }
            xmlMap.put(clazz, map);
            return map;
        }
    }

    private XmlMap mapAnnotationlessBean(Object bean) {
        Class<?> clazz = bean.getClass();
        XmlMap map = new XmlMap();
        map.name = clazz.getSimpleName();
        map.fields = new ArrayList<FieldMap>();
        map.attributes = new ArrayList<FieldMap>(0);
        map.fieldMap = new HashMap<String, FieldMap>();
        do {
            Field[] declaredField = clazz.getDeclaredFields();
            for (Field field : declaredField) {
                //if (!Modifier.isTransient(field.getModifiers())) {
                    FieldMap fieldMap = new FieldMap();
                    fieldMap.name = field.getName();
                    fieldMap.required = false;
                    fieldMap.field = field;
                    fieldMap.field.setAccessible(true);
                    fieldMap.printable = Types.isPrintable(field.getType());
                    map.fields.add(fieldMap);
                    map.fieldMap.put(fieldMap.name, fieldMap);
                //}
            }
        } while ((clazz = clazz.getSuperclass()) != null);
        return map;
    }

    private void mapAttributes(Class<?> clazz, XmlMap map) {
        map.attributes = new ArrayList<FieldMap>();
        Class<?> superclass = clazz;
        do {
	        Field[] declaredField = superclass.getDeclaredFields();
	        for (Field field : declaredField) {
	            XmlAttribute attr = field.getAnnotation(XmlAttribute.class);
	            if (attr != null && !Types.isNullOrEmpty(attr.name()) && Types.isPrintable(field.getType())) {
	                FieldMap fieldMap = new FieldMap();
	                fieldMap.name = attr.name();
	                fieldMap.required = attr.required();
	                fieldMap.field = field;
	                fieldMap.field.setAccessible(true);
	                fieldMap.printable = true;
	                map.attributes.add(fieldMap);
	            }
	        }
        } while ((superclass = superclass.getSuperclass()).isAnnotationPresent(XmlType.class));
    }

    private void mapFields(Class<?> clazz, XmlMap map) {
        map.fields = new ArrayList<FieldMap>();
        map.fieldMap = new HashMap<String, FieldMap>();
        String[] fields = map.propOrder;
        for (int i = 0; i < fields.length; i++) {
            try {
                FieldMap fieldMap = new FieldMap();
                Field field = findField(clazz, fields[i]);
                XmlElement element = field.getAnnotation(XmlElement.class);
                if (!Types.isNullOrEmpty(element.name())) {
                    fieldMap.name = element.name();
                    fieldMap.required = element.required();
                    fieldMap.nillable = element.nillable();
                    fieldMap.field = field;
                    fieldMap.field.setAccessible(true);
                    fieldMap.printable = Types.isPrintable(field.getType());
                    map.fields.add(fieldMap);
                    map.fieldMap.put(fieldMap.name, fieldMap);
                }
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }

	private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superclass = clazz.getSuperclass();
			if (superclass.isAnnotationPresent(XmlType.class)) {
				return findField(superclass, fieldName);
			} else {
				throw e;
			}
		}
	}
}
