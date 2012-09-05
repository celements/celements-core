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
done

echo "Migration finished."
