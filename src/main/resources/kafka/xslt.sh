#!/bin/bash

kafkaBroker=${1:-broker:9092}

echo " --- xslt.sh --- "
echo $kafkaBroker

docker run --interactive --rm --network=faith \
        confluentinc/cp-kafkacat \
        kafkacat -b $kafkaBroker \
                -t XSLT \
                -K: \
                -P <<EOF
cujoToKnitware.xslt:<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"><xsl:attribute-set name="active-set"><xsl:attribute name="active">true</xsl:attribute></xsl:attribute-set><xsl:template match="/"><switchServiceModificationInstruction><xsl:attribute name="switchServiceId"><xsl:value-of select="modifyVoiceFeatures/SWITCH_SERVICE_ID"/></xsl:attribute><xsl:attribute name="netstreamCorrelationId"><xsl:value-of select="modifyVoiceFeatures/ORDER_ID"/></xsl:attribute><features><xsl:for-each select="modifyVoiceFeatures/FEATURES/code"><xsl:element name="{.}" use-attribute-sets="active-set"/></xsl:for-each></features></switchServiceModificationInstruction></xsl:template></xsl:stylesheet>
EOF