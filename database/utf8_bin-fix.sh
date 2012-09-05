#!/bin/sh

database=$1

echo -n "mysql password: "
read -s password

echo "fix database $database  ..."
echo "alter table xwikidoc modify column XWD_FULLNAME varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL; \
alter table xwikidoc modify column XWD_NAME varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL; \
alter table xwikidoc modify column XWD_WEB varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL; \
alter table xwikiobjects modify column XWO_NAME varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL;" \
| mysql -u root --password="$password" "$database"


echo "Fix finished."
