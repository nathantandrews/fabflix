#!/bin/bash
mysql -u bandrews --password=123 < dbreset.sql
mvn exec:java -Dexec.mainClass="UpdateSecurePassword"
