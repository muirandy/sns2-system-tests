INSERT into AUDIT
SELECT ROWKEY,
'ParsedJsonToModifyVoiceFeaturesMessage' as "EVENT",
'' as "ASSOCIATION"
FROM RAW_VOIP_INSTRUCTIONS;
