package db.migration;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.regex.Pattern;

public class V12__change_federation_to_engine_block_resource implements SpringJdbcMigration {

    private final String currentValue = "DataType=\"http://www.w3.org/2001/XMLSchema#string\">Federation</AttributeValue>";
    private final String replacementValue = "DataType=\"http://www.w3.org/2001/XMLSchema#string\">EngineBlock</AttributeValue>";

    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        jdbcTemplate.query("SELECT id, policy_xml FROM pdp_policies WHERE latest_revision = 1 ", resultSet -> {
            long id = resultSet.getLong("id");
            String xml = resultSet.getString("policy_xml");
            String updatedXml = xml.replaceFirst(currentValue, replacementValue);
            jdbcTemplate.update("UPDATE pdp_policies SET policy_xml = ? WHERE id = ?",
                updatedXml, id);

        });
    }
}
