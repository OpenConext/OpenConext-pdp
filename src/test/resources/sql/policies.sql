INSERT INTO `pdp_policies` (`policy_xml`,`name`)
  VALUES (
'<?xml version="1.0" encoding="UTF-8"?>
<Policy xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17" PolicyId="urn:com:att:xacml:policy:id:4ec39305-37d5-40c4-a614-8a258683b114"
        Version="1"
        RuleCombiningAlgId="urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable">
    <Description>No access to SURFspot unless subject is a student, employee, staff or member</Description>
    <PolicyDefaults>
        <XPathVersion>http://www.w3.org/TR/1999/REC-xpath-19991116</XPathVersion>
    </PolicyDefaults>
    <Target>
        <AnyOf>
            <AllOf>
                <Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
                    <AttributeValue
                        DataType="http://www.w3.org/2001/XMLSchema#string">https://www.surfspot.nl/simplesaml/module.php/saml/sp/metadata.php/saml</AttributeValue>
                    <AttributeDesignator
                        AttributeId="urn:oasis:names:tc:SAML:2.0:nameid-format:entity"
                        DataType="http://www.w3.org/2001/XMLSchema#string"
                        Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource"
                        MustBePresent="false"
                    />
                </Match>
            </AllOf>
        </AnyOf>
    </Target>
    <Rule
            Effect="Permit"
            RuleId="http://axiomatics.com/alfa/identifier/SURFconext.SURFspotAccess.permitAccess">
        <Description />
        <Target>
            <AnyOf>
                <AllOf>
                    <Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
                        <AttributeValue
                            DataType="http://www.w3.org/2001/XMLSchema#string">student</AttributeValue>
                        <AttributeDesignator
                            AttributeId="urn:mace:dir:attribute-def:eduPersonAffiliation"
                            DataType="http://www.w3.org/2001/XMLSchema#string"
                            Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
                            MustBePresent="false"
                        />
                    </Match>
                </AllOf>
                <AllOf>
                    <Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
                        <AttributeValue
                            DataType="http://www.w3.org/2001/XMLSchema#string">employee</AttributeValue>
                        <AttributeDesignator
                            AttributeId="urn:mace:dir:attribute-def:eduPersonAffiliation"
                            DataType="http://www.w3.org/2001/XMLSchema#string"
                            Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
                            MustBePresent="false"
                        />
                    </Match>
                </AllOf>
                <AllOf>
                    <Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
                        <AttributeValue
                            DataType="http://www.w3.org/2001/XMLSchema#string">staff</AttributeValue>
                        <AttributeDesignator
                            AttributeId="urn:mace:dir:attribute-def:eduPersonAffiliation"
                            DataType="http://www.w3.org/2001/XMLSchema#string"
                            Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
                            MustBePresent="false"
                        />
                    </Match>
                </AllOf>
                <AllOf>
                    <Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
                        <AttributeValue
                            DataType="http://www.w3.org/2001/XMLSchema#string">member</AttributeValue>
                        <AttributeDesignator
                            AttributeId="urn:mace:dir:attribute-def:eduPersonAffiliation"
                            DataType="http://www.w3.org/2001/XMLSchema#string"
                            Category="urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"
                            MustBePresent="false"
                        />
                    </Match>
                </AllOf>
            </AnyOf>
        </Target>
    </Rule>
    <Rule
            Effect="Deny"
            RuleId="http://axiomatics.com/alfa/identifier/SURFconext.SURFspotAccess.denyAccess">
        <Description />
        <Target />
        <AdviceExpressions>
            <AdviceExpression AdviceId="http://example.com/advice/reasonForDeny"
                                     AppliesTo="Deny">
                <AttributeAssignmentExpression AttributeId="DenyMessage" Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource">
                    <AttributeValue
                            DataType="http://www.w3.org/2001/XMLSchema#string">You are not a member of the SURF organization</AttributeValue>
                </AttributeAssignmentExpression>
            </AdviceExpression>
        </AdviceExpressions>
    </Rule>
</Policy>
', 'SURFspotAccess'),
('
<?xml version="1.0" encoding="UTF-8"?>
<Policy xmlns="urn:oasis:names:tc:xacml:3.0:core:schema:wd-17" PolicyId="urn:com:att:xacml:policy:id:4ec39305-37d5-40c4-a614-8a258683b114"
        Version="1"
        RuleCombiningAlgId="urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable">
    <Description>Policy for requiring team membership</Description>
    <Target>
        <AnyOf>
            <AllOf>
                <Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
                    <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">http://admin-sp</AttributeValue>
                    <AttributeDesignator Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource"
                                         AttributeId="urn:oasis:names:tc:SAML:2.0:nameid-format:entity"
                                         DataType="http://www.w3.org/2001/XMLSchema#string"
                                         MustBePresent="true" />
                </Match>
            </AllOf>
        </AnyOf>
    </Target>
    <Rule RuleId="SURFconext.TeamAccess.permitAccess" Effect="Permit">
        <Description>To access admin-sp one must be a member of the team urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:managementvo</Description>
        <Target>
            <AnyOf>
                <AllOf>
                    <Match MatchId="urn:oasis:names:tc:xacml:1.0:function:string-equal">
                        <AttributeValue DataType="http://www.w3.org/2001/XMLSchema#string">urn:collab:group:test.surfteams.nl:nl:surfnet:diensten:managementvo</AttributeValue>
                        <AttributeDesignator Category="urn:oasis:names:tc:xacml:3.0:attribute-category:resource"
                                             AttributeId="urn:mace:dir:attribute-def:group-name"
                                             DataType="http://www.w3.org/2001/XMLSchema#string"
                                             MustBePresent="false" />
                    </Match>
                </AllOf>
            </AnyOf>
        </Target>
    </Rule>
    <Rule Effect="Deny" RuleId="SURFconext.TeamAccess.denyAccess">
        <Description />
        <Target />
    </Rule>
</Policy>
', 'TeamAccess');