mysql -u mytestuser --password=My6$Password < dbreset.sql
mvn exec:java -Dexec.mainClass="UpdateSecurePassword"
@REM mvn exec:java -Dexec.mainClass="UpdateSecurePassword2"
