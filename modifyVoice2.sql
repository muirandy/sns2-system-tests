--For running 5.1.2 in docker-compose
CREATE STREAM T1_RAW_VOIP_INSTRUCTIONS as
SELECT transaction->"operatorId" as "OPERATOR_ID",
       transaction->"instruction"->"order"->"operatorOrderId" as "OPERATOR_ORDER_ID",
       transaction->"instruction"->"order"->"orderId" as "ORDER_ID",
       transaction->"instruction"->"modifyFeaturesInstruction"->"serviceId" as "SERVICE_ID",
       transaction->"instruction"->"modifyFeaturesInstruction"->"features" as "FEATURES"
FROM INSTRUCTIONS_STREAM_1 WITH (VALUE_FORMAT='JSON');

--Adjustment to above CSAS to remove FEATURES/Feature
--Stream: 1552503296913 | -7971399265186620278 | sky | SogeaVoipModify_1141812285075116952 | -7971399265186620278 | 31642339 | [{code=CallerDisplay}, {code=RingBack}, {code=ChooseToRefuse}]
--Topic:  {"OPERATOR_ID":"sky","OPERATOR_ORDER_ID":"SogeaVoipModify_1141812285075116952","ORDER_ID":"-7971399265186620278","SERVICE_ID":"31642339","FEATURES":[{"code":"CallerDisplay"},{"code":"RingBack"},{"code":"ChooseToRefuse"}]}
CREATE STREAM T_RAW_VOIP_INSTRUCTIONS as
SELECT transaction->"operatorId" as "OPERATOR_ID",
       transaction->"instruction"->"order"->"operatorOrderId" as "OPERATOR_ORDER_ID",
       transaction->"instruction"->"order"->"orderId" as "ORDER_ID",
       transaction->"instruction"->"modifyFeaturesInstruction"->"serviceId" as "SERVICE_ID",
       transaction->"instruction"->"modifyFeaturesInstruction"->"features"->"feature" as "FEATURES"
FROM INSTRUCTIONS_STREAM_1 WITH (VALUE_FORMAT='JSON');



-- Mheh
-- CREATE STREAM RAW_VOIP_INSTRUCTIONS AS
-- SELECT transaction->"operatorId" as "OPERATOR_ID",
--        transaction->instruction->"order"->"OPERATORORDERID" as "OPERATOR_ORDER_ID",
--        transaction->instruction->"order"->orderId as "ORDER_ID",
--        transaction->instruction->modifyFeaturesInstruction->serviceId as "SERVICE_ID",
--        transaction->instruction->modifyFeaturesInstruction->features as "FEATURES"
-- FROM INSTRUCTIONS_STREAM_1 WITH (VALUE_FORMAT='JSON');
--
--
-- SELECT "transaction->operatorId" FROM INSTRUCTIONS_STREAM_2 WITH (VALUE_FORMAT='JSON');



--Output from stream: 1552503296913 | -7971399265186620278 | sky | SogeaVoipModify_1141812285075116952 | -7971399265186620278 | 31642339 | {feature=[{code=CallerDisplay}, {code=RingBack}, {code=ChooseToRefuse}]}
-- This one worked when running Kafka locally:
--Output from stream: 1552494088852 | 3668128509961052222 | sky | SogeaVoipModify_-7538954556292112580 | 3668128509961052222 | 31642339 | {FEATURE=[{CODE=CallerDisplay}, {CODE=RingBack}, {CODE=ChooseToRefuse}]}
--Output from topic:  {"OPERATOR_ID":"sky","OPERATOR_ORDER_ID":"SogeaVoipModify_-7538954556292112580","ORDER_ID":"3668128509961052222","SERVICE_ID":"31642339","FEATURES":{"FEATURE":[{"CODE":"CallerDisplay"},{"CODE":"RingBack"},{"CODE":"ChooseToRefuse"}]}}
-- CREATE STREAM DAVE AS
-- >SELECT transaction->operatorId as OPERATOR_ID,
--       >       transaction->instruction->"order"->"OPERATORORDERID" as "OPERATOR_ORDER_ID",
--       >       transaction->instruction->"order"->orderId as "ORDER_ID",
--       >       transaction->instruction->modifyFeaturesInstruction->serviceId as "SERVICE_ID",
--       >       transaction->instruction->modifyFeaturesInstruction->features as "FEATURES"
--       >FROM INSTRUCTIONS_STREAM_1 WITH (VALUE_FORMAT='JSON');