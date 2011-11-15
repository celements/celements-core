-- PLEASE NOTE: THIS FILE IS STRICTLY APPEND-ONLY.  ADD ALL CHANGES TO THE
-- END OF THE FILE.  FOR EXCEPTIONS, CONTACT fabian.pichler@synventis.com
-- TablesHistory helds all commands needed to migrate a database from one SchemaVersion to the next.

-- inital table
CREATE TABLE IF NOT EXISTS SchemaVersion (
 `schema` varchar(30) NOT NULL,
 `version`  INT NOT NULL
);
INSERT INTO SchemaVersion
 (`schema`, `version`)
 VALUES
 ('celements.web','1');

-- fix Collation problems in the xwikidoc table.
alter table xwikidoc modify XWD_FULLNAME varchar(255) NOT NULL,
      CHARACTER SET latin1 COLLATE latin1_bin,
    modify XWD_NAME varchar(255) NOT NULL,
      CHARACTER SET latin1 COLLATE latin1_bin,
    modify XWD_WEB varchar(255) NOT NULL,
      CHARACTER SET latin1 COLLATE latin1_bin;

UPDATE SchemaVersion SET `version` = '2' WHERE `schema` = 'celements.web';

ALTER TABLE xwikidoc
      ADD UNIQUE INDEX `fulnameIDX` (`XWD_FULLNAME`,`XWD_LANGUAGE`),
      ADD INDEX `webNameUNIQUEIDX` (`XWD_WEB`(150),`XWD_NAME`(150),`XWD_LANGUAGE`),
      ADD INDEX `webIDX` (`XWD_WEB`),
      ADD INDEX `menuSelectINDX`
      (`XWD_FULLNAME`(100),`XWD_TRANSLATION`,`XWD_PARENT`(100),`XWD_WEB`(100)),
      ADD INDEX `languageIDX` (`XWD_LANGUAGE`),
      ADD INDEX `elementsIDX` (`XWD_ELEMENTS`),
      ADD INDEX `classXMLindex` (`XWD_CLASS_XML`(30));

UPDATE SchemaVersion SET `version` = '3' WHERE `schema` = 'celements.web';

ALTER TABLE xwikiobjects
      ADD INDEX `classnameIDX` (`XWO_CLASSNAME`),
      ADD INDEX `docnameIDX` (`XWO_NAME`,`XWO_NUMBER`),
      ADD INDEX `selectNewsIDX` (`XWO_CLASSNAME`(150),`XWO_NAME`(150));

UPDATE SchemaVersion SET `version` = '4' WHERE `schema` = 'celements.web';

alter table xwikiattachment modify XWA_FILENAME varchar(255) NOT NULL default '',
      CHARACTER SET latin1 COLLATE latin1_bin;

ALTER TABLE xwikiattachment
      ADD index docIdIDX (XWA_DOC_ID);

UPDATE SchemaVersion SET `version` = '5' WHERE `schema` = 'celements.web';

ALTER TABLE xwikiobjects
   modify XWO_NAME varchar(255) NOT NULL,
     CHARACTER SET latin1 COLLATE latin1_bin,
   modify XWO_CLASSNAME varchar(255) NOT NULL,
     CHARACTER SET latin1 COLLATE latin1_bin;
  
UPDATE SchemaVersion SET `version` = '6' WHERE `schema` = 'celements.web';

CREATE TABLE IF NOT EXISTS cel_xwikiarticleclass(
  `AC_ID` int(10) NOT NULL,
  `AC_ARTICLE_ID` int(10) default NULL,
  `AC_LANG` varchar(2) default NULL,
  `AC_TITEL` varchar(100) default NULL,
  `AC_EXTRACT` text default NULL,
  `AC_CONTENT` text default NULL,
  `AC_EDITOR` varchar(20) default NULL,
  `AC_PUBLISH_DATE` date default NULL,
  `AC_ARCHIVE_DATE` date default NULL,
  `AC_HAS_COMMENTS` smallint(1) default 0,
  `AC_IS_SUBSCRIBABLE` smallint(1) default 0,
  PRIMARY KEY  (AC_ID)
) TYPE=MyISAM;

UPDATE SchemaVersion SET `version` = '7' WHERE `schema` = 'celements.web';

insert into cel_xwikiarticleclass (`AC_ID`,
  `AC_ARTICLE_ID`,`AC_LANG`,`AC_TITEL`,`AC_EXTRACT`,`AC_CONTENT`,`AC_EDITOR`,
  `AC_PUBLISH_DATE`,`AC_HAS_COMMENTS`)
  select ac.XWO_ID as AC_ID,
    acId.XWI_VALUE as AC_ARTICLE_ID,
    acLang.XWS_VALUE as AC_LANG,
    acTitle.XWS_VALUE as AC_TITEL,
    acExtract.XWL_VALUE as AC_EXTRACT,
    acContent.XWL_VALUE as AC_CONTENT,
    acEditor.XWS_VALUE as AC_EDITOR,
    acPublish.XWS_VALUE as AC_PUBLISH_DATE,
    acComment.XWI_VALUE as AC_HAS_COMMENTS
   from xwikiobjects ac
    left outer join cel_xwikiarticleclass celAc on (
      ac.XWO_ID = celAc.AC_ID)
    left outer join xwikiintegers acId on (
      ac.XWO_ID = acId.XWI_ID and acId.XWI_NAME='id')
    left outer join xwikistrings acLang on (
      ac.XWO_ID = acLang.XWS_ID and acLang.XWS_NAME='lang')
    left outer join xwikistrings acTitle on (
      ac.XWO_ID = acTitle.XWS_ID and acTitle.XWS_NAME='title')
    left outer join xwikilargestrings acExtract on (
      ac.XWO_ID = acExtract.XWL_ID and acExtract.XWL_NAME='extract')
    left outer join xwikilargestrings acContent on (
      ac.XWO_ID = acContent.XWL_ID and acContent.XWL_NAME='content')
    left outer join xwikistrings acEditor on (
      ac.XWO_ID = acEditor.XWS_ID and acEditor.XWS_NAME='blogeditor')
    left outer join xwikidates acPublish on (
      ac.XWO_ID = acPublish.XWS_ID and acPublish.XWS_NAME='publishdate')
    left outer join xwikiintegers acComment on (
      ac.XWO_ID = acComment.XWI_ID and acComment.XWI_NAME='hasComments')
   where ac.XWO_CLASSNAME = 'XWiki.ArticleClass'
    and celAc.AC_ID is null;

delete from xwikiintegers
   where XWI_ID in (select AC_ID from cel_xwikiarticleclass);

delete from xwikistrings
   where XWS_ID in (select AC_ID from cel_xwikiarticleclass);

delete from xwikilargestrings
   where XWL_ID in (select AC_ID from cel_xwikiarticleclass);

delete from xwikidates
   where XWS_ID in (select AC_ID from cel_xwikiarticleclass);

UPDATE SchemaVersion SET `version` = '8' WHERE `schema` = 'celements.web';

alter table xwikiattachment_content modify column XWA_CONTENT LONGBLOB NOT NULL;
alter table xwikiattachment_archive modify column XWA_ARCHIVE LONGBLOB NOT NULL;

UPDATE SchemaVersion SET `version` = '9' WHERE `schema` = 'celements.web';
 