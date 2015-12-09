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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SabResponseParser {

  public List<String> parse(String soap) throws XMLStreamException {
    //despite it's name, the XMLInputFactoryImpl is not thread safe
    XMLInputFactory factory = XMLInputFactory.newInstance();

    Reader reader = new StringReader(soap);
    XMLEventReader eventReader = factory.createXMLEventReader(reader);

    List<String> roles = new ArrayList<>();
    boolean processRoles = false;

    while (eventReader.hasNext()) {
      XMLEvent event = eventReader.nextEvent();

      switch (event.getEventType()) {
        case XMLStreamConstants.START_ELEMENT:
          StartElement startElement = event.asStartElement();
          switch (startElement.getName().getLocalPart()) {
            case "Attribute":
              Attribute name = startElement.getAttributeByName(new QName("Name"));
              if (name != null && name.getValue() != null && name.getValue().equals("urn:oid:1.3.6.1.4.1.5923.1.1.1.7")) {
                processRoles = true;
              }
              break;
            case "AttributeValue":
              if (processRoles) {
                roles.add(eventReader.getElementText());
              }
              break;
          }
          break;
        case XMLStreamConstants.END_ELEMENT:
          EndElement endElement = event.asEndElement();
          if (processRoles && endElement.getName().getLocalPart().equals("Attribute")) {
            //we got what we wanted
            return roles;
          }
      }

    }
    return roles;
  }

}
