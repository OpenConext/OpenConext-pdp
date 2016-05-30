#!/bin/bash
SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
SP_ENTITY_META_DATA=$SCRIPT_DIR/../src/main/resources/service-registry/saml20-sp-remote.json
IDP_ENTITY_META_DATA=$SCRIPT_DIR/../src/main/resources/service-registry/saml20-idp-remote.json

if [ ! -f $SP_ENTITY_META_DATA ];
  then
    echo "File $SP_ENTITY_META_DATA does not exists."
    exit 1
fi

if [ ! -f $IDP_ENTITY_META_DATA ];
  then
    echo "File $IDP_ENTITY_META_DATA does not exists."
    exit 1
fi

rm -fr $SP_ENTITY_META_DATA;
rm -fr $IDP_ENTITY_META_DATA;

curl -i -H "Content-Type: application/json" --user metadata.client:secret https://multidata.test.surfconext.nl/service-providers.json >> $SP_ENTITY_META_DATA
curl -i -H "Content-Type: application/json" --user metadata.client:secret https://multidata.test.surfconext.nl/identity-providers.json >> $IDP_ENTITY_META_DATA