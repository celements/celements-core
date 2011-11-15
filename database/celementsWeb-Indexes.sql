-- adding the following indexes.

ALTER TABLE xwikidoc
      ADD UNIQUE INDEX `fulnameIDX` (`XWD_FULLNAME`,`XWD_LANGUAGE`),
      ADD INDEX `webNameUNIQUEIDX` (`XWD_WEB`(150),`XWD_NAME`(150),`XWD_LANGUAGE`),
      ADD INDEX `webIDX` (`XWD_WEB`),
      ADD INDEX `menuSelectINDX`
      (`XWD_FULLNAME`(100),`XWD_TRANSLATION`,`XWD_PARENT`(100),`XWD_WEB`(100)),
      ADD INDEX `languageIDX` (`XWD_LANGUAGE`),
      ADD INDEX `elementsIDX` (`XWD_ELEMENTS`),
      ADD INDEX `classXMLindex` (`XWD_CLASS_XML`(30));

ALTER TABLE xwikiobjects
      ADD INDEX `classnameIDX` (`XWO_CLASSNAME`),
      ADD INDEX `docnameIDX` (`XWO_NAME`,`XWO_NUMBER`),
      ADD INDEX `selectNewsIDX` (`XWO_CLASSNAME`(150),`XWO_NAME`(150));

ALTER TABLE xwikiattachment
      ADD index docIdIDX (XWA_DOC_ID);

-- fix for big attachments
alter table xwikiattachment_content modify column XWA_CONTENT LONGBLOB NOT NULL;
alter table xwikiattachment_archive modify column XWA_ARCHIVE LONGBLOB NOT NULL;
