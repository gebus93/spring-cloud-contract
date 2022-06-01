/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.contract.verifier.util.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Helper class containing namespaces of provided XML document.
 *
 * @author Łukasz Gębicki
 */
public class DOMNamespaceContext implements NamespaceContext {

	private static final NamespacesExtractor NAMESPACES_EXTRACTOR = new DfsNamespacesExtractor();

	private final Map<String, String> namespaceMap;

	public DOMNamespaceContext(Node contextNode) {
		namespaceMap = NAMESPACES_EXTRACTOR.extract(contextNode);
	}

	public String getNamespaceURI(String arg0) {
		return namespaceMap.get(arg0);
	}

	public String getPrefix(String arg0) {
		for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
			if (entry.getValue().equals(arg0)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public Iterator<String> getPrefixes(String arg0) {
		return namespaceMap.keySet().iterator();
	}

}

/**
 * Interface defining API for namespaces extracting strategy.
 *
 * @author Łukasz Gębicki
 */
interface NamespacesExtractor {

	Map<String, String> extract(Node n);

}

/**
 * Namespaces extracting strategy based on DFS algorithm.
 *
 * @author Łukasz Gębicki
 */
class DfsNamespacesExtractor implements NamespacesExtractor {

	@Override
	public Map<String, String> extract(Node node) {
		Node rootNode = node;
		while (rootNode.getParentNode() != null) {
			rootNode = rootNode.getParentNode();
		}

		Map<String, String> namespaceMap = new HashMap<>();
		Stack<Node> stack = new Stack<>();
		stack.add(rootNode);
		while (!stack.isEmpty()) {
			Node item = stack.pop();
			for (int i = 0, len = item.getChildNodes().getLength(); i < len; i++) {
				stack.add(item.getChildNodes().item(i));
			}
			if (item instanceof Element) {
				Element el = (Element) item;
				NamedNodeMap map = el.getAttributes();
				for (int x = 0, mapLen = map.getLength(); x < mapLen; x++) {
					Attr attr = (Attr) map.item(x);
					if ("xmlns".equals(attr.getPrefix())) {
						namespaceMap.put(attr.getLocalName(), attr.getValue());
					}
				}
			}
		}
		return namespaceMap;
	}

}
