#!/bin/sh

database=$1

echo -n "mysql password: "
read -s password

echo "show tables \G" | mysql -u root --password="$password" "$database" \
| grep -v '\*\*\*\*' | awk -F':' '{ print $2 }' \
| while read tablename ; do
  echo "migrating table $tablename ..."
  echo "ALTER TABLE \`$tablename\` CONVERT TO CHARACTER SET utf8 COLLATE utf8_bin;" \
  | mysql -u root --password="$password" "$database"
  echo "alter table xwikidoc modify column XWD_FULLNAME varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL; \
alter table xwikidoc modify column XWD_NAME varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL; \
alter table xwikidoc modify column XWD_WEB varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL; \
alter table xwikiobjects modify column XWO_NAME varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL;" \
| mysql -u root --password="$password" "$database"
done

echo "Migration finished."
