Accountable
=============================================================

Turning your weekly expenses into something worthwhile.

Local Setup
-------------------------------------------------------------

Install MySQL and then setup the dev database and user:

	CREATE DATABASE accountable DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
	CREATE DATABASE accountable_test DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;
	CREATE USER 'user'@'localhost' IDENTIFIED BY 'password';
	GRANT ALL ON accountable.* TO 'user'@'localhost';
	GRANT ALL ON accountable_test.* TO 'user'@'localhost';
	FLUSH PRIVILEGES

From there you can run `sbt` and then `flywayMigrate` to migrate to the newest database.