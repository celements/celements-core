#!/bin/bash

database=$1

echo -n "mysql password: "
read -s password

echo "show tables \G" | mysql -u root --password="$password" "$database" \
| grep -v '\*\*\*\*' | awk -F':' '{ print $2 }' \
| while read tablename ; do
  echo "migrating table $tablename ..."
  echo "ALTER TABLE \`$tablename\` ENGINE=InnoDB;;" \
  | mysql -u root --password="$password" "$database"
done

echo "innodb Migration finished."
