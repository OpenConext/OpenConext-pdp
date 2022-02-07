package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.regex.Pattern;

public class V11__add_federation_resource extends BaseJavaMigration {

    private final String targetTag = "</Target>";

    private final String replacementFederation =

            "        <AnyOf>\n" +
                    "            <AllOf>\n" +
                    "                <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
                    "                    <AttributeValue\n" +
                    "                        DataType=\"http://www.w3.org/2001/XMLSchema#string\">Federation</AttributeValue>\n" +
                    "                    <AttributeDesignator\n" +
                    "                        AttributeId=\"ClientID\"\n" +
                    "                        DataType=\"http://www.w3.org/2001/XMLSchema#string\"\n" +
                    "                        Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\"\n" +
                    "                        MustBePresent=\"true\"\n" +
                    "                    />\n" +
                    "                </Match>\n" +
                    "            </AllOf>\n" +
                    "        </AnyOf>\n" +
                    "    " + targetTag + "\n";

    private final Pattern pattern = Pattern.compile(Pattern.quote(targetTag));

    public void migrate(Context context) {
        JdbcTemplate jdbcTemplate =
                new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
        jdbcTemplate.query("SELECT id, policy_xml FROM pdp_policies WHERE latest_revision = 1 ", resultSet -> {
            long id = resultSet.getLong("id");
            String xml = resultSet.getString("policy_xml");
            String updatedXml = pattern.matcher(xml).replaceFirst(replacementFederation);
            jdbcTemplate.update("UPDATE pdp_policies SET policy_xml = ? WHERE id = ?",
                    updatedXml, id);

        });
    }
}
