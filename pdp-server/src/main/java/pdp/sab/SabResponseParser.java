/*
 * Copyright 2013 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pdp.sab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SabResponseParser {

  private static final Logger LOG = LoggerFactory.getLogger(SabResponseParser.class);

  public static final String XPATH_ORGANISATION = "//saml:Attribute[@Name='urn:oid:1.3.6.1.4.1.1076.20.100.10.50.1']/saml:AttributeValue";
  public static final String XPATH_ROLES = "//saml:Attribute[@Name='urn:oid:1.3.6.1.4.1.5923.1.1.1.7']/saml:AttributeValue";
  public static final String XPATH_STATUSCODE = "//samlp:StatusCode/@Value";
  public static final String XPATH_STATUSMESSAGE = "//samlp:StatusMessage";

  public static final String SAMLP_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";
  private static final String SAMLP_RESPONDER = "urn:oasis:names:tc:SAML:2.0:status:Responder";

  /**
   * Prefix of the status message if a user is queried that cannot be found.
   */
  private static final String NOT_FOUND_MESSAGE_PREFIX = "Could not find any roles for given NameID";

  public List<String> parse(String soap) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
    List<String> roles = new ArrayList<>();
    XPath xpath = getXPath();
    Document document = createDocument(soap);
    validateStatus(document, xpath);

    XPathExpression rolesExpr = xpath.compile(XPATH_ROLES);
    NodeList rolesNodeList = (NodeList) rolesExpr.evaluate(document, XPathConstants.NODESET);
    for (int i = 0; rolesNodeList != null && i < rolesNodeList.getLength(); i++) {
      Node node = rolesNodeList.item(i);
      if (node != null) {
        roles.add(StringUtils.trimWhitespace(node.getTextContent()));
      }
    }
    return roles;
  }

  private void validateStatus(Document document, XPath xpath) throws XPathExpressionException, IOException {

    XPathExpression statusCodeExpression = xpath.compile(XPATH_STATUSCODE);
    String statusCode = (String) statusCodeExpression.evaluate(document, XPathConstants.STRING);

    if (SAMLP_SUCCESS.equals(statusCode)) {
      // Success, validation returns.
      return;
    } else {
      // Status message is only set if status code not 'success'.
      XPathExpression statusMessageExpression = xpath.compile(XPATH_STATUSMESSAGE);
      String statusMessage = (String) statusMessageExpression.evaluate(document, XPathConstants.STRING);

      if (SAMLP_RESPONDER.equals(statusCode) && statusMessage.startsWith(NOT_FOUND_MESSAGE_PREFIX)) {
        LOG.debug("Given nameId not found in SAB. Is regarded by us as 'valid' response, although server response indicates a server error.");
        return;
      } else {
        throw new IOException("Unsuccessful status. Code: '" + statusCode + "', message: " + statusMessage);
      }
    }
  }

  private Document createDocument(String soap) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setIgnoringElementContentWhitespace(true);
    factory.setValidating(false);

    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(new ByteArrayInputStream(soap.getBytes()));
  }

  private XPath getXPath() {
    XPath xPath = javax.xml.xpath.XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(new SabNgNamespaceResolver());
    return xPath;
  }

  private class SabNgNamespaceResolver implements NamespaceContext {

    @Override
    public String getNamespaceURI(String prefix) {
      if (prefix.equals("samlp")) {
        return "urn:oasis:names:tc:SAML:2.0:protocol";
      } else if (prefix.equals("saml")) {
        return "urn:oasis:names:tc:SAML:2.0:assertion";
      } else {
        return XMLConstants.NULL_NS_URI;
      }
    }

    @Override
    public String getPrefix(String namespaceURI) {
      return null;
    }

    @Override
    public Iterator<?> getPrefixes(String namespaceURI) {
      return null;
    }
  }
}
