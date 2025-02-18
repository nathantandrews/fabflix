#!/bin/bash
mysql -u mytestuser -p < dbreset.sql
mvn exec:java -Dexec.mainClass="UpdateSecurePassword"
mvn exec:java -Dexec.mainClass="UpdateSecurePassword2"
