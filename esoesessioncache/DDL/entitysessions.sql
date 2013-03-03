  CREATE TABLE "AAA_OWNER"."ACTIVE_ENTITY_SESSIONS" 
   (	"SESSIONID" VARCHAR2(255 BYTE) NOT NULL ENABLE, 
	"DESCRIPTORID" VARCHAR2(255 BYTE) NOT NULL ENABLE, 
	"DESCRIPTOR_SESSIONID" VARCHAR2(255 BYTE) NOT NULL ENABLE, 
	 PRIMARY KEY ("SESSIONID", "DESCRIPTORID", "DESCRIPTOR_SESSIONID")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT)
  TABLESPACE "AAA_D1"  ENABLE
   ) PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255 NOCOMPRESS LOGGING
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT)
  TABLESPACE "AAA_D1" ;
 

  CREATE INDEX "AAA_OWNER"."AAA_QUERYACTIVE" ON "AAA_OWNER"."ACTIVE_ENTITY_SESSIONS" ("SESSIONID") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT)
  TABLESPACE "AAA_D1" ;

 
ALTER TABLE "AAA_OWNER"."ACTIVE_ENTITY_SESSIONS" ADD (
  CONSTRAINT ACTIVE_SESS_FK 
  FOREIGN KEY (SESSIONID) 
  REFERENCES AAA_OWNER.ACTIVE_SESSIONS (SESSIONID)
  ON DELETE CASCADE);
