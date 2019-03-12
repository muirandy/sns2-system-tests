SELECT o.serviceId, v.switchServiceId
FROM ENHANCED_VOIP_STREAM_3 o
LEFT JOIN MODIFY_VOICE_FEATURE_MSGS v
ON o.serviceId = v.serviceId;