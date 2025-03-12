#!/bin/bash
mysql -u mytestuser --password='My6$Password' < dbreset.sql
#. ./dataxf.sh ../../fabflix-meta/movie-data.sql
#mysql -u mytestuser --password='My6$Password' < dbreset.sql
#java -jar ../password-updater/target/fabflix-password-updater-jar-with-dependencies.jar
