#!/bin/bash
. ./dataxf.sh ../../fabflix-meta/movie-data.sql
mysql -u ba914792 --password=123 < dbreset.sql
java -jar ../password-updater/target/fabflix-password-updater-jar-with-dependencies.jar
