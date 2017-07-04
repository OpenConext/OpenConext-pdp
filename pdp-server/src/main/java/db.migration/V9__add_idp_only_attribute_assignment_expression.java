package db.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.regex.Pattern;

public class V9__add_idp_only_attribute_assignment_expression implements SpringJdbcMigration {

    private final String adviceExpressionEndTag = "</AdviceExpression>";

    private final String replacementIdpOnly =
        "                <AttributeAssignmentExpression\n" +
            "                    AttributeId=\"IdPOnly\"\n" +
            "                    Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n" +
            "                    <AttributeValue\n" +
            "                        DataType=\"http://www.w3.org/2001/XMLSchema#boolean\">true</AttributeValue>\n" +
            "                </AttributeAssignmentExpression>\n" +
            "            " + adviceExpressionEndTag;

    private final String replacementNoIdp =
        "                <AttributeAssignmentExpression\n" +
            "                    AttributeId=\"IdPOnly\"\n" +
            "                    Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\">\n" +
            "                    <AttributeValue\n" +
            "                        DataType=\"http://www.w3.org/2001/XMLSchema#boolean\">false</AttributeValue>\n" +
            "                </AttributeAssignmentExpression>\n" +
            "            " + adviceExpressionEndTag;

    private final Pattern pattern = Pattern.compile(Pattern.quote(adviceExpressionEndTag));

    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        jdbcTemplate.query("SELECT id, policy_xml FROM pdp_policies WHERE latest_revision = 1 ", resultSet -> {
            long id = resultSet.getLong("id");
            String xml = resultSet.getString("policy_xml");
            String updatedXml = xml.contains("AttributeId=\"IDPentityID\"") ?
                pattern.matcher(xml).replaceAll(replacementIdpOnly) :
                pattern.matcher(xml).replaceAll(replacementNoIdp);
            jdbcTemplate.update("UPDATE pdp_policies SET policy_xml = ? WHERE id = ?",
                updatedXml, id);

        });
    }
}
