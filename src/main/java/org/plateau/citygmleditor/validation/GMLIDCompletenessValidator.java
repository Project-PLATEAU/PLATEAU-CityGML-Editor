package org.plateau.citygmleditor.validation;

import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GMLIDCompletenessValidator implements IValidator {
    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        File file = new File(World.getActiveInstance().getCityModel().getGmlPath());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);
        doc.getDocumentElement().normalize();

        // get all tag in document
        List<Node> allTags = new ArrayList<>();
        XmlUtil.recursiveFindNodeByAttribute(doc, allTags, TagName.GML_ID);
        List<ValidationResultMessage> messages = new ArrayList<>();

        List<String> allID = allTags.stream().map(e -> ((Element) e).getAttribute(TagName.GML_ID)).collect(Collectors.toList());
        if (allID.contains("")) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, "Exists GmlId null\\n"));
        }

        Map<String, Integer> elementCountMap = new HashMap<>();
        // Count the number of occurrences of each element
        for (String gmlID : allID) {
            elementCountMap.put(gmlID, elementCountMap.getOrDefault(gmlID, 0) + 1);
        }

        String messageError = MessageError.ERR_C01_001_1;
        for (Map.Entry<String, Integer> entry : elementCountMap.entrySet()) {
            String gmlID = entry.getKey();
            if (entry.getValue() > 1) {
                messageError = messageError + MessageFormat.format(MessageError.ERR_C01_001_2, gmlID);
            }
        }

        if (!messageError.equals(MessageError.ERR_C01_001_1)) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, messageError));
        }

        return messages;
    }
}
