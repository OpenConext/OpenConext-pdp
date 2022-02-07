package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class V12__change_federation_to_engine_block_resource extends BaseJavaMigration {

    private final String currentValue = "DataType=\"http://www.w3.org/2001/XMLSchema#string\">Federation</AttributeValue>";
    private final String replacementValue = "DataType=\"http://www.w3.org/2001/XMLSchema#string\">EngineBlock</AttributeValue>";

    public void migrate(Context context) {
        JdbcTemplate jdbcTemplate =
                new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
        jdbcTemplate.query("SELECT id, policy_xml FROM pdp_policies WHERE latest_revision = 1 ", resultSet -> {
            long id = resultSet.getLong("id");
            String xml = resultSet.getString("policy_xml");
            String updatedXml = xml.replaceFirst(currentValue, replacementValue);
            jdbcTemplate.update("UPDATE pdp_policies SET policy_xml = ? WHERE id = ?",
                    updatedXml, id);

        });
    }
}
